# 차닥 (Chadak)

경고등과 소모품 신호를 즉시 정차, 이번 주 정비, 관찰로 나누고, 주행 중에는 음성으로 안내한 뒤 정비소를 경유지로 넣는 차량용 앱입니다.

현대자동차그룹 차량용 앱 개발 공모전(https://appdevchallenge.auto-hmg.io/) 출품용입니다. 1차 접수 마감은 2026-11-15이고, 2차 실동작 시연은 2026-12-03~04에 Pleos Connect Emulator 또는 실차에서 합니다.

## 앱 이름과 한 줄 소개

- 앱 이름: 차닥
- 한 줄 소개: 차의 경고를 세 단계로 나누고, 운전 중에는 말로 알린 다음 정비소를 길에 올립니다.

## 무엇을 하나요

주행 중에는 화면 대신 포그라운드 서비스가 말합니다. 예라고 답하면 가까운 정비소를 NaviHelper 경유지로 넣을 준비를 합니다. 주차(P)이고 주행 규제가 풀려 있을 때만 상세 화면과 소모품 타임라인 자리를 엽니다.

분류는 세 단계입니다.

| 단계 | 코드 | 예 |
| --- | --- | --- |
| 즉시 정차 | `STOP_NOW` | 브레이크액 부족, 출력 제한, 엔진오일 위험, 타이어 공기압 위험 |
| 이번 주 정비 | `SERVICE_THIS_WEEK` | 엔진오일 부족, 타이어 공기압 낮음, 점검 주행거리 도달 |
| 관찰 | `OBSERVE` | 워셔액 부족, 점검 주행거리가 가까움 |

공개 API로 받을 신호는 엔진오일, 브레이크액, 워셔액, 출력 제한, 누적 주행거리입니다. Telltale과 타이어 공기압 리스너는 `PRIVATE_CAR_SAFETY`라 호출하지 않습니다. 타이어 저압은 디버그 목(`DebugMockVehicleSource`)으로만 넣습니다. 기어와 속도는 Vehicle SDK의 숨은 Gear API가 아니라 `CarPropertyManager`의 `GEAR_SELECTION`, `PERF_VEHICLE_SPEED`와 `CarUxRestrictionsManager`를 쓸 예정입니다. 정비소 검색 API는 없어서 `assets/repair_shops.json`에서 가장 가까운 곳을 고릅니다. 경로가 없으면 `requestRoute`, 있으면 `addWaypoint(..., WaypointIndex.FIRST)`입니다. 음성은 `Mode.ON_DEVICE`와 `Locale.KOREA`입니다.

근거와 API 이름은 `docs/feasibility.md`에 있습니다.

## 실행 방법

1. Android Studio에서 이 저장소를 엽니다. JDK 17을 사용합니다.
2. Pleos Connect Emulator로 실행하거나, SDK 없이 흐름만 보려면 디버그 빌드를 일반 에뮬레이터에 설치합니다. 디버그 빌드는 차량 서비스 없이 목 신호를 씁니다.
3. 앱을 켜면 주행 화면과 "차닥 주행 안내" 알림이 나옵니다. 상세 목록은 보이지 않습니다.
4. 디버그 빌드에서는 화면 아래 버튼으로 신호를 넣습니다. 같은 동작은 adb로도 됩니다. 앱 프로세스가 떠 있어야 합니다.

```bash
adb shell am broadcast -a ai.chadak.DEBUG_EVENT --es type TIRE_LOW
adb shell am broadcast -a ai.chadak.DEBUG_EVENT --es type ENGINE_OIL_LOW
adb shell am broadcast -a ai.chadak.DEBUG_EVENT --es type GEAR_PARK
```

`type`은 `TIRE_LOW`, `TIRE_CRITICAL`, `ENGINE_OIL_LOW`, `ENGINE_OIL_CRITICAL`, `BRAKE_FLUID_LOW`, `WASHER_LOW`, `POWER_DOWN`, `ODOMETER_DUE`, `ODOMETER_SOON`, `GEAR_PARK`, `GEAR_DRIVE`, `CLEAR`입니다. 액션 문자열은 `ai.chadak.DEBUG_EVENT`이고, 애플리케이션 ID는 `com.mujin.chadak`입니다. 이 리시버는 디버그 빌드에만 들어갑니다.

5. 안내 문장이 뜨면 디버그 버튼 `응` 또는 `아니`로 예/아니오를 넣습니다. `응`이면 강남 좌표 기준으로 가장 가까운 샘플 정비소 문장이 나옵니다. 실제 NaviHelper 호출은 v0.3.0에서 연결합니다.
6. `주차 (P)`를 누르면 상세 화면이 열리고, 거기서 소모품 타임라인 자리로 갈 수 있습니다.

## 테스트 환경과 필요한 설정

- Android Studio, JDK 17, Gradle 8.10.2, Android Gradle Plugin 8.8.2, Kotlin 2.0.0
- `minSdk` 28, `compileSdk` / `targetSdk` 35
- Pleos Connect Emulator. 공모전 안내의 개발 환경 페이지에서 에뮬레이터 요청과 SDK Update Site를 확인합니다. 시스템 이미지는 API 34이고, AVD는 cold boot입니다. quick boot에서는 VHAL을 쓸 수 없습니다.
- Virtual Console: 에뮬레이터 실행 후 `adb forward tcp:8088 tcp:8088`, 브라우저에서 `http://127.0.0.1:8088/`. Motion의 Gear Selection으로 P/R/N/D를 바꿀 수 있습니다. 엔진오일·워셔액·파워다운 카드는 없고, TPMS 텔테일은 켜도 이 앱이 읽을 수 없습니다. 그래서 데모의 타이어·오일 이벤트는 목 소스입니다.
- 음성 실측은 에뮬레이터 마이크가 필요합니다. 온디바이스 한국어 엔진이 이미지에 있는지는 아직 확인하지 못했습니다.
- 시연용 PC는 GPU가 있는 Windows/Linux 노트북을 권장합니다. 이 저장소를 빌드하는 환경과 시연 장비는 다를 수 있습니다.

SDK는 Maven `https://nexus-playground.pleos.ai/repository/maven-releases/`에서 받습니다.

| 아티팩트 | 버전 |
| --- | --- |
| `ai.pleos.playground:Vehicle` | 2.3.2 |
| `ai.pleos.playground:NaviHelper` | 3.0.5 |
| `ai.pleos.playground:TextToSpeech` | 3.3.1 |
| `ai.pleos.playground:SpeechToText` | 3.3.1 |
| `ai.pleos.playground:FusedLocation` | 1.1.3 |

`local.properties`는 기기마다 다르므로 커밋하지 않습니다. Android Studio가 SDK 경로를 만들고, CI는 `ANDROID_HOME`을 사용합니다.

## 빌드

```bash
./gradlew assembleDebug lint test
./gradlew assembleRelease
./gradlew packageSubmissionApk
```

`packageSubmissionApk`는 `app/build/submission/차분석가_차닥_release.apk`를 만듭니다. 이름 규칙은 `apk/README.md`를 봅니다. 서명 비밀이 없으면 release APK도 디버그 키로 서명되어 설치할 수 있습니다. 키스토어는 저장소에 넣지 않습니다. `CONTRIBUTING.md`의 서명 절을 봅니다.

## APK 위치

제출 규칙의 위치는 저장소 `apk/`입니다. 지금은 이름 규칙(`apk/README.md`)과 `.gitkeep`만 있습니다. 빌드 산출물은 git에 올리지 않습니다.

- 로컬 파일: `app/build/submission/차분석가_차닥_release.apk`
- 제출 때 복사할 위치: `apk/차분석가_차닥_release.apk` (규칙: `apk/팀명_앱이름_release.apk`)
- 태그를 푸시하면 GitHub Release에 같은 규칙의 APK가 붙습니다. 파일이 크면 그 Release URL을 제출합니다. 스켈레톤 시점에는 아직 Release가 없습니다.

## 브랜치와 버전

`main`은 항상 빌드와 디버그 데모가 되어야 합니다. 직접 푸시하지 않고 PR로 병합합니다.

- `feature/*`, `fix/*`, `docs/*`, `chore/*`
- 커밋은 Conventional Commits

버전은 `version.properties` 한곳입니다. `versionCode = major * 10000 + minor * 100 + patch`이고 minor와 patch는 0–99입니다. 태그는 `v0.1.0`처럼 `v` + `versionName`입니다.

## 로드맵

| 버전 | 내용 |
| --- | --- |
| v0.1.0 | 스켈레톤. 분류 엔진, 화면 자리, 목 신호, CI |
| v0.2.0 | 경고 수신. 공개 Safety/Odometer 리스너와 기어·속도 |
| v0.3.0 | 온디바이스 TTS/STT와 NaviHelper 경유지 |
| v1.0.0 | 1차 제출본 (2026-11-13) |
| v1.1.0 | 2차 PT (2026-12-03~04) |

## 패키지

`com.mujin.chadak`

- `signal` 신호 원천
- `triage` 순수 Kotlin 분류
- `voice` TTS/STT 인터페이스
- `navi` 경유지
- `ui` 주행 화면, 주차 상세, 타임라인, 주행 중 포그라운드 서비스
