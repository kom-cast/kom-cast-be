# Kom-Cast 백엔드 API 상세 명세서 (API Spec)

본 명세서는 프론트엔드(`kom-cast-fe`) 연동 및 모의(Mock) 응답 구성을 위한 백엔드 REST API 설계 사양서입니다.  
**개인별 브리핑 생성 시각 지정 기능이 기획적으로 제거됨**에 따라 관련 속성 및 엔드포인트가 정정되었습니다.

해커톤의 진행 속도를 위해 모든 요청은 단순 HTTP Header인 `X-User-Id: 1`과 같은 식별 방식을 우선 적용하며, 요청 및 응답 바디는 모두 `application/json` 형식을 기준으로 합니다.

---

## 🛠️ 공통 사양

### Request Header
인증 모듈이 배제된 해커톤 환경이므로, 헤더에 아래 사용자 식별 값을 탑재하여 요청을 전송합니다.
```http
X-User-Id: 1
```

---

## 1. 개인화 및 온보딩 설정 (Preferences)

### 1) 온보딩/환경 설정 통합 조회 (`GET /api/v1/preferences`)
- **설명**: 현재 사용자의 닉네임, 보유 종목, 관심 산업 분야, 키워드 필터링 정보 및 알림 수신 상태를 종합적으로 반환합니다. (개인 시간 설정은 제외)
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

### 2) 온보딩/환경 설정 전체 저장 및 갱신 (`PUT /api/v1/preferences`)
- **설명**: 온보딩 과정이 완료되었거나 설정 페이지에서 환경설정을 일괄적으로 변경하여 저장할 때 호출합니다.
- **Request Header**: `X-User-Id: 1`
- **Request Body (JSON)**:
```json
{
  "nickname": "민준",
  "portfolio": ["005930", "000660"],
  "interests": ["005930", "035420"],
  "industries": ["IND001", "IND005"],
  "includeKeywords": ["반도체", "AI"],
  "excludeKeywords": ["가십"],
  "freeText": "반도체 위주로 대본을 생성해주세요.",
  "briefingDuration": "15",
  "voice": "sunghoon",
  "notifyBriefing": true,
  "notifyPriceAlert": true,
  "notifyMarketing": true
}
```
- **Response Body (JSON)**:
  - **Status Code**: `200 OK`
```json
{
  "status": "SUCCESS",
  "message": "Preferences updated successfully."
}
```

### 3) 브리핑 목소리 단건 변경 (`PATCH /api/v1/preferences/voice`)
- **설명**: 마이페이지의 목소리 재설정 화면에서 성우 TTS 목소리를 변경할 때 호출합니다.
- **Request Header**: `X-User-Id: 1`
- **Request Body (JSON)**:
```json
{
  "voice": "suyeon"
}
```
- **Response Body (JSON)**:
  - **Status Code**: `200 OK`
```json
{
  "status": "SUCCESS",
  "message": "Voice set to suyeon."
}
```

### 4) 브리핑 재생 분량(Duration) 변경 (`PATCH /api/v1/preferences/briefing-duration`)
- **설명**: 마이페이지의 브리핑 길이 재설정 화면에서 분량만 변경할 때 호출합니다. (시간대 변경 API는 제거됨)
- **Request Header**: `X-User-Id: 1`
- **Request Body (JSON)**:
```json
{
  "briefingDuration": "15"
}
```
- **Response Body (JSON)**:
  - **Status Code**: `200 OK`
```json
{
  "status": "SUCCESS",
  "message": "Briefing duration updated to 15 minutes."
}
```

### 5) 개별 푸시 알림 설정 변경 (`PATCH /api/v1/preferences/notifications`)
- **설명**: 알림 설정 페이지에서 알림별로 활성 여부를 토글할 때 호출합니다.
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
  - **Status Code**: `200 OK`
```json
{
  "status": "SUCCESS",
  "message": "Notification preferences updated."
}
```

---

## 2. 관심/보유 종목 및 산업 분야 관리 (Stocks & Industries)

### 1) 전체 주식 마스터 리스트 조회 (`GET /api/v1/stocks`)
- **설명**: 온보딩이나 설정에서 종목을 검색하여 등록할 때 필요한 마스터 종목 데이터입니다.
- **Response Body (JSON)**:
```json
[
  { "name": "삼성전자", "code": "005930" },
  { "name": "SK하이닉스", "code": "000660" },
  { "name": "NAVER", "code": "035420" },
  { "name": "LG화학", "code": "051910" },
  { "name": "삼성SDI", "code": "006400" },
  { "name": "카카오", "code": "035720" }
]
```

### 2) 사용자의 보유/관심 종목 리스트 및 시세 조회 (`GET /api/v1/stocks/my`)
- **설명**: 홈 화면에 노출할 사용자의 보유/관심 종목 실시간(또는 주기적 적재) 시세를 조회합니다.
- **Request Header**: `X-User-Id: 1`
- **Response Body (JSON)**:
```json
[
  {
    "name": "삼성전자",
    "code": "005930",
    "price": 73400,
    "change": 1.2
  },
  {
    "name": "SK하이닉스",
    "code": "000660",
    "price": 189000,
    "change": 3.4
  }
]
```

### 3) 관심/보유 종목 추가 등록 (`POST /api/v1/stocks/my`)
- **설명**: 보유/관심 종목 설정 화면에서 특정 종목을 리스트에 추가합니다.
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
  "message": "Stock 035420 registered to portfolio."
}
```

### 4) 관심/보유 종목 삭제 (`DELETE /api/v1/stocks/my/{code}`)
- **설명**: 사용자의 보유/관심 종목 목록에서 특정 종목을 해제합니다.
- **Request Header**: `X-User-Id: 1`
- **Response Body (JSON)**:
  - **Status Code**: `200 OK`
```json
{
  "status": "SUCCESS",
  "message": "Stock 035420 removed from portfolio."
}
```

### 5) 전체 관심 산업 분야 목록 조회 (`GET /api/v1/industries`)
- **설명**: 온보딩 단계에서 제공할 수 있는 선택 산업 분야 전체 리스트를 조회합니다.
- **Response Body (JSON)**:
```json
[
  { "code": "IND001", "name": "반도체" },
  { "code": "IND002", "name": "2차전지" },
  { "code": "IND003", "name": "바이오/헬스케어" },
  { "code": "IND004", "name": "금융" },
  { "code": "IND005", "name": "AI/빅테크" },
  { "code": "IND006", "name": "자동차" },
  { "code": "IND007", "name": "엔터테인먼트" },
  { "code": "IND008", "name": "게임" }
]
```

---

## 3. 브리핑 조회 및 재생 (Briefings)

### 1) 오늘의 개인화 브리핑 단건 조회 (`GET /api/v1/briefings/today`)
- **설명**: 사용자의 당일 맞춤 브리핑 데이터 및 재생 화면에 노출될 오디오와 시간대별 자막 스크립트를 반환합니다.
- **Request Header**: `X-User-Id: 1`
- **Response Body (JSON)**:
  - **Status Code**: `200 OK`
```json
{
  "id": 1004,
  "date": "2026-07-19",
  "headline": "삼성전자·SK하이닉스 실적 서프라이즈",
  "audioUrl": "https://ncp-object-storage.com/briefings/20260719-user1.mp3",
  "durationSeconds": 600,
  "segments": [
    {
      "fraction": 0.0,
      "stock": "삼성전자",
      "text": "삼성전자 관련 주요 소식으로 오늘의 브리핑을 시작합니다."
    },
    {
      "fraction": 0.18,
      "stock": "삼성전자",
      "text": "삼성전자가 2분기 잠정 실적을 발표했습니다. 매출은 전년 동기 대비 23% 증가한 74조원을 기록했습니다."
    },
    {
      "fraction": 0.4,
      "stock": "SK하이닉스",
      "text": "SK하이닉스는 메모리 가격 상승에 힘입어 목표주가가 상향 조정됐습니다."
    },
    {
      "fraction": 0.62,
      "stock": "2차전지",
      "text": "2차전지 분야 전반이 반등하며 관련주들이 강세를 보이고 있습니다."
    },
    {
      "fraction": 0.85,
      "stock": "시장 전체",
      "text": "시장 관련 주요 이슈를 마지막으로 오늘의 브리핑을 마칩니다."
    }
  ]
}
```

### 2) 과거 브리핑 보관함 이력 조회 (`GET /api/v1/briefings`)
- **설명**: 보관함 페이지에서 이전에 생성되었던 사용자의 지난 브리핑 기록을 페이징 단위로 목록 조회합니다.
- **Request Header**: `X-User-Id: 1`
- **Query Parameter**: `page=0&size=10`
- **Response Body (JSON)**:
```json
{
  "content": [
    {
      "id": 1004,
      "date": "2025-07-11",
      "headline": "삼성전자 실적 발표, 반도체 업황 개선",
      "duration": "10"
    },
    {
      "id": 1003,
      "date": "2025-07-10",
      "headline": "2차전지 분야 반등, NAVER 신규 서비스 발표",
      "duration": "10"
    },
    {
      "id": 1002,
      "date": "2025-07-09",
      "headline": "금리 동결 소식, 금융주 강세",
      "duration": "5"
    }
  ],
  "pageNumber": 0,
  "pageSize": 10,
  "totalElements": 3,
  "totalPages": 1
}
```

### 3) 특정 과거 브리핑 상세 조회 (`GET /api/v1/briefings/{id}`)
- **설명**: 보관함 목록에서 지난 브리핑 카드를 클릭했을 때 해당 날짜의 오디오 및 스크립트를 재조회합니다.
- **Request Header**: `X-User-Id: 1`
- **Response Body (JSON)**: (오늘의 브리핑 조회 구조와 동일)

---

## 4. 알림센터 (Notifications)

### 1) 수신 알림 목록 조회 (`GET /api/v1/notifications`)
- **설명**: 사용자의 수신 알림 내역을 조회합니다. 읽지 않은 알림은 `unread: true` 상태를 가집니다.
- **Request Header**: `X-User-Id: 1`
- **Response Body (JSON)**:
```json
[
  {
    "id": 1,
    "type": "BRIEFING",
    "title": "오늘의 브리핑이 준비됐어요",
    "description": "새로운 맞춤형 아침 브리핑을 들어보세요",
    "time": "방금 전",
    "unread": true
  },
  {
    "id": 2,
    "type": "STOCK_ALERT",
    "title": "삼성전자 +5% 급등",
    "description": "보유종목 삼성전자가 급등했습니다",
    "time": "1시간 전",
    "unread": true
  },
  {
    "id": 3,
    "type": "VOICE_UPDATE",
    "title": "새로운 목소리가 추가됐어요",
    "description": "마이페이지에서 새 목소리를 들어보세요",
    "time": "어제",
    "unread": false
  }
]
```

### 2) 특정 알림 읽음 처리 (`PATCH /api/v1/notifications/{id}/read`)
- **설명**: 알림 목록에서 개별 알림을 확인하여 읽음 처리합니다.
- **Request Header**: `X-User-Id: 1`
- **Response Body (JSON)**:
```json
{
  "status": "SUCCESS",
  "message": "Notification 1 marked as read."
}
```

### 3) 모든 알림 일괄 읽음 처리 (`POST /api/v1/notifications/read-all`)
- **설명**: 알림 목록에 들어갈 때 또는 '모두 읽음' 버튼 클릭 시 일괄적으로 안 읽은 상태를 해제합니다.
- **Request Header**: `X-User-Id: 1`
- **Response Body (JSON)**:
```json
{
  "status": "SUCCESS",
  "message": "All notifications marked as read."
}
```
