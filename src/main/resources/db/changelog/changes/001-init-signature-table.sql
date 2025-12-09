--liquibase formatted sql
--changeset signature:init-schema
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS signature (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    signer_name TEXT,
    signer_position TEXT,
    signer_organization TEXT,

    certificate_serial_number TEXT,
    certificate_valid_from TIMESTAMPTZ,
    certificate_valid_to TIMESTAMPTZ,
    is_certificate_valid_at_signing_time BOOLEAN,

    signed_at TIMESTAMPTZ,
    signature_bytes BYTEA,
    signature_base64 TEXT,

    file_id UUID,
    file_name TEXT,
    file_hash TEXT,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

