import { httpLedger } from "./httpLedger";
import type { LedgerEntry, LedgerSummaryRow } from "./types";

export async function getLedger(params: {
    accountId?: string;
    from?: string;
    to?: string;
}): Promise<LedgerEntry[]> {
    const { data } = await httpLedger.get<LedgerEntry[]>("/api/ledger", { params });
    return data;
}

export async function getLedgerSummary(params: { date: string }): Promise<LedgerSummaryRow[]> {
    const { data } = await httpLedger.get<LedgerSummaryRow[]>("/api/ledger/summary", { params });
    return data;
  }