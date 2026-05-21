// Supabase Edge Function — 매일 자정 KST 정산 실행
// 배포: supabase functions deploy settle-daily
// 스케줄: 대시보드 > Database > Cron > "5 15 * * *" (KST 00:05)

import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

Deno.serve(async (req) => {
  try {
    const supabase = createClient(
      Deno.env.get("SUPABASE_URL")!,
      Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!
    );

    // 어제 날짜 (KST = UTC+9)
    const now = new Date();
    now.setHours(now.getHours() + 9); // UTC → KST
    now.setDate(now.getDate() - 1);
    const yesterday = now.toISOString().split("T")[0];

    const { error } = await supabase.rpc("settle_daily_rankings", {
      target_date: yesterday,
    });

    if (error) {
      console.error("정산 오류:", error);
      return new Response(JSON.stringify({ error: error.message }), {
        status: 500,
        headers: { "Content-Type": "application/json" },
      });
    }

    console.log(`✅ ${yesterday} 정산 완료`);
    return new Response(
      JSON.stringify({ success: true, date: yesterday }),
      { headers: { "Content-Type": "application/json" } }
    );
  } catch (e) {
    console.error(e);
    return new Response(JSON.stringify({ error: String(e) }), {
      status: 500,
      headers: { "Content-Type": "application/json" },
    });
  }
});
