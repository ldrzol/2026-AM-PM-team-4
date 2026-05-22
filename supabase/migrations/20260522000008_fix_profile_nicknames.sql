update public.profiles
   set username = coalesce(nullif(username, ''), 'user_' || left(id::text, 6))
 where username is null
    or username = '';

update public.profiles
   set display_name = coalesce(nullif(display_name, ''), nullif(username, ''), 'user_' || left(id::text, 6))
 where display_name is null
    or display_name = '';
