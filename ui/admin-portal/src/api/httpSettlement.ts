// src/api/httpSettlement.ts
import axios from "axios";
import { nanoid } from "nanoid";

const baseURL = import.meta.env.VITE_SETTLEMENT_API_BASE_URL;
export const httpSettlement = axios.create({ baseURL, timeout: 15000 });

httpSettlement.interceptors.request.use((config) => {
  config.headers?.set?.("Content-Type", "application/json");
  config.headers?.set?.("X-Correlation-Id", nanoid());
  return config;
});
