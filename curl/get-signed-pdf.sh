#!/bin/bash

# ID подписи (замени на нужный UUID)
SIGNATURE_ID="23a5d210-0e35-41dc-a25f-e49970533d1e"

# Куда сохранить PDF
OUTPUT_FILE="signed-$SIGNATURE_ID.pdf"

curl -X GET "http://localhost:8081/api/v1/signatures/$SIGNATURE_ID/pdf" \
  --output "$OUTPUT_FILE"

echo "Saved signed PDF to: $OUTPUT_FILE"
