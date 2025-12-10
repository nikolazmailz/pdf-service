#!/bin/bash

# ID подписи (замени на нужный UUID)
SIGNATURE_ID="aef8eef2-133e-4ea9-9908-2fd91220d25a"

# Куда сохранить PDF
OUTPUT_FILE="signed-$SIGNATURE_ID.pdf"

curl -X GET "http://localhost:8081/api/v1/signatures/$SIGNATURE_ID/pdf" \
  --output "$OUTPUT_FILE"

echo "Saved signed PDF to: $OUTPUT_FILE"
