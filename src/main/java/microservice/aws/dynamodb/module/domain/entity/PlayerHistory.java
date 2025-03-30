package microservice.aws.dynamodb.module.domain.entity;

import java.time.Instant;
import java.util.UUID;

import microservice.aws.dynamodb.module.application.request.ScoreDTO;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@DynamoDbBean
public class PlayerHistory {
    private String playerId;
    private UUID gameId;
    private Double score;
    private Instant createdAt;
    private String gameMode;

    public static PlayerHistory fromScore(String playerId, ScoreDTO scoreDto) {
        var playerHistory = new PlayerHistory();

        playerHistory.setPlayerId(playerId);
        playerHistory.setGameId(UUID.randomUUID());
        playerHistory.setScore(scoreDto.score());
        playerHistory.setCreatedAt(Instant.now());

        return playerHistory;
    }

    @DynamoDbPartitionKey
    @DynamoDbAttribute("player_id")
    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    @DynamoDbSortKey
    @DynamoDbAttribute("game_id")
    public UUID getGameId() {
        return gameId;
    }

    public void setGameId(UUID gameId) {
        this.gameId = gameId;
    }

    @DynamoDbAttribute("score")
    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    @DynamoDbAttribute("created_at")
    public Instant  getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant  createdAt) {
        this.createdAt = createdAt;
    }

    @DynamoDbAttribute("game_mode")
    public String getGameMode() {
        return gameMode;
    }

    public void setGameMode(String gameMode) {
        this.gameMode = gameMode;
    }
}