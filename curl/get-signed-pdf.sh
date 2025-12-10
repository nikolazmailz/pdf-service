#!/bin/bash

# ID подписи (замени на нужный UUID)
SIGNATURE_ID="21696ced-65ea-4951-ad33-458a57659719"

# Куда сохранить PDF
OUTPUT_FILE="signed-$SIGNATURE_ID.pdf"

curl -X GET "http://localhost:8081/api/v1/signatures/$SIGNATURE_ID/pdf" \
  --output "$OUTPUT_FILE"

echo "Saved signed PDF to: $OUTPUT_FILE"
