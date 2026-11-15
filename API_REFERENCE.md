# NAEP API Reference

State captured on 2026-10-20. This document describes the checked-out implementation and live responses, not an intended or corrected contract. Secret values and bearer/refresh tokens are deliberately redacted.

## 1 Summary

- Branch: `feat/typescript-migration`; tracked worktree was clean before this report was created. Recent commits: `cbf39f5`, `8e650b0`, `0c46528`, `0028db6`, `a49930a`.
- Implementation language: TypeScript. Node.js `v26.8.2`; installed Express `4.22.3`.
- `POST /creations/:id/inquiries` builds JPEG URLs from the cover's stored media URL and does not depend on `CLOUDINARY_CLOUD_NAME`. Only `POST /uploads/signature` returns `UPLOADS_NOT_CONFIGURED`.
- PostgreSQL was reachable (`pg_isready`: accepting connections). `schema_migrations` contains:
  - `001_phase1_core.sql` — applied 2026-10-02.
  - `003_self_engagement_guard.sql` — applied 2026-10-02.
  - `migrations/reserved/002_phase2_reserved.sql` — present, reserved, not applied. The migration runner only applies SQL files directly under `migrations/`.
- `.env` variable status (names only; no values disclosed):

  | Variable | State |
  |---|---|
  | `PORT` | set |
  | `NODE_ENV` | set |
  | `DATABASE_URL` | set |
  | `JWT_ACCESS_SECRET` | set |
  | `JWT_ACCESS_TTL` | set |
  | `REFRESH_TTL_DAYS` | set |
  | `BCRYPT_ROUNDS` | set |
  | `CLOUDINARY_CLOUD_NAME` | set |
  | `CLOUDINARY_API_KEY` | set |
  | `CLOUDINARY_API_SECRET` | set |
  | `CLOUDINARY_UPLOAD_FOLDER` | set |
  | `APP_BASE_URL` | set |
  | `DEFAULT_COUNTRY_CODE` | set |

  `TRUST_PROXY` is supported by the application but was not present in `.env`; its default is `false`.

## 2 How to run and connect

Use pnpm only. From the repository root:

```sh
pnpm install
pnpm run migrate
pnpm run dev
```

The server binds to `0.0.0.0`; `PORT` defaults to `3000`. The base URL is `http://<host>:3000/api/v1`. For the Android emulator on the development machine use:

```text
http://10.0.2.2:3000/api/v1
```

Health check: `GET /api/v1/health`. The app accepts JSON request bodies up to 1 MiB. Use `Authorization: Bearer <accessToken>` on protected routes. Response JSON uses camelCase.

The development server was started for the live captures and then stopped. A follow-up request confirmed port `3000` was free.

## 3 Endpoint table

Auth column: **none** means no auth middleware; **optionalAuth** accepts a valid bearer token for richer data and treats missing/invalid tokens as a guest; **requireAuth** requires a valid access token. “Artisan only” is enforced inside the service for creation, not by a separate route middleware. Rate limits are per client IP, fixed at 60 seconds, and have no environment configuration.

All request objects are strict Zod schemas: unknown keys are rejected. Unless otherwise specified, a successful response is HTTP 200 with `{ "data": ... }`. The response DTO names below are fully defined immediately after the route table. All routes can also produce `500 INTERNAL_ERROR`; malformed JSON is `400 VALIDATION_ERROR`, a body over 1 MiB is `413 PAYLOAD_TOO_LARGE`, and database errors may map to `400 VALIDATION_ERROR`, `409 CONFLICT`, or `422 VALIDATION_ERROR`.

| Method and full path | Auth | Limiter | Params, query, body and validation | Success response | Route-specific error codes (HTTP) |
|---|---|---|---|---|---|
| `GET /api/v1/health` | none | public: 120/min | None | 200 `Health` | `RATE_LIMITED` (429) |
| `POST /api/v1/auth/register` | none | auth: 10/min | Body: `fullName`, `phoneNumber`, `email`, `password` required; `platform` optional, default `unknown`. Name trimmed, 2–150 chars; phone trimmed, 1–30 chars then normalized to E.164; email trimmed, max 254, validated and lowercased; password 8–128 chars and must contain at least one ASCII letter and digit; platform `web|android|ios|unknown`. | 201 `AuthResult` | `VALIDATION_ERROR` (422), `PHONE_TAKEN` (409), `EMAIL_TAKEN` (409), `RATE_LIMITED` (429) |
| `POST /api/v1/auth/login` | none | auth: 10/min | Body: `identifier` required, trimmed 1–254 chars; `password` required, 1–128 chars; optional `platform`, default `unknown`. Identifier containing `@` is used as email, otherwise normalized as a phone. | 200 `AuthResult` | `VALIDATION_ERROR` (422), `INVALID_CREDENTIALS` (401), `FORBIDDEN` (403, inactive account), `RATE_LIMITED` (429) |
| `POST /api/v1/auth/refresh` | none | auth: 10/min | Body: `refreshToken` required, exactly 96 hexadecimal chars; optional `platform`, but validator defaults it to `unknown`. | 200 `TokenPair` | `VALIDATION_ERROR` (422), `REFRESH_INVALID` (401), `FORBIDDEN` (403, inactive account), `RATE_LIMITED` (429) |
| `POST /api/v1/auth/logout` | none | none | Body: `refreshToken` required, exactly 96 hexadecimal chars. Does not require a bearer access token. | 204, no body | `VALIDATION_ERROR` (422) |
| `GET /api/v1/me` | requireAuth | none | No params, query, or body | 200 `User` | `UNAUTHORIZED` / `TOKEN_EXPIRED` (401), `NOT_FOUND` (404, deleted user) |
| `PATCH /api/v1/me` | requireAuth | none | Strict body; at least one of `fullName`, `preferredMode`, or the picture pair. `fullName` trimmed, 2–150 chars; `preferredMode` is `enthusiast|artisan`; `profilePictureUrl` is an HTTPS URL up to 2048 chars or null; `profilePicturePublicId` trimmed, 1–255 chars or null. Picture fields must both be strings or both be null. A set picture must use the current user's `profiles/<userId>/` folder and, when Cloudinary is configured, a URL on that configured Cloudinary cloud containing the public ID. | 200 `User` | `UNAUTHORIZED` / `TOKEN_EXPIRED` (401), `VALIDATION_ERROR` (422), `NOT_FOUND` (404) |
| `POST /api/v1/me/onboarding` | requireAuth | none | Body: required `mode`, `enthusiast|artisan` | 200 `User` | `UNAUTHORIZED` / `TOKEN_EXPIRED` (401), `VALIDATION_ERROR` (422), `NOT_FOUND` (404) |
| `GET /api/v1/me/creations` | requireAuth | none | Query: optional positive-integer `page` and `pageSize`; defaults 1 and 20; maximum effective `pageSize` 50. | 200 `CreationSummary[]` with `PageMeta` | `UNAUTHORIZED` / `TOKEN_EXPIRED` (401), `VALIDATION_ERROR` (422) |
| `GET /api/v1/categories` | none | public: 120/min | None | 200 `Category[]` | `RATE_LIMITED` (429) |
| `POST /api/v1/artisans` | requireAuth | none | Body: required `primaryCategoryId` integer 1–32767; optional nullable `bio` trimmed max 2000, `location` trimmed max 150, `whatsappNumber` trimmed 1–30 chars then E.164-normalized. | 201 `ArtisanWriteProfile` | `UNAUTHORIZED` / `TOKEN_EXPIRED` (401), `VALIDATION_ERROR` (422, including missing category or invalid phone), `GALLERY_EXISTS` (409) |
| `PATCH /api/v1/artisans/me` | requireAuth | none | Same profile fields, all optional; `primaryCategoryId` may be null; at least one field required. | 200 `ArtisanWriteProfile` | `UNAUTHORIZED` / `TOKEN_EXPIRED` (401), `VALIDATION_ERROR` (422), `NOT_FOUND` (404) |
| `GET /api/v1/artisans/:id` | optionalAuth | public: 120/min | Path `id`: UUID. Reads optional `X-Client-Platform` header (`web|android|ios|unknown`, otherwise treated as `unknown`). | 200 `ArtisanReadProfile` | `VALIDATION_ERROR` (422), `NOT_FOUND` (404), `RATE_LIMITED` (429) |
| `GET /api/v1/artisans/:id/creations` | optionalAuth | public: 120/min | Path `id`: UUID. Query optional `categoryId`, integer 1–32767. | 200 `CreationSummary[]` (unpaged) | `VALIDATION_ERROR` (422), `NOT_FOUND` (404), `RATE_LIMITED` (429) |
| `POST /api/v1/uploads/signature` | requireAuth | none | Body optional `purpose`: `creation|profile`, default `creation`. | 200 `UploadSignature` | `UNAUTHORIZED` / `TOKEN_EXPIRED` (401), `VALIDATION_ERROR` (422), `UPLOADS_NOT_CONFIGURED` (503) |
| `POST /api/v1/creations` | requireAuth; artisan only | none | Body: required `title` trimmed 1–255 chars, `categoryId` integer 1–32767, `media` array 1–8. Optional nullable `description` max 5000, `dimensions` max 100, nonnegative numeric `price`; `inventoryCount` integer ≥0 default 1; `availabilityStatus` `available|unavailable` default `available`; `status` `draft|published|archived` default `published`. Each media item requires valid URL max 2048 and trimmed `publicId` 1–255; optional positive-integer `width`, `height`. Available creations must have stock. | 201 `CreationDetail` | `UNAUTHORIZED` / `TOKEN_EXPIRED` (401), `NOT_AN_ARTISAN` (403), `VALIDATION_ERROR` (422), `AVAILABLE_NEEDS_STOCK` (422), `CONFLICT` (409, e.g. duplicate media public ID) |
| `GET /api/v1/creations/me` | requireAuth | none | Query: optional positive-integer `page` and `pageSize`; defaults 1 and 20; maximum effective `pageSize` 50. | 200 `CreationSummary[]` with `PageMeta` | `UNAUTHORIZED` / `TOKEN_EXPIRED` (401), `VALIDATION_ERROR` (422) |
| `GET /api/v1/creations` | optionalAuth | public: 120/min | Query optional `sort`=`likes|views|recent` (default `likes`), `categoryId` integer 1–32767, positive-integer `page` (default 1), `pageSize` 1–50 (default 20). | 200 `CreationSummary[]` with `PageMeta` | `VALIDATION_ERROR` (422), `RATE_LIMITED` (429) |
| `GET /api/v1/creations/:id` | optionalAuth | public: 120/min | Path `id`: UUID. | 200 `CreationDetail` | `VALIDATION_ERROR` (422), `NOT_FOUND` (404), `RATE_LIMITED` (429) |
| `PATCH /api/v1/creations/:id` | requireAuth; owner only | none | Path `id`: UUID. Any nonempty subset of creation fields from `POST /creations`; same limits/enums. `media`, if supplied, must contain 1–8 items. | 200 `CreationDetail` | `UNAUTHORIZED` / `TOKEN_EXPIRED` (401), `NOT_FOUND` (404), `FORBIDDEN` (403), `VALIDATION_ERROR` (422), `AVAILABLE_NEEDS_STOCK` (422) |
| `PATCH /api/v1/creations/:id/availability` | requireAuth; owner only | none | Path `id`: UUID. Body required `availabilityStatus`: `available|unavailable`. | 200 `CreationDetail` | `UNAUTHORIZED` / `TOKEN_EXPIRED` (401), `NOT_FOUND` (404), `FORBIDDEN` (403), `VALIDATION_ERROR` (422), `AVAILABLE_NEEDS_STOCK` (422) |
| `DELETE /api/v1/creations/:id` | requireAuth; owner only | none | Path `id`: UUID. Soft-deletes the creation. | 204, no body | `UNAUTHORIZED` / `TOKEN_EXPIRED` (401), `NOT_FOUND` (404), `FORBIDDEN` (403) |
| `POST /api/v1/creations/:id/view` | requireAuth | none | Path `id`: UUID. No body. Idempotent; self-view silently skipped; success is always 204. | 204, no body | `UNAUTHORIZED` / `TOKEN_EXPIRED` (401), `VALIDATION_ERROR` (422), `NOT_FOUND` (404) |
| `PUT /api/v1/creations/:id/like` | requireAuth | none | Path `id`: UUID. No body. Idempotent. | 200 `{liked, likesCount}` | `UNAUTHORIZED` / `TOKEN_EXPIRED` (401), `VALIDATION_ERROR` (422), `NOT_FOUND` (404), `SELF_LIKE_NOT_ALLOWED` (403) |
| `DELETE /api/v1/creations/:id/like` | requireAuth | none | Path `id`: UUID. No body. Idempotent. | 200 `{liked, likesCount}` | `UNAUTHORIZED` / `TOKEN_EXPIRED` (401), `VALIDATION_ERROR` (422), `NOT_FOUND` (404) |
| `POST /api/v1/creations/:id/inquiries` | requireAuth | none | Path `id`: UUID. No body. Reads optional `X-Client-Platform` (`web|android|ios|unknown`, otherwise `unknown`). | 200 `Inquiry` | `UNAUTHORIZED` / `TOKEN_EXPIRED` (401), `VALIDATION_ERROR` (422), `NOT_FOUND` (404), `SELF_INQUIRY_NOT_ALLOWED` (403) |

Both `/creations/me` and `/me/creations` are registered aliases for the current user’s creations.

### Response DTOs and nullability

- `Health`: `{status: string}`; currently `"ok"`.
- `User`: `id:string` (UUID), `fullName:string`, `email:string`, `phoneNumber:string` (E.164), `profilePictureUrl:string|null`, `preferredMode:"enthusiast"|"artisan"`, `onboardingCompleted:boolean`, `isArtisan:boolean`.
- `AuthResult`: `{user:User, accessToken:string, refreshToken:string, tokenType:"Bearer", expiresIn:number}`. `expiresIn` is seconds. Refresh returns `TokenPair` only: `{accessToken:string, refreshToken:string, tokenType:"Bearer", expiresIn:number}`.
- `Category`: `{id:number, slug:string, name:string}`.
- `CreationSummary`: `{id:string, artisanId:string, categoryId:number, title:string, description:string|null, dimensions:string|null, price:number|null, currency:string, inventoryCount:number, availabilityStatus:"available"|"unavailable", status:"draft"|"published"|"archived", viewsCount:number, likesCount:number, createdAt:string, updatedAt:string, categoryName:string, coverImageUrl:string|null, artisanName:string, artisanProfilePictureUrl:string|null}`. These are flat fields; feed/gallery lists do not include viewer flags.
- `CreationDetail`: every `CreationSummary` key plus `isLikedByMe:boolean`, `isOwnCreation:boolean`, and `media:MediaItem[]`. `MediaItem` is `{id:string, url:string, publicId:string, position:number, width:number|null, height:number|null}`. Details include both viewer flags even for guests.
- Artisan POST/PATCH result (`ArtisanWriteProfile`): `{userId:string, bio:string|null, location:string|null, primaryCategoryId:number|null, whatsappNumber:string|null, isActive:boolean, galleryOpenedAt:string, updatedAt:string}`. `updatedAt` is included here; POST's primary category is non-null in practice.
- Artisan GET result (`ArtisanReadProfile`): `{userId:string, bio:string|null, location:string|null, primaryCategoryId:number|null, primaryCategoryName:string|null, whatsappNumber:string|null, isActive:boolean, galleryOpenedAt:string, fullName:string, profilePictureUrl:string|null, recentCreations:CreationSummary[]}`. Guest `whatsappNumber` is null; signed-in profile lookup falls back to the artisan's account phone when no separate WhatsApp number is set.
- `Inquiry`: `{whatsappUrl:string, message:string, captionMessage:string, whatsappNumber:string, imageUrl:string|null}`. `whatsappUrl` uses `https://wa.me/<digits>?text=<encoded message>`. `whatsappNumber` is the same artisan WhatsApp/account fallback number as the link, digits only. The message's `Photo:` line uses the stored cover URL with `f_jpg,q_auto,w_800/`; `imageUrl` uses `f_jpg,q_auto,w_1080/`. If the stored URL contains `/image/upload/`, the transformation is inserted immediately after it; other URLs are returned unchanged. `captionMessage` is the message without the photo line. With no cover media, `imageUrl` is null and there is no photo line.
- `UploadSignature`: `{cloudName:string, apiKey:string, timestamp:number, signature:string, folder:string}`. Treat the signature as short-lived sensitive upload data.
- `LikeResult`: `{liked:boolean, likesCount:number}`.
- Paginated routes return `meta:{page:number,pageSize:number,total:number,hasMore:boolean}` alongside `data`.

## 4 Detailed endpoints with real example JSON

Live captures below were made against the running local API. Arrays are trimmed to their first two returned items; bearer tokens, refresh tokens, API key, and signature are redacted. The new capture account and fake-media creation were actually inserted into the local database.

### Health, categories, and feed

`GET /health`:

```json
{"data":{"status":"ok"}}
```

`GET /categories` (first two of five):

```json
{"data":[{"id":1,"slug":"paintings","name":"Paintings"},{"id":2,"slug":"pottery-ceramics","name":"Pottery and Ceramics"}]}
```

`GET /creations?sort=likes&page=1&pageSize=2&categoryId=1` and `GET /creations?sort=views&page=1&pageSize=2&categoryId=1` both returned the following first two items and pagination metadata in this capture:

```json
{"data":[{"id":"4b428757-c7f6-412b-82b9-f9fd9d666eb0","artisanId":"01e830e5-c022-4bc1-86c7-ff7311874edb","categoryId":1,"title":"Smoke creation 4c380cbc-4342-4817-8bef-629054214ab7","description":"Created by the API smoke test.","dimensions":null,"price":null,"currency":"MWK","inventoryCount":1,"availabilityStatus":"unavailable","status":"published","viewsCount":1,"likesCount":1,"createdAt":"2026-10-02T21:16:02.275Z","updatedAt":"2026-10-02T21:16:02.420Z","categoryName":"Paintings","coverImageUrl":"https://res.cloudinary.com/demo/image/upload/sample.jpg","artisanName":"Smoke a","artisanProfilePictureUrl":null},{"id":"1ddc590d-f89a-433c-8cf9-98b030cdc2d2","artisanId":"5a0a4510-5d68-49f5-8edd-7b876d341063","categoryId":1,"title":"Smoke creation 499fb1c1-c668-41f5-b405-8a7356a69bd5","description":"Created by the API smoke test.","dimensions":null,"price":null,"currency":"MWK","inventoryCount":1,"availabilityStatus":"unavailable","status":"published","viewsCount":1,"likesCount":1,"createdAt":"2026-10-02T21:08:57.145Z","updatedAt":"2026-10-02T21:08:57.300Z","categoryName":"Paintings","coverImageUrl":"https://res.cloudinary.com/demo/image/upload/sample.jpg","artisanName":"Smoke a","artisanProfilePictureUrl":null}],"meta":{"page":1,"pageSize":2,"total":21,"hasMore":true}}
```

### Creation detail and artisan profile as guest and signed in

Both Guest and signed-in `GET /creations/4b428757-c7f6-412b-82b9-f9fd9d666eb0` returned a detail object with `isLikedByMe:false` and `isOwnCreation:false` for those viewers:

```json
{"data":{"id":"4b428757-c7f6-412b-82b9-f9fd9d666eb0","artisanId":"01e830e5-c022-4bc1-86c7-ff7311874edb","categoryId":1,"title":"Smoke creation 4c380cbc-4342-4817-8bef-629054214ab7","description":"Created by the API smoke test.","dimensions":null,"price":null,"currency":"MWK","inventoryCount":1,"availabilityStatus":"unavailable","status":"published","viewsCount":1,"likesCount":1,"createdAt":"2026-10-02T21:16:02.275Z","updatedAt":"2026-10-02T21:16:02.420Z","categoryName":"Paintings","coverImageUrl":"https://res.cloudinary.com/demo/image/upload/sample.jpg","artisanName":"Smoke a","artisanProfilePictureUrl":null,"isLikedByMe":false,"isOwnCreation":false,"media":[{"id":"05076904-43ae-4432-9fbe-b2681b70d0fe","url":"https://res.cloudinary.com/demo/image/upload/sample.jpg","publicId":"smoke/4c380cbc-4342-4817-8bef-629054214ab7","position":0,"width":null,"height":null}]}}
```

Guest `GET /artisans/01e830e5-c022-4bc1-86c7-ff7311874edb`:

```json
{"data":{"userId":"01e830e5-c022-4bc1-86c7-ff7311874edb","bio":null,"location":null,"primaryCategoryId":1,"primaryCategoryName":"Paintings","whatsappNumber":null,"isActive":true,"galleryOpenedAt":"2026-10-02T21:16:02.257Z","fullName":"Smoke a","profilePictureUrl":null,"recentCreations":[{"id":"4b428757-c7f6-412b-82b9-f9fd9d666eb0","artisanId":"01e830e5-c022-4bc1-86c7-ff7311874edb","categoryId":1,"title":"Smoke creation 4c380cbc-4342-4817-8bef-629054214ab7","description":"Created by the API smoke test.","dimensions":null,"price":null,"currency":"MWK","inventoryCount":1,"availabilityStatus":"unavailable","status":"published","viewsCount":1,"likesCount":1,"createdAt":"2026-10-02T21:16:02.275Z","updatedAt":"2026-10-02T21:16:02.420Z","categoryName":"Paintings","coverImageUrl":"https://res.cloudinary.com/demo/image/upload/sample.jpg","artisanName":"Smoke a","artisanProfilePictureUrl":null}]}}
```

Signed-in GET of that profile included the contact number:

```json
{"data":{"userId":"01e830e5-c022-4bc1-86c7-ff7311874edb","bio":null,"location":null,"primaryCategoryId":1,"primaryCategoryName":"Paintings","whatsappNumber":"+265991721952","isActive":true,"galleryOpenedAt":"2026-10-02T21:16:02.257Z","fullName":"Smoke a","profilePictureUrl":null,"recentCreations":[{"id":"4b428757-c7f6-412b-82b9-f9fd9d666eb0","artisanId":"01e830e5-c022-4bc1-86c7-ff7311874edb","categoryId":1,"title":"Smoke creation 4c380cbc-4342-4817-8bef-629054214ab7","description":"Created by the API smoke test.","dimensions":null,"price":null,"currency":"MWK","inventoryCount":1,"availabilityStatus":"unavailable","status":"published","viewsCount":1,"likesCount":1,"createdAt":"2026-10-02T21:16:02.275Z","updatedAt":"2026-10-02T21:16:02.420Z","categoryName":"Paintings","coverImageUrl":"https://res.cloudinary.com/demo/image/upload/sample.jpg","artisanName":"Smoke a","artisanProfilePictureUrl":null}]}}
```

`GET /artisans/01e830e5-c022-4bc1-86c7-ff7311874edb/creations` returned the same creation as a flat array (no grouping or pagination):

```json
{"data":[{"id":"4b428757-c7f6-412b-82b9-f9fd9d666eb0","artisanId":"01e830e5-c022-4bc1-86c7-ff7311874edb","categoryId":1,"title":"Smoke creation 4c380cbc-4342-4817-8bef-629054214ab7","description":"Created by the API smoke test.","dimensions":null,"price":null,"currency":"MWK","inventoryCount":1,"availabilityStatus":"unavailable","status":"published","viewsCount":1,"likesCount":1,"createdAt":"2026-10-02T21:16:02.275Z","updatedAt":"2026-10-02T21:16:02.420Z","categoryName":"Paintings","coverImageUrl":"https://res.cloudinary.com/demo/image/upload/sample.jpg","artisanName":"Smoke a","artisanProfilePictureUrl":null}]}
```

### Authentication, onboarding, and creating content

New registration returned HTTP 201. Secret token values are redacted:

```json
{"data":{"user":{"id":"a9807278-b8bd-4d2f-b274-22e2389cb413","fullName":"Android Capture User","email":"android-179252339473234@naep.test","phoneNumber":"+265993394732","profilePictureUrl":null,"preferredMode":"enthusiast","onboardingCompleted":false,"isArtisan":false},"accessToken":"[REDACTED]","refreshToken":"[REDACTED]","tokenType":"Bearer","expiresIn":900}}
```

`POST /auth/login` for `demo1@naep.test` / `Password123!` returned HTTP 200 with the same `AuthResult` shape. Captured user fields were `id:"ab39eee4-2b05-4806-bd39-495fc1f488a9"`, `fullName:"Demo Enthusiast"`, `email:"demo1@naep.test"`, `phoneNumber:"+265991000001"`, `profilePictureUrl:null`, `preferredMode:"enthusiast"`, `onboardingCompleted:false`, `isArtisan:false`; tokens are omitted/redacted.

`POST /auth/refresh` returned HTTP 200:

```json
{"data":{"accessToken":"[REDACTED]","refreshToken":"[REDACTED]","tokenType":"Bearer","expiresIn":900}}
```

`GET /me` for the newly registered account:

```json
{"data":{"id":"a9807278-b8bd-4d2f-b274-22e2389cb413","fullName":"Android Capture User","email":"android-179252339473234@naep.test","phoneNumber":"+265993394732","profilePictureUrl":null,"preferredMode":"enthusiast","onboardingCompleted":false,"isArtisan":false}}
```

`PATCH /me` updates the current user's name, mode, and/or picture. For example, update the name and remove an existing picture:

```http
PATCH /api/v1/me
Authorization: Bearer <access-token>
Content-Type: application/json

{"fullName":"New Name","profilePictureUrl":null,"profilePicturePublicId":null}
```

It returns HTTP 200 with the same `User` response shape as `GET /me`. The body is strict and cannot be empty. Picture URL and public ID must be supplied together: both as strings to set a picture, or both as `null` to remove it; either field alone or a mixed null/string pair returns `422 VALIDATION_ERROR`.

**Profile picture flow:** request `POST /uploads/signature` with `{"purpose":"profile"}` while authenticated. The returned folder is scoped to `profiles/<userId>`; upload the image directly to Cloudinary with the signed fields, then send its `secure_url` and `public_id` together to `PATCH /me`. The API verifies that both belong to that user's profile folder, persists them, and best-effort deletes the replaced asset. Send both picture fields as `null` to remove the picture; the previous Cloudinary asset is then deleted best-effort.

`POST /me/onboarding` with `{"mode":"artisan"}` returned:

```json
{"data":{"id":"a9807278-b8bd-4d2f-b274-22e2389cb413","fullName":"Android Capture User","email":"android-179252339473234@naep.test","phoneNumber":"+265993394732","profilePictureUrl":null,"preferredMode":"artisan","onboardingCompleted":true,"isArtisan":false}}
```

`POST /artisans` with bio `Capture profile`, location `Lilongwe, Malawi`, and primary category `1` returned HTTP 201:

```json
{"data":{"userId":"a9807278-b8bd-4d2f-b274-22e2389cb413","bio":"Capture profile","location":"Lilongwe, Malawi","primaryCategoryId":1,"whatsappNumber":null,"isActive":true,"galleryOpenedAt":"2026-10-20T19:09:56.245Z","updatedAt":"2026-10-20T19:09:56.245Z"}}
```

`POST /creations` with one synthetic media item returned HTTP 201:

```json
{"data":{"id":"d4215d6d-c24d-4595-8549-f13d5e309ae6","artisanId":"a9807278-b8bd-4d2f-b274-22e2389cb413","categoryId":1,"title":"Android API Capture Artwork","description":"Synthetic test item created for the requested live capture.","dimensions":"20 cm x 30 cm","price":12345.67,"currency":"MWK","inventoryCount":2,"availabilityStatus":"available","status":"published","viewsCount":0,"likesCount":0,"createdAt":"2026-10-20T19:09:56.307Z","updatedAt":"2026-10-20T19:09:56.307Z","categoryName":"Paintings","coverImageUrl":"https://res.cloudinary.com/demo/image/upload/fake-capture.jpg","artisanName":"Android Capture User","artisanProfilePictureUrl":null,"isLikedByMe":false,"isOwnCreation":true,"media":[{"id":"090fb666-7341-415b-be29-9c8805e9d4ee","url":"https://res.cloudinary.com/demo/image/upload/fake-capture.jpg","publicId":"naep/fake-capture-179252339473234","position":0,"width":800,"height":600}]}}
```

`PATCH /creations/d4215d6d-c24d-4595-8549-f13d5e309ae6/availability` with `{"availabilityStatus":"unavailable"}` returned HTTP 200. The returned `CreationDetail` matched the create response except `availabilityStatus:"unavailable"` and `updatedAt:"2026-10-20T19:09:56.464Z"`.

### Engagement, inquiry, upload, and error examples

For the synthetic creation above, the live responses were:

```json
{"like":{"status":200,"body":{"data":{"liked":true,"likesCount":1}}},"unlike":{"status":200,"body":{"data":{"liked":false,"likesCount":0}}},"view":{"status":204,"body":null}}
```

Inquiry returned HTTP 200. The cover URL is transformed from the stored media URL, using explicit JPEG output so WhatsApp can preview or attach it. `captionMessage` omits the photo line and `whatsappNumber` is the digits-only number also used in `whatsappUrl`:

```json
{"data":{"whatsappUrl":"https://wa.me/265993394732?text=Hello%2C%20I%20am%20interested%20in%20this%20creation%20on%20NAEP%3A%0ATitle%3A%20Android%20API%20Capture%20Artwork%0ACategory%3A%20Paintings%0ADimensions%3A%2020%20cm%20x%2030%20cm%0ADescription%3A%20Synthetic%20test%20item%20created%20for%20the%20requested%20live%20capture.%0APrice%3A%20MWK%2012%2C345.67%0AAvailability%3A%20Unavailable%0AView%20creation%3A%20http%3A%2F%2Flocalhost%3A3000%2Fcreations%2Fd4215d6d-c24d-4595-8549-f13d5e309ae6%0APhoto%3A%20https%3A%2F%2Fres.cloudinary.com%2Fdemo%2Fimage%2Fupload%2Ff_jpg%2Cq_auto%2Cw_800%2Ffake-capture.jpg","message":"Hello, I am interested in this creation on NAEP:\nTitle: Android API Capture Artwork\nCategory: Paintings\nDimensions: 20 cm x 30 cm\nDescription: Synthetic test item created for the requested live capture.\nPrice: MWK 12,345.67\nAvailability: Unavailable\nView creation: http://localhost:3000/creations/d4215d6d-c24d-4595-8549-f13d5e309ae6\nPhoto: https://res.cloudinary.com/demo/image/upload/f_jpg,q_auto,w_800/fake-capture.jpg","captionMessage":"Hello, I am interested in this creation on NAEP:\nTitle: Android API Capture Artwork\nCategory: Paintings\nDimensions: 20 cm x 30 cm\nDescription: Synthetic test item created for the requested live capture.\nPrice: MWK 12,345.67\nAvailability: Unavailable\nView creation: http://localhost:3000/creations/d4215d6d-c24d-4595-8549-f13d5e309ae6","whatsappNumber":"265993394732","imageUrl":"https://res.cloudinary.com/demo/image/upload/f_jpg,q_auto,w_1080/fake-capture.jpg"}}
```

`POST /uploads/signature` returned HTTP 200; values needed for signing have been redacted:

```json
{"data":{"cloudName":"bor4mtcx","apiKey":"[REDACTED]","timestamp":1792523396,"signature":"[REDACTED]","folder":"naep_assets/creations"}}
```

`POST /auth/logout` with the registered account's original refresh token (already rotated by the prior refresh call) returned HTTP 204 and no body:

```json
null
```

Validation error (HTTP 422):

```json
{"error":{"code":"VALIDATION_ERROR","message":"Request validation failed","details":[{"field":"fullName","message":"Too small: expected string to have >=2 characters","code":"too_small"},{"field":"phoneNumber","message":"Invalid input: expected string, received undefined","code":"invalid_type"}]}}
```

Unauthenticated `GET /me` (HTTP 401):

```json
{"error":{"code":"UNAUTHORIZED","message":"Authentication is required"}}
```

Unknown route (HTTP 404):

```json
{"error":{"code":"NOT_FOUND","message":"Route not found"}}
```

Rate-limit response (HTTP 429):

```json
{"error":{"code":"RATE_LIMITED","message":"Too many requests. Please try again later."}}
```

## 5 Cross-cutting facts

- **Tokens:** configured access-token TTL is `15m`; observed `expiresIn` is 900 seconds. Refresh tokens expire after 30 days. Refresh tokens are 48 random bytes encoded as 96 hex characters; only their SHA-256 hashes are stored. Refresh atomically revokes the old token and issues a new token pair. Reusing the old token returns `401 REFRESH_INVALID`. Caveat: the refresh validator defaults an omitted platform to `unknown`, so rotation may replace the stored platform with `unknown`.
- **Rate limits:** shared public limiter is 120 requests / 60 seconds; shared auth limiter is 10 requests / 60 seconds. Both use the `express-rate-limit` default in-memory store, standard rate-limit headers, and return `429 RATE_LIMITED`. Limits are hard-coded, not configurable by environment. Other routes have no limiter. The live 429 was deliberately triggered by a short health burst.
- **Paging:** default page 1, default page size 20, maximum effective page size 50. `GET /me/creations` applies the maximum in pagination logic; feed validation also caps at 50. Pagination metadata is `{page,pageSize,total,hasMore}`.
- **Identifiers and values:** user, artisan, creation, and media IDs are UUIDs. Category IDs are small integers (1–32767 accepted). Phone numbers are normalized to E.164 using `DEFAULT_COUNTRY_CODE=265` (for example, `0991234567` becomes `+265991234567`). Price is a JSON number or null; currency is `MWK`.
- **Dates and JSON:** PostgreSQL timestamps serialize as ISO-8601 UTC strings (for example `2026-10-20T19:09:56.307Z`). JSON object keys are camelCase.
- **Errors:** error envelope is `{error:{code:string,message:string,details?:unknown}}`; `details` is omitted unless present. Zod validation errors are HTTP 422 and include an array of `{field,message,code}`. Unexpected errors map to `500 INTERNAL_ERROR`; stack traces are not returned.
- **CORS and middleware:** Helmet is enabled. CORS uses `{origin:true}` (reflects request origins; no allowlist is configured; credentials are not enabled). Express JSON body limit is 1 MiB. Morgan development logging is enabled.
- **Proxy and headers:** `TRUST_PROXY` supports boolean, hop-count, or proxy-list configuration and defaults to `false`. Protected routes read `Authorization: Bearer ...`. Artisan profile and inquiry controllers read `X-Client-Platform`; only `web`, `android`, `ios`, and `unknown` are accepted, with all other/missing values treated as `unknown`. Auth register/login/refresh also accept a `platform` JSON field.
- **Guest behavior:** `optionalAuth` ignores invalid/expired bearer tokens and continues as Guest. Guests may read feeds, categories, profiles, and details; they cannot record views, like, create inquiries, or access protected routes. Artisan profile contact is hidden from guests. Self views are skipped; self likes and inquiries are forbidden.
- **Demo accounts:** `demo1@naep.test` / `Password123!` is active, enthusiast mode, not onboarded, and has no artisan profile. `demo2@naep.test` and `demo3@naep.test` use the same password; both are active, artisan mode, not onboarded, and have profiles with five creations each in the live database. Example seeded data includes “Lake Malawi at Dawn” (MWK 25,000), “Hand-thrown Clay Vessel” (MWK 30,000), “Chichewa Pattern Pot” (MWK 42,000), and “Harvest Colours” (MWK 98,000). The database also contains accumulated smoke-test data.
- **Health:** `/health` returns a static liveness value; it does not verify database or Cloudinary health.

## 6 Differences from the planned contract

Compared with section 3 of `docs/NAEP_Backend_Agent_Prompts.md`:

1. **Extra routes:** `GET /me/creations` and `GET /creations/me` both exist in code but are not listed in the section 3 endpoint table.
3. **Logout authorization:** planned `POST /auth/logout` requires auth (`A`); implementation has no `requireAuth` middleware. It only validates the refresh token in the body and revokes it if found.
4. **Registration fields:** the planned body allows optional `profilePictureUrl` and `profilePicturePublicId`; the actual strict schema rejects both as unknown fields. Actual registration adds optional `platform`, which the planned register body does not list.
5. **Auth response fields:** planned `AuthResult` lists access token, refresh token, and user. Actual register/login add `tokenType` and `expiresIn`. Actual refresh returns the token pair plus those two fields, without a user object.
6. **Creation summary shape:** planned summary nests `cover:{url,publicId}|null` and `artisan:{id,fullName,profilePictureUrl}`. Actual JSON uses flat `coverImageUrl`, `artisanId`, `artisanName`, and `artisanProfilePictureUrl`; it does not return a cover public ID or nested artisan object. Actual summaries also include `description`, `dimensions`, `inventoryCount`, `status`, `createdAt`, and `updatedAt`.
7. **Creation detail shape:** planned detail adds an `artisan` object including `location`; actual detail keeps the flat summary fields and has no nested artisan or artisan location. It does return `isLikedByMe`, `isOwnCreation`, and media items.
8. **Artisan profile shape:** planned profile contains nested `primaryCategory:{id,name}|null`; actual response splits this into `primaryCategoryId` and `primaryCategoryName`. Actual profiles additionally include `isActive` and `galleryOpenedAt`; write responses include `updatedAt`. `whatsappNumber` is present as a key and null for guests.
9. **Gallery response organization:** the contract describes artisan creations as grouped by category. Actual `GET /artisans/:id/creations` returns a flat, unpaged array, ordered by category sort order and then creation recency.
10. **WhatsApp default representation:** the contract says opening a gallery defaults WhatsApp to the user phone. The database keeps `whatsapp_number` null when omitted; profile GET for a signed-in viewer and inquiry lookup dynamically fall back to the account phone, but the POST gallery response itself contains `whatsappNumber:null`.

The planned and actual methods for engagement match (`POST` view, `PUT` like, `DELETE` unlike). Health, categories, onboarding, creation availability, inquiries, and upload-signature routes are present. The planned section does not specify success status codes beyond view’s 204, so other status-code differences cannot be asserted from that section.

## 7 Known problems or things the mobile app must work around

- **Inquiry image compatibility:** inquiry URLs use the cover's stored media URL and request `f_jpg` explicitly for WhatsApp compatibility. `imageUrl` is null and no photo line is included when a creation has no cover media.
- **Use actual response field names:** parse flat `coverImageUrl`, `artisanName`, and `artisanProfilePictureUrl`; do not expect nested `cover` or `artisan`. Likewise use `primaryCategoryId` / `primaryCategoryName`, not a nested category object.
- **Registration does not accept profile photos:** send only the fields in its validator. Including planned profile-picture fields causes a validation response.
- **Refresh platform caveat:** send the intended `platform` explicitly on every refresh to avoid the default `unknown` being persisted on the replacement token.
- **Gallery category grouping is client-side:** the endpoint returns a flat list and no page metadata; group by `categoryId` or `categoryName` in the app if the UI needs grouped sections.
- **The report’s live tests left database data behind:** account `android-179252339473234@naep.test` and creation `d4215d6d-c24d-4595-8549-f13d5e309ae6` remain in local PostgreSQL. The creation uses a synthetic media URL, is set to `unavailable`, and has one view; unlike restored its like count to zero. Inquiry was recorded. No cleanup endpoint exists. The demo dataset also includes 36 smoke-prefixed users and other smoke-test records.
- **One live refresh token remains active:** the test refreshed the new account’s token and then sent the now-revoked original token to logout, which returned 204 but could not revoke the replacement token. This is a side effect of the requested refresh/logout capture sequence.
- **Rate-limit burst:** the live capture exhausted the public limiter. It is in-memory and expires with its window or server restart; allow a minute before relying on a fresh public request quota.
- **Android networking:** the API base is plain HTTP in development. Android release builds may block cleartext HTTP; configure a debug cleartext exception or use HTTPS outside local development.
