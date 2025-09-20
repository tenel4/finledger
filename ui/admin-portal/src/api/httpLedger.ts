// src/api/httpLedger.ts
import axios from "axios";
import { nanoid } from "nanoid";

const baseURL = import.meta.env.VITE_LEDGER_API_BASE_URL;
export const httpLedger = axios.create({ baseURL, timeout: 15000 });

httpLedger.interceptors.request.use((config) => {
  config.headers?.set?.("Content-Type", "application/json");
  config.headers?.set?.("X-Correlation-Id", nanoid());
  return config;
});
