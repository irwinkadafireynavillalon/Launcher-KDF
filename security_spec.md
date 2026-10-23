# Security Specification (`security_spec.md`)

## 1. Data Invariants
1. **Authentication Gate**: No document in `/users/{userId}/sync_notes/{noteId}` can be read, listed, created, updated, or deleted unless `request.auth != null`.
2. **Strict User Isolation**: A user can only read, list, create, update, or delete notes where the path variable `userId == request.auth.uid` and `resource.data.userId == request.auth.uid`.
3. **Path ID Hardening**: Both `userId` and `noteId` must match `^[a-zA-Z0-9_\-]+$` and be at most 128 characters (`isValidId`).
4. **Schema & Size Bounds**:
   - `userId`: string, equals `request.auth.uid`, max 128 chars.
   - `title`: string, 1..200 chars.
   - `content`: string, 1..20000 chars.
   - `language`: string, 1..60 chars.
   - `createdAt`: timestamp, `<= request.time`, immutable on update.
   - `updatedAt`: timestamp, `<= request.time`.

## 2. The "Dirty Dozen" Payloads
1. **Unauthenticated Read**: `GET /users/alice_123/sync_notes/note_1` with `auth = null` -> REJECT.
2. **Cross-User Read**: `GET /users/bob_456/sync_notes/note_1` as `alice_123` -> REJECT.
3. **Identity Spoofing on Create**: `POST /users/alice_123/sync_notes/note_1` with `{"userId": "bob_456", ...}` as `alice_123` -> REJECT.
4. **Shadow/Ghost Field Injection**: `POST` or `PATCH` with extra field `{"isAdmin": true}` -> REJECT via `.keys().hasOnly(...)` / `.affectedKeys().hasOnly(...)`.
5. **Missing Required Key**: `POST` missing `language` or `createdAt` -> REJECT via `.keys().hasAll(...)`.
6. **Oversized Title Poisoning**: `title` of 500 chars (> 200 limit) -> REJECT.
7. **Empty Content Poisoning**: `content` of `""` (0 chars) -> REJECT.
8. **Type Confusion on Content**: `content: 12345` (int instead of string) -> REJECT.
9. **Client Future Timestamp Spoofing**: `createdAt` set in the future (`> request.time`) -> REJECT.
10. **Immutable Field Mutation**: `PATCH` modifying `createdAt` or `userId` -> REJECT.
11. **Path ID Poisoning**: `noteId` containing spaces or special characters (`note$bad!`) -> REJECT via `isValidId(noteId)`.
12. **Update-Gap Bypass**: `PATCH` changing `title` to an integer or oversized string -> REJECT via `isValidCloudSyncNote(incoming())` at the start of `allow update`.
