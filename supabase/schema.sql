-- ============================================================
-- Mintly 가계부 앱 — Supabase Schema
-- 실행 순서: Supabase 대시보드 > SQL Editor 에 붙여넣기
-- ============================================================

-- ── 1) profiles ──────────────────────────────────────────────
create table if not exists profiles (
  id              uuid primary key references auth.users(id) on delete cascade,
  username        text unique not null,
  display_name    text not null,
  email           text,
  phone           text,
  avatar_seed     text not null default 'default',
  avatar_color    text not null default 'mint',
  avatar_face     jsonb not null default '{}',
  coins           int  not null default 0,
  current_hat     text,
  current_outfit  text,
  forced_outfit   text,
  forced_until    timestamptz,
  has_crown_until timestamptz,
  share_mode      text not null default 'percent' check (share_mode in ('percent','amount')),
  created_at      timestamptz default now()
);

-- 회원가입 시 자동으로 profiles 레코드 생성하는 트리거
create or replace function public.handle_new_user()
returns trigger language plpgsql security definer set search_path = public as $$
begin
  insert into public.profiles (id, username, display_name, phone)
  values (
    new.id,
    coalesce(new.raw_user_meta_data->>'username', split_part(new.email, '@', 1)),
    coalesce(new.raw_user_meta_data->>'display_name', split_part(new.email, '@', 1)),
    new.raw_user_meta_data->>'phone'
  );
  return new;
end;
$$;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
  after insert on auth.users
  for each row execute procedure public.handle_new_user();

-- ── 2) friend_groups ─────────────────────────────────────────
create table if not exists friend_groups (
  id          uuid primary key default gen_random_uuid(),
  name        text not null,
  emoji       text not null default '🏠',
  invite_code text unique not null default upper(substr(md5(random()::text), 1, 6)),
  owner_id    uuid not null references profiles(id),
  created_at  timestamptz default now()
);

create table if not exists group_members (
  group_id  uuid references friend_groups(id) on delete cascade,
  user_id   uuid references profiles(id)      on delete cascade,
  joined_at timestamptz default now(),
  primary key (group_id, user_id)
);

-- ── 3) categories ────────────────────────────────────────────
create table if not exists categories (
  id         uuid primary key default gen_random_uuid(),
  owner_id   uuid references profiles(id),
  name       text not null,
  icon       text not null,
  color      text not null,
  kind       text not null check (kind in ('income','expense')),
  sort_order int default 0
);

-- 기본 카테고리 시드
insert into categories (name, icon, color, kind, sort_order) values
  ('식비',    'restaurant',      '#E5896B', 'expense', 1),
  ('교통',    'directions_bus',  '#6BA3D6', 'expense', 2),
  ('카페',    'local_cafe',      '#B8862A', 'expense', 3),
  ('쇼핑',    'shopping_bag',    '#C77DD9', 'expense', 4),
  ('생활',    'home',            '#7D9C5C', 'expense', 5),
  ('의료',    'medical_services','#DC5B5B', 'expense', 6),
  ('문화',    'movie',           '#9C7DD9', 'expense', 7),
  ('기타지출','more_horiz',      '#767D84', 'expense', 99),
  ('월급',    'payments',        '#74BBAE', 'income',  1),
  ('용돈',    'savings',         '#5BA89A', 'income',  2),
  ('기타수입','attach_money',    '#468A7E', 'income',  99)
on conflict do nothing;

-- ── 4) transactions ──────────────────────────────────────────
create table if not exists transactions (
  id          uuid primary key default gen_random_uuid(),
  user_id     uuid not null references profiles(id) on delete cascade,
  group_id    uuid references friend_groups(id),
  kind        text not null check (kind in ('income','expense')),
  category_id uuid references categories(id),
  amount      int  not null check (amount > 0),
  memo        text,
  occurred_on date not null,
  created_at  timestamptz default now()
);

create index if not exists idx_transactions_user_date on transactions (user_id, occurred_on);
create index if not exists idx_transactions_group_date on transactions (group_id, occurred_on);

-- ── 5) daily_rankings ────────────────────────────────────────
create table if not exists daily_rankings (
  group_id     uuid references friend_groups(id) on delete cascade,
  user_id      uuid references profiles(id)      on delete cascade,
  for_date     date not null,
  spent_amount int  not null default 0,
  rank         int  not null,
  is_winner    bool not null default false,
  is_loser     bool not null default false,
  created_at   timestamptz default now(),
  primary key (group_id, user_id, for_date)
);

-- ── 6) costumes ──────────────────────────────────────────────
create table if not exists costumes (
  id         text primary key,
  kind       text not null check (kind in ('hat','outfit')),
  name       text not null,
  price      int  not null default 0,
  rarity     text not null default 'common' check (rarity in ('common','rare','epic','legendary')),
  icon       text not null default '',
  is_shop    bool not null default true,
  is_special bool not null default false
);

insert into costumes (id, kind, name, price, rarity, icon, is_shop, is_special) values
  ('hat_crown',       'hat',    '왕관',       800, 'legendary', '👑', true,  true),
  ('hat_cap',         'hat',    '캡 모자',    200, 'common',    '🧢', true,  false),
  ('hat_bunny',       'hat',    '토끼 귀',    300, 'rare',      '🐰', true,  false),
  ('hat_flower',      'hat',    '꽃 화관',    450, 'epic',      '🌸', true,  false),
  ('hat_beanie',      'hat',    '비니',       250, 'common',    '🧶', true,  false),
  ('outfit_hoodie',   'outfit', '후드티',     400, 'rare',      '🧥', true,  false),
  ('outfit_suit',     'outfit', '정장',       600, 'epic',      '👔', true,  false),
  ('outfit_casual',   'outfit', '캐주얼',     350, 'common',    '👕', true,  false),
  ('outfit_beggar',   'outfit', '거지옷',       0, 'common',    '🧺', false, true),
  ('outfit_sports',   'outfit', '스포츠웨어', 380, 'common',    '🏃', true,  false),
  ('outfit_hanbok',   'outfit', '한복',       700, 'epic',      '👘', true,  false)
on conflict do nothing;

-- ── 7) user_costumes ─────────────────────────────────────────
create table if not exists user_costumes (
  user_id     uuid references profiles(id)  on delete cascade,
  costume_id  text references costumes(id)  on delete cascade,
  acquired_at timestamptz default now(),
  primary key (user_id, costume_id)
);

-- ── 8) roulette_tickets ──────────────────────────────────────
create table if not exists roulette_tickets (
  id         uuid primary key default gen_random_uuid(),
  user_id    uuid references profiles(id) on delete cascade,
  source     text not null,
  used       bool not null default false,
  created_at timestamptz default now(),
  used_at    timestamptz
);

-- ── 9) chat_rooms ────────────────────────────────────────────
create table if not exists chat_rooms (
  id         uuid primary key default gen_random_uuid(),
  group_id   uuid references friend_groups(id) on delete cascade,
  name       text not null,
  created_at timestamptz default now()
);

-- ── 10) chat_messages ────────────────────────────────────────
create table if not exists chat_messages (
  id         uuid primary key default gen_random_uuid(),
  room_id    uuid references chat_rooms(id)  on delete cascade,
  user_id    uuid references profiles(id)    on delete cascade,
  content    text not null,
  created_at timestamptz default now()
);

create index if not exists idx_chat_messages_room on chat_messages (room_id, created_at);

-- ── 11) favorites ────────────────────────────────────────────
create table if not exists favorites (
  id          uuid primary key default gen_random_uuid(),
  user_id     uuid references profiles(id) on delete cascade,
  kind        text not null check (kind in ('income','expense')),
  category_id uuid references categories(id),
  amount      int,
  memo        text,
  sort_order  int default 0,
  created_at  timestamptz default now()
);

-- ════════════════════════════════════════════════════════════
-- RLS (Row Level Security)
-- ════════════════════════════════════════════════════════════

alter table profiles        enable row level security;
alter table friend_groups   enable row level security;
alter table group_members   enable row level security;
alter table categories      enable row level security;
alter table transactions    enable row level security;
alter table daily_rankings  enable row level security;
alter table user_costumes   enable row level security;
alter table roulette_tickets enable row level security;
alter table chat_rooms      enable row level security;
alter table chat_messages   enable row level security;
alter table favorites       enable row level security;

-- ────────────────────────────────────────────────────────────
-- group_members 자기참조로 인한 무한재귀 방지용 Security Definer 함수
-- 이 함수는 RLS 를 우회하여 현재 사용자의 그룹 ID 목록을 반환합니다
-- ────────────────────────────────────────────────────────────
create or replace function public.get_my_group_ids()
returns setof uuid language sql security definer stable as $$
  select group_id from public.group_members where user_id = auth.uid();
$$;

-- profiles
create policy "profiles_select" on profiles for select using (
  id = auth.uid()
  or id in (
    select user_id from public.group_members
    where group_id in (select public.get_my_group_ids())
  )
);
create policy "profiles_insert" on profiles for insert with check (id = auth.uid());
create policy "profiles_update" on profiles for update using (id = auth.uid());

-- friend_groups
create policy "fg_select" on friend_groups for select using (
  owner_id = auth.uid()
  or id in (select public.get_my_group_ids())
);
create policy "fg_insert" on friend_groups for insert with check (owner_id = auth.uid());

-- group_members (Security Definer 함수로 자기참조 재귀 제거)
create policy "gm_select" on group_members for select using (
  group_id in (select public.get_my_group_ids())
);
create policy "gm_insert" on group_members for insert with check (user_id = auth.uid());
create policy "gm_delete" on group_members for delete using (user_id = auth.uid());

-- categories
create policy "read global + own categories" on categories for select using (
  owner_id is null or owner_id = auth.uid()
);
create policy "manage own categories" on categories for all using (
  owner_id = auth.uid()
);

-- transactions
create policy "own transactions" on transactions for all using (user_id = auth.uid());

-- daily_rankings
create policy "dr_select" on daily_rankings for select using (
  group_id in (select public.get_my_group_ids())
);

-- user_costumes
create policy "own costumes" on user_costumes for all using (user_id = auth.uid());

-- roulette_tickets
create policy "own tickets" on roulette_tickets for all using (user_id = auth.uid());

-- chat_rooms
create policy "cr_select" on chat_rooms for select using (
  group_id in (select public.get_my_group_ids())
);
create policy "cr_insert" on chat_rooms for insert with check (
  group_id in (select public.get_my_group_ids())
);

-- chat_messages
create policy "cm_select" on chat_messages for select using (
  room_id in (
    select id from public.chat_rooms
    where group_id in (select public.get_my_group_ids())
  )
);
create policy "cm_insert" on chat_messages for insert with check (user_id = auth.uid());

-- favorites
create policy "own favorites" on favorites for all using (user_id = auth.uid());

-- costumes (공개 읽기 허용)
alter table costumes enable row level security;
create policy "costumes are public" on costumes for select using (true);

-- ════════════════════════════════════════════════════════════
-- Realtime 활성화
-- ════════════════════════════════════════════════════════════
alter publication supabase_realtime add table daily_rankings;
alter publication supabase_realtime add table chat_messages;
alter publication supabase_realtime add table profiles;

-- ════════════════════════════════════════════════════════════
-- RPC 함수
-- ════════════════════════════════════════════════════════════

-- 코인 추가
create or replace function add_coins(p_user_id uuid, p_amount int)
returns void language plpgsql security definer as $$
begin
  update profiles set coins = coins + p_amount where id = p_user_id;
end;
$$;

-- 코스튬 구매 (코인 차감 + 인벤토리 추가)
create or replace function purchase_costume(p_user_id uuid, p_costume_id text, p_price int)
returns void language plpgsql security definer as $$
begin
  if (select coins from profiles where id = p_user_id) < p_price then
    raise exception '코인이 부족합니다';
  end if;
  update profiles set coins = coins - p_price where id = p_user_id;
  insert into user_costumes (user_id, costume_id) values (p_user_id, p_costume_id)
  on conflict do nothing;
end;
$$;

-- 자정 정산 함수 (수입 대비 지출 % 기준 랭킹, 미입력자 제외)
create or replace function settle_daily_rankings(target_date date)
returns void language plpgsql security definer as $$
begin
  -- 거래 기록이 있는 멤버만 랭킹에 포함 (수입 대비 지출 % 오름차순)
  insert into daily_rankings (group_id, user_id, for_date, spent_amount, rank, is_winner, is_loser)
  select
    gm.group_id,
    gm.user_id,
    target_date,
    coalesce(exp.total, 0) as spent,
    rank() over (
      partition by gm.group_id
      order by
        case
          when coalesce(inc.total, 0) > 0
          then coalesce(exp.total, 0)::float / inc.total::float
          else coalesce(exp.total, 0)::float
        end asc
    ),
    false, false
  from group_members gm
  -- 오늘 거래 기록이 하나라도 있는 멤버만
  join (
    select distinct user_id, group_id
    from transactions
    where occurred_on = target_date
  ) has_tx on has_tx.user_id = gm.user_id and has_tx.group_id = gm.group_id
  left join (
    select user_id, group_id, sum(amount) as total
    from transactions
    where occurred_on = target_date and kind = 'expense'
    group by user_id, group_id
  ) exp on exp.user_id = gm.user_id and exp.group_id = gm.group_id
  left join (
    select user_id, group_id, sum(amount) as total
    from transactions
    where occurred_on = target_date and kind = 'income'
    group by user_id, group_id
  ) inc on inc.user_id = gm.user_id and inc.group_id = gm.group_id
  on conflict do nothing;

  -- 1등/꼴등 표시
  update daily_rankings set is_winner = true
    where for_date = target_date and rank = 1;
  update daily_rankings dr set is_loser = true
    where for_date = target_date
      and rank = (
        select max(rank) from daily_rankings
        where for_date = target_date and group_id = dr.group_id
      );

  -- 1등에게 룰렛권 3장 + 24시간 왕관
  insert into roulette_tickets (user_id, source)
  select user_id, 'daily_winner' from daily_rankings
  where for_date = target_date and is_winner = true
  union all
  select user_id, 'daily_winner' from daily_rankings
  where for_date = target_date and is_winner = true
  union all
  select user_id, 'daily_winner' from daily_rankings
  where for_date = target_date and is_winner = true;

  update profiles set has_crown_until = (target_date + interval '2 days')::timestamptz
    where id in (select user_id from daily_rankings where for_date = target_date and is_winner = true);

  -- 꼴등에게 거지옷 강제 착용
  update profiles
    set forced_outfit = 'outfit_beggar',
        forced_until  = (target_date + interval '2 days')::timestamptz
    where id in (select user_id from daily_rankings where for_date = target_date and is_loser = true);
end;
$$;
