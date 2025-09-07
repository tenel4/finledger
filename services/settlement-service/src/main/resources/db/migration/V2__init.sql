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
    status VARCHAR(16) NOT NULL,
    message_key UUID UNIQUE NOT NULL
);

-- Processed message table
CREATE TABLE processed_message (
    message_key UUID PRIMARY KEY,
    processed_at TIMESTAMP NOT NULL DEFAULT NOW()
);