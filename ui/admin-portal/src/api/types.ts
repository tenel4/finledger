export type UUID = string;

export type Side = "BUY" | "SELL";
export type SettlementStatus = "PENDING" | "SETTLED" | "FAILED";
export type ReferenceType = "TRADE" | "SETTLEMENT";
export type OutboxStatus = "PENDING" | "RETRY" | "PROCESSING" | "SENT" | "DEAD";

export interface TradeCreateRequest {
    symbol: string;
    side: Side;
    quantity: number;
    price: number;
    currency: string;
    buyerAccountId: UUID;
    sellerAccountId: UUID;
  }
  
  export interface TradeResponse {
    id: UUID;
    tradeTime: string;
    messageKey: UUID;
    symbol: string;
    side: Side;
    quantity: number;
    price: number;
    currency: string;
    buyerAccountId: UUID;
    sellerAccountId: UUID;
  }

  export interface Settlement {
    id: UUID;
    tradeId: UUID;
    valueDate: string; // YYYY-MM-DD
    grossAmount: number;
    fees: number;
    netAmount: number;
    currency: string;
    status: SettlementStatus;
    messageKey: UUID;
  }

  export interface LedgerEntry {
    id: UUID;
    entryTime: string;
    accountId: UUID;
    currency: string;
    amountSigned: number; // +Dr, -Cr
    referenceType: ReferenceType;
    referenceId: UUID;
    messageKey: UUID;
  }

  export interface LedgerSummaryRow {
    accountId: UUID;
    currency: string;
    sum: number;
  }
  
  export interface EodRunResponse {
    runId: string;
    date: string; // YYYY-MM-DD
    anomalies: number;
    reportPath: string; // e.g. /reports/YYYY-MM-DD/recon.json
    downloadUrl?: string; // if server exposes
  }

  export interface ApiError {
    status: number;
    message: string;
    errorId?: string;
    details?: Record<string, unknown>;
  }

  export interface OutboxEvent {
  id: string;
  type: string;
  aggregateType?: string;
  aggregateId?: string;
  payload: string;
  status: OutboxStatus;
  retryCount: number;
  nextAttemptAt?: string;
  lastError?: string;
  createdAt: string;
  updatedAt?: string;
}

export interface DeadOutboxEvent {
  id: string;
  type: string;
  payload: string;
  retryCount: number;
  lastError?: string;
  createdAt: string;
  deadAt: string;
}

export interface PagedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface OutboxStats {
  counts: Record<OutboxStatus, number>;
  due_now: number;
}