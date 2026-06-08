# NPC 뽑기 상점 아이템 구매 및 보상 지급

## Produce 시스템 / Unity Backend

| 항목 | 내용 |
|---|---|
| 인터페이스 ID | INF_UNITY_029 |
| 이름 | NPC 뽑기 상점 아이템 구매 및 뽑기 실행 |
| Method | POST |
| URI | /api/v1/users/me/npcs/{npcId}/items/{npcItemId}/gacha |
| 작성자 | nh-backend |
| 작성일 | 2026-06-02 |

## 설명

NPC 뽑기 상점에서 뽑기 아이템을 구매하고, 해당 아이템의 등급에 맞는 보상 아이템을 추첨한다.

구매 가격만큼 유저 재화를 차감한 뒤, 뽑기 결과 아이템의 `itemType`에 따라 서버 내부 보상 지급 로직을 수행한다.

`GOLD_COUPON`, `GEM_COUPON`, `EXP_COUPON` 타입 아이템은 유저 인벤토리에 저장하지 않는다. 대신 `Item.goldCoupon`, `Item.gemCoupon`, `Item.expCoupon` 값을 기준으로 유저의 `wallet` 또는 `profile` 값을 즉시 증가시킨다.

## Request

### Path Variable

| NO | 레벨 | 항목 | 유형 | 필수여부 | SIZE | 항목명 | 설명 |
|---:|---:|---|---|:---:|---|---|---|
| 1 | 1 | npcId | Long | Y |  | NPC ID | Path Variable - NPC DB ID |
| 2 | 1 | npcItemId | Long | Y |  | NPC 상점 아이템 ID | Path Variable - NPC 상점 아이템 ID |

### Request 예시

```json
{}
```

## 내부 처리 순서

| 순서 | 처리 | 설명 |
|---:|---|---|
| 1 | NPC 조회 | `npcId`로 NPC 존재 여부 확인 |
| 2 | NPC 상점 아이템 조회 | `npcItemId`로 구매 대상 아이템 조회 |
| 3 | 가격 검증 | 유저 보유 재화가 아이템 가격보다 부족한지 확인 |
| 4 | 재화 차감 | 구매 가격만큼 `wallet.gold` 또는 `wallet.gem` 차감 |
| 5 | 등급 기반 뽑기 | 구매한 뽑기 아이템의 등급에 맞는 보상 아이템 추첨 |
| 6 | 보상 타입 판별 | 뽑힌 아이템의 `itemType` 확인 |
| 7 | 보상 지급 | 타입별 내부 처리 로직 수행 |
| 8 | 결과 응답 | 변경된 `wallet`, `profile`, `drawnItem`, `reward` 반환 |

## 내부 보상 처리 규칙

| itemType | 내부 처리 ID | 처리 방식 | 인벤토리 저장 여부 |
|---|---|---|:---:|
| `GOLD_COUPON` | INF_UNITY_030 | `Item.goldCoupon` 값만큼 `wallet.gold` 증가 | N |
| `GEM_COUPON` | INF_UNITY_031 | `Item.gemCoupon` 값만큼 `wallet.gem` 증가 | N |
| `EXP_COUPON` | INF_UNITY_032 | `Item.expCoupon` 값만큼 `profile.exp` 증가 | N |
| 그 외 일반 아이템 | - | 유저 인벤토리에 아이템 추가 | Y |

## Response

### HEADER

`200 OK`

| NO | 레벨 | 항목 | 유형 | 필수여부 | SIZE | 항목명 | 설명 |
|---:|---:|---|---|:---:|---|---|---|
| 1 | 1 | wallet | Object | Y |  | 지갑 상태 | 구매 및 보상 반영 후 지갑 상태 |
| 2 | 2 | gold | Long | Y |  | 현재 골드 | 최종 골드 |
| 3 | 2 | gem | Long | Y |  | 현재 보석 | 최종 보석 |
| 4 | 1 | profile | Object | Y |  | 프로필 상태 | 경험치 보상 반영 후 프로필 상태 |
| 5 | 2 | exp | Long | Y |  | 현재 경험치 | 최종 경험치 |
| 6 | 1 | drawnItem | Object | Y |  | 뽑힌 아이템 | 뽑기 결과 아이템 |
| 7 | 2 | itemId | Long | Y |  | 아이템 ID | 뽑힌 아이템 DB ID |
| 8 | 2 | rId | String | Y |  | 리소스 ID | 아이템 리소스 ID |
| 9 | 2 | itemName | String | Y |  | 아이템명 | 뽑힌 아이템 이름 |
| 10 | 2 | itemType | String | Y |  | 아이템 타입 | 뽑힌 아이템 타입 |
| 11 | 2 | itemGrade | String | Y |  | 아이템 등급 | 뽑힌 아이템 등급 |
| 12 | 2 | goldCoupon | Integer | Y |  | 골드 보상값 | `GOLD_COUPON` 지급량 |
| 13 | 2 | gemCoupon | Integer | Y |  | 보석 보상값 | `GEM_COUPON` 지급량 |
| 14 | 2 | expCoupon | Integer | Y |  | 경험치 보상값 | `EXP_COUPON` 지급량 |
| 15 | 1 | reward | Object | Y |  | 지급 결과 | 실제 지급된 보상 |
| 16 | 2 | type | String | Y |  | 보상 타입 | `GOLD`, `GEM`, `EXP`, `ITEM` |
| 17 | 2 | amount | Integer | Y |  | 지급량 | 실제 지급 수량 |
| 18 | 1 | acquiredInventoryItem | Object | N |  | 획득 인벤토리 아이템 | 쿠폰 보상일 경우 `null` |

### Response 예시 - GEM_COUPON

```json
{
  "success": true,
  "message": "구매가 완료되었습니다.",
  "data": {
    "wallet": {
      "gold": 4910,
      "gem": 20
    },
    "profile": {
      "exp": 1500
    },
    "drawnItem": {
      "itemId": 6,
      "rId": "gem_coupon_epic_001",
      "itemName": "Epic Gem Coupon",
      "itemType": "GEM_COUPON",
      "itemGrade": "EPIC",
      "goldCoupon": 0,
      "gemCoupon": 10,
      "expCoupon": 0
    },
    "reward": {
      "type": "GEM",
      "amount": 10
    },
    "acquiredInventoryItem": null
  }
}
```

### Response 예시 - 일반 아이템

```json
{
  "success": true,
  "message": "구매가 완료되었습니다.",
  "data": {
    "wallet": {
      "gold": 4910,
      "gem": 10
    },
    "profile": {
      "exp": 1500
    },
    "drawnItem": {
      "itemId": 12,
      "rId": "potion_hp_001",
      "itemName": "HP Potion",
      "itemType": "CONSUMABLE",
      "itemGrade": "NORMAL",
      "goldCoupon": 0,
      "gemCoupon": 0,
      "expCoupon": 0
    },
    "reward": {
      "type": "ITEM",
      "amount": 1
    },
    "acquiredInventoryItem": {
      "userItemId": 33,
      "itemId": 12,
      "quantity": 1,
      "equipped": false,
      "acquiredAt": "2026-06-02T12:00:00"
    }
  }
}
```

## Error Message

### HEADER

`400 Bad Request`

```json
{
  "success": false,
  "message": "재화가 부족합니다.",
  "data": null
}
```

### HEADER

`404 Not Found`

```json
{
  "success": false,
  "message": "NPC를 찾을 수 없습니다. / 상점 아이템을 찾을 수 없습니다.",
  "data": null
}
```

### HEADER

`500 Internal Server Error`

```json
{
  "success": false,
  "message": "서버 오류가 발생했습니다.",
  "data": null
}
```

# 내부 보상 지급 처리 문서

아래 `INF_UNITY_030`, `INF_UNITY_031`, `INF_UNITY_032`는 클라이언트가 직접 호출하는 API가 아니다. 별도 Method와 URI를 제공하지 않으며, `INF_UNITY_029` 내부 서비스 로직에서만 실행된다.

## INF_UNITY_030

| 항목 | 내용 |
|---|---|
| 인터페이스 ID | INF_UNITY_030 |
| 이름 | GOLD_COUPON 내부 보상 지급 |
| 구분 | Internal Service Logic |
| 호출 위치 | INF_UNITY_029 내부 |

### 설명

`INF_UNITY_029`에서 뽑힌 아이템의 `itemType`이 `GOLD_COUPON`인 경우 수행되는 내부 보상 지급 로직이다.

클라이언트가 직접 호출할 수 있는 API가 아니며, 별도 URI를 제공하지 않는다.

### 입력값

| 항목 | 유형 | 필수여부 | 설명 |
|---|---|:---:|---|
| userId | Long | Y | 보상을 지급받을 유저 ID |
| drawnItem | Item | Y | 뽑기 결과 아이템 |

### 처리 규칙

| 조건 | 처리 |
|---|---|
| `drawnItem.itemType == GOLD_COUPON` | `drawnItem.goldCoupon`만큼 `wallet.gold` 증가 |
| `drawnItem.goldCoupon <= 0` | 보상 지급 실패 처리 |
| 인벤토리 저장 | 하지 않음 |

### 반환값

| 항목 | 유형 | 설명 |
|---|---|---|
| reward.type | String | `GOLD` |
| reward.amount | Integer | 지급된 골드 수량 |
| wallet.gold | Long | 지급 후 골드 |

## INF_UNITY_031

| 항목 | 내용 |
|---|---|
| 인터페이스 ID | INF_UNITY_031 |
| 이름 | GEM_COUPON 내부 보상 지급 |
| 구분 | Internal Service Logic |
| 호출 위치 | INF_UNITY_029 내부 |

### 설명

`INF_UNITY_029`에서 뽑힌 아이템의 `itemType`이 `GEM_COUPON`인 경우 수행되는 내부 보상 지급 로직이다.

클라이언트가 직접 호출할 수 있는 API가 아니며, 별도 URI를 제공하지 않는다.

### 입력값

| 항목 | 유형 | 필수여부 | 설명 |
|---|---|:---:|---|
| userId | Long | Y | 보상을 지급받을 유저 ID |
| drawnItem | Item | Y | 뽑기 결과 아이템 |

### 처리 규칙

| 조건 | 처리 |
|---|---|
| `drawnItem.itemType == GEM_COUPON` | `drawnItem.gemCoupon`만큼 `wallet.gem` 증가 |
| `drawnItem.gemCoupon <= 0` | 보상 지급 실패 처리 |
| 인벤토리 저장 | 하지 않음 |

### 반환값

| 항목 | 유형 | 설명 |
|---|---|---|
| reward.type | String | `GEM` |
| reward.amount | Integer | 지급된 보석 수량 |
| wallet.gem | Long | 지급 후 보석 |

## INF_UNITY_032

| 항목 | 내용 |
|---|---|
| 인터페이스 ID | INF_UNITY_032 |
| 이름 | EXP_COUPON 내부 보상 지급 |
| 구분 | Internal Service Logic |
| 호출 위치 | INF_UNITY_029 내부 |

### 설명

`INF_UNITY_029`에서 뽑힌 아이템의 `itemType`이 `EXP_COUPON`인 경우 수행되는 내부 보상 지급 로직이다.

클라이언트가 직접 호출할 수 있는 API가 아니며, 별도 URI를 제공하지 않는다.

### 입력값

| 항목 | 유형 | 필수여부 | 설명 |
|---|---|:---:|---|
| userId | Long | Y | 보상을 지급받을 유저 ID |
| drawnItem | Item | Y | 뽑기 결과 아이템 |

### 처리 규칙

| 조건 | 처리 |
|---|---|
| `drawnItem.itemType == EXP_COUPON` | `drawnItem.expCoupon`만큼 `profile.exp` 증가 |
| `drawnItem.expCoupon <= 0` | 보상 지급 실패 처리 |
| 인벤토리 저장 | 하지 않음 |

### 반환값

| 항목 | 유형 | 설명 |
|---|---|---|
| reward.type | String | `EXP` |
| reward.amount | Integer | 지급된 경험치 |
| profile.exp | Long | 지급 후 경험치 |
