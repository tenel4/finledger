// src/features/ledger/LedgerSummary.tsx
import { useEffect, useState } from "react";
import { getLedgerSummary } from "../../api/ledger";
import type { LedgerSummaryRow } from "../../api/types";
import { Input } from "../../components/Input";
import { Button } from "../../components/Button";

export default function LedgerSummary() {
  const [date, setDate] = useState<string>(new Date().toISOString().slice(0,10));
  const [rows, setRows] = useState<LedgerSummaryRow[]>([]);
  const [loading, setLoading] = useState(false);

  const fetchData = async () => {
    setLoading(true);
    try { setRows(await getLedgerSummary({ date })); }
    finally { setLoading(false); }
  };

  useEffect(() => { fetchData(); }, []);

  const imbalance = rows.reduce<Record<string, number>>((acc, r) => {
    const key = r.currency;
    acc[key] = (acc[key] ?? 0) + r.sum;
    return acc;
  }, {});

  return (
    <div className="card">
      <h3>Trial balance summary</h3>
      <div className="row">
        <Input type="date" value={date} onChange={(e) => setDate(e.target.value)} />
        <Button onClick={fetchData}>Refresh</Button>
      </div>
      {loading ? <div>Loading…</div> : (
        <ul>
          {rows.map((r, i) => (
            <li key={i}><b>{r.accountId}</b> {r.currency} = {r.sum.toFixed(2)}</li>
          ))}
        </ul>
      )}
      <p className="muted">Imbalance by currency: {Object.entries(imbalance).map(([c, v]) => `${c}=${v.toFixed(2)}`).join(", ") || "0"}</p>
    </div>
  );
}
