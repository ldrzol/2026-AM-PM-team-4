create or replace function public.get_group_ranking_members(
  p_group_id uuid,
  p_date     date
)
returns table(
  user_id          uuid,
  username         text,
  display_name     text,
  avatar_seed      text,
  avatar_color     text,
  avatar_face      jsonb,
  email            text,
  coins            int,
  phone            text,
  current_hat      text,
  current_outfit   text,
  forced_outfit    text,
  forced_until     timestamptz,
  has_crown_until  timestamptz,
  share_mode       text,
  check_streak     int,
  last_checkin     date,
  created_at       timestamptz,
  spent_amount     bigint,
  income_amount    bigint
)
language sql
security definer
stable
set search_path = public
as $$
  select
    gm.user_id,
    coalesce(p.username, 'friend') as username,
    coalesce(nullif(p.display_name, ''), p.username, '친구') as display_name,
    coalesce(p.avatar_seed, 'default') as avatar_seed,
    coalesce(p.avatar_color, 'mint') as avatar_color,
    coalesce(p.avatar_face, '{}'::jsonb) as avatar_face,
    p.email,
    coalesce(p.coins, 0) as coins,
    p.phone,
    p.current_hat,
    p.current_outfit,
    p.forced_outfit,
    p.forced_until,
    p.has_crown_until,
    coalesce(p.share_mode, 'percent') as share_mode,
    coalesce(p.check_streak, 0) as check_streak,
    p.last_checkin,
    p.created_at,
    coalesce(exp.total, 0) as spent_amount,
    coalesce(inc.total, 0) as income_amount
  from group_members gm
  left join profiles p on p.id = gm.user_id
  left join (
    select user_id, sum(amount)::bigint as total
    from transactions
    where group_id = p_group_id
      and occurred_on = p_date
      and kind = 'expense'
    group by user_id
  ) exp on exp.user_id = gm.user_id
  left join (
    select user_id, sum(amount)::bigint as total
    from transactions
    where group_id = p_group_id
      and occurred_on = p_date
      and kind = 'income'
    group by user_id
  ) inc on inc.user_id = gm.user_id
  where gm.group_id = p_group_id
    and exists (
      select 1
      from group_members mine
      where mine.group_id = p_group_id
        and mine.user_id = auth.uid()
    )
  order by gm.joined_at asc, gm.user_id asc;
$$;

grant execute on function public.get_group_ranking_members(uuid, date) to authenticated;
