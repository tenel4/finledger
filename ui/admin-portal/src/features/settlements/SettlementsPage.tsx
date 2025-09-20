// src/features/settlements/SettlementsPage.tsx
import { useEffect, useState } from "react";
import { getSettlements } from "../../api/settlement";
import type { Settlement } from "../../api/types";
import { DataTable } from "../../components/DataTable";
import type { ColumnDef } from "@tanstack/react-table";
import { Select } from "../../components/Select";
import { Input } from "../../components/Input";
import { Button } from "../../components/Button";

export default function SettlementsPage() {
  const [rows, setRows] = useState<Settlement[]>([]);
  const [loading, setLoading] = useState(false);
  const [filters, setFilters] = useState<{ status?: string; date?: string }>({});

  const fetchData = async () => {
    setLoading(true);
    try {
      const data = await getSettlements({
        status: (filters.status as any) || undefined,
        date: filters.date || undefined,
      });
      setRows(data);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchData(); }, []);

  const columns: ColumnDef<Settlement>[] = [
    { header: "Trade ID", accessorKey: "tradeId" },
    { header: "Value Date", accessorKey: "valueDate" },
    { header: "Gross Amount", accessorKey: "grossAmount" },
    { header: "Fees", accessorKey: "fees" },
    { header: "Net Amount", accessorKey: "netAmount" },
    { header: "Currency", accessorKey: "currency" },
    { header: "Status", accessorKey: "status" },
  ];

  return (
    <div className="page">
      <h2>Settlements</h2>
      <div className="filters">
        <Select value={filters.status ?? ""} onChange={(e) => setFilters(f => ({ ...f, status: e.target.value }))}>
          <option value="">Any status</option>
          <option value="PENDING">PENDING</option>
          <option value="SETTLED">SETTLED</option>
          <option value="FAILED">FAILED</option>
        </Select>
        <Input type="date" value={filters.date ?? ""} onChange={(e) => setFilters(f => ({ ...f, date: e.target.value }))} />
        <Button onClick={fetchData}>Apply</Button>
      </div>
      <DataTable data={rows} columns={columns} loading={loading} emptyText="No settlements found" />
    </div>
  );
}
