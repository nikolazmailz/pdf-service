#!/bin/bash

# ID подписи (замени на нужный UUID)
SIGNATURE_ID="d812b5ef-4876-46d5-b82c-d8b1f1ed9a76"

# Куда сохранить PDF
OUTPUT_FILE="signed-$SIGNATURE_ID.pdf"

curl -X GET "http://localhost:8081/api/v1/signatures/$SIGNATURE_ID/pdf" \
  --output "$OUTPUT_FILE"

echo "Saved signed PDF to: $OUTPUT_FILE"
