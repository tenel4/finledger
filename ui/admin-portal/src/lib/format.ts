// src/lib/format.ts
import dayjs from "dayjs";

export function fmtDateTime(iso: string) {
  return dayjs(iso).format("YYYY-MM-DD HH:mm:ss");
}

export function fmtDate(iso: string) {
  return dayjs(iso).format("YYYY-MM-DD");
}

export function fmtAmount(num: number, currency?: string) {
  return `${currency ?? ""} ${num.toFixed(2)}`;
}
