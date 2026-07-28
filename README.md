# 🎧 Kom-Cast 백엔드 서비스 (kom-cast-be)

AI 기반 맞춤형 주식/증시 음성 브리핑 서비스 **Kom-Cast**의 백엔드 REST API 서버입니다.

---

## 📌 1. 프로젝트 개요
* **서비스명**: Kom-Cast (컴캐스트)
* **주요 기능**: 
  * 사용자의 관심/보유 종목, 관심 산업 섹터, 키워드 필터링에 기반한 개인 맞춤형 AI 데일리 음성 브리핑 제공
  * 백엔드 API를 통해 맞춤형 오디오 파일 URL 및 시간대별 자막 싱크 스크립트 데이터 제공
  * 보유 종목 시세 조회, 브리핑 보관함 이력 관리, 알림센터 푸시 이력 관리
* **인프라 고려사항**: NCP Financial VPC 환경 타겟팅 (PostgreSQL DB, Object Storage 연동 구조)

---

## 🛠️ 2. 기술 스펙 (Tech Stack)
* **Language**: Java 21 (LTS)
* **Framework**: Spring Boot 3.3.1
* **ORM / Database**: 
  * Spring Data JPA
  * H2 Database (Local 테스트용)
  * PostgreSQL (Prod 배포용)
* **API Documentation**: Springdoc OpenAPI v2 (`springdoc-openapi-starter-webmvc-ui:2.6.0`)
* **Build Tool**: Gradle
* **Utilities**: Lombok, Spring Validation

---

## 🚀 3. 로컬 실행 방법

### 1) 프로젝트 클론 및 이동
```bash
git clone https://github.com/kom-cast/kom-cast-be.git
cd kom-cast-be
```

### 2) 빌드 및 서버 구동 (터미널)
* **macOS / Linux**:
  ```bash
  ./gradlew bootRun
  ```
* **Windows**:
  ```cmd
  gradlew.bat bootRun
  ```

### 3) IDE (IntelliJ IDEA) 실행
* IntelliJ IDEA에서 `kom-cast-be` 디렉토리를 **Gradle 프로젝트**로 Open합니다.
* `src/main/java/com/komcast/be/KomCastBeApplication.java` 파일을 실행(Run)합니다.
* 기본 설정은 `local` 프로파일이 적용되어 별도의 DB 설치 없이 H2 인메모리 데이터베이스로 가볍게 구동됩니다.

---

## 📖 4. Swagger API 명세서 확인 및 테스트 방법

서버 구동 후 브라우저에서 아래 주소로 접속하시면 시각화된 Swagger UI를 통해 모든 REST API 명세를 확인하고 직접 테스트해 보실 수 있습니다.

* **Swagger UI 접속 주소**: [http://localhost:8080/api/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
* **API 헤더 테스트 팁**:
  * 해커톤 진행을 위해 회원 인증 모듈 대신 HTTP Header의 `X-User-Id`를 사용하여 유저를 식별합니다.
  * Swagger UI 상단의 **`Authorize`** 버튼을 누르거나 각 API의 Header 파라미터에 `1` (기본값)을 입력하여 테스트하실 수 있습니다.
