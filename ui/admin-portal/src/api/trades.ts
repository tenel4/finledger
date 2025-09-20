import { httpSettlement } from "./httpSettlement";
import type { TradeCreateRequest, TradeResponse, Side } from "./types";

export async function createTrade(req: TradeCreateRequest): Promise<TradeResponse> {
    const { data } = await httpSettlement.post<TradeResponse>("/api/trades", req);
    return data;
}

export async function getTrades(params: {
    symbol?: string;
    from?: string;
    to?: string;
    side?: Side
}): Promise<TradeResponse[]> {
    const { data } = await httpSettlement.get<TradeResponse[]>("/api/trades", { params });
    return data;
}