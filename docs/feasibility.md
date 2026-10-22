# 차닥 (Android/Kotlin 차량용 앱) 기술 타당성 조사

- 조사일: 2026-10-21 · 대상: 현대자동차그룹 차량용 앱 개발 공모전 (접수 ~11/15, 2차 PT 12/3–4 실동작 시연 필수)
- 기준 SDK 버전 (Pleos SDK 릴리즈 노트 2026-10-19): Vehicle **2.3.2**, NaviHelper **3.0.5**, TextToSpeech **3.3.1**, SpeechToText **3.3.1**, FusedLocation **1.1.3**, Pleos Connect Emulator **v2.1.0** (Android 14 / API 34 이미지)
- 조사 방법
  - 공식 문서 `document.pleos.ai`: 가이드 페이지는 서버 렌더링이라 직접 읽음. API Reference(Dokka) 페이지는 본문이 JS로 렌더링되어 정적 수집으로는 내용이 비어 있음.
  - 그래서 **Pleos Maven(nexus) 저장소에 공개된 `-sources.jar`, AAR, POM을 내려받아 실제 클래스·메서드·어노테이션을 확인**했음. 아래 API 이름은 모두 소스에서 확인한 것.
  - 공식 GitHub 샘플(`github.com/Pleos-Playground/pleos-sample-{vehicle,navi,fusedlocation}-app`)과 공모전 사이트 가이드/FAQ/공지도 확인함.
- 판정 기준: **가능** = 공개 API로 구현 가능 · **부분 가능** = 우회나 제약이 있음 · **불가** = 3rd-party 앱이 쓸 수 있는 공개 API가 없음 · **확인 불가** = 문서·소스로 확정할 수 없어 에뮬레이터에서 실측해야 함

---

## 1. 요약 표

| # | 항목 | 판정 | 핵심 API / 근거 |
|---|---|---|---|
| 1a | 경고등(Telltale) 콜백 | **불가** (3rd-party) | `Safety.registerTelltales(TelltaleInfoListener)` / `getTelltalesInfo()` 둘 다 `PleosPermission.PRIVATE_CAR_SAFETY`(signature\|privileged)가 필요함. 공식 샘플 `PublicApiPolicy.kt`도 이 API들을 **blockedApis**로 막아 둠 |
| 1b | 엔진오일 레벨 | **가능** | `Safety.registerEngineOilLevelState(OilLevelStateListener)` → `onOilLevelUpdated(level: OilLevelState?)`, `getEngineOilLevelState`, `checkEngineOilLevelStateCapabilities`. 권한 `pleos.car.permission.CAR_SAFETY`(normal). enum `OilLevelState { LOW, HIGH, NORMAL, ERROR, CRITICALLY_LOW }` |
| 1c | 브레이크액 레벨 | **가능** | `Safety.registerBrakeFluidLevelState(OilLevelStateListener)`, `getBrakeFluidLevelState`. CAR_SAFETY |
| 1d | 타이어 저압(TPMS) | **불가** (공개 콜백 없음) | 공개 API는 `Tire.getLowTirePressure(area: WheelArea, ...)`뿐인데, 이건 **임계값(kPa)**을 돌려줄 뿐 경고 상태가 아님. `getTirePressure`/`registerTirePressure(TirePressureListener)`는 `@hide` + PRIVATE_CAR_SAFETY이고 샘플도 차단함. TPMS 경고등(`TelltaleType.TPMS`)도 private Telltale 경로라 쓸 수 없음 |
| 1e | 워셔액 부족 | **가능** | `Safety.registerLowWasherFluidWaringState(LowWasherFluidWaringListener)` → `onLowWasherFluidWaringUpdated(state: Boolean?)` (원문 철자 **Waring** 그대로), `hasLowWasherFluidWarning`. CAR_SAFETY |
| 1f | 출력 제한(파워다운/림프 모드) | **가능** (의미는 확인 필요) | `Safety.registerPowerDownWarningIndicator(PowerDownWarningIndicatorListener)` → `onPowerDownWarningIndicatorUpdated(state: Boolean?)`. KDoc 원문: "If the state is true, there is a problem with the engine." 림프 모드와 1:1로 같은 신호인지는 **확인 불가** |
| 1g | 누적 주행거리(ODO) | **가능** | `Odometer.registerDistanceDriven(DistanceDrivenListener)` → `onDistanceDrivenUpdated(value: Float?)`, `getCurrentDistanceDriven`. 권한 `CAR_DRIVE` |
| 2 | 주행/주차 판별 (속도·기어) | **부분 가능** | 기어: AAOS `CarPropertyManager` + `VehiclePropertyIds.GEAR_SELECTION`(normal `android.car.permission.CAR_POWERTRAIN`)을 쓰는 게 권장 경로임. Vehicle SDK `Vehicle.getGear()`→`Gear.registerGearSelection(GearSelectionListener.onSelected(VehicleGear?))`도 있지만 `@hide`·`@Deprecated("use the Google GEAR_SELECTION property")` 상태. 속도: Vehicle SDK의 `VehicleSpeedListener`는 2.3.2에서 **삭제**됨. Pleos FAQ는 `CarPropertyManager`로 `PERF_VEHICLE_SPEED`를 읽으라고 안내함. 주행 규제는 **CarUxRestrictions로 처리하는 것이 필수**(Pleos 가이드) |
| 3 | NaviHelper: 정비소 POI 검색 + 경유지 | **부분 가능** | POI **검색 API는 없음**. 경유지는 `NaviHelper.addWaypoint(RequestWaypointInfo(lat, lng, poiName, waypointIndex = WaypointIndex.FIRST/SECOND, poiId, address, poiSubId))`로 가능(최대 2개, 권한 `NAVI_ROUTE_SEARCH`). 경로가 없을 때는 `requestRoute(RouteInfo)` 또는 `requestRouteWithWaypoints(...)`로 대체. 정비소 목록은 앱이 직접 갖고 있어야 함 |
| 4 | TTS 발화 + STT로 "응/아니" 인식 | **가능** (환경 리스크 있음) | TTS: `TextToSpeech(ctx, Mode.ON_DEVICE)`, `initialize(OnInitializeListener)`, `setLocale(Locale.KOREA)`, `speak(text)`, `EventListener.onDone()`. STT: `SpeechToText(ctx, Mode.ON_DEVICE)`, `addListener(ResultListener)`, `request()`, `onUpdated(stt: String, completed: Boolean)`, `stop()`. ko-KR 지원. 권한 `pleos.car.permission.TTS_SERVICE`, `STT_SERVICE`. 의도 해석(NLU)은 없으므로 문자열 매칭을 직접 구현해야 함 |
| 5 | Virtual Console로 타이어 저압·엔진오일 부족 시뮬레이션 | **부분 가능 → 실질적으로 불가** | Virtual Console Safety ▸ **Telltales** 카드에서 TPMS 경고등을 Off/On/Blink로 바꿀 수는 있음. 하지만 앱은 Telltale을 읽을 수 없음(1a). **엔진오일/브레이크액/워셔액/파워다운용 카드는 사용 가이드에 없음.** 기어 P/R/N/D, Odometer, 브레이크 페달은 시뮬레이션 가능 |
| 6 | Linux x86_64 박스에서 에뮬레이터 실행 | **부분 가능** | Linux는 공식 지원(SDK Update Site "URL for **Windows/Linux**", 이미지 `system-images;android-34;pleos_car_emulator;x86_64`, 약 4.09GB, 다운로드 공개). 다만 문서는 **외장 GPU 필수**(최소 RTX 3060 Laptop 6GB 수준)와 **AVD RAM 4GB**를 요구함. 박스는 GPU 없음(`/dev/dri` 없음), 여유 RAM 약 3GB, `/dev/kvm`은 있으나 box 사용자 권한이 없음(sudo 가능). SwiftShader 소프트웨어 렌더링으로 개발용 구동은 시도해 볼 수 있지만 **확인 불가**. **PT용 시연 장비로는 GPU 노트북(Win/Linux, Mac은 arm64 이미지)을 권장** |
| 7 | 90초 데모 시나리오 | **부분 가능** | 타이어 저압 트리거는 공개 API가 없어 **디버그 Mock 소스가 필수**. 엔진오일 LOW는 API는 있지만 콘솔 카드가 없어 트리거 수단이 **확인 불가**(VHAL 주입 또는 Mock). 기어→P 전환은 Virtual Console + `GEAR_SELECTION`으로 가능. TTS→STT "응"→경유지 추가는 API상 가능 |
| 8 | 제출 규칙 | **확인됨** | GitHub 저장소 루트의 `apk/` 폴더에 **`팀명_앱이름_release.apk`**(실행 가능한 release), README.md 필수 항목 있음, private 저장소면 심사 계정을 Collaborator로 초대. minSdk ≥ 28, Gradle ≥ 8.0, Java/Kotlin. 공모전이 요구하는 패키지명·targetSdk·manifest meta-data 규정은 **없음** |

---

## 2. 항목별 상세

### 1. 차량 경고 신호 콜백

공통 사항:
- 진입점은 `ai.pleos.playground.vehicle.Vehicle(context)` → `initialize()` → `getSafety()` / `getTire()` / `getOdometer()` 순서이고, 종료할 때 `release()`를 호출함.
- 모든 리스너는 `VehicleListener`(sealed interface)를 상속하며 `onFailed(e: Exception)`을 공통으로 가짐.
- 가용 여부는 `check*Capabilities(onComplete: (CapabilityConfig, ...) -> Unit, onFailed)`로 먼저 확인해야 함. 문서에 따르면 차량 모델마다 지원 여부가 다름.

| 신호 | 등록 / 조회 메서드 | 콜백 | 권한 | 비고 |
|---|---|---|---|---|
| Telltale | `Safety.registerTelltales(TelltaleInfoListener)`, `getTelltalesInfo { telltales: Telltales -> }` | `onTelltaleInfoUpdated(telltales: Telltales?)` | `PRIVATE_CAR_SAFETY` | 3rd-party는 사용 불가. 모델은 `TelltaleData(type: TelltaleType, state: TelltaleState{OFF,ON,BLINK}, color, priority, warningCount)`. `TelltaleType`에는 `TPMS(6)`, `ENGINE_OIL_PRESSURE(20)`, `POWER_DOWN(24)`, `ENGINE(27)`, `MASTER_WARNING(12)` 등이 있음 |
| 엔진오일 | `Safety.registerEngineOilLevelState(OilLevelStateListener)` / `unregister…` / `getEngineOilLevelState` | `onOilLevelUpdated(level: OilLevelState?)` | `CAR_SAFETY` | AAOS 표준 `ENGINE_OIL_LEVEL`(0x11400303, VehicleOilLevel 값 체계)과 대응함. SDK 내부 enum `PleosPropertyIds.ENGINE_OIL_LEVEL`으로 확인 |
| 브레이크액 | `Safety.registerBrakeFluidLevelState(OilLevelStateListener)` / `getBrakeFluidLevelState` | 위와 같음 | `CAR_SAFETY` | `checkBrakeFluidLevelStateCapabilities` |
| (참고) 변속기오일 | `Safety.registerTransmissionOilLevelState` | 위와 같음 | `CAR_SAFETY` | |
| 워셔액 | `Safety.registerLowWasherFluidWaringState(LowWasherFluidWaringListener)` / `hasLowWasherFluidWarning` | `onLowWasherFluidWaringUpdated(state: Boolean?)` | `CAR_SAFETY` | 메서드명 철자가 Waring임에 주의 |
| 파워다운 | `Safety.registerPowerDownWarningIndicator(PowerDownWarningIndicatorListener)` / `hasPowerDownWarningIndicator` | `onPowerDownWarningIndicatorUpdated(state: Boolean?)` | `CAR_SAFETY` | |
| 타이어 | `Tire.checkLowTirePressureCapabilities { cfg, wheels -> }`, `Tire.getLowTirePressure(WheelArea, …)` | 없음 (공개 콜백 없음) | `CAR_SAFETY` | 반환값은 "critically low tire pressure **threshold** in kPa"이므로 경고 판단에 쓸 수 없음. 현재 압력 `getTirePressure`/`registerTirePressure`는 private |
| ODO | `Odometer.registerDistanceDriven(DistanceDrivenListener)` / `getCurrentDistanceDriven` | `onDistanceDrivenUpdated(value: Float?)` | `CAR_DRIVE` | Virtual Console ▸ Motion ▸ Odometer로 시뮬레이션 가능 |

근거:
- 퍼미션 표 (Safety, Tire → CAR_SAFETY / Odometer → CAR_DRIVE): https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle-permission/
- API 레퍼런스 경로 (본문은 JS 렌더링):
  - https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/safety/register-engine-oil-level-state
  - …/safety/register-brake-fluid-level-state
  - …/safety/register-low-washer-fluid-waring-state
  - …/safety/register-power-down-warning-indicator
  - …/safety/register-telltales
  - …/tire/get-low-tire-pressure
  - …/odometer/register-distance-driven
- 소스: `https://nexus-playground.pleos.ai/repository/maven-releases/ai/pleos/playground/Vehicle/2.3.2/Vehicle-2.3.2-sources.jar`
  - `api/Safety.kt`, `api/Tire.kt`, `listener/*.kt`, `constant/PleosPermission.kt`, `constant/telltale/TelltaleType.kt`
- 공식 샘플의 차단 목록: https://github.com/Pleos-Playground/pleos-sample-vehicle-app → `app/src/main/java/.../explorer/PublicApiPolicy.kt`의 `blockedApis` (`Safety.getTelltalesInfo`, `Safety.registerTelltales`, `Tire.getTirePressure`, `Tire.registerTirePressure` 등)

### 2. 주행/주차 판별
- **주행 규제는 CarUxRestrictions로 처리하는 것이 필수임.**
  - 근거: https://document.pleos.ai/docs/pleos-only/vehicle-app-developer-guide/driving-restriction-development-guide
  - 문서 요지: "Pleos Connect 앱은 플랫폼이 제공하는 CarUxRestrictions 상태를 기준으로 주행 중 UX 제한을 적용해야 합니다."
  - 사용 API: `CarUxRestrictionsManager.registerListener()` → `onUxRestrictionsChanged(CarUxRestrictions)`, `isRequiresDistractionOptimization()`, `getActiveRestrictions()`
- **DO(Distraction Optimization)를 선언하지 않은 앱은 주행 중 사용할 수 없음**(주차 중에만 가능).
  - 근거: https://document.pleos.ai/docs/pleos-only/vehicle-app-planning-guide/driving-restiction-policy
  - DO 선언 방법(`distractionOptimized` meta-data 등)은 Pleos 문서에 나와 있지 않음 → **확인 불가**.
- **기어:** `CarPropertyManager.registerCallback(callback, VehiclePropertyIds.GEAR_SELECTION /*0x11400400*/, rate)`. 권한은 normal `android.car.permission.CAR_POWERTRAIN`.
  - Vehicle SDK의 `Gear`는 `@hide`이고 deprecated 메시지가 "use the Google GEAR_SELECTION property"임.
  - Virtual Console ▸ Motion ▸ Gear Selection(P/R/N/D)으로 시뮬레이션 가능.
- **속도:** `VehiclePropertyIds.PERF_VEHICLE_SPEED`(m/s). 권한은 `android.car.permission.CAR_SPEED`.
  - Google 문서상 보호 수준을 확인해야 하고, dangerous라면 런타임 요청이 필요함.
  - 근거 (Pleos FAQ "클러스터 속도 조회 방법"): https://document.pleos.ai/docs/pleos-only/vehicle-app-developer-guide/vehicle-app-faq
  - Vehicle SDK의 `VehicleSpeedListener`는 2.3.2에서 삭제됨: https://document.pleos.ai/docs/pleos-only/release-note/sdk-261015
  - Virtual Console 사용 가이드에는 **속도 카드가 없음**(셋업 가이드에는 "차량 속도 … 상태 변경" 문구가 있음) → 에뮬레이터에서 속도를 조작할 수 있는지 **확인 불가**.
- **권장 판별식:** `parked = (gear == GEAR_PARK) || !uxRestrictions.isRequiresDistractionOptimization()`
- **빌드 설정:** `android.car` API를 쓰려면 `android { useLibrary("android.car") }`가 필요함(AAOS 표준). FusedLocation AAR이 `android.hardware.type.automotive`를 `required=true`로 merge함.

### 3. NaviHelper
- 공개 메서드 전체 목록 (`NaviHelper.kt`):
  - 초기화·리스너: `initialize()` / `initialize(onConnected, onFailed)`, `addListener/removeListener(NaviHelperEventListener)`
  - 경로·경유지: `requestRoute(RouteInfo)`, `requestRouteWithWaypoints(RouteWithWaypointsRequest)`, `cancelRoute()`, `addWaypoint(RequestWaypointInfo)`, `removeWaypoint(WaypointIndex)`, `changeRouteOption`, `requestReRoute`
  - 조회: `getBookmarkInfo/getRecentDestinationInfo/getRouteStateInfo/getCurrentLocationInfo/getDestinationInfo/getWaypointInfo/getTBTInfo/getChargerOperatorInfo`, `requestNavigationScreen`, `requestTrip`
- 위 목록에 **POI 검색(카테고리/키워드) API는 없음** → 정비소 데이터는 앱 번들 JSON 등 자체 데이터로 준비하고, `FusedLocation` 위치 기준으로 거리를 계산해 가장 가까운 곳을 고름.
- `RequestWaypointInfo(latitude, longitude, poiName: String?, waypointIndex: WaypointIndex{FIRST,SECOND,ALL}, poiId: String, address: String, poiSubId: String, poiProvider: String = "")`
  - v3.0.5에서 좌표 인자 순서가 lat, lng로 바뀌었음.
- 문서 예제는 `requestRoute`로 목적지를 먼저 설정한 뒤 `addWaypoint`를 호출함. 경로가 없을 때 `addWaypoint`가 어떻게 동작하는지는 **확인 불가** → 경로가 없으면 정비소를 목적지로 `requestRoute`.
- 결과 콜백(실제 소스상 이름): `onWayPointInfo(List<WaypointInfo>)`, `onWaypointChanged(WaypointChangedInfo)`, `onError(NaviErrorCode)`, `onNavigationStatus(NavigationStatus{INACTIVE, INITIALIZING, FREE_DRIVE, ROUTE_DRIVE})`
  - 문서 예제의 `onWaypointInfo`는 오타로 보이며 소스는 `onWayPointInfo`임.
- 근거:
  - https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/waypoint-settings
  - …/NaviHelper/declaring-permissions
  - `NaviHelper-3.0.5-sources.jar`, `navi-data-3.0.4-sources.jar`
- 에뮬레이터에 Navi Service/내비 앱이 들어 있는지는 문서상 명시가 없음("내비게이션이 탑재된 차량에 한해 전체 호환") → **실측 필요**.

### 4. TTS + STT (예/아니오)
- **모드:** `Mode.HYBRID`(기본), `SERVER`, `ON_DEVICE`. 서버 모드는 My Project의 Client ID/Secret으로 `registerApp(clientID, clientSecret, OnServerConnectionListener)`를 해야 함.
  - 프로젝트 문서에는 TTS/STT/FusedLocation이 아직 "준비 중(Coming Soon)" 상태로 나와 있음 → **ON_DEVICE 모드로 설계할 것**(앱 등록 불필요).
- **TTS 흐름:** `TextToSpeech(ctx, Mode.ON_DEVICE)` → `initialize(OnInitializeListener{onSuccess,onFailure})` → `addEventListener(EventListener{onReady,onStart,onUpdatedRms,onDone,onStop,onError})` → `setLocale(Locale.KOREA)` → `speak("…", isFinal=true)`
- **STT 흐름:** `SpeechToText(ctx, Mode.ON_DEVICE)` → `initialize()` → `addListener(ResultListener{onReady,onStartedRecognition,onUpdated(stt, completed),onUpdatedEpdData,onUpdatedRms,onEndedRecognition,onError})` → `setLocale(Locale.KOREA)` → `request()` → `stop()`
- **응답 판정:** `completed == true`일 때 정규화한 텍스트를 YES 집합(응/어/예/네/그래/좋아)·NO 집합(아니/아니요/싫어/됐어)과 매칭함. TTS `onDone()` 이후에 `request()`를 호출할 것.
- **헤드리스 데모용 대안:** `sendAudio(ByteArray)` + `completeAudioSend()`로 미리 녹음한 16kHz/16bit/mono PCM "응"을 넣을 수 있음(**ON_DEVICE 전용**).
- 근거: https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/intro, …/start-and-stop, …/set-locale, …/event-listeners, …/SpeechToText/result-listeners, …/SpeechToText/sending-audio-data, https://document.pleos.ai/docs/pleos-only/create-my-project
- 에뮬레이터 이미지에 한국어 On-device 엔진이 들어 있는지 → **확인 불가**(실측 필요).

### 5. Virtual Console
- **접속:** `adb forward tcp:8088 tcp:8088` 후 브라우저에서 `http://127.0.0.1:8088/`
- **카드 목록**(사용 가이드 기준):
  - Vehicle: Notification 1
  - Motion: Gear Selection, Brake Pedal, Odometer, Steering Angle, Steering Wheel Heat
  - ADAS: Cruise State/Target Speed
  - Convenience: 도어·조명·와이퍼 등
  - **Safety: Telltales**(주차 브레이크, 안전벨트, ABS, **TPMS** 등 Off/On/Blink)
  - Energy, Climate
- 엔진오일·브레이크액·워셔액·파워다운·타이어 압력 값 카드는 **없음**.
- 결론:
  - TPMS 경고등은 "켤 수는 있지만" 3rd-party 앱은 Telltale API를 쓸 수 없어 받지 못함.
  - 엔진오일 LOW도 콘솔로는 만들 수 없음.
  - 대안 1 (확인 불가): 에뮬레이터 루트 셸(`adb root`; 공식 구 샘플 README가 `adb root`/`setenforce 0` 사용을 안내함)에서 AAOS `cmd car_service inject-vhal-event 0x11400303 <0~4>`로 `ENGINE_OIL_LEVEL`을 주입하는 방법. Pleos Vehicle Service가 이 표준 프로퍼티를 그대로 쓰는지는 실측해야 함.
  - 대안 2: 앱 내부 **디버그 Mock 소스**.
- 근거: https://document.pleos.ai/docs/pleos-only/app-test-guide/virtual-console-guide , https://document.pleos.ai/docs/connect/guide/getting-started/application-development/use-vehicle-emulator

### 6. Linux 박스에서 에뮬레이터 실행
- **공식 요구 조건**(근거: https://document.pleos.ai/docs/connect/guide/getting-started/application-development/setup-connect-sdk-emulator , …/configure-pleos-connect-emulator):
  - SDK Update Site URL:
    - Windows/Linux: `https://nexus-playground.pleos.ai/repository/raw-releases/connect-avd/2.x/crp-sys-img.xml`
    - Mac: `…/crp-arm64-sys-img.xml`
  - 패키지 `system-images;android-34;pleos_car_emulator;x86_64` (tag `android-automotive`, vendor `pleos`)
    - 아카이브 `crp_car_emulator.zip` 4,087,751,964 B, HTTP 200으로 공개 다운로드 가능
    - emulator 패키지 ≥ 24 필요
  - Cold boot 필수(quick boot에서는 VHAL을 쓸 수 없음), RAM 4GB, **그래픽 카드 필수**(최소 RTX 3060 Laptop 6GB / RX 6600M 8GB)
- **박스 실측 사양:**

  | 항목 | 실측값 | 판단 |
  |---|---|---|
  | CPU | 8코어, vmx 지원 | 충분 |
  | 메모리 | 총 15GB, 가용 약 3GB, swap 0 | AVD 4GB 권장 대비 부족 |
  | 디스크 | 여유 120GB | 충분 |
  | `/dev/kvm` | 있음, 단 box 사용자에 권한 없음(group 103) | sudo 가능하므로 `sudo usermod -aG <kvm그룹> box` 또는 ACL로 해결 가능 |
  | GPU | 없음(`/dev/dri` 없음) | `-gpu swiftshader_indirect`만 가능 |
  | 디스플레이 | DISPLAY `:4` 사용 가능 | |

- **판단:**
  - 개발 중 API 동작 검증 용도로 박스에서 SwiftShader + RAM 2–3GB로 띄우는 것은 시도할 수 있음(**확인 불가**). 공유 박스에서 메모리가 부족해질 위험이 있음.
  - 12/3–4 오프라인 PT 시연은 **GPU 노트북(Windows/Linux x86_64, 또는 Apple Silicon Mac + arm64 이미지)** 사용이 필수에 가깝다고 판단함.
  - 시연 실패에 대비해 화면 녹화본을 백업으로 준비할 것.

### 7. 90초 데모 시나리오 타당성

| 단계 | 판정 | 구현 방법 |
|---|---|---|
| ① 주행 중(D) 타이어 저압 감지 | **불가 → Mock** | `DebugMockVehicleSource.emitTireLow(FL, 180kPa)`. 디버그 오버레이 버튼이나 `adb shell am broadcast -a ai.chadak.DEBUG_EVENT --es type TIRE_LOW`로 트리거 |
| ② TTS 경고 | 가능 | 주행 중에는 Activity가 차단될 수 있으므로 **Foreground Service**에서 TTS를 실행. 오디오 포커스는 `AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK`(Pleos 오디오 포커스 가이드) |
| ③ "응" 인식 | 가능(환경 리스크) | STT ON_DEVICE `request()`. 에뮬레이터 마이크는 Extended Controls ▸ Microphone에서 활성화. 대안은 `sendAudio()`로 녹음 PCM 주입 |
| ④ 정비소 경유지 추가 | 부분 가능 | 번들 정비소 데이터 + `FusedLocation.getLastKnownLocation` 기준 최근접 정비소 → `addWaypoint(RequestWaypointInfo(..., WaypointIndex.FIRST, ...))`. 경로가 없으면 `requestRoute` |
| ⑤ 엔진오일 LOW | 부분 가능 | 실제 API `registerEngineOilLevelState`를 연결해 두고, 트리거는 VHAL 주입(확인 불가) 또는 Mock |
| ⑥ P로 변속 → 상세 화면 | 가능 | Virtual Console ▸ Gear Selection = P → `GEAR_SELECTION` 콜백(+ CarUxRestrictions 해제) → 상세 Activity 실행 |

- **결론: 데모 흐름 자체는 성립함.** 단, ①과 ⑤는 "실차라면 이 API에 연결된다"는 구조를 보여 주고 이벤트는 Mock으로 주입하는 형태가 현실적임.
- 심사 항목인 '기술 활용성'을 고려하면, 발표에서 **Mock 계층이 실제 Pleos API 인터페이스를 그대로 대체하는 구조**라는 점을 명확히 보여 주는 것이 좋음.

### 8. 제출 규칙
공모전 측 규정(근거: https://appdevchallenge.auto-hmg.io/guide/submission , https://appdevchallenge.auto-hmg.io/faq , https://appdevchallenge.auto-hmg.io/guide/development , 공지 https://appdevchallenge.auto-hmg.io/notice/c0a1a000-2026-4923-8000-000000000006 ):

- **필수:** 접수양식(기획서) DOCX와 개인정보 동의서
  - 제출처: `appdevchallenge@hyundai-partners.com`
  - 메일 제목: `[차량용 앱 개발 공모전] 팀명(개인은 성명)_대표자명`
  - 수정 제출 시 제목 앞에 `[수정제출]`을 붙이고, 전체 자료를 다시 첨부하며 팀원 전원을 CC
- **선택(가산점):** 시연 영상, GitHub 저장소(소스 + APK)
  - APK만 따로 제출할 수 없음.
  - APK는 저장소의 **`apk/` 폴더**에 넣고 파일명은 **`팀명_앱이름_release.apk`**, **실행 가능한 release 빌드**여야 함.
  - 용량이 초과되면 GitHub Releases에 올리고 Release 페이지 URL을 제출.
  - private 저장소면 공지된 심사용 계정을 Collaborator로 초대(쓰기 권한이 함께 부여되는 점 주의).
  - **README.md 필수 항목:** 앱 이름과 한 줄 소개, 실행 방법, 테스트 환경 및 필요한 설정, APK 위치 또는 Release 링크
- **허용 SDK:** Vehicle, NaviHelper, TextToSpeech, SpeechToText, FusedLocation
- **개발 환경:** Android API 28 이상, Gradle 8.0 이상, Java/Kotlin
- **2차 PT:** 1차 합격 20팀은 에뮬레이터 구동 화면이나 기기로 실동작 앱을 시연하는 것이 필수
- 공모전이 **강제하지 않는** 것: 패키지명, targetSdk, 특정 manifest meta-data, 앱 카테고리. App Market 등록 단계에서는 카테고리를 선택함.
  - '차량 관리'는 **Mobility Services** 카테고리 예시("주행과 밀접한 관련이 있는 차량 관리 서비스")에 맞음: https://document.pleos.ai/docs/pleos-only/vehicle-app-planning-guide/app-category
- Pleos 측 요구: minSdk ≥ 28(서명 키 로테이션/lineage 때문). App Market 업로드는 APK 형식, 최대 600MB.

---

## 3. 리스크와 대응책

| 리스크 | 영향 | 대응 |
|---|---|---|
| TPMS·Telltale 공개 API 없음 | 핵심 시나리오 ① | `VehicleSignalSource` 인터페이스를 두고 `PleosVehicleSource`(실제)와 `DebugMockVehicleSource`(debug 빌드와 데모 플래그)로 분리. 기획서에는 "Pleos 공개 API 확대 시 즉시 연동"으로 명시. 파트너십 문의(partnership@pleos.ai)나 10/28 설명회 Q&A에서 CAR_SAFETY 수준 TPMS API 공개 계획을 확인 |
| 엔진오일 LOW를 콘솔로 트리거할 수 없음 | 시나리오 ⑤ | ① `adb root` + `cmd car_service inject-vhal-event` 실측 ② 실패하면 Mock |
| 에뮬레이터 GPU/RAM 요구 | 박스 구동 불안정, PT 시연 실패 | 박스는 빌드와 APK 산출 위주로 사용하고, 시연은 GPU 노트북으로. 시연 영상을 사전 녹화해 백업 |
| DO 미선언 앱은 주행 중 차단 | 주행 중 UI가 표시되지 않음 | 주행 중에는 Foreground Service + TTS/STT 음성 위주로 동작하고, 상세 UI는 P에서만 보여 줌(규제와도 부합). DO meta-data(`distractionOptimized`) 적용 가능 여부는 실측 |
| TTS/STT On-device 엔진, 한국어, 마이크 미확인 | 음성 단계 실패 | `sendAudio()`로 녹음 PCM 주입. 그래도 실패하면 화면의 "예/아니오" 대형 버튼(주차 시)과 Android 기본 TTS로 폴백(폴백은 허용 SDK 외이므로 데모 백업 용도로만) |
| NaviHelper POI 검색 없음, 에뮬레이터 내비 탑재 미확인 | 경유지 단계 | 정비소 seed JSON(예: 블루핸즈 몇 곳 좌표)을 앱에 포함. Navi Service가 없으면 `onError`/연결 실패를 감지해 앱 내 "경유지 추가됨" UI를 시뮬레이션 |
| `getGear()`는 hide/deprecated, 속도 리스너 삭제 | 주행/주차 판별 | `CarPropertyManager`(GEAR_SELECTION, PERF_VEHICLE_SPEED) + `CarUxRestrictionsManager` 사용. Vehicle SDK 2.3.2 기준으로 고정 |
| 외부 API(지도/검색) 사용 허용 여부 불명 | 정비소 데이터 | 번들 데이터 우선. 외부 API 허용 여부는 설명회 Q&A에서 확인 |

---

## 4. 프로젝트 스캐폴딩용 확정 정보

**Maven 저장소** (`settings.gradle.kts`)

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://nexus-playground.pleos.ai/repository/maven-releases/")
    }
}
```

**의존성** (2026-10-19 기준 최신 = maven-metadata `release` 값)

```kotlin
implementation("ai.pleos.playground:Vehicle:2.3.2")
implementation("ai.pleos.playground:NaviHelper:3.0.5")
implementation("ai.pleos.playground:TextToSpeech:3.3.1")
implementation("ai.pleos.playground:SpeechToText:3.3.1")
implementation("ai.pleos.playground:FusedLocation:1.1.3")
```

- 전이 의존성: kotlin-stdlib 1.9.25, core-ktx 1.8.0, coroutines-android 1.6.3, gson 2.10.1, `ai.pleos.playground:vehicle-aidl:1.1.1`, `vehicle-data:1.1.11`, `navi-data:3.0.4`, `navi-aidl:3.0.1`, `fused-location-aidl:0.0.5`, `fused-location-data:0.0.3`, `ai.pleos.playground.security:universal-access:0.6.0`

**빌드 버전** (공식 Vehicle 샘플 기준)

| 항목 | 값 |
|---|---|
| JDK | 17 |
| Gradle wrapper | 8.10.2 |
| AGP | 8.8.2 |
| Kotlin | 2.0.0 (+ compose plugin) |
| compileSdk / targetSdk | 35 |
| **minSdk** | **28** (Pleos 배포 요건. 샘플 자체는 23) |
| AAOS API | `useLibrary("android.car")` |
| 에뮬레이터 | API 34 Pleos 이미지 |

**AndroidManifest** (필요 최소)

```xml
<uses-feature android:name="android.hardware.type.automotive" android:required="true"/>  <!-- FusedLocation AAR이 merge -->
<uses-permission android:name="pleos.car.permission.CAR_SAFETY"/>     <!-- 엔진오일/브레이크액/워셔/파워다운/저압 임계값 -->
<uses-permission android:name="pleos.car.permission.CAR_DRIVE"/>      <!-- Odometer, Brake, (Gear) -->
<uses-permission android:name="pleos.car.permission.NAVI_ROUTE"/>
<uses-permission android:name="pleos.car.permission.NAVI_ROUTE_SEARCH"/>
<uses-permission android:name="pleos.car.permission.TTS_SERVICE"/>
<uses-permission android:name="pleos.car.permission.STT_SERVICE"/>
<uses-permission android:name="pleos.car.permission.FUSED_LOCATION"/>
<uses-permission android:name="android.car.permission.CAR_POWERTRAIN"/> <!-- GEAR_SELECTION (normal) -->
<uses-permission android:name="android.car.permission.CAR_SPEED"/>      <!-- PERF_VEHICLE_SPEED -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>
```

- SDK AAR이 자동으로 merge하는 항목:
  - 권한 `ai.pleos.playground.service.vehicle.VEHICLE_BINDING`, `ai.pleos.playground.service.location.fused.FUSED_LOCATION_BINDING`, `INTERNET`
  - `<queries>` (VEHICLE_START, NAVI_START, caas SPEECH_START)
  - SDK 버전 meta-data
- 따라서 앱에서 별도로 Pleos 전용 meta-data를 추가할 필요는 없음. (문서에서 확인된 범위 기준)
