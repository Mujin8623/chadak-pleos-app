# 제출용 APK

공모전 제출 규칙(https://appdevchallenge.auto-hmg.io/guide/submission)은 저장소의 `apk/` 폴더에 **실행 가능한 release APK**를 두는 것입니다.

파일 이름 규칙은 다음과 같습니다.

```text
팀명_앱이름_release.apk
```

예: `무진_차닥_release.apk`

이 폴더에는 이름 규칙 설명과 `.gitkeep`만 커밋합니다. 빌드된 APK, 키스토어, `output-metadata.json`은 커밋하지 않습니다. 용량이 GitHub 파일 한도를 넘으면 GitHub Release URL을 제출합니다. `v*` 태그가 그 Release를 만듭니다.

## 이 저장소의 파일 이름

한글 파일 이름은 CI와 일부 도구에서 깨질 수 있어, 기본 산출물 이름은 ASCII입니다.

```text
TEAM_Chadak_release.apk
```

- `TEAM`은 Gradle 속성 `teamName`입니다. `gradle.properties`의 자리표시자이며, 제출 전에 팀명으로 바꿉니다.
- `Chadak`는 앱 이름 `차닥`의 ASCII 표기입니다. 규칙 그대로의 한글 이름이 필요하면 아래처럼 빌드합니다.

```bash
./gradlew packageSubmissionApk -PteamName=팀명 -PapkAppName=차닥
```

산출물은 `app/build/submission/`에 생깁니다. 제출 때 그 파일을 이 폴더에 복사하거나, Release에 첨부된 파일을 사용합니다.

릴리스 워크플로는 `vars.TEAM_NAME`이 있으면 그 값을 `teamName`으로 쓰고, APK 앱 토큰은 `Chadak`로 고정합니다. 서명 시크릿이 없으면 디버그 키로 서명합니다. 서명은 `CONTRIBUTING.md`를 봅니다.
