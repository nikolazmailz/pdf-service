#!/bin/bash

# ID подписи (замени на нужный UUID)
SIGNATURE_ID="6556ce5c-2280-4466-bcfc-8cee38a9f62e"

# Куда сохранить PDF
OUTPUT_FILE="signed-$SIGNATURE_ID.pdf"

curl -X GET "http://localhost:8081/api/v1/signatures/$SIGNATURE_ID/pdf" \
  --output "$OUTPUT_FILE"

echo "Saved signed PDF to: $OUTPUT_FILE"
