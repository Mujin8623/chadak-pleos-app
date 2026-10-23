# Changelog

이 프로젝트의 변경 기록은 [Keep a Changelog](https://keepachangelog.com/ko/1.1.0/)를 따르고, 버전은 [SemVer](https://semver.org/lang/ko/)를 따릅니다.

## [Unreleased]

### Changed

- 제출용 APK 기본 파일 이름을 `TEAM_Chadak_release.apk`에서 `차량분석가_차닥_release.apk`로 바꿨습니다(공모전 규칙 `apk/팀명_앱이름_release.apk`). 릴리스 워크플로는 UTF-8 로캘로 빌드하고 APK를 `submission-apk` 아티팩트로도 올립니다.

## [0.1.0] - 2026-10-21

### Added

- 차닥 Android 스켈레톤. 패키지 `com.mujin.chadak`, 모듈 `app`.
- 신호(`VehicleSignalSource`, Pleos 스텁, `FakeSignalSource`, `DebugMockVehicleSource`), 3단계 분류 엔진과 단위 테스트, 음성·경유지 인터페이스, 주행/주차/타임라인 화면.
- `version.properties` 단일 버전 소스. `versionCode = major * 10000 + minor * 100 + patch`.
- 디버그 빌드용 오버레이와 `ai.chadak.DEBUG_EVENT` 브로드캐스트. 타이어 공기압은 공개 API가 없어 이 경로로만 넣습니다.
- CI(`assembleDebug`, `lint`, 단위 테스트)와 `v*` 태그 릴리스 워크플로. 서명 비밀이 없으면 디버그 키로 서명한 실행 가능한 release APK를 만듭니다.
- Pleos Maven 의존성: Vehicle 2.3.2, NaviHelper 3.0.5, TextToSpeech 3.3.1, SpeechToText 3.3.1, FusedLocation 1.1.3.
