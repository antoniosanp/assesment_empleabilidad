# Technical Decisions & Architectural Trade-offs - Riwi Messaging Platform

## Author
- **GitHub**: [antoniosanp](https://github.com/antoniosanp)

---

## 1. Database Schema & Constraint Decisions

### Choice of Foreign Key `ON DELETE` Actions
- **`rw_channels.rw_created_by` -> `ON DELETE SET NULL`**:
  - *Rationale*: If an administrator or team member who created a channel is deleted from the system, deleting the entire channel and its message history would destroy business continuity and team audit trails. Setting the creator to `NULL` preserves historical conversation context.
- **`rw_channel_members` -> `ON DELETE CASCADE`**:
  - *Rationale*: Channel membership records are pure junction entities. Deleting a user or channel should automatically discard associated membership junction rows to avoid orphan join records.
- **`rw_messages.rw_channel_id` and `rw_messages.rw_sender_id` -> `ON DELETE RESTRICT`**:
  - *Rationale*: To prevent accidental hard deletion of active channels or user accounts containing valid communication archives.

### Prohibition of Physical `DELETE` on Messages
- *Rationale*: Messaging compliance requires auditability. Messages are soft-deleted by setting `rw_is_deleted = true` and recording `rw_deleted_at`. Soft-deleted messages are filtered out from normal chat views and RAG AI context retrieval queries.

---

## 2. Keyset Pagination over OFFSET Pagination

- *Problem*: Traditional SQL `LIMIT x OFFSET y` exhibits $O(N)$ performance degradation as offset values grow large, requiring the database engine to scan and discard thousands of unneeded rows.
- *Decision*: Implemented Keyset Pagination using indexed tuples `(rw_created_at, rw_id)` and parameter `:afterId`.
- *Benefit*: Guarantees constant $O(1)$ query execution time regardless of total table message volume, while preventing duplicate or skipped items when new messages arrive during user scrolling.

---

## 3. Database-Level Security (Row Level Security & Transaction Actor Binding)

- *Decision*: Security enforcement is implemented inside PostgreSQL via Row Level Security (RLS) policies rather than relying solely on application-level filtering.
- *Implementation*: Every database transaction execution sets session property `app.current_user_id`. Policies on `rw_channels` and `rw_messages` evaluate membership against `rw_channel_members`.
- *Benefit*: Even if an application bug or custom SQL query bypasses Java-level checks, PostgreSQL rejects unauthorized row access at the database engine level.

---

## 4. RAG AI Provider & Resilience Failover Strategy

- *Decision*: Integrated Google AI Studio SDK (`gemini-3.6-flash`) with native RAG context retrieval.
- *Failover Pipeline*: To defend against occasional 503 Service Unavailable spikes during peak demand on experimental AI models, an automated fallback pipeline was implemented: `gemini-3.6-flash` -> `gemini-2.5-flash` -> `gemini-1.5-flash`.
- *Explicit Refusal Requirement*: System prompt `v1.1-RAG-IntelligentContext` strictly instructs Gemini to issue an explicit refusal (*"No poseo permisos o contexto suficiente en tus canales autorizados..."*) whenever context is missing for requested private channels.

---

## 5. Real-Time Protocol Choice (STOMP WebSockets)

- *Decision*: Selected STOMP over WebSockets with SockJS fallback.
- *Rationale*: Pure WebSockets lack structured frame semantics. STOMP provides pub/sub topic routing (`/topic/channels/{channelId}`), structured headers, and explicit message framing out of the box.

---

## 6. Docker Multi-Stage Containerization

- *Decision*: Utilized multi-stage Docker builds for both Backend (Maven JDK 21 -> Eclipse Temurin 21 JRE Alpine) and Frontend (Node 20 Alpine -> Nginx Alpine).
- *Benefit*: Significantly reduces final container image sizes, eliminates build tools from production runtimes, and enhances container security.
