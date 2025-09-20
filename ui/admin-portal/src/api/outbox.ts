import { httpSettlement } from "./httpSettlement";
import type { DeadOutboxEvent, OutboxEvent, OutboxStats, PagedResponse } from "./types";

export async function getOutboxStats(): Promise<OutboxStats> {
    const { data } = await httpSettlement.get<OutboxStats>("/admin/outbox/stats");
    return data;
}

export async function getOutboxMetrics(): Promise<Record<string, number>> {
    const { data } = await httpSettlement.get<Record<string, number>>("/admin/outbox/metrics");
    return data;
}

export async function browseOutbox(params?: {
    status?: OutboxStats;
    eventType?: string;
    page?: number;
    size?: number;
}): Promise<PagedResponse<OutboxEvent>> {
    const { data } = await httpSettlement.get<PagedResponse<OutboxEvent>>("/admin/outbox", { params });
    return data
}

export async function browseDeadOutbox(params?: {
    page?: number;
    size?: number;
}): Promise<PagedResponse<DeadOutboxEvent>> {
    const { data } = await httpSettlement.get<PagedResponse<DeadOutboxEvent>>("/admin/outbox/dead", { params });
    return data;
}

export async function requeueOutbox(id: string) {
    const { data } = await httpSettlement.post(`/admin/outbox/requeue/${id}`);
    return data;
}

export async function requeueDeadOutbox(id: string) {
  const { data } = await httpSettlement.post(`/admin/outbox/requeue-dead/${id}`);
  return data;
}