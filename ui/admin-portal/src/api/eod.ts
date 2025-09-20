import dayjs from "dayjs";
import { httpSettlement } from "./httpSettlement";
import type { EodRunResponse } from "./types";

export async function runEod(date: string | Date = new Date()): Promise<EodRunResponse> {
  const formattedDate =
    typeof date === "string" ? date : dayjs(date).format("YYYY-MM-DD");

  const { data } = await httpSettlement.post<EodRunResponse>(
    "/api/eod/run",
    null, // no request body
    { params: { date: formattedDate } }
  );

  return data;
}
