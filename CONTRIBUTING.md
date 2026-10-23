# Contributing

`main`은 항상 빌드되고, 디버그 빌드는 목 신호로 데모할 수 있어야 합니다. `main`에 직접 푸시하지 않습니다.

## 브랜치

PR로만 병합합니다.

- `feature/*` 기능
- `fix/*` 버그 수정
- `docs/*` 문서
- `chore/*` 빌드, CI, 스켈레톤처럼 제품 동작이 아닌 변경

## 커밋

[Conventional Commits](https://www.conventionalcommits.org/)를 사용합니다.

- `feat:` 사용자에게 보이는 기능
- `fix:` 버그 수정
- `docs:` 문서
- `chore:` 도구와 정리
- `test:` 테스트만

## 버전

`version.properties`가 유일한 버전 소스입니다.

- `versionName` = `MAJOR.MINOR.PATCH`
- `versionCode` = `MAJOR * 10000 + MINOR * 100 + PATCH`
- `MINOR`와 `PATCH`는 0 이상 99 이하입니다. 같은 공식이 `VersionCodes`와 `app/build.gradle.kts`에 있습니다.

## 릴리스

1. `CHANGELOG.md`의 `[Unreleased]` 내용을 `## [x.y.z] - YYYY-MM-DD`로 옮기고, `version.properties`를 그 버전으로 맞춥니다.
2. `main`에 병합한 뒤 `vX.Y.Z` 태그를 푸시합니다. 태그는 `version.properties`와 같아야 합니다. 예: `v0.1.0`.
3. `.github/workflows/release.yml`이 release APK를 만들고 GitHub Release에 첨부합니다. 릴리스 노트는 해당 CHANGELOG 절입니다.
4. 제출용 파일 이름은 `팀명_앱이름_release.apk`입니다. 기본 파일은 `차량분석가_차닥_release.apk`입니다. 저장소 변수 `TEAM_NAME`이 있으면 그 값을 팀명으로 씁니다. 자세한 내용은 `apk/README.md`입니다.

## 비밀과 서명

저장소는 공개입니다. 비밀, 키스토어, 서명 비밀번호, API 키, `local.properties`, 개인 정보를 커밋하지 않습니다.

아래 파일은 `.gitignore`에 있습니다.

- `*.jks`
- `*.keystore`
- `keystore.properties`
- `local.properties`
- `.env`

릴리스 서명은 GitHub Actions 시크릿만 읽습니다.

| 시크릿 | 내용 |
| --- | --- |
| `KEYSTORE_BASE64` | 키스토어 파일을 `base64 -w 0`으로 인코딩한 한 줄 |
| `KEYSTORE_PASSWORD` | 스토어 비밀번호 |
| `KEY_ALIAS` | 키 별칭 |
| `KEY_PASSWORD` | 키 비밀번호 |

네 값이 모두 있고 디코딩한 파일이 있으면 release 서명을 사용합니다. 하나라도 없으면 디버그 키스토어로 서명한, 그래도 설치 가능한 release APK를 만듭니다. 로컬에서 같은 동작을 쓰려면 키스토어를 저장소 밖에 두고 `KEYSTORE_PATH`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` 환경 변수만 넘깁니다. 키스토어를 워크스페이스에 넣지 않습니다.

TTS/STT는 `Mode.ON_DEVICE`만 사용합니다. 서버 모드의 Client ID와 Client Secret은 이 저장소에 두지 않습니다.
