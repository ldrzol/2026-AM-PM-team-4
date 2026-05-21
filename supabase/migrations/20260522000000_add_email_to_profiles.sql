-- profiles 테이블에 email 컬럼 추가
-- 이메일 변경 기능에서 인증 링크 없이 직접 저장하는 용도

alter table profiles
  add column if not exists email text;
