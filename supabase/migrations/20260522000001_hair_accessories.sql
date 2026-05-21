-- 기존 아우터 아이템 샵 판매 중지
update costumes set is_shop = false
where kind = 'outfit';

-- 새 헤어 악세서리 추가
insert into costumes (id, kind, name, price, rarity, icon, is_shop, is_special) values
  ('hat_bow',         'hat', '리본',         150, 'common',    '🎀', true,  false),
  ('hat_cat',         'hat', '고양이 귀',    350, 'rare',      '🐱', true,  false),
  ('hat_star',        'hat', '별 핀',        180, 'common',    '⭐', true,  false),
  ('hat_flower_clip', 'hat', '꽃 핀',        220, 'rare',      '🌸', true,  false),
  ('hat_side_ribbon', 'hat', '사이드 리본',  280, 'rare',      '🎗', true,  false)
on conflict (id) do nothing;
