# 🚗 붕붕이 (BoongBoong-i)

> 안전하고 신뢰도 높은 대학생 동행 커뮤니티 플랫폼  
> 학교 이메일 기반 실명 인증과 운전면허 검증, 비금전적 보증 포인트 시스템을 통해 금전 거래 없이도 안전하게 카풀할 수 있는 웹 커뮤니티 서비스입니다.

---

## 📚 목차 (Table of Contents)
- [소개 (Introduction)](#-소개-introduction)
- [주요 기능 및 특징 (Features)](#-주요-기능-및-특징-features)
- [기술 스택 (Tech Stack)](#-기술-스택-tech-stack)
- [인증/세션 설계 요약 (Session-based Auth)](#-인증세션-설계-요약-session-based-auth)
- [설치 및 실행 (Getting Started)](#-설치-및-실행-getting-started)
- [기여 가이드 (Contributing)](#-기여-가이드-contributing)
- [라이센스 (License)](#-라이센스-license)

---

## 🧭 소개 (Introduction)

붕붕이는 대학 구성원을 위한 비영리 동행(카풀) 커뮤니티 플랫폼입니다.  
멋쟁이사자처럼 한서대학교 동아리에서 개발하며, 호의동승 원칙(금전 대가 없는 카풀)을 기반으로 운영됩니다.  

- 학교 이메일 인증으로 신원 확인  
- 운전면허 검증 완료자만 운전자 활동 가능  
- 금전 거래 차단 및 신뢰 점수/보증 포인트 제도 운영  

> 🎯 목표: 대학생 사이에 안전하고 편리한 동행 문화 정착 & 법적 위험 최소화

---

## ✨ 주요 기능 및 특징 (Features)

| 기능 | 설명 |
|------|------|
| 대학교 이메일 인증 | `@*.ac.kr` 이메일 + OTP 인증, 실명 기반 신뢰성 확보, 만 14세 미만 가입 제한 |
| 운전자 면허 검증 | 외부 API 기반 면허 진위 확인, 원본 이미지 즉시 파기 |
| 닉네임 & 개인정보 보호 | 프로필에는 닉네임만 표시, 민감 정보는 암호화 저장 (AWS KMS) |
| 금전 거래 차단 | 요금/송금/정산 UI 불가, 금전 관련 키워드 자동 필터링 |
| 보증 포인트 제도 | 매칭 시 포인트 잠금 → 완료 시 반환, 노쇼·악성 취소 시 차감 |
| 신뢰 점수 & 레벨 | 매너 점수 기반 가감, 프로필에 레벨 배지 및 매너온도 표시 |
| 매칭 후 전용 채팅 | 매칭 확정 시 그룹 채팅방 생성, 기본 안내 메시지 제공 |
| 채팅 필터링 | 금전/욕설/외부 연락처 필터링 & 자동 마스킹 |
| 리뷰 및 신고 | 종료 후 48시간 이내 리뷰 가능, 칭찬 태그 중심 / 단계별 제재 |
| 관리자 감사 로그 | 신고 처리, 제재 이력, 채팅 로그 등 일정 기간 보존 |
| UI/UX | 동행 모집 피드, 필터링 검색, 레벨·차량 정보 표시, PWA & 푸시 알림 |

---

## 🛠 기술 스택 (Tech Stack)

| 영역 | 사용 기술 |
|------|-----------|
| Frontend | Next.js, Tailwind CSS, React Hook Form, PWA |
| Backend | Spring Boot 3.x (Java 21), Gradle, Spring Security(세션 기반), Spring Session(Redis) |
| Database | MySQL 8.x, Spring Data JPA, QueryDSL |
| Caching & Session | Redis (세션 저장소, OTP 캐시, Pub/Sub) |
| Storage & Security | AWS S3(면허 이미지), AWS KMS(암호화) |
| Real-time Chat | Spring WebSocket, STOMP, SockJS, Redis Pub/Sub |
| Email & External API | Spring Mail(학교 메일 OTP), 운전면허 진위확인 API |
| DevOps | Docker, GitHub Actions(CI/CD), AWS EC2, Nginx(HTTPS/리버스 프록시) |

---

## 🔐 인증/세션 설계 요약 (Session-based Auth)

| 항목 | 정책 |
|------|------|
| 로그인 | 서버 세션 생성 후 `JSESSIONID` HttpOnly 쿠키 발급 |
| 세션 저장소 | Spring Session + Redis (수평 확장 지원) |
| 쿠키 보안 | HttpOnly, Secure, SameSite=Lax(또는 Strict) |
| CSRF 보호 | POST/PUT/DELETE 요청 시 CSRF 토큰 적용 |
| 세션 ID 회전 | 로그인/권한 상승 시 Session Fixation 방지 |
| 타임아웃 | 30~120분 권장, 활동 시 연장(슬라이딩 윈도우) |
| 동시 접속 제한 | 사용자당 세션 수 제한 가능 |
| 로그아웃 | 사용자 요청/관리자 제재 시 세션 무효화 |
| Remember-Me | 장기 쿠키 사용 시 짧은 TTL + 서명키 회전 |

---

## ⚙ 설치 및 실행 (Getting Started)

> 백엔드 코드 실행 가이드(환경 변수, DB 마이그레이션 등)는 추후 작성 예정입니다.

예시:
```bash
# 필수 환경
Java 21
MySQL 8.x
Redis

# 빌드 & 실행
./gradlew clean build
java -jar build/libs/boongboongi.jar
