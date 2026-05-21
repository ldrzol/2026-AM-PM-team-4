-- 그룹 멤버별 일일 지출/수입 집계 RPC
-- security definer 로 RLS 를 우회해 같은 그룹 멤버의 거래를 집계합니다.
create or replace function public.get_group_daily_spending(
  p_group_id uuid,
  p_date     date
)
returns table(
  user_id       uuid,
  spent_amount  bigint,
  income_amount bigint
)
language sql
security definer
stable
set search_path = public
as $$
  select
    gm.user_id,
    coalesce(exp.total, 0) as spent_amount,
    coalesce(inc.total, 0) as income_amount
  from group_members gm
  left join (
    select user_id, sum(amount) as total
    from transactions
    where occurred_on = p_date and kind = 'expense'
    group by user_id
  ) exp on exp.user_id = gm.user_id
  left join (
    select user_id, sum(amount) as total
    from transactions
    where occurred_on = p_date and kind = 'income'
    group by user_id
  ) inc on inc.user_id = gm.user_id
  where gm.group_id = p_group_id;
$$;

grant execute on function public.get_group_daily_spending(uuid, date) to authenticated;
