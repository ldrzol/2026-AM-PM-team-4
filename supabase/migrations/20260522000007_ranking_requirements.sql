create or replace function public.settle_daily_rankings(target_date date)
returns void
language plpgsql
security definer
set search_path = public
as $$
begin
  delete from daily_rankings where for_date = target_date;

  insert into daily_rankings (group_id, user_id, for_date, spent_amount, rank, is_winner, is_loser)
  with member_totals as (
    select
      gm.group_id,
      gm.user_id,
      coalesce(sum(t.amount) filter (where t.kind = 'expense'), 0) as spent,
      coalesce(sum(t.amount) filter (where t.kind = 'income'), 0) as income
    from group_members gm
    left join transactions t
      on t.group_id = gm.group_id
     and t.user_id = gm.user_id
     and t.occurred_on = target_date
    group by gm.group_id, gm.user_id
  ),
  ranked as (
    select
      group_id,
      user_id,
      spent,
      rank() over (
        partition by group_id
        order by (spent::numeric / income::numeric) asc, spent asc, user_id asc
      ) as rank_value
    from member_totals
    where income > 0
  )
  select
    group_id,
    user_id,
    target_date,
    spent,
    rank_value,
    rank_value = 1,
    false
  from ranked;

  update daily_rankings dr
     set is_loser = true
   where dr.for_date = target_date
     and dr.rank = (
       select max(rank)
       from daily_rankings
       where for_date = target_date
         and group_id = dr.group_id
     );

  insert into roulette_tickets (user_id, source)
  select user_id, 'daily_winner'
  from daily_rankings
  where for_date = target_date and is_winner = true
  union all
  select user_id, 'daily_winner'
  from daily_rankings
  where for_date = target_date and is_winner = true;

  update profiles
     set has_crown_until = (target_date + interval '2 days')::timestamptz
   where id in (
     select user_id
     from daily_rankings
     where for_date = target_date and is_winner = true
   );

  update profiles
     set forced_outfit = 'outfit_beggar',
         forced_until = (target_date + interval '2 days')::timestamptz
   where id in (
     select user_id
     from daily_rankings
     where for_date = target_date and is_loser = true
   );
end;
$$;

grant execute on function public.settle_daily_rankings(date) to authenticated;

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
    coalesce(sum(t.amount) filter (where t.kind = 'expense'), 0)::bigint as spent_amount,
    coalesce(sum(t.amount) filter (where t.kind = 'income'), 0)::bigint as income_amount
  from group_members gm
  left join profiles p on p.id = gm.user_id
  left join transactions t
    on t.group_id = gm.group_id
   and t.user_id = gm.user_id
   and t.occurred_on = p_date
  where gm.group_id = p_group_id
    and exists (
      select 1
      from group_members mine
      where mine.group_id = p_group_id
        and mine.user_id = auth.uid()
    )
  group by gm.group_id, gm.user_id, gm.joined_at, p.id
  order by gm.joined_at asc, gm.user_id asc;
$$;

grant execute on function public.get_group_ranking_members(uuid, date) to authenticated;
