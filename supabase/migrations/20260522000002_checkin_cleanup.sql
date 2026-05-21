-- 출석체크 컬럼 추가
alter table profiles
  add column if not exists check_streak int not null default 0,
  add column if not exists last_checkin date;

-- 코스튬 상점에서 사이드 리본·캡 모자 제거
update costumes set is_shop = false where id in ('hat_side_ribbon', 'hat_cap');
