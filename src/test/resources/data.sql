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

