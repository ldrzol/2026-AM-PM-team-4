-- 출석체크를 DB에서 원자적으로 처리합니다.
alter table public.profiles
  add column if not exists check_streak int not null default 0,
  add column if not exists last_checkin date;

update public.costumes
   set is_shop = false
 where id in ('hat_bucket', 'hat_side_ribbon');

create or replace function public.check_in()
returns int
language plpgsql
security definer
set search_path = public
as $$
declare
  v_user_id uuid := auth.uid();
  v_last_checkin date;
  v_streak int;
  v_new_streak int;
  v_reward int;
  v_today date := timezone('Asia/Seoul', now())::date;
begin
  if v_user_id is null then
    raise exception 'Not logged in';
  end if;

  select last_checkin, check_streak
    into v_last_checkin, v_streak
    from public.profiles
   where id = v_user_id
   for update;

  if not found then
    raise exception 'Profile not found';
  end if;

  if v_last_checkin = v_today then
    return 0;
  end if;

  v_new_streak := case
    when v_last_checkin = v_today - 1 then coalesce(v_streak, 0) + 1
    else 1
  end;
  v_reward := 50;

  update public.profiles
     set check_streak = v_new_streak,
         last_checkin = v_today,
         coins = coins + v_reward
   where id = v_user_id;

  return v_reward;
end;
$$;

grant execute on function public.check_in() to authenticated;
