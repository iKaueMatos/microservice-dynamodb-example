package microservice.aws.dynamodb.module.infra.http.controller;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import microservice.aws.dynamodb.module.application.request.ScoreDTO;
import microservice.aws.dynamodb.module.domain.entity.PlayerHistory;
import microservice.aws.dynamodb.module.domain.entity.PlayerProfile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.awspring.cloud.dynamodb.DynamoDbTemplate;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

@RestController
@RequestMapping("/v1/players")
public class PlayerController {
    private final DynamoDbTemplate dynamoDbTemplate;

    public PlayerController(DynamoDbTemplate dynamoDbTemplate) {
        this.dynamoDbTemplate = dynamoDbTemplate;
    }

    @PostMapping("/{playerId}/games")
    public ResponseEntity<Void> createGame(@PathVariable("playerId") String playerId, @RequestBody ScoreDTO scoreDto) {
        // Limit the number of games a player can play per day
        var today = Instant.now().truncatedTo(ChronoUnit.DAYS);
        var key = Key.builder().partitionValue(playerId).build();
        var conditional = QueryConditional.keyEqualTo(key);

        var gamesToday = dynamoDbTemplate.query(QueryEnhancedRequest.builder()
                .queryConditional(conditional).build(), PlayerHistory.class)
                .items().stream()
                .filter(game -> !game.getCreatedAt().isBefore(today))
                .count();

        if (gamesToday >= 5) {
            return ResponseEntity.status(429).build();
        }

        var playerHistory = PlayerHistory.fromScore(playerId, scoreDto);
        playerHistory.setGameMode("casual");
        dynamoDbTemplate.save(playerHistory);
        return ResponseEntity.status(201).build();
    }

    @GetMapping("/{playerId}/games")
    public ResponseEntity<List<PlayerHistory>> listGames(@PathVariable("playerId") String playerId) {

        var key = Key.builder().partitionValue(playerId).build();

        var conditional = QueryConditional.keyEqualTo(key);

        var playerHistoryList = dynamoDbTemplate.query(QueryEnhancedRequest.builder()
                        .queryConditional(conditional).build(),
                PlayerHistory.class);

        return ResponseEntity.ok(playerHistoryList.items().stream().toList());

    }

    @GetMapping("/{playerId}/games/{gameId}")
    public ResponseEntity<PlayerHistory> getById(@PathVariable("playerId") String playerId,
                                                 @PathVariable("gameId") String gameId) {
        var user = dynamoDbTemplate.load(Key.builder()
                        .partitionValue(playerId)
                        .sortValue(gameId)
                        .build(), PlayerHistory.class);

        return user == null ?
                ResponseEntity.notFound().build() : ResponseEntity.ok(user);
    }

    @DeleteMapping("/{playerId}/games/{gameId}")
    public ResponseEntity<Void> delete(@PathVariable("playerId") String playerId,
                                       @PathVariable("gameId") String gameId) {
        var key = Key.builder()
                .partitionValue(playerId)
                .sortValue(gameId)
                .build();

        var player = dynamoDbTemplate.load(key, PlayerHistory.class);

        if (player == null) {
            return ResponseEntity.notFound().build();
        }

        dynamoDbTemplate.delete(key, PlayerHistory.class);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{playerId}/games/{gameId}")
    public ResponseEntity<Void> update(@PathVariable("playerId") String playerId,
                                       @PathVariable("gameId") String gameId,
                                       @RequestBody ScoreDTO scoreDto) {
        var key = Key.builder()
                .partitionValue(playerId)
                .sortValue(gameId)
                .build();

        var player = dynamoDbTemplate.load(key, PlayerHistory.class);
        if (player == null) {
            return ResponseEntity.notFound().build();
        }

        player.setScore(scoreDto.score());
        dynamoDbTemplate.save(player);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/profiles")
    public ResponseEntity<Void> createProfile(@RequestBody PlayerProfile profile) {
        var existingProfiles = dynamoDbTemplate.query(QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(Key.builder().partitionValue(profile.getPlayerId()).build()))
                .build(), PlayerProfile.class);

        boolean emailExists = existingProfiles.items().stream()
                .anyMatch(existingProfile -> existingProfile.getEmail().equalsIgnoreCase(profile.getEmail()));

        if (emailExists) {
            return ResponseEntity.status(409).build();
        }

        profile.setRegistrationDate(Instant.now());
        profile.setStatus("active");

        dynamoDbTemplate.save(profile);
        return ResponseEntity.status(201).build();
    }

    @GetMapping("/profiles/{playerId}")
    public ResponseEntity<PlayerProfile> getProfile(@PathVariable("playerId") String playerId) {
        var profile = dynamoDbTemplate.load(Key.builder().partitionValue(playerId).build(), PlayerProfile.class);
        return profile == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(profile);
    }

    @PutMapping("/profiles/{playerId}")
    public ResponseEntity<Void> updateProfile(@PathVariable("playerId") String playerId, @RequestBody PlayerProfile updatedProfile) {
        var profile = dynamoDbTemplate.load(Key.builder().partitionValue(playerId).build(), PlayerProfile.class);
        if (profile == null) {
            return ResponseEntity.notFound().build();
        }

        updatedProfile.setPlayerId(playerId);
        dynamoDbTemplate.save(updatedProfile);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/profiles/{playerId}")
    public ResponseEntity<Void> deleteProfile(@PathVariable("playerId") String playerId) {
        var profile = dynamoDbTemplate.load(Key.builder().partitionValue(playerId).build(), PlayerProfile.class);
        if (profile == null) {
            return ResponseEntity.notFound().build();
        }

        dynamoDbTemplate.delete(profile);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/profiles/{playerId}/deactivate")
    public ResponseEntity<Void> deactivateProfile(@PathVariable("playerId") String playerId) {
        var profile = dynamoDbTemplate.load(Key.builder().partitionValue(playerId).build(), PlayerProfile.class);
        if (profile == null) {
            return ResponseEntity.notFound().build();
        }

        profile.setStatus("banned");
        dynamoDbTemplate.save(profile);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{playerId}/games/filter")
    public ResponseEntity<List<PlayerHistory>> filterGames(@PathVariable("playerId") String playerId,
                                                           @RequestParam(required = false) Double minScore,
                                                           @RequestParam(required = false) Double maxScore,
                                                           @RequestParam(required = false) Instant startDate,
                                                           @RequestParam(required = false) Instant endDate
    ) {
        var key = Key.builder().partitionValue(playerId).build();
        var conditional = QueryConditional.keyEqualTo(key);

        var playerHistoryList = dynamoDbTemplate.query(QueryEnhancedRequest.builder()
                        .queryConditional(conditional).build(),
                PlayerHistory.class);

        var filteredList = playerHistoryList.items().stream()
                .filter(history -> (minScore == null || history.getScore() >= minScore) &&
                                   (maxScore == null || history.getScore() <= maxScore) &&
                                   (startDate == null || !history.getCreatedAt().isBefore(startDate)) &&
                                   (endDate == null || !history.getCreatedAt().isAfter(endDate)))
                .toList();

        return ResponseEntity.ok(filteredList);
    }

    @GetMapping("/{playerId}/games/top-scores")
    public ResponseEntity<List<PlayerHistory>> getTopScores(@PathVariable("playerId") String playerId) {
        var key = Key.builder().partitionValue(playerId).build();
        var conditional = QueryConditional.keyEqualTo(key);

        var playerHistoryList = dynamoDbTemplate.query(QueryEnhancedRequest.builder()
                        .queryConditional(conditional).build(),
                PlayerHistory.class);

        var topScores = playerHistoryList.items().stream()
                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                .limit(10)
                .toList();

        return ResponseEntity.ok(topScores);
    }

    @PostMapping
    public ResponseEntity<Void> createPlayer(@RequestBody PlayerProfile player) {
        var key = Key.builder().partitionValue(player.getPlayerId()).build();

        var existingPlayer = dynamoDbTemplate.load(Key.builder().partitionValue(player.getPlayerId()).build(), PlayerProfile.class);
        if (existingPlayer != null) {
            return ResponseEntity.status(409).build();
        }

        dynamoDbTemplate.save(player);
        return ResponseEntity.status(201).build();
    }
}