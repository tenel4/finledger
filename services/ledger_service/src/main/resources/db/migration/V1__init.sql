-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE ledger_entry (
  id UUID PRIMARY KEY,
  entry_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  account_id UUID NOT NULL,
  currency VARCHAR(3) NOT NULL,
  amount_signed NUMERIC(18,4) NOT NULL,
  reference_type VARCHAR(24) NOT NULL,
  reference_id UUID NOT NULL,
  message_key UUID NOT NULL
);
CREATE TABLE processed_message (
  message_key UUID PRIMARY KEY,
  processed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);
