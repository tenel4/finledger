import { httpSettlement } from "./httpSettlement";
import type { Settlement, SettlementStatus } from "./types";

export async function getSettlements(params: {
    status?: SettlementStatus;
    date?: string; // YYYY-MM-DD
}): Promise<Settlement[]> {
    const { data } = await httpSettlement.get<Settlement[]>("/api/settlements", { params });
    return data;
}