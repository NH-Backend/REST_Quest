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
      (1, '연습용 검', 'sword_001', '연습용 검이다.', 100, 0, 50, 0, 0, 0, 'WEAPON', 'COMMON'),
      (2, '초보자 검', 'sword_002', '초보자용 검이다.', 200, 0, 100, 0, 0, 0, 'WEAPON', 'COMMON'),
      (3, '녹슨 철검', 'sword_003', '관리가 안 된 녹슨 검이다.', 300, 0, 150, 0, 0, 0, 'WEAPON', 'COMMON'),
      (4, '강철 검', 'sword_004', '단단한 강철로 제련된 검이다.', 800, 0, 400, 0, 0, 0, 'WEAPON', 'UNCOMMON'),
      (5, '용병의 대검', 'sword_005', '숙련된 용병들이 사용하는 검이다.', 1500, 0, 750, 0, 0, 0, 'WEAPON', 'UNCOMMON'),
      (6, '불꽃의 장검', 'sword_006', '날카로운 칼날에 화염이 깃들어 있다.', 5000, 0, 2500, 0, 0, 0, 'WEAPON', 'RARE'),
      (7, '명장의 카타나', 'sword_007', '한 시대를 풍미한 명장이 만든 도이다.', 12000, 0, 6000, 0, 0, 0, 'WEAPON', 'EPIC'),
      (8, '드래곤 슬레이어', 'sword_008', '용의 심장을 께뚫었다는 전설의 검이다.', 50000, 0, 25000, 0, 0, 0, 'WEAPON', 'LEGENDARY'),
      (9, '나무 활', 'bow_001', '평범한 나무로 만든 활이다.', 150, 0, 75, 0, 0, 0, 'WEAPON', 'COMMON'),
      (10, '강화궁', 'bow_002', '사거리가 개선된 강화 활이다.', 900, 0, 450, 0, 0, 0, 'WEAPON', 'UNCOMMON'),
      (11, '엘프의 곡궁', 'bow_003', '엘프들이 사용하는 유연한 활이다.', 4500, 0, 2250, 0, 0, 0, 'WEAPON', 'RARE');

-- 1. NPC 마스터 데이터 적재 (npcs 테이블)
-- 필드 매핑: id, rId, name, description, locationKey, active
INSERT INTO npcs (id, r_id, name, description, location_key, active)
VALUES
    (1, 'npc_merchant_001', '잡화상인 아리', '모험에 필요한 무기들을 팔고 있습니다.', 'village_center', 1),
    (2, 'npc_blacksmith_001', '대장장이 고든', '단단한 장비들을 제련하고 수리합니다.', 'village_forge', 1),
    (3, 'npc_secret_001', '비밀상인 잭', '기획상 아직 오픈되지 않은 히든 NPC입니다.', 'swamp_dark', 0); -- 🌟 active = false 테스트용

-- 2. NPC 상점 판매 아이템 및 재고 적재 (npc_item 테이블)
-- 필드 매핑: id, npcId, itemId, quantity, sortOrder
-- 유저님의 실제 item 테이블 PK (id: 1 ~ 11)와 정확하게 매핑했습니다.
INSERT INTO npc_item (id, npc_id, item_id, quantity, sort_order)
VALUES
-- [1번 상인: 잡화상인 아리 ── 활(Bow) 종류 판매]
(1, 1, 9, 99, 1),  -- 나무 활 (item id: 9, 재고 99개, 1번 슬롯)
(2, 1, 10, 50, 2), -- 강화궁 (item id: 10, 재고 50개, 2번 슬롯)
(3, 1, 11, 5, 3),  -- 엘프의 곡궁 (item id: 11, 재고 5개, 3번 슬롯)

-- [2번 상인: 대장장이 고든 ── 검(Sword) 종류 판매 ── 정렬 뒤틀기 실증 세팅]
-- 일부러 데이터 인입 순서와 sort_order 순서를 꼬아놓아서, 우리가 구현한 Stream.sorted()가 작동하는지 테스트합니다.
(4, 2, 4, 10, 4),  -- 강철 검 (item id: 4, 재고 10개, 4번 슬롯 배치)
(5, 2, 1, 999, 1), -- 연습용 검 (item id: 1, 재고 999개, 1번 슬롯 배치)
(6, 2, 3, 20, 3),  -- 녹슨 철검 (item id: 3, 재고 20개, 3번 슬롯 배치)
(7, 2, 2, 50, 2);  -- 초보자 검 (item id: 2, 재고 50개, 2번 슬롯 배치)