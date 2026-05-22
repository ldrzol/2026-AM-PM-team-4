-- Keep ranking spending scoped to the selected friend group.
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
    select user_id, group_id, sum(amount) as total
    from transactions
    where occurred_on = p_date
      and group_id = p_group_id
      and kind = 'expense'
    group by user_id, group_id
  ) exp on exp.user_id = gm.user_id and exp.group_id = gm.group_id
  left join (
    select user_id, group_id, sum(amount) as total
    from transactions
    where occurred_on = p_date
      and group_id = p_group_id
      and kind = 'income'
    group by user_id, group_id
  ) inc on inc.user_id = gm.user_id and inc.group_id = gm.group_id
  where gm.group_id = p_group_id;
$$;

grant execute on function public.get_group_daily_spending(uuid, date) to authenticated;

do $$
begin
  alter publication supabase_realtime add table transactions;
exception
  when duplicate_object then null;
end $$;
