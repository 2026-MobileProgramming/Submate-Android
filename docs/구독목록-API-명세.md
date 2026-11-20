# 구독 목록 API 명세 · 데이터 사용법 (앱 기준)

## 1. 저장 방식
- `NetworkModule.USE_LOCAL_DATA = true` (현재): 구독 목록을 기기 안(SharedPreferences `subscribe_list`)에 서버와 같은 JSON 형식으로 저장. 앱을 껐다 켜도 유지됨. 첫 실행 때만 피그마 예시 7개가 들어감 (`SubscribeLocalServiceImpl`)
- `false`: `NetworkModule.BASE_URL` 서버와 Retrofit 통신 (`SubscribeServiceImpl`, `SubscribeApi`). http 주소를 쓰면 매니페스트에 cleartext 허용이 필요함

## 2. 구독 서버 API
| 기능 | Method | URL | Body | 응답 |
|---|---|---|---|---|
| 목록 조회 | GET | /subscriptions | - | Subscribe[] |
| 추가 | POST | /subscriptions | SubscribeRequest | Subscribe |
| 수정 | PUT | /subscriptions/{id} | SubscribeRequest | Subscribe |
| 삭제 | DELETE | /subscriptions/{id} | - | 200/204 |

```json
{
  "id": 1,
  "category": "OTT",            // OTT, MUSIC, FOOD, SHOPPING, AI, DIGITAL_CONTENT, ETC
  "serviceName": "Netflix",
  "status": "ACTIVE",           // ACTIVE(구독중), CANCEL_SCHEDULED(해지예정), CANCELED(해지완료)
  "billingCycle": "MONTHLY",    // MONTHLY(월간), YEARLY(연간)
  "paymentMonth": 0,            // 연간 결제·무료체험일 때만 1~12, 월간이면 0
  "paymentDay": 3,              // 1~31 (무료체험이면 체험 종료일)
  "currency": "KRW",            // KRW, JPY, USD, FREE_TRIAL
  "amount": 4250,               // FREE_TRIAL이면 0
  "memo": "",
  "packageName": "com.netflix.mediaclient"   // 서비스명과 같은 이름의 설치된 앱 패키지명. 없으면 ""
}
```
SubscribeRequest는 id를 뺀 같은 필드.

## 3. 환율 API
- Frankfurter (무료, 인증키 없음, 하루 한 번 갱신): `GET https://api.frankfurter.dev/v1/latest?base=USD&symbols=KRW` → `{"base":"USD","date":"...","rates":{"KRW":1357.2}}`
- 앱 코드: `ExchangeRateApi` → `ExchangeRateServiceImpl` → `ExchangeRateRepositoryImpl`
- USD, JPY 각각 요청해서 `SubscribeCurrency`의 환율에 반영. 1시간 안에는 다시 요청하지 않음
- 마지막 환율은 기기에 저장(SharedPreferences `exchange_rate`). 한 번도 못 받았으면 기본값(USD 1,357 / JPY 9.1) 사용
- 구독은 사용자가 고른 통화·금액 그대로 저장되고, 원화 환산은 볼 때마다 최신 환율로 계산됨

## 4. 설치된 앱 연결 (리포트 사용량 조회용)
- 저장할 때 서비스명에 맞는 설치된 앱이 있으면 그 앱의 `packageName`을 구독에 저장. 없으면 `""`
  - ① 서비스 목록에 있는 서비스: **패키지명 표**(`ServicePackages`)로 찾음. 앱 표시 이름이 서비스명과 달라도 연결됨 (예: YouTube Music Premium → `com.google.android.apps.youtube.music`, 쿠팡 와우 → 쿠팡 앱 `com.coupang.mobile`). 서비스명은 앱 이름이 아니라 **결제하는 구독 이름**으로 통일하고(쿠팡 와우, 네이버플러스 멤버십, 컬리멤버스, SSG 유니버스), 앱은 사용 시간 조회용으로만 연결
  - ② 그 밖의 서비스(직접 입력 등): 서비스명과 **같은 이름(대소문자·띄어쓰기 무시)** 의 설치된 앱
  - 기타 카테고리의 클라우드 저장공간: Google One, 네이버 MYBOX는 표에 있어 자동 연결됨. iCloud+는 안드로이드 앱이 없어 연결되지 않음(항상 앱 없음). Apple TV는 안드로이드 앱이 있으나 패키지명을 아직 확인하지 못해 연결하지 않음. 구글 클라우드(개발자용 GCP)는 요금이 사용량으로 바뀌어 목록에 넣지 않음(직접 입력)
  - 한 서비스에 앱이 여러 개면(예: 네이버 웹툰·시리즈) 표에 적은 순서대로 **먼저 설치된 앱 하나**만 연결됨. 구독 하나에는 `packageName`이 하나만 저장되므로 여러 앱의 사용 시간을 합치지는 않음
  - 서비스 목록에 서비스를 추가하면 `ServicePackages`의 static 블록에도 `put("서비스명", "패키지명")` 한 줄 추가
- 앱 목록: `PackageManager.queryIntentActivities(MAIN/LAUNCHER)` + `ResolveInfo.loadLabel` (`InstalledAppServiceImpl`). Android 11+ 때문에 `AndroidManifest.xml`에 `<queries>` 선언이 들어가 있음 (지우면 안 됨)
- 구독 상세 화면: 서비스명 아래에 연결 여부 안내가 뜨고, 직접 입력 중에는 서비스 목록·설치된 앱 이름 추천 목록이 뜸
- 코드: `domain/model/InstalledApp`, `domain/repository/InstalledAppRepository`, `data/service/installedapp`, `data/repository/installedapp`
- **리포트 담당:** 저장된 값 대신 `installedAppRepository.resolvePackageName(item)`을 쓰면 됨. 앱을 나중에 설치했거나 지운 경우까지 반영한 패키지명을 돌려줌(없으면 ""). 설치된 앱 목록은 처음 한 번만 읽으므로, 리포트 화면 ViewModel이 만들어질 때 `installedAppRepository.refresh()`를 한 번 호출해야 최신 상태가 반영됨. 이 값으로 `UsageStatsManager.queryUsageStats(...)` 조회. `PACKAGE_USAGE_STATS` 권한 요청 화면(설정 → 앱 사용량 권한)은 설정 담당 범위

## 4-1. 직접 입력한 서비스명 정리 규칙 (`ServiceNameMatcher`)
- 대소문자·띄어쓰기 무시하고 이미 있는 이름으로 맞춤. "앞 글자만 대문자" 방식은 쓰지 않음 (ChatGPT, FLO 같은 이름이 깨지기 때문)
- 순서: ① 서비스 목록(`ServicePreset.getAllServices()`) ② 별명 표(넷플릭스→Netflix, 챗지피티→ChatGPT 등) ③ 설치된 앱 이름 ④ 없으면 입력 그대로(앞뒤 공백만 정리)
- 바뀌는 시점: 저장할 때, 서비스명 칸에서 벗어날 때. 타이핑 중에는 바꾸지 않고 추천만 띄움
- 그래서 저장된 `serviceName`은 같은 서비스면 항상 같은 표기 → 다른 탭에서 서비스명으로 묶거나 비교할 때 그대로 비교해도 됨
- 별명 추가는 `ServiceNameMatcher`의 static 블록에 `alias("정식 이름", "별명", ...)` 한 줄

## 5. 계산 규칙
- 외화 → 원화: 환율 곱한 뒤 100원 단위 반올림 (`SubscribeCurrency.toKrw`, `SubscribeItem.getKrwAmount`)
- 월 금액: 연간 요금은 1/12 (`getMonthlyKrwAmount`), 무료체험은 개수·금액에서 제외
- 목록 '전체' 탭: 해지완료 제외 / 해지완료 탭 요약은 "비활성 구독"

## 6. 다른 탭(캘린더·리포트·설정)에서 구독 데이터 쓰는 법
`SubscribeRepository`는 앱에 하나뿐(Singleton)이라 어느 화면에서 주입받아도 같은 데이터.
```java
@HiltViewModel
public class SettingViewModel extends ViewModel {
    private final LiveData<List<SubscribeItem>> subscribeList;

    @Inject
    public SettingViewModel(SubscribeRepository repository) {
        subscribeList = repository.observeSubscribeList();   // 해지완료 포함 전체
        repository.refresh(null);                            // 목록 불러오기 (꼭 호출)
    }
}
```
- **`refresh()`를 꼭 호출해야 함.** Repository는 만들어질 때 스스로 목록을 불러오지 않음(중복 조회 방지). 그 화면의 ViewModel이 처음 만들어질 때 한 번 호출하면 됨. 실패 안내가 필요하면 `null` 대신 `ResultCallback`을 넘기기
- 구독이 추가/수정/삭제되거나 환율이 바뀌면 LiveData가 자동으로 다시 전달됨
- 화면이 아닌 곳(결제일 알림 작업 등)에서는 `repository.getSubscribeList()`로 바로 가져오면 됨. 어느 스레드에서 불러도 안전하고, 읽기 전용 목록이라 수정하면 오류가 남(추가·수정·삭제는 repository 메서드로). 단, 아직 `refresh()`가 한 번도 끝나지 않았으면 빈 목록이므로, 앱이 막 시작된 상태라면 `refresh(callback)`의 결과를 쓰기
- 요약 값: `SubscribeSummary.from(list)` (피그마 예시 데이터 기준 구독중 5 / 무료체험 1 / 해지예정 1)
  - `getSubscribingCount()` 해지완료 제외 전체 (목록 '전체' 칩 숫자, 무료체험·해지예정 포함)
  - `getFreeTrialCount()` 무료체험
  - `getCancelScheduledCount()` 해지예정 (무료체험은 제외)
  - `getCanceledCount()` 해지완료
  - `getMonthlyKrwAmount()` 월 구독료 합계(해지완료·무료체험 제외)
- 캘린더: `item.getPaymentDayIn(year, month)` → 그 달의 결제일(없으면 0). 31일 결제는 말일로 맞춤. 무료체험·해지완료는 0이고 무료체험 종료일은 `getPaymentMonth()/getPaymentDay()`
- 리포트: 4번 참고
- 화면 표시 문자열: `SubscribeFormatter` (`formatPrice`, `formatConvertedPrice`, `formatSchedule`, `formatKrw`, `categoryColor`)

## 7. 공통 UI (`presentation/common/view`)
- `SubmateDialog(activity)`: 공통 알림창. 별도 창(Dialog)을 띄우지 않고 현재 화면 위에 직접 그림. 뒤로가기를 누르면 알림창만 닫힘
- `SubmateToast.show(activity, message)`: 하단 메시지
- 주의: 테스트 에뮬레이터(Medium Phone API 37.2)에서 안드로이드 기본 팝업 창(ListPopupWindow, Dialog)이 화면에 보이지 않는 현상이 있었음. 드롭다운은 `SubscribeDropdown`(화면 위에 직접 그리는 방식)을 사용
