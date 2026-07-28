# Kom-Cast 백엔드 API 상세 명세서 (API Spec)

본 명세서는 프론트엔드(`kom-cast-fe`) 연동 및 타 서버(`kom-cast-data`, `kom-cast-ai`) 통신을 위한 백엔드 REST API 설계 사양서입니다.

해커톤의 진행 속도를 위해 모든 클라이언트 요청은 단순 HTTP Header인 `X-User-Id: 1`과 같은 식별 방식을 우선 적용하며, 요청 및 응답 바디는 모두 `application/json` 형식을 기준으로 합니다.

---

## 🛠️ 공통 사양

### Request Header
인증 모듈이 배제된 해커톤 환경이므로, 헤더에 아래 사용자 식별 값을 탑재하여 요청을 전송합니다.
```http
X-User-Id: 1
```

---

## 00. Internal Batch (데이터/AI 서버 연동)

### 1) 데이터 정제 완료 트리거 수신 (`POST /api/v1/internal/batch-complete`)
- **설명**: `kom-cast-data` 서버가 매일 아침 수집 및 데이터 정제 배치를 완료한 후 통지하는 Webhook 수신 엔드포인트입니다. 수신 시 유저별 맞춤 브리핑 생성 준비 및 알림을 발행합니다.
- **Request Body (JSON)**:
```json
{
  "run_date": "2026-07-27",
  "status": "SUCCEEDED",
  "jobs": {
    "dart_corp_codes": "job-1",
    "industries": "job-2",
    "market_prices": "job-3",
    "industry_prices": "job-4",
    "news": "job-5",
    "dart_disclosures": "job-6"
  }
}
```
- **Response Body (JSON)**:
```json
{
  "status": "SUCCESS",
  "message": "Batch completion event received and processed."
}
```

---

## 0. Health Check (서버 상태 확인)

### 1) 서버 헬스 체크 (`GET /health` 또는 `GET /api/v1/health`)
- **설명**: 서버 정상 구동 여부를 확인합니다.
- **Response Body (JSON)**:
```json
{
  "status": "UP",
  "service": "kom-cast-be",
  "message": "Server is running normally."
}
```

---

## 1. 개인화 및 온보딩 설정 (Preferences)

### 1) 온보딩/환경 설정 통합 조회 (`GET /api/v1/preferences`)
- **설명**: 현재 사용자의 닉네임, 보유 종목, 관심 산업 분야, 키워드 필터링 정보 및 알림 수신 상태를 종합적으로 반환합니다.
- **Request Header**: `X-User-Id: 1`
- **Response Body (JSON)**:
```json
{
  "nickname": "민준",
  "portfolio": ["005930", "000660"],
  "interests": ["005930", "035420"],
  "industries": [
    { "code": "IND001", "name": "반도체" },
    { "code": "IND002", "name": "2차전지" }
  ],
  "includeKeywords": ["실적", "서프라이즈"],
  "excludeKeywords": ["정치", "루머"],
  "freeText": "반도체 관련 위주로 자세하게 대본을 써주세요.",
  "briefingDuration": "10",
  "voice": "jieun",
  "notifyBriefing": true,
  "notifyPriceAlert": true,
  "notifyMarketing": false
}
```

---

## 2. 관심/보유 종목 및 산업 분야 관리 (Stocks & Industries)

### 1) 전체 주식 마스터 리스트 조회 (`GET /api/v1/stocks`)
- **설명**: 온보딩이나 설정에서 종목을 검색하여 등록할 때 필요한 마스터 종목 데이터입니다.
- **Response Body (JSON)**:
```json
[
  { "name": "삼성전자", "code": "005930", "price": 73400, "change": 1.2 },
  { "name": "SK하이닉스", "code": "000660", "price": 189000, "change": 3.4 }
]
```

### 2) 사용자의 보유/관심 종목 리스트 및 시세 조회 (`GET /api/v1/stocks/my`)
- **설명**: 사용자가 등록한 보유/관심 종목 목록 및 실시간 시세를 반환합니다. (등록 종목이 없으면 빈 배열 `[]` 반환)
- **Request Header**: `X-User-Id: 1`
- **Response Body (JSON)**:
```json
[
  { "name": "삼성전자", "code": "005930", "price": 73400, "change": 1.2 }
]
```

### 3) 관심/보유 종목 등록 (`POST /api/v1/stocks/my`)
- **Request Header**: `X-User-Id: 1`
- **Request Body (JSON)**:
```json
{
  "code": "035420",
  "type": "PORTFOLIO"
}
```
- **Response Body (JSON)**:
  - **Status Code**: `201 Created`
```json
{
  "status": "SUCCESS",
  "message": "Stock 035420 registered."
}
```

### 4) 관심/보유 종목 일괄 등록 (`POST /api/v1/stocks/my/batch`)
- **설명**: 온보딩 UX 지원용으로 여러 종목 코드를 한 번에 일괄 등록합니다.
- **Request Header**: `X-User-Id: 1`
- **Request Body (JSON)**:
```json
{
  "codes": ["005930", "000660"],
  "type": "PORTFOLIO"
}
```
- **Response Body (JSON)**:
  - **Status Code**: `201 Created`
```json
{
  "status": "SUCCESS",
  "message": "Batch stocks registered successfully."
}
```

### 5) 관심/보유 종목 삭제 (`DELETE /api/v1/stocks/my/{code}`)
- **Request Header**: `X-User-Id: 1`
- **Response Body (JSON)**:
  - **Status Code**: `200 OK`
```json
{
  "status": "SUCCESS",
  "message": "Stock 035420 removed."
}
```

### 6) 전체 관심 산업 분야 목록 조회 (`GET /api/v1/industries`)
- **Response Body (JSON)**:
```json
[
  { "code": "IND001", "name": "반도체" },
  { "code": "IND002", "name": "2차전지" }
]
```

### 7) 나의 관심 산업 분야 목록 조회 (`GET /api/v1/industries/my`)
- **설명**: 현재 사용자가 등록한 관심 산업 분야 목록을 반환합니다.
- **Request Header**: `X-User-Id: 1`
- **Response Body (JSON)**:
```json
[
  { "code": "IND001", "name": "반도체" }
]
```

### 8) 관심 산업 분야 등록 (`POST /api/v1/industries/my`)
- **Request Header**: `X-User-Id: 1`
- **Request Body (JSON)**:
```json
{
  "code": "IND001"
}
```
- **Response Body (JSON)**:
  - **Status Code**: `201 Created`
```json
{
  "status": "SUCCESS",
  "message": "Industry IND001 registered."
}
```

### 9) 관심 산업 분야 일괄 등록 (`POST /api/v1/industries/my/batch`)
- **설명**: 온보딩 UX 지원용으로 여러 산업 분야 코드를 한 번에 일괄 등록합니다.
- **Request Header**: `X-User-Id: 1`
- **Request Body (JSON)**:
```json
{
  "codes": ["IND001", "IND002"]
}
```
- **Response Body (JSON)**:
  - **Status Code**: `201 Created`
```json
{
  "status": "SUCCESS",
  "message": "Batch industries registered successfully."
}
```

### 10) 관심 산업 분야 삭제 (`DELETE /api/v1/industries/my/{code}`)
- **Request Header**: `X-User-Id: 1`
- **Response Body (JSON)**:
  - **Status Code**: `200 OK`
```json
{
  "status": "SUCCESS",
  "message": "Industry IND001 removed."
}
```

---

## 3. 브리핑 조회 및 재생 (Briefings)

### 1) 오늘의 개인화 브리핑 단건 조회 (`GET /api/v1/briefings/today`)
- **Request Header**: `X-User-Id: 1`
- **Response Body (JSON)**:
```json
{
  "id": 1004,
  "date": "2026-07-27",
  "headline": "삼성전자·SK하이닉스 실적 서프라이즈",
  "audioUrl": "https://ncp-object-storage.com/briefings/20260727-user1.mp3",
  "durationSeconds": 600,
  "segments": [
    {
      "fraction": 0.0,
      "stock": "삼성전자",
      "text": "삼성전자 관련 주요 소식으로 오늘의 브리핑을 시작합니다."
    }
  ]
}
```

---

## 4. 알림센터 (Notifications)

### 1) 수신 알림 목록 조회 (`GET /api/v1/notifications`)
- **Request Header**: `X-User-Id: 1`
- **Response Body (JSON)**:
```json
[
  {
    "id": 3,
    "type": "BRIEFING",
    "title": "오늘의 브리핑이 준비됐어요",
    "description": "데이터 정제가 완료되어 맞춤형 아침 브리핑이 준비되었습니다.",
    "time": "방금 전",
    "unread": true
  }
]
```

### 2) 알림 설정 조회 (`GET /api/v1/notifications/settings`)
- **Request Header**: `X-User-Id: 1`
- **Response Body (JSON)**:
```json
{
  "notifyBriefing": true,
  "notifyPriceAlert": true,
  "notifyMarketing": false
}
```

### 3) 알림 설정 수정 (`PATCH /api/v1/notifications/settings`)
- **Request Header**: `X-User-Id: 1`
- **Request Body (JSON)**:
```json
{
  "notifyBriefing": true,
  "notifyPriceAlert": false,
  "notifyMarketing": false
}
```
- **Response Body (JSON)**:
```json
{
  "status": "SUCCESS",
  "message": "Notification settings updated."
}
```

### 4) 특정 알림 읽음 처리 (`PATCH /api/v1/notifications/{id}/read`)
- **Response Body (JSON)**:
```json
{
  "status": "SUCCESS",
  "message": "Notification 1 marked as read."
}
```

### 5) 모든 알림 일괄 읽음 처리 (`POST /api/v1/notifications/read-all`)
- **Response Body (JSON)**:
```json
{
  "status": "SUCCESS",
  "message": "All notifications marked as read."
}
```
