# Create Table
aws --endpoint="http://localhost:4566" dynamodb create-table \
  --region "sa-east-1" \
  --table-name "player_history" \
  --attribute-definitions \
    "AttributeName=player_id,AttributeType=S" \
    "AttributeName=game_id,AttributeType=S" \
  --key-schema \
    "AttributeName=player_id,KeyType=HASH" \
    "AttributeName=game_id,KeyType=RANGE" \
  --provisioned-throughput \
      "ReadCapacityUnits=5,WriteCapacityUnits=5"

aws --endpoint="http://localhost:4566" dynamodb create-table \
  --region "sa-east-1" \
  --table-name "player_profile" \
  --attribute-definitions \
    "AttributeName=player_id,AttributeType=S" \
    "AttributeName=status,AttributeType=S" \
  --key-schema \
    "AttributeName=player_id,KeyType=HASH" \
  --provisioned-throughput \
      "ReadCapacityUnits=5,WriteCapacityUnits=5"