#!/bin/bash

# ID подписи (замени на нужный UUID)
SIGNATURE_ID="316c3c8b-bebd-458a-8707-160626218d54"

# Куда сохранить PDF
OUTPUT_FILE="signed-$SIGNATURE_ID.pdf"

curl -X GET "http://localhost:8081/api/v1/signatures/$SIGNATURE_ID/pdf" \
  --output "$OUTPUT_FILE"

echo "Saved signed PDF to: $OUTPUT_FILE"
