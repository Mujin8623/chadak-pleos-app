# 제출용 APK

공모전 제출 규칙(https://appdevchallenge.auto-hmg.io/guide/submission)은 저장소의 `apk/` 폴더에 **실행 가능한 release APK**를 두는 것입니다.

파일 이름 규칙은 다음과 같습니다.

```text
팀명_앱이름_release.apk
```

이 저장소: `차분석가_차닥_release.apk` (팀명 `차 분석가`는 공백 없이 `차분석가`, 앱 이름 `차닥`)

이 폴더에는 이름 규칙 설명과 `.gitkeep`만 커밋합니다. 빌드된 APK, 키스토어, `output-metadata.json`은 커밋하지 않습니다. 용량이 GitHub 파일 한도를 넘으면 GitHub Release URL을 제출합니다. `v*` 태그가 그 Release를 만듭니다.

## 이 저장소의 파일 이름

기본 산출물 이름은 규칙 그대로입니다.

```text
차분석가_차닥_release.apk
```

- `차분석가`는 Gradle 속성 `teamName`의 기본값입니다. 팀명 `차 분석가`를 공백 없이 씁니다. 속성 값에 공백이 있어도 파일 이름에서는 빠집니다.
- `차닥`은 Gradle 속성 `apkAppName`의 기본값입니다.
- `gradle.properties`는 ISO-8859-1로 읽히므로 한글 값은 `\uXXXX`로 적어 두었습니다. 명령줄에서는 그대로 넘겨도 됩니다.

```bash
./gradlew packageSubmissionApk                      # app/build/submission/차분석가_차닥_release.apk
./gradlew packageSubmissionApk -PteamName=다른팀명   # 팀명만 바꿀 때
```

산출물은 `app/build/submission/`에 생깁니다. 제출 때 그 파일을 이 폴더(`apk/차분석가_차닥_release.apk`)에 복사하거나, Release에 첨부된 파일을 사용합니다. 경로에 한글이 있으니 셸에서는 따옴표로 감쌉니다.

```bash
cp "app/build/submission/차분석가_차닥_release.apk" "apk/차분석가_차닥_release.apk"
```

릴리스 워크플로는 UTF-8 로캘(`LANG=C.UTF-8`)로 빌드하고, `vars.TEAM_NAME`이 있으면 그 값을 `teamName`으로 씁니다. 같은 APK를 `submission-apk` 아티팩트로도 올립니다. 서명 시크릿이 없으면 디버그 키로 서명합니다. 서명은 `CONTRIBUTING.md`를 봅니다.
