# Create Table
aws --endpoint="http://localhost:4566" dynamodb delete-table --region "sa-east-1" --table-name "player_history"
aws --endpoint="http://localhost:4566" dynamodb delete-table --region "sa-east-1" --table-name "player_profile"