# Riwi Internal Messaging Platform - REST API Documentation

## 🌐 Overview & Swagger UI
- **Base URL**: `http://localhost:8080/api`
- **Swagger UI Document**: `http://localhost:8080/swagger-ui.html` (or `http://localhost:8080/swagger-ui/index.html`)
- **OpenAPI JSON Spec**: `http://localhost:8080/v3/api-docs`

---

## 🔑 Test User Credentials (BCrypt Hashed)
All test accounts use password **`123456`**:

| Role | Email | Password | User ID (UUID) |
| :--- | :--- | :--- | :--- |
| Admin / Security Lead | `admin@riwi.io` | `123456` | `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` |
| Senior Frontend Dev | `juan.perez@riwi.io` | `123456` | `b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a22` |
| Lead Backend Engineer | `maria.gomez@riwi.io` | `123456` | `c2eebc99-9c0b-4ef8-bb6d-6bb9bd380a33` |
| IT Support Specialist | `pedro.soporte@riwi.io` | `123456` | `d3eebc99-9c0b-4ef8-bb6d-6bb9bd380a44` |

---

## 🛡️ Authentication Header
All protected endpoints require the HTTP Authorization header:
```http
Authorization: Bearer <your_access_token>
```

---

## 🚀 Endpoints Documentation

### 1. Authentication Endpoints (`/api/auth`)

#### `POST /api/auth/login`
- **Description**: Authenticates user using email and password, returning JWT Access Token and Refresh Token.
- **Request Body**:
```json
{
  "email": "admin@riwi.io",
  "password": "123456"
}
```
- **Response Body (`200 OK`)**:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "user": {
    "id": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
    "email": "admin@riwi.io",
    "fullName": "Admin Sistema",
    "jobTitle": "System Administrator & Security Lead",
    "role": "ADMIN",
    "isActive": true,
    "createdAt": "2026-08-27T07:29:10Z"
  }
}
```

---

#### `POST /api/auth/refresh`
- **Description**: Performs stateless Refresh Token rotation, returning a new Access Token and new Refresh Token.
- **Request Body**:
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```
- **Response Body (`200 OK`)**:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9.new...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9.rotated...",
  "tokenType": "Bearer",
  "user": {
    "id": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
    "email": "admin@riwi.io",
    "fullName": "Admin Sistema",
    "jobTitle": "System Administrator & Security Lead",
    "role": "ADMIN",
    "isActive": true,
    "createdAt": "2026-08-27T07:29:10Z"
  }
}
```

---

### 2. User Endpoints (`/api/users`)

#### `GET /api/users`
- **Description**: Retrieves list of all active users.
- **Headers**: `Authorization: Bearer <token>`
- **Response Body (`200 OK`)**:
```json
[
  {
    "id": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
    "email": "admin@riwi.io",
    "fullName": "Admin Sistema",
    "jobTitle": "System Administrator & Security Lead",
    "role": "ADMIN",
    "isActive": true,
    "createdAt": "2026-08-27T07:29:10Z"
  },
  {
    "id": "b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a22",
    "email": "juan.perez@riwi.io",
    "fullName": "Juan Perez",
    "jobTitle": "Senior Frontend Developer",
    "role": "MEMBER",
    "isActive": true,
    "createdAt": "2026-08-27T07:29:10Z"
  }
]
```

---

#### `GET /api/users/me`
- **Description**: Retrieves current authenticated user details from token context.
- **Headers**: `Authorization: Bearer <token>`
- **Response Body (`200 OK`)**:
```json
{
  "id": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
  "email": "admin@riwi.io",
  "fullName": "Admin Sistema",
  "jobTitle": "System Administrator & Security Lead",
  "role": "ADMIN",
  "isActive": true,
  "createdAt": "2026-08-27T07:29:10Z"
}
```

---

#### `PUT /api/users/{id}`
- **Description**: Updates user details or deactivates account using stored procedure `rw_sp_update_or_delete_user`.
- **Headers**: `Authorization: Bearer <token>`
- **Request Body**:
```json
{
  "fullName": "Admin Sistema Updated",
  "jobTitle": "Chief Information Security Officer",
  "isActive": true
}
```
- **Response Body (`200 OK`)**:
```json
{
  "id": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
  "email": "admin@riwi.io",
  "fullName": "Admin Sistema Updated",
  "jobTitle": "Chief Information Security Officer",
  "role": "ADMIN",
  "isActive": true,
  "createdAt": "2026-08-27T07:29:10Z"
}
```

---

### 3. Channel Endpoints (`/api/channels`)

#### `GET /api/channels`
- **Description**: Lists conversations and unread counts for current user using PostgreSQL view `rw_v_user_conversations`.
- **Headers**: `Authorization: Bearer <token>`
- **Response Body (`200 OK`)**:
```json
[
  {
    "channelId": "11111111-1111-1111-1111-111111111111",
    "channelName": "General",
    "channelType": "PUBLIC",
    "userId": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
    "memberRole": "OWNER",
    "lastReadAt": "2026-08-27T07:29:10Z",
    "lastMessageId": 2,
    "lastMessageContent": "¡Hola equipo! Excelente iniciativa para mejorar la comunicación interna.",
    "lastMessageAt": "2026-08-27T07:29:10Z",
    "lastMessageSenderId": "b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a22",
    "unreadCount": 0
  }
]
```

---

#### `POST /api/channels`
- **Description**: Creates a new PUBLIC, PRIVATE, or DIRECT channel and assigns initial members.
- **Headers**: `Authorization: Bearer <token>`
- **Request Body**:
```json
{
  "name": "Proyecto Antigravity",
  "type": "PRIVATE",
  "memberUserIds": [
    "b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a22",
    "c2eebc99-9c0b-4ef8-bb6d-6bb9bd380a33"
  ]
}
```
- **Response Body (`201 Created`)**:
```json
{
  "id": "44444444-4444-4444-4444-444444444444",
  "name": "Proyecto Antigravity",
  "type": "PRIVATE",
  "createdBy": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
  "createdAt": "2026-08-27T09:30:00Z"
}
```

---

### 4. Message Endpoints (`/api/messages` & `/api/channels/{channelId}/messages`)

#### `GET /api/channels/{channelId}/messages?afterId=0&limit=30`
- **Description**: Retrieves channel message history using SQL Keyset Pagination function `rw_fn_get_channel_messages` without OFFSET.
- **Headers**: `Authorization: Bearer <token>`
- **Response Body (`200 OK`)**:
```json
{
  "items": [
    {
      "id": 1,
      "channelId": "11111111-1111-1111-1111-111111111111",
      "senderId": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
      "senderName": "Admin Sistema",
      "content": "Bienvenidos a la nueva plataforma de mensajería interna de Riwi Co. S.A.S.",
      "status": "SENT",
      "isEdited": false,
      "isDeleted": false,
      "createdAt": "2026-08-27T07:29:10Z"
    }
  ],
  "nextAfterId": 1,
  "hasMore": false
}
```

---

#### `POST /api/channels/{channelId}/messages`
- **Description**: Posts a new message to a channel with RLS validation in PostgreSQL.
- **Headers**: `Authorization: Bearer <token>`
- **Request Body**:
```json
{
  "content": "Hola equipo, confirmo la revisión de código."
}
```
- **Response Body (`201 Created`)**:
```json
{
  "id": 5,
  "channelId": "11111111-1111-1111-1111-111111111111",
  "senderId": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
  "senderName": "Admin Sistema",
  "content": "Hola equipo, confirmo la revisión de código.",
  "status": "SENT",
  "isEdited": false,
  "isDeleted": false,
  "createdAt": "2026-08-27T09:32:00Z"
}
```

---

#### `PUT /api/messages/{id}`
- **Description**: Soft edits a message. Preserves original text in `rw_original_content` for audit recovery.
- **Headers**: `Authorization: Bearer <token>`
- **Request Body**:
```json
{
  "content": "Hola equipo, confirmo la revisión de código (Editado)."
}
```
- **Response Body (`200 OK`)**:
```json
{
  "id": 5,
  "channelId": "11111111-1111-1111-1111-111111111111",
  "senderId": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
  "senderName": "Admin Sistema",
  "content": "Hola equipo, confirmo la revisión de código (Editado).",
  "status": "SENT",
  "isEdited": true,
  "isDeleted": false,
  "createdAt": "2026-08-27T09:32:00Z"
}
```

---

#### `DELETE /api/messages/{id}`
- **Description**: Soft deletes a message (`rw_is_deleted = true`). Physical deletion is forbidden.
- **Headers**: `Authorization: Bearer <token>`
- **Response**: `204 No Content`

---

#### `GET /api/messages/search?q=PostgreSQL`
- **Description**: Searches messages accessible by actor and highlights matches using `<mark>` tags via SQL function `rw_fn_search_messages`.
- **Headers**: `Authorization: Bearer <token>`
- **Response Body (`200 OK`)**:
```json
[
  {
    "messageId": 3,
    "channelId": "22222222-2222-2222-2222-222222222222",
    "channelName": "Desarrollo Backend",
    "senderName": "Maria Gomez",
    "content": "Recordatorio privado: La migración a PostgreSQL 15 con Row Level Security y pgvector fue completada con éxito.",
    "highlightedSnippet": "Recordatorio privado: La migración a <mark>PostgreSQL</mark> 15 con Row Level Security y pgvector fue completada con éxito.",
    "createdAt": "2026-08-27T07:29:10Z"
  }
]
```

---

### 5. AI Copilot Endpoints (`/api/copilot`)

#### `POST /api/copilot/query`
- **Description**: Executes RAG prompt against AI Copilot. Retrieves context strictly from accessible channels via SQL function `rw_fn_get_copilot_context`. Includes source citations and explicit refusal when permissions or context are lacking.
- **Headers**: `Authorization: Bearer <token>`
- **Request Body**:
```json
{
  "query": "¿Qué avances se reportaron sobre la migración a PostgreSQL?"
}
```
- **Response Body (`200 OK`)**:
```json
{
  "answer": "Hola Admin Sistema (System Administrator & Security Lead). Según el contexto de tus canales permitidos: He analizado 1 mensaje(s) fuente. En el canal 'Desarrollo Backend', Maria Gomez indicó: \"Recordatorio privado: La migración a PostgreSQL 15 con Row Level Security y pgvector fue completada con éxito.\".",
  "citations": [
    {
      "messageId": 3,
      "channelId": "22222222-2222-2222-2222-222222222222",
      "channelName": "Desarrollo Backend",
      "senderName": "Maria Gomez",
      "contentSnippet": "Recordatorio privado: La migración a PostgreSQL 15 con Row Level Security y pgvector fue completada con éxito."
    }
  ],
  "tokensUsed": 155,
  "isRefusedDueToPermissionsOrContext": false
}
```

---

#### `GET /api/copilot/usage`
- **Description**: Audits total token consumption per user using SQL function `rw_fn_get_copilot_usage_by_user`.
- **Headers**: `Authorization: Bearer <token>`
- **Response Body (`200 OK`)**:
```json
[
  {
    "userId": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
    "fullName": "Admin Sistema",
    "jobTitle": "System Administrator & Security Lead",
    "totalQueries": 1,
    "totalTokensUsed": 155,
    "lastQueryAt": "2026-08-27T09:33:00Z"
  }
]
```
