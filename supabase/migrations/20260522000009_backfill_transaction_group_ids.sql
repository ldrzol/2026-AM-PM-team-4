with member_counts as (
  select
    user_id,
    count(*) as group_count,
    min(group_id::text)::uuid as only_group_id
  from public.group_members
  group by user_id
)
update public.transactions t
   set group_id = mc.only_group_id
  from member_counts mc
 where t.user_id = mc.user_id
   and t.group_id is null
   and mc.group_count = 1;
