# Architecture Specification - Riwi Messaging Platform

## Author
- **GitHub**: [antoniosanp](https://github.com/antoniosanp)

---

## 1. Database Architecture & 3FN Normalization

The database architecture is designed in PostgreSQL 16 using strict Third Normal Form (3FN) rules to ensure zero data redundancy, transactional integrity, and compliance with the `rw_` naming standard.

### Entity Relationships & Cardinalities
1. **`rw_users` (1) to `rw_channels` (N)**:
   - Cardinality: 1:N (Optional creator).
   - Foreign Key: `rw_channels.rw_created_by` references `rw_users(rw_id)` with `ON DELETE SET NULL`.

2. **`rw_channels` (1) to `rw_channel_members` (N) and `rw_users` (1) to `rw_channel_members` (N)**:
   - Cardinality: N:M junction table.
   - Unique Constraint: `(rw_channel_id, rw_user_id)` prevents duplicate memberships.
   - Foreign Keys: `ON DELETE CASCADE`.

3. **`rw_channels` (1) to `rw_messages` (N)**:
   - Cardinality: 1:N.
   - Foreign Key: `rw_messages.rw_channel_id` references `rw_channels(rw_id)` with `ON DELETE RESTRICT`.

4. **`rw_users` (1) to `rw_messages` (N)**:
   - Cardinality: 1:N.
   - Foreign Key: `rw_messages.rw_sender_id` references `rw_users(rw_id)` with `ON DELETE RESTRICT`.

5. **`rw_users` (1) to `rw_copilot_usage_logs` (N)**:
   - Cardinality: 1:N.
   - Foreign Key: `rw_copilot_usage_logs.rw_user_id` references `rw_users(rw_id)` with `ON DELETE CASCADE`.

---

## 2. PostgreSQL Row Level Security (RLS) & Stored Logic

### Row Level Security (RLS)
PostgreSQL Row Level Security policies enforce multi-tenant channel and message isolation:
- **`rw_policy_channel_members_select`**: Grants visibility into `rw_channels` and `rw_messages` only if `rw_channel_members` contains a matching record where `rw_user_id = NULLIF(current_setting('app.current_user_id', true), '')::uuid`.
- Public channels (`PUBLIC`) remain accessible to all active authenticated users.

### Stored Logic Components
1. **Views**: `rw_v_user_conversations` dynamically projects channel metadata, unread message counts, and symmetric interlocutor names for `DIRECT` messaging channels.
2. **Functions**:
   - `rw_fn_get_channel_messages`: Keyset pagination reader.
   - `rw_fn_search_messages`: Full-text search with keyword highlighting.
   - `rw_fn_get_copilot_context`: Vector HNSW RAG search returning top authorized context snippets.
3. **Stored Procedures**:
   - `rw_sp_get_users`: Scoped user list retrieval.
   - `rw_sp_manage_user`: Transactional creation, update, and soft-deactivation of user accounts.
4. **Triggers**: `rw_trg_update_messages_timestamp` automatically maintains `rw_updated_at` timestamps on row modification.

---

## 3. Backend Architecture & SOLID Principles

The Spring Boot backend enforces a clean flat-layered architecture:
- **Controller Layer** (`com.riwi.messaging.controller`): Handles HTTP routing, request validation (`@Valid`), OpenAPI documentation, and response entity wrapping.
- **Service Layer** (`com.riwi.messaging.service`): Orchestrates business logic, transaction management (`@Transactional`), BCrypt security, JWT generation, and AI provider invocation.
- **Repository Layer** (`com.riwi.messaging.repository`): Interfaces with PostgreSQL using Spring Data JPA and native SQL functions.
- **Security Layer** (`com.riwi.messaging.security`): Stateless JWT filter intercepting incoming requests, extracting claims, and setting SecurityContext authentication.

### SOLID Principles Applied
- **Single Responsibility Principle (SRP)**: Each service (e.g. `AuthService`, `CopilotService`, `MessageService`) handles a single domain context.
- **Open/Closed Principle (OCP) & Interface Segregation (ISP)**: `AiProviderService` interface allows swapping AI backends without modifying high-level orchestration components.
- **Dependency Inversion Principle (DIP)**: Controllers and services depend on abstractions (interfaces) rather than concrete implementations.

---

## 4. Frontend Architecture & Real-Time Engine

The React + TypeScript frontend is structured around a fixed 3-Zone desktop layout:
1. **Zone 1 (Left 280px)**: Channel & Direct Chat Sidebar with modal creation interface.
2. **Zone 2 (Center Flex 1)**: Active Chat Window with Keyset lazy loading, infinite scroll preservation, and real-time message state indicators (`PENDING`, `SENT`, `FAILED`).
3. **Zone 3 (Right 360px)**: AI Copilot RAG panel with source citations, refusal indicators, and token metrics.

### Real-Time Messaging Engine
- **Protocol**: STOMP over SockJS (`/ws`).
- **Topic Subscription**: Subscribes to `/topic/channels/{channelId}`.
- **Deduplication Engine**: Matches incoming STOMP payloads against local pending state, preventing double-rendering for the sender.

---

## 5. RAG AI Copilot Pipeline

1. **Request Ingestion**: User submits a natural language question.
2. **RLS Context Retrieval**: SQL function `findCopilotContextTextFallback` fetches top recent messages strictly from channels authorized for `currentUser.id`.
3. **Prompt Framing**: Constructs structured input combining System Prompt (`v1.1-RAG-IntelligentContext`), authenticated user identity, and authorized message snippets.
4. **LLM Execution & Failover**: Calls Google AI Studio API (`gemini-3.6-flash` -> `gemini-2.5-flash` -> `gemini-1.5-flash`).
5. **Auditing**: Logs query prompt, response, user ID, and token usage into `rw_copilot_usage_logs`.
