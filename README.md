README Checklist
•	Project goal + GIF screenshot.
•	Architecture diagram (Mermaid):
sequenceDiagram
  participant UI
  participant Sett as Settlement
  participant MQ as RabbitMQ
  participant Led as Ledger
  UI->>Sett: POST /trades
  Sett-->>MQ: TradeCreated
  MQ-->>Sett: TradeCreated (retry/idempotent)
  Sett-->>MQ: SettlementCreated
  MQ-->>Led: SettlementCreated
  Led-->>Led: Write Dr/Cr entries (idempotent)
  UI->>Sett: POST /eod/run
  Sett-->>UI: recon report link
•	Running locally (docker compose).
•	API examples.
•	Design decisions + trade offs.
