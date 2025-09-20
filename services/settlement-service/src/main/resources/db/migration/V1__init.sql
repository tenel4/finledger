-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Trade table
CREATE TABLE trade (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    symbol VARCHAR(12) NOT NULL,
    side VARCHAR(4) NOT NULL,
    quantity BIGINT NOT NULL,
    price NUMERIC(18,4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    notional_amount NUMERIC(36,8),
    buyer_account_id UUID NOT NULL,
    seller_account_id UUID NOT NULL,
    trade_time TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Settlement table
CREATE TABLE settlement (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    trade_id UUID NOT NULL,
    value_date DATE NOT NULL,
    gross_amount NUMERIC(18,4) NOT NULL,
    fees NUMERIC(18,4) NOT NULL,
    net_amount NUMERIC(18,4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(16) NOT NULL,
    message_key UUID UNIQUE NOT NULL
);

-- Processed message table
CREATE TABLE processed_message (
    message_key UUID PRIMARY KEY,
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

--Outbox Event table
CREATE TABLE outbox_event (
    id UUID PRIMARY KEY,
    type VARCHAR(255) NOT NULL,
    aggregate_type VARCHAR(255),
    aggregate_id VARCHAR(255),
    payload VARCHAR(10000) NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING', -- PENDING, RETRY, PROCESSING, SENT, DEAD
    retry_count INT NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMP NULL,
    last_error VARCHAR(2000),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
);

CREATE INDEX idx_outbox_due ON outbox_event (status, next_attempt_at NULLS FIRST, created_at);
CREATE INDEX idx_outbox_created_at ON outbox_event (created_at);
CREATE INDEX idx_outbox_type ON outbox_event (type);

-- Dead-letter table for poison messages
CREATE TABLE dead_outbox_event (
    id UUID PRIMARY KEY,
    type VARCHAR(255) NOT NULL,
    payload VARCHAR(10000) NOT NULL,
    retry_count INT NOT NULL,
    last_error VARCHAR(2000),
    created_at TIMESTAMP NOT NULL,
    dead_at TIMESTAMP NOT NULL DEFAULT NOW()
);