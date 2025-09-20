// src/features/trades/TradesPage.tsx
import { useEffect, useState } from "react";
import { getTrades } from "../../api/trades";
import type { TradeResponse } from "../../api/types";
import { DataTable } from "../../components/DataTable";
import type { ColumnDef } from "@tanstack/react-table";
import dayjs from "dayjs";
import TradeForm from "./TradeForm";
import { Input } from "../../components/Input";
import { Select } from "../../components/Select";
import { Button } from "../../components/Button";

export default function TradesPage() {
  const [rows, setRows] = useState<TradeResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [filters, setFilters] = useState<{ symbol?: string; side?: "BUY" | "SELL" | ""; from?: string; to?: string }>({});

  const fetchData = async () => {
    setLoading(true);
    try {
      const data = await getTrades({
        symbol: filters.symbol || "AAPL",
        side: (filters.side as any) || "BUY",
        from: filters.from || "2025-09-19",
        to: filters.to || "2025-09-19",
      });
      setRows(data);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchData(); }, []);

  const columns: ColumnDef<TradeResponse>[] = [
    { header: "Symbol", accessorKey: "symbol" },
    { header: "Side", accessorKey: "side" },
    { header: "Quantity", accessorKey: "quantity" },
    { header: "Price", accessorKey: "price" },
    { header: "Currency", accessorKey: "currency" },
    { header: "Buyer", accessorKey: "buyerAccountId" },
    { header: "Seller", accessorKey: "sellerAccountId" },
    { header: "Time", cell: ({ row }) => dayjs(row.original.tradeTime).format("YYYY-MM-DD HH:mm:ss") },
  ];

  return (
    <div className="page">
      <h2>Trades</h2>
      <TradeForm />
      <div className="filters">
        <Input placeholder="Symbol" value={filters.symbol ?? ""} onChange={(e) => setFilters(f => ({ ...f, symbol: e.target.value }))} />
        <Select value={filters.side ?? ""} onChange={(e) => setFilters(f => ({ ...f, side: e.target.value as any }))}>
          <option value="">Any side</option>
          <option value="BUY">BUY</option>
          <option value="SELL">SELL</option>
        </Select>
        <Input type="date" value={filters.from ?? ""} onChange={(e) => setFilters(f => ({ ...f, from: e.target.value }))} />
        <Input type="date" value={filters.to ?? ""} onChange={(e) => setFilters(f => ({ ...f, to: e.target.value }))} />
        <Button onClick={fetchData}>Apply</Button>
      </div>
      <DataTable data={rows} columns={columns} loading={loading} emptyText="No trades found" />
    </div>
  );
}
