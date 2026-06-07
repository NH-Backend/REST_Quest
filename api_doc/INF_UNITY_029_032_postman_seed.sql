-- INF_UNITY_029~032 Postman 테스트용 시드 데이터
-- 사용 순서
-- 1. 애플리케이션 실행
-- 2. H2 console 또는 연결된 DB 클라이언트에서 이 SQL 실행
-- 3. POST /api/v1/users/register 로 테스트 유저 생성
-- 4. POST /api/v1/auth/login 으로 accessToken 발급
-- 5. Authorization: Bearer {accessToken} 헤더로 gacha API 호출
--
-- 테스트 API
-- POST /api/v1/users/me/npcs/29001/items/{npcItemId}/gacha
--
-- npcItemId 목록
-- 29101: GOLD_COUPON 테스트용 뽑기
-- 29102: GEM_COUPON 테스트용 뽑기
-- 29103: EXP_COUPON 테스트용 뽑기
-- 29104: 일반 아이템 인벤토리 지급 테스트용 뽑기
--
-- 주의
-- 현재 뽑기 로직은 구매한 GACHA 아이템과 같은 item_grade의 아이템 중 하나를 랜덤으로 뽑는다.
-- 깨끗한 DB에서는 아래 데이터만 존재하므로 각 npcItemId가 의도한 보상을 안정적으로 반환한다.
-- 이미 같은 등급의 다른 아이템이 DB에 있으면 해당 아이템도 뽑기 후보에 포함될 수 있다.

DELETE FROM npc_item WHERE id BETWEEN 29101 AND 29104;
DELETE FROM item WHERE id BETWEEN 29201 AND 29208;
DELETE FROM npc WHERE id = 29001;

INSERT INTO npc (
    id,
    r_id,
    name,
    description,
    location_key,
    active
) VALUES (
    29001,
    'npc_gacha_test_001',
    '테스트 뽑기 NPC',
    'INF_UNITY_029~032 Postman 테스트용 뽑기 NPC입니다.',
    'test_gacha_zone',
    true
);

-- 구매 대상 GACHA 아이템 4종
INSERT INTO item (
    id,
    item_name,
    r_id,
    description,
    gold_price,
    gem_price,
    sell_price,
    exp_coupon,
    gem_coupon,
    gold_coupon,
    item_type,
    item_grade
) VALUES
      (
        29201,
        'Common Gold Gacha Box',
        'gacha_gold_common_001',
        'COMMON 등급 골드 쿠폰 보상 테스트용 뽑기 상자입니다.',
        100,
        0,
        0,
        0,
        0,
        0,
        'GACHA',
        'COMMON'
      ),
      (
        29202,
        'Uncommon Gem Gacha Box',
        'gacha_gem_uncommon_001',
        'UNCOMMON 등급 보석 쿠폰 보상 테스트용 뽑기 상자입니다.',
        100,
        0,
        0,
        0,
        0,
        0,
        'GACHA',
        'UNCOMMON'
      ),
      (
        29203,
        'Rare Exp Gacha Box',
        'gacha_exp_rare_001',
        'RARE 등급 경험치 쿠폰 보상 테스트용 뽑기 상자입니다.',
        100,
        0,
        0,
        0,
        0,
        0,
        'GACHA',
        'RARE'
      ),
      (
        29204,
        'Epic Item Gacha Box',
        'gacha_item_epic_001',
        'EPIC 등급 일반 아이템 인벤토리 지급 테스트용 뽑기 상자입니다.',
        100,
        0,
        0,
        0,
        0,
        0,
        'GACHA',
        'EPIC'
      );

-- 뽑기 결과 보상 아이템 4종
INSERT INTO item (
    id,
    item_name,
    r_id,
    description,
    gold_price,
    gem_price,
    sell_price,
    exp_coupon,
    gem_coupon,
    gold_coupon,
    item_type,
    item_grade
) VALUES
      (
        29205,
        'Common Gold Coupon',
        'gold_coupon_common_001',
        '골드 1000을 지급합니다.',
        0,
        0,
        0,
        0,
        0,
        1000,
        'GOLD_COUPON',
        'COMMON'
      ),
      (
        29206,
        'Uncommon Gem Coupon',
        'gem_coupon_uncommon_001',
        '보석 30을 지급합니다.',
        0,
        0,
        0,
        0,
        30,
        0,
        'GEM_COUPON',
        'UNCOMMON'
      ),
      (
        29207,
        'Rare Exp Coupon',
        'exp_coupon_rare_001',
        '경험치 500을 지급합니다.',
        0,
        0,
        0,
        500,
        0,
        0,
        'EXP_COUPON',
        'RARE'
      ),
      (
        29208,
        'Epic Test Sword',
        'sword_epic_test_001',
        '인벤토리 지급 테스트용 EPIC 검입니다.',
        0,
        0,
        500,
        0,
        0,
        0,
        'WEAPON',
        'EPIC'
      );

-- NPC 상점에 GACHA 아이템 배치
INSERT INTO npc_item (
    id,
    npc_id,
    item_id,
    quantity,
    sort_order
) VALUES
      (29101, 29001, 29201, 999, 1),
      (29102, 29001, 29202, 999, 2),
      (29103, 29001, 29203, 999, 3),
      (29104, 29001, 29204, 999, 4);
