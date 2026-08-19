# 보수 가계부 API 문서

> Spring Boot + PostgreSQL + JWT  
> Base URL: `http://localhost:8080`  
> Swagger: `/swagger-ui.html`

---

## 개요


| 항목     | 값                                                       |
| ------ | ------------------------------------------------------- |
| 서버 포트  | `8080`                                                  |
| DB     | PostgreSQL `jdbc:postgresql://localhost:5432/housebook` |
| 스키마 관리 | Flyway (`ddl-auto: validate`)                           |
| 인증 방식  | JWT Bearer (`Authorization: Bearer <token>`)            |
| 공개 API | `/api/auth/**`만                                         |
| 데이터 격리 | household(가계부) 단위                                       |


### 요청 흐름

```
클라이언트
  → JwtAuthFilter (Bearer 토큰 검증)
  → Controller
  → Service
  → Repository (JPA)
  → PostgreSQL
```

로그인 userId → `household_members`에서 household 조회 → 모든 도메인 API가 그 household 스코프로만 CRUD

---

## DB 테이블 관계

```
users ──< household_members >── households
  │                                │
  │                                ├── categories
  │                                ├── cards ──> users (owner, optional)
  │                                ├── transactions ──> categories, cards?, users
  │                                └── import_batches ──> cards?
```

### 테이블 상세

#### users


| 컬럼                      | 타입                  | 비고       |
| ----------------------- | ------------------- | -------- |
| id                      | BIGSERIAL PK        |          |
| email                   | VARCHAR(255) UNIQUE |          |
| password                | VARCHAR(255)        | BCrypt   |
| name                    | VARCHAR(100)        |          |
| birth_date              | DATE                | nullable |
| created_at / updated_at | TIMESTAMP           |          |


#### households


| 컬럼                      | 타입             | 비고    |
| ----------------------- | -------------- | ----- |
| id                      | BIGSERIAL PK   |       |
| name                    | VARCHAR        |       |
| invite_code             | VARCHAR UNIQUE | 초대 코드 |
| created_at / updated_at | TIMESTAMP      |       |


#### household_members


| 컬럼           | 타입              | 비고                                     |
| ------------ | --------------- | -------------------------------------- |
| id           | BIGSERIAL PK    |                                        |
| household_id | FK → households | CASCADE                                |
| user_id      | FK → users      | CASCADE, UNIQUE(household_id, user_id) |
| role         | ENUM            | `OWNER` / `MEMBER`                     |
| joined_at    | TIMESTAMP       |                                        |


#### categories


| 컬럼                      | 타입              | 비고                   |
| ----------------------- | --------------- | -------------------- |
| id                      | BIGSERIAL PK    |                      |
| household_id            | FK → households | CASCADE              |
| parent_id               | FK → categories | SET NULL, 대분류면 null  |
| name                    | VARCHAR         |                      |
| type                    | ENUM            | `INCOME` / `EXPENSE` |
| color                   | VARCHAR         |                      |
| sort_order              | INT             | 정렬 순서                |
| created_at / updated_at | TIMESTAMP       |                      |


가계부 생성 시(또는 카테고리가 비어 있을 때) 엑셀 구조 기본 트리를 시드한다.

**지출 대분류 → 소분류**

- 생활비 → 외식비, 식대/생필품, 통신비/인터넷, 멤버십 비용
- 주거비 → 집 원금, 집 이자, 관리비
- 자동차 → 충전비/통비, 보험, 세금, 주차비
- 보험 → 수민, 보영, 혜린
- 수민 → 개인용돈, 계모임
- 보영 → 개인용돈, 계모임
- 혜린 → 햇살이용품
- 미래준비 → 적금, 수민 IRP, 투자모으기, 코인
- 병원 → 병원약
- 기타 → 카드 연회비, 경조사비, 특수지출

**수입**: 급여, 기타수입

#### cards


| 컬럼                      | 타입              | 비고                          |
| ----------------------- | --------------- | --------------------------- |
| id                      | BIGSERIAL PK    |                             |
| household_id            | FK → households | CASCADE                     |
| name                    | VARCHAR         |                             |
| type                    | ENUM            | `CREDIT` / `DEBIT` / `CASH` |
| owner_user_id           | FK → users      | SET NULL, optional          |
| created_at / updated_at | TIMESTAMP       |                             |


#### transactions


| 컬럼                      | 타입                               | 비고                   |
| ----------------------- | -------------------------------- | -------------------- |
| id                      | BIGSERIAL PK                     |                      |
| household_id            | FK → households                  | CASCADE              |
| type                    | ENUM                             | `INCOME` / `EXPENSE` |
| amount                  | NUMERIC(14,2)                    |                      |
| transaction_date        | DATE                             |                      |
| category_id             | FK → categories                  | required             |
| card_id                 | FK → cards                       | SET NULL, optional   |
| user_id                 | FK → users                       | 작성자                  |
| memo                    | VARCHAR(500)                     |                      |
| created_at / updated_at | TIMESTAMP                        |                      |
| INDEX                   | (household_id, transaction_date) |                      |


#### import_batches


| 컬럼             | 타입              | 비고                                         |
| -------------- | --------------- | ------------------------------------------ |
| id             | BIGSERIAL PK    |                                            |
| household_id   | FK → households | CASCADE                                    |
| provider       | ENUM            | `SAMSUNG_CARD` / `GYEONGGI_LOCAL_CURRENCY` |
| card_id        | FK → cards      | SET NULL                                   |
| file_checksum  | VARCHAR(64)     | UNIQUE(household_id, checksum)             |
| imported_count | INT             |                                            |
| skipped_count  | INT             |                                            |
| created_at     | TIMESTAMP       |                                            |


---

## 엔드포인트 한눈에 보기


| Method | Path                                | Auth | 설명           |
| ------ | ----------------------------------- | ---- | ------------ |
| POST   | `/api/auth/signup`                  | 공개   | 회원가입         |
| POST   | `/api/auth/login`                   | 공개   | 로그인          |
| GET    | `/api/users/me`                     | JWT  | 내 정보 조회      |
| PATCH  | `/api/users/me`                     | JWT  | 내 정보 수정      |
| PUT    | `/api/users/me/password`            | JWT  | 비밀번호 변경      |
| DELETE | `/api/users/me`                     | JWT  | 회원 탈퇴        |
| POST   | `/api/households`                   | JWT  | 가계부 생성       |
| GET    | `/api/households/me`                | JWT  | 내 가계부 조회     |
| GET    | `/api/households/invite-code`       | JWT  | 초대 코드 조회     |
| POST   | `/api/households/join`              | JWT  | 가계부 참여       |
| GET    | `/api/categories`                   | JWT  | 카테고리 목록      |
| POST   | `/api/categories`                   | JWT  | 카테고리 생성      |
| PUT    | `/api/categories/{categoryId}`      | JWT  | 카테고리 수정      |
| DELETE | `/api/categories/{categoryId}`      | JWT  | 카테고리 삭제      |
| GET    | `/api/cards`                        | JWT  | 카드 목록        |
| POST   | `/api/cards`                        | JWT  | 카드 생성        |
| PUT    | `/api/cards/{cardId}`               | JWT  | 카드 수정        |
| DELETE | `/api/cards/{cardId}`               | JWT  | 카드 삭제        |
| GET    | `/api/transactions`                 | JWT  | 월별 거래 조회     |
| POST   | `/api/transactions`                 | JWT  | 거래 생성        |
| PUT    | `/api/transactions/{transactionId}` | JWT  | 거래 수정        |
| DELETE | `/api/transactions/{transactionId}` | JWT  | 거래 삭제        |
| GET    | `/api/statistics/monthly`           | JWT  | 월별 통계        |
| GET    | `/api/statistics/range`             | JWT  | 기간(월별 추이) 통계 |
| POST   | `/api/imports`                      | JWT  | 명세서 임포트      |
| POST   | `/api/admin/auth/login`             | 공개  | 관리자 로그인      |
| GET    | `/api/admin/stats`                  | 관리자 JWT | 서비스 통계  |
| GET    | `/api/admin/users`                  | 관리자 JWT | 전체 회원 목록 |
| GET    | `/api/admin/households`             | 관리자 JWT | 전체 가계부 목록 |
| POST   | `/api/admin/users/{userId}/block`   | 관리자 JWT | 회원 차단   |
| POST   | `/api/admin/users/{userId}/unblock` | 관리자 JWT | 회원 차단 해제 |
| DELETE | `/api/admin/users/{userId}`         | 관리자 JWT | 회원 강제 탈퇴 |


---

## 1. Auth

### POST `/api/auth/signup` — 공개

회원가입 후 JWT 발급

**Request Body**

```json
{
  "email": "user@example.com",
  "password": "password123",
  "name": "홍길동",
  "birthDate": "1990-01-01"
}
```


| 필드        | 타입     | 제약         |
| --------- | ------ | ---------- |
| email     | string | 필수         |
| password  | string | 필수, 8~100자 |
| name      | string | 필수         |
| birthDate | date   | `@Past`    |


**Response** `201`

```json
{
  "accessToken": "eyJhbGciOi...",
  "userId": 1,
  "email": "user@example.com",
  "name": "홍길동",
  "birthDate": "1990-01-01"
}
```

**DB**

- `users` INSERT
- 이메일 중복 시 `409`

---

### POST `/api/auth/login` — 공개

로그인 후 JWT 발급

**Request Body**

```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response** `200` — signup과 동일한 `TokenResponse`

**DB**

- `users` SELECT by email
- BCrypt 비밀번호 검증
- 실패 시 `401`

---

## 2. Users

> Header: `Authorization: Bearer <token>`

### GET `/api/users/me`

**Response**

```json
{
  "id": 1,
  "email": "user@example.com",
  "name": "홍길동",
  "birthDate": "1990-01-01"
}
```

**DB**: `users` SELECT

---

### PATCH `/api/users/me`

**Request Body**

```json
{
  "name": "홍길동",
  "birthDate": "1990-01-01"
}
```

**DB**: `users.name`, `users.birth_date` UPDATE

---

### PUT `/api/users/me/password`

**Request Body**

```json
{
  "currentPassword": "oldPassword123",
  "newPassword": "newPassword456"
}
```

**Response**: `204 No Content` (현재 비밀번호가 틀리면 `401 Unauthorized`)

**DB**: `users.password` UPDATE

---

### DELETE `/api/users/me`

**Request Body**

```json
{
  "password": "currentPassword123"
}
```

**Response**: `204 No Content` (비밀번호가 틀리면 `401 Unauthorized`)

탈퇴 시 소속 가계부 멤버십은 함께 삭제되고, 본인이 작성한 거래는 삭제되지 않고 작성자만
"탈퇴한 사용자"로 표시됩니다(가족이 공유하는 가계부 기록은 보존). 본인 소유 카드/자산은
소유자 없음으로 남습니다.

**DB**: `users` DELETE (`household_members` CASCADE, `transactions.user_id`/`cards.owner_user_id`/`assets.owner_user_id` SET NULL)

---

## 3. Households

> Header: `Authorization: Bearer <token>`  
> 한 유저는 가계부 하나에만 소속 가능

### POST `/api/households`

가계부 생성 (본인이 OWNER)

**Request Body**

```json
{
  "name": "우리집 가계부"
}
```

**Response** `201`

```json
{
  "id": 1,
  "name": "우리집 가계부",
  "inviteCode": "ABC123",
  "members": [
    {
      "userId": 1,
      "name": "홍길동",
      "email": "user@example.com",
      "role": "OWNER"
    }
  ]
}
```

**DB**

1. `households` INSERT
2. `household_members` INSERT (role = OWNER)
3. 이미 소속 시 `409`

---

### GET `/api/households/me`

내 가계부 + 멤버 목록 조회

**DB**

1. `household_members`에서 userId로 소속 조회
2. `households` 조회
3. 멤버 목록 + `users` 조인

---

### GET `/api/households/invite-code`

**Response**

```json
{
  "inviteCode": "ABC123"
}
```

**DB**: `households.invite_code` 조회

---

### POST `/api/households/join`

초대 코드로 가계부 참여 (role = MEMBER)

**Request Body**

```json
{
  "inviteCode": "ABC123"
}
```

**DB**

1. `households`를 invite_code로 조회
2. `household_members` INSERT (role = MEMBER)
3. 이미 소속 `409` / 코드 없음 `404`

---

## 4. Categories

> Header: `Authorization: Bearer <token>`  
> household 스코프

### GET `/api/categories`

카테고리가 하나도 없으면 엑셀 기본 트리를 자동 시드한 뒤 반환한다.

**Response**

```json
[
  {
    "id": 1,
    "name": "생활비",
    "type": "EXPENSE",
    "color": "#e64980",
    "parentId": null,
    "sortOrder": 0
  },
  {
    "id": 2,
    "name": "외식비",
    "type": "EXPENSE",
    "color": "#e64980",
    "parentId": 1,
    "sortOrder": 0
  }
]
```

**DB**: `categories` WHERE household_id = 내 household (sort_order, id 순)

---

### POST `/api/categories`

**Request Body**

```json
{
  "name": "외식비",
  "type": "EXPENSE",
  "color": "#e64980",
  "parentId": 1
}
```


| 필드       | 값                                         |
| -------- | ----------------------------------------- |
| type     | `INCOME` / `EXPENSE`                      |
| parentId | optional. 대분류면 null. 상위는 같은 type의 대분류만 가능 |


**Response** `201`  
**DB**: `categories` INSERT

---

### PUT `/api/categories/{categoryId}`

**Request Body** — name, color, parentId 수정 (type은 변경하지 않음)

**DB**: household 소유 검증 후 UPDATE

---

### DELETE `/api/categories/{categoryId}`

**Response** `204`  
**DB**: DELETE  
하위 카테고리가 있으면 `400`. 거래 FK가 있으면 DB 제약으로 실패할 수 있음.

---

## 5. Cards

> Header: `Authorization: Bearer <token>`  
> household 스코프

### GET `/api/cards`

**Response**

```json
[
  {
    "id": 1,
    "name": "삼성카드",
    "type": "CREDIT",
    "ownerUserId": 1,
    "ownerName": "홍길동"
  }
]
```

**DB**: `cards` + owner `users` 조인

---

### POST `/api/cards`

**Request Body**

```json
{
  "name": "삼성카드",
  "type": "CREDIT",
  "ownerUserId": 1
}
```


| 필드          | 값                                  |
| ----------- | ---------------------------------- |
| type        | `CREDIT` / `DEBIT` / `CASH`        |
| ownerUserId | optional. 있으면 같은 household 멤버인지 검증 |


**Response** `201`  
**DB**: `cards` INSERT

---

### PUT `/api/cards/{cardId}`

household + owner 검증 후 UPDATE

---

### DELETE `/api/cards/{cardId}`

**Response** `204`  
**DB**: `cards` DELETE

---

## 6. Transactions

> Header: `Authorization: Bearer <token>`  
> household 스코프

### GET `/api/transactions?year=&month=`

**Query Params**


| 파라미터  | 타입  | 필수  |
| ----- | --- | --- |
| year  | int | O   |
| month | int | O   |


**Response**

```json
[
  {
    "id": 1,
    "type": "EXPENSE",
    "amount": 15000.00,
    "transactionDate": "2026-08-01",
    "categoryId": 1,
    "categoryName": "식비",
    "categoryColor": "#FF5733",
    "cardId": 1,
    "cardName": "삼성카드",
    "userId": 1,
    "userName": "홍길동",
    "memo": "점심"
  }
]
```

**DB**: `transactions` WHERE household_id + 해당 월 기간, 날짜 ASC

---

### POST `/api/transactions`

**Request Body**

```json
{
  "type": "EXPENSE",
  "amount": 15000.00,
  "transactionDate": "2026-08-01",
  "categoryId": 1,
  "cardId": 1,
  "memo": "점심"
}
```


| 필드         | 제약                     |
| ---------- | ---------------------- |
| amount     | ≥ 0.01                 |
| categoryId | 필수, 같은 household       |
| cardId     | optional, 같은 household |
| memo       | optional               |


**Response** `201`  
**DB**: category/card 소속 검증 → `transactions` INSERT (user_id = 로그인 유저)

---

### PUT `/api/transactions/{transactionId}`

household 소유 거래 UPDATE (작성자 변경 없음)

---

### DELETE `/api/transactions/{transactionId}`

**Response** `204`  
**DB**: DELETE

---

## 7. Statistics

> Header: `Authorization: Bearer <token>`

### GET `/api/statistics/monthly?year=&month=`

**Query Params**: `year`, `month` (필수)

**Response**

```json
{
  "totalIncome": 3000000.00,
  "totalExpense": 1200000.00,
  "netAmount": 1800000.00,
  "byCategory": [
    {
      "categoryId": 2,
      "categoryName": "외식비",
      "color": "#e64980",
      "type": "EXPENSE",
      "amount": 350000.00,
      "parentId": 1,
      "parentName": "생활비"
    }
  ],
  "byParentCategory": [
    {
      "categoryId": 1,
      "categoryName": "생활비",
      "color": "#e64980",
      "type": "EXPENSE",
      "amount": 500000.00,
      "parentId": null,
      "parentName": null
    }
  ],
  "byCard": [
    {
      "cardId": 1,
      "cardName": "삼성카드",
      "amount": 500000.00
    }
  ],
  "byMember": [
    {
      "userId": 1,
      "userName": "홍길동",
      "income": 3000000.00,
      "expense": 800000.00
    }
  ]
}
```


| 필드               | 설명                           |
| ---------------- | ---------------------------- |
| netAmount        | totalIncome − totalExpense   |
| byCategory       | 소분류(리프) 단위 집계 + parent 정보    |
| byParentCategory | 대분류 단위 합산 (부모 없으면 자기 자신이 그룹) |


**DB**

1. `household_members`로 household 확인
2. 월간 `transactions` 로드 (+ category.parent, card, user)
3. 메모리에서 집계

---

### GET `/api/statistics/range?fromYear=&fromMonth=&toYear=&toMonth=`

여러 달 추이 (최대 24개월)

**Response**

```json
{
  "months": [
    {
      "year": 2025,
      "month": 6,
      "totalIncome": 3000000.00,
      "totalExpense": 1200000.00,
      "netAmount": 1800000.00,
      "byParentCategory": [
        {
          "categoryId": 1,
          "categoryName": "생활비",
          "color": "#e64980",
          "type": "EXPENSE",
          "amount": 500000.00,
          "parentId": null,
          "parentName": null
        }
      ]
    }
  ]
}
```

---

## 8. Imports

> Header: `Authorization: Bearer <token>`  
> Content-Type: `multipart/form-data`  
> 파일 최대 10MB

### POST `/api/imports`

카드 명세서 일괄 임포트

**Form Params**


| 파라미터     | 타입            | 필수  | 설명                                         |
| -------- | ------------- | --- | ------------------------------------------ |
| provider | string        | O   | `SAMSUNG_CARD` / `GYEONGGI_LOCAL_CURRENCY` / `COUPANG` / `KBANK` |
| cardId   | long          | X   | 기존 카드 ID                                   |
| cardName | string        | X   | 카드 이름 (없으면 생성)                             |
| file     | MultipartFile | O   | 명세서 파일                                     |


**Response**

```json
{
  "importedCount": 42,
  "skippedCount": 3,
  "totalAmount": 850000.00,
  "cardId": 1,
  "cardName": "삼성카드",
  "categoryBreakdown": []
}
```

**DB 처리 흐름**

1. household 확인 + 파일 SHA-256 계산
2. `import_batches`에 같은 checksum 있으면 `409` (중복 임포트 방지)
3. (필요 시) Office 파일 복호화
4. provider별 StatementParser로 파싱
5. 카드: cardId 또는 이름으로 찾거나 CREDIT 카드 생성 → `cards`
6. 가맹점 → MerchantCategoryClassifier → category 찾거나 EXPENSE 카테고리 생성 → `categories`
7. 각 행을 EXPENSE로 `transactions` INSERT
8. `import_batches` INSERT

**관련 테이블**: `import_batches`, `transactions`, `categories`, `cards`, `households`, `household_members`, `users`

---

## 9. Admin

일반 회원 계정과 별개의 **마스터 관리자 계정**(`users` 테이블에 없음, `application.yml`의
`admin.username`/`admin.password-hash`로만 존재)으로 로그인해서 쓰는 전용 API. 발급되는 JWT에
`role: ADMIN` 클레임이 들어가고, `/api/admin/**`(로그인 제외)는 이 클레임이 있어야만 접근 가능.

### POST `/api/admin/auth/login` — 공개

**Request Body**

```json
{ "username": "admin", "password": "..." }
```

**Response**

```json
{ "accessToken": "..." }
```

**DB**: 없음 (설정값과 비교만 함)

---

### GET `/api/admin/stats`

전체 회원 수, 가계부 수, 거래 수, 최근 7일 신규 가입/가계부 수.

**DB**: `users`, `households`, `transactions` COUNT

---

### GET `/api/admin/users`

전체 회원 목록. 각 회원의 소속 가계부, 가계부 내 역할(OWNER/MEMBER), 작성한 거래 수, 차단 여부 포함.

**DB**: `users`, `household_members`, `households`, `transactions` SELECT

---

### GET `/api/admin/households`

전체 가계부 목록. 구성원 이름, 거래 수 포함.

**DB**: `households`, `household_members`, `users`, `transactions` SELECT

---

### POST `/api/admin/users/{userId}/block` / `POST .../unblock`

회원 로그인 차단/해제. 차단된 계정은 `/api/auth/login`에서 `403`.

**DB**: `users.blocked` UPDATE

---

### DELETE `/api/admin/users/{userId}`

관리자가 비밀번호 확인 없이 강제 탈퇴시킴. 동작은 본인 탈퇴(`DELETE /api/users/me`)와 동일 —
거래는 남고 작성자만 "탈퇴한 사용자"로 표시됨.

**DB**: `users` DELETE (`household_members` CASCADE, `transactions`/`cards`/`assets`의 user 참조 SET NULL)

---

## Controller → Service → DB 매핑


| Controller            | Service            | 테이블                                                       |
| --------------------- | ------------------ | --------------------------------------------------------- |
| AuthController        | AuthService        | `users`                                                   |
| UserController        | UserService        | `users`                                                   |
| HouseholdController   | HouseholdService   | `households`, `household_members`, `users`                |
| CategoryController    | CategoryService    | `categories`, `households`, `household_members`           |
| CardController        | CardService        | `cards`, `households`, `household_members`, `users`       |
| TransactionController | TransactionService | `transactions` + FK 테이블                                   |
| StatisticsController  | StatisticsService  | `transactions`, `household_members`                       |
| ImportController      | ImportService      | `import_batches`, `transactions`, `categories`, `cards` 등 |


공통: `HouseholdService.getHouseholdIdForUser`가 카테고리/카드/거래/통계/임포트의 게이트웨이

---

## 인증 흐름

```
요청
 → JwtAuthFilter (Authorization: Bearer … 검증)
 → SecurityContext에 principal = userId(Long) 저장
 → SecurityFilterChain
     · /api/auth/**, swagger → permitAll
     · 그 외 → authenticated
 → @CurrentUserId Long userId 로 Controller에 주입
```


| 항목          | 값                       |
| ----------- | ----------------------- |
| JWT subject | userId                  |
| 만료          | 7일 (604800000ms)        |
| ROLE 기반 권한  | 없음 (household 소속으로만 격리) |


---

## 에러 응답

공통 형식:

```json
{
  "message": "에러 메시지"
}
```


| HTTP | 상황                           |
| ---- | ---------------------------- |
| 400  | validation 실패                |
| 401  | 인증 실패 / 토큰 없음                |
| 403  | 권한 없음                        |
| 404  | 리소스 없음                       |
| 409  | 중복/충돌 (이메일, 이미 소속, 중복 임포트 등) |
| 500  | 서버 오류                        |


---

## DB 설정 (application.yml)


| 항목           | 값                                                                                                         |
| ------------ | --------------------------------------------------------------------------------------------------------- |
| URL          | `jdbc:postgresql://localhost:5432/${DB_NAME:housebook}`                                                   |
| User         | `${DB_USER:sumin}`                                                                                        |
| Password     | `${DB_PASSWORD:}`                                                                                         |
| JPA ddl-auto | `validate`                                                                                                |
| Flyway       | `classpath:db/migration`                                                                                  |
| 마이그레이션       | `V1__init.sql`, `V2__import_batches.sql`, `V3__add_birth_date_to_users.sql`, `V4__category_hierarchy.sql` |
| CORS         | `http://localhost:8081`, `http://localhost:19006`                                                         |


