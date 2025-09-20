// src/features/outbox/OutboxAdminPage.tsx
import { useEffect, useState } from "react";
import type { DeadOutboxEvent, OutboxEvent } from "../../api/types";
import {
  getOutboxStats,
  getOutboxMetrics,
  browseOutbox,
  browseDeadOutbox,
  requeueOutbox,
  requeueDeadOutbox,
} from "../../api/outbox";
import { Button } from "../../components/Button";
import { DataTable } from "../../components/DataTable";
import { useToast } from "../../components/Toast";

export default function OutboxAdminPage() {
  const [stats, setStats] = useState<any>(null);
  const [metrics, setMetrics] = useState<Record<string, number>>({});
  const [activeEvents, setActiveEvents] = useState<OutboxEvent[]>([]);
  const [deadEvents, setDeadEvents] = useState<DeadOutboxEvent[]>([]);
  const [tab, setTab] = useState<"active" | "dead">("active");
  const toast = useToast();

  const refresh = async () => {
    setStats(await getOutboxStats());
    setMetrics(await getOutboxMetrics());
    setActiveEvents((await browseOutbox({ page: 0, size: 25 })).content);
    setDeadEvents((await browseDeadOutbox({ page: 0, size: 25 })).content);
  };

  useEffect(() => {
    refresh();
  }, []);

  const handleRequeue = async (id: string, dead = false) => {
    if (dead) {
      await requeueDeadOutbox(id);
      toast.success(`Requeued dead event ${id}`);
    } else {
      await requeueOutbox(id);
      toast.success(`Requeued event ${id}`);
    }
    refresh();
  };

  return (
    <div className="page">
      <h2>Outbox Admin</h2>

      {stats && (
        <div className="card">
          <h3>Stats</h3>
          <pre>{JSON.stringify(stats, null, 2)}</pre>
        </div>
      )}

      {metrics && (
        <div className="card">
          <h3>Metrics</h3>
          <pre>{JSON.stringify(metrics, null, 2)}</pre>
        </div>
      )}

      <div style={{ margin: "1rem 0" }}>
        <Button onClick={() => setTab("active")} disabled={tab === "active"}>
          Active
        </Button>
        <Button onClick={() => setTab("dead")} disabled={tab === "dead"} style={{ marginLeft: 8 }}>
          Dead
        </Button>
      </div>

      {tab === "active" && (
        <DataTable
          data={activeEvents}
          columns={[
            { header: "ID", accessorKey: "id" },
            { header: "Type", accessorKey: "type" },
            { header: "Status", accessorKey: "status" },
            { header: "Retries", accessorKey: "retryCount" },
            { header: "Created", accessorKey: "createdAt" },
            {
              header: "Actions",
              cell: ({ row }) => (
                <Button onClick={() => handleRequeue(row.original.id)}>Requeue</Button>
              ),
            },
          ]}
        />
      )}

      {tab === "dead" && (
        <DataTable
          data={deadEvents}
          columns={[
            { header: "ID", accessorKey: "id" },
            { header: "Type", accessorKey: "type" },
            { header: "Retries", accessorKey: "retryCount" },
            { header: "Dead At", accessorKey: "deadAt" },
            {
              header: "Actions",
              cell: ({ row }) => (
                <Button onClick={() => handleRequeue(row.original.id, true)}>Requeue from Dead</Button>
              ),
            },
          ]}
        />
      )}
    </div>
  );
}
