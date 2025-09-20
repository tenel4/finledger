// src/features/ledger/LedgerPage.tsx
import { useEffect, useState } from "react";
import { getLedger } from "../../api/ledger";
import type { LedgerEntry } from "../../api/types";
import { DataTable } from "../../components/DataTable";
import type { ColumnDef } from "@tanstack/react-table";
import { Input } from "../../components/Input";
import dayjs from "dayjs";
import LedgerSummary from "./LedgerSummary";
import { Button } from "../../components/Button";

export default function LedgerPage() {
  const [rows, setRows] = useState<LedgerEntry[]>([]);
  const [loading, setLoading] = useState(false);
  const [filters, setFilters] = useState<{ accountId?: string; from?: string; to?: string }>({});

  const fetchData = async () => {
    setLoading(true);
    try {
      setRows(await getLedger({
        accountId: filters.accountId || undefined,
        from: filters.from || undefined,
        to: filters.to || undefined,
      }));
    } finally { setLoading(false); }
  };

  useEffect(() => { fetchData(); }, []);

  const columns: ColumnDef<LedgerEntry>[] = [
    { header: "Time", cell: ({ row }) => dayjs(row.original.entryTime).format("YYYY-MM-DD HH:mm:ss") },
    { header: "Account", accessorKey: "accountId" },
    { header: "Currency", accessorKey: "currency" },
    { header: "Amount", accessorKey: "amountSigned" },
    { header: "RefType", accessorKey: "referenceType" },
    { header: "RefId", accessorKey: "referenceId" },
  ];

  return (
    <div className="page">
      <h2>Ledger</h2>
      <div className="filters">
        <Input placeholder="Account ID" value={filters.accountId ?? ""} onChange={(e) => setFilters(f => ({ ...f, accountId: e.target.value }))} />
        <Input type="date" value={filters.from ?? ""} onChange={(e) => setFilters(f => ({ ...f, from: e.target.value }))} />
        <Input type="date" value={filters.to ?? ""} onChange={(e) => setFilters(f => ({ ...f, to: e.target.value }))} />
        <Button onClick={fetchData}>Apply</Button>
      </div>
      <DataTable data={rows} columns={columns} loading={loading} emptyText="No ledger entries" />
      <LedgerSummary />
    </div>
  );
}
