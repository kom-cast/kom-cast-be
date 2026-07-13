# kom-cast-be
## Git Convention

### Branch
- feat/#이슈번호-기능명
- fix/#이슈번호-기능명
- refactor/#이슈번호-기능명

예)
- feat/12-login-api
- fix/18-token-error

### Commit

형식
```
타입: 내용
```

예)
```
feat: 로그인 API 구현
fix: JWT 토큰 검증 오류 수정
refactor: UserService 분리
docs: README 수정
chore: 의존성 업데이트
```

### Issue
- 기능 단위로 생성
- 작업 시작 전 담당자 지정

### Pull Request
- 하나의 기능만 포함
- 관련 이슈 연결 (`Closes #번호`)
