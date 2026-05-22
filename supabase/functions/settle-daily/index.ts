// Run after 04:00 KST to settle the previous ranking day.
// Deploy: supabase functions deploy settle-daily
// Cron example: "5 19 * * *" UTC = 04:05 KST

import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

Deno.serve(async () => {
  try {
    const supabase = createClient(
      Deno.env.get("SUPABASE_URL")!,
      Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!,
    );

    const now = new Date();
    const kstNow = new Date(now.getTime() + 9 * 60 * 60 * 1000);
    kstNow.setDate(kstNow.getDate() - 1);
    const targetDate = kstNow.toISOString().split("T")[0];

    const { error } = await supabase.rpc("settle_daily_rankings", {
      target_date: targetDate,
    });

    if (error) {
      console.error("settle_daily_rankings failed:", error);
      return new Response(JSON.stringify({ error: error.message }), {
        status: 500,
        headers: { "Content-Type": "application/json" },
      });
    }

    console.log(`settled ranking date ${targetDate}`);
    return new Response(JSON.stringify({ success: true, date: targetDate }), {
      headers: { "Content-Type": "application/json" },
    });
  } catch (e) {
    console.error(e);
    return new Response(JSON.stringify({ error: String(e) }), {
      status: 500,
      headers: { "Content-Type": "application/json" },
    });
  }
});
