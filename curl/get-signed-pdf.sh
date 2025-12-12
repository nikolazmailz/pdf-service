#!/bin/bash

# ID подписи (замени на нужный UUID)
SIGNATURE_ID="20dc0e17-3217-420f-8ac3-017649c0b3dd"

# Куда сохранить PDF
OUTPUT_FILE="signed-$SIGNATURE_ID.pdf"

curl -X GET "http://localhost:8081/api/v1/signatures/$SIGNATURE_ID/pdf" \
  --output "$OUTPUT_FILE"

echo "Saved signed PDF to: $OUTPUT_FILE"
