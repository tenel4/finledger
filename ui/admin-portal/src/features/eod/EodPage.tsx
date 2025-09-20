// src/features/eod/EodPage.tsx
import { useState } from "react";
import { runEod } from "../../api/eod";
import { Button } from "../../components/Button";
import dayjs from "dayjs";
import { useToast } from "../../components/Toast";
import { Input } from "../../components/Input";

export default function EodPage() {
  const [running, setRunning] = useState(false);
  const [date, setDate] = useState(dayjs().format("YYYY-MM-DD"));
  const [result, setResult] = useState<{
    runId: string; date: string; anomalies: number; reportPath: string; downloadUrl?: string;
  }>();

  const toast = useToast();

  const onRun = async () => {
    setRunning(true);
    try {
      const res = await runEod();
      setResult(res);
      toast.success(`EOD run started for ${res.date}`);
    } catch (e: any) {
      toast.error(e.message ?? "Failed to run EOD");
    } finally {
      setRunning(false);
    }
  };

  return (
    <div className="page">
      <h2>EOD Reconciliation</h2>

      <div className="row" style={{ gap: "8px", marginBottom: "12px" }}>
        <Input
          type="date"
          value={date}
          onChange={(e) => setDate(e.target.value)}
        />
        <Button onClick={onRun} disabled={running}>
          {running ? "Running…" : "Run EOD"}
        </Button>
      </div>

      {result && (
        <div className="card">
          <p><b>Date:</b> {result.date}</p>
          <p><b>Anomalies:</b> {result.anomalies}</p>
          {result.downloadUrl
            ? <a href={result.downloadUrl}>Download report</a>
            : <p className="muted">Report saved at {result.reportPath}</p>}
        </div>
      )}
      <p className="muted">Tip: expose a static file server for /reports in Docker to enable direct downloads.</p>
    </div>
  );
}
