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
  "serviceName": "넷플릭스",
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
- 저장할 때 서비스명과 **같은 이름(대소문자·띄어쓰기 무시)** 의 설치된 앱이 있으면 그 앱의 `packageName`을 구독에 저장. 없으면 `""`
- 앱 목록: `PackageManager.queryIntentActivities(MAIN/LAUNCHER)` + `ResolveInfo.loadLabel` (`InstalledAppServiceImpl`). Android 11+ 때문에 `AndroidManifest.xml`에 `<queries>` 선언이 들어가 있음 (지우면 안 됨)
- 구독 상세 화면: 서비스명 아래에 연결 여부 안내가 뜨고, 직접 입력 중에는 서비스 목록·설치된 앱 이름 추천 목록이 뜸
- 코드: `domain/model/InstalledApp`, `domain/repository/InstalledAppRepository`, `data/service/installedapp`, `data/repository/installedapp`
- **리포트 담당:** 저장된 값 대신 `installedAppRepository.resolvePackageName(item)`을 쓰면 됨. 앱을 나중에 설치했거나 지운 경우까지 반영한 패키지명을 돌려줌(없으면 ""). 이 값으로 `UsageStatsManager.queryUsageStats(...)` 조회. `PACKAGE_USAGE_STATS` 권한 요청 화면(설정 → 앱 사용량 권한)은 설정 담당 범위

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
    }
}
```
- 구독이 추가/수정/삭제되거나 환율이 바뀌면 LiveData가 자동으로 다시 전달됨
- 화면이 아닌 곳(결제일 알림 작업 등)에서는 `repository.getSubscribeList()`로 바로 가져오면 됨. 어느 스레드에서 불러도 안전하고, 읽기 전용 목록이라 수정하면 오류가 남(추가·수정·삭제는 repository 메서드로)
- 요약 값: `SubscribeSummary.from(list)` (피그마 예시 데이터 기준 구독중 5 / 무료체험 1 / 해지예정 1)
  - `getSubscribingCount()` 해지완료 제외 전체 (목록 '전체' 칩 숫자, 무료체험·해지예정 포함)
  - `getFreeTrialCount()` 무료체험
  - `getCancelScheduledCount()` 해지예정 (무료체험은 제외)
  - `getCanceledCount()` 해지완료
  - `getMonthlyKrwAmount()` 월 구독료 합계(해지완료·무료체험 제외)
- 캘린더: `item.getPaymentDayIn(year, month)` → 그 달의 결제일(없으면 0). 31일 결제는 말일로 맞춤. 무료체험은 0이고 종료일은 `getPaymentMonth()/getPaymentDay()`
- 리포트: 4번 참고
- 화면 표시 문자열: `SubscribeFormatter` (`formatPrice`, `formatConvertedPrice`, `formatSchedule`, `formatKrw`, `categoryColor`)

## 7. 공통 UI (`presentation/common/view`)
- `SubmateDialog(activity)`: 공통 알림창. 별도 창(Dialog)을 띄우지 않고 현재 화면 위에 직접 그림. 뒤로가기를 누르면 알림창만 닫힘
- `SubmateToast.show(activity, message)`: 하단 메시지
- 주의: 테스트 에뮬레이터(Medium Phone API 37.2)에서 안드로이드 기본 팝업 창(ListPopupWindow, Dialog)이 화면에 보이지 않는 현상이 있었음. 드롭다운은 `SubscribeDropdown`(화면 위에 직접 그리는 방식)을 사용
