package com.example.musicbackend;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class SubscriptionController {

    private final Table subscriptionsTable;
    private final S3Service s3Service;

    public SubscriptionController(DynamoDB dynamoDB, S3Service s3Service) {
        this.subscriptionsTable = dynamoDB.getTable("subscriptions");
        this.s3Service = s3Service;
    }

    @GetMapping("/subscriptions")
    public ResponseEntity<?> getSubscriptions(@RequestParam String email) {
        email = clean(email);

        if (email.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.message("Missing email"));
        }

        QuerySpec spec = new QuerySpec()
                .withKeyConditionExpression("email = :email")
                .withValueMap(new ValueMap().withString(":email", email));

        ItemCollection<QueryOutcome> items = subscriptionsTable.query(spec);

        List<Map<String, Object>> subscriptions = new ArrayList<>();

        for (Item item : items) {
            subscriptions.add(toSubscriptionMap(item));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("subscriptions", subscriptions);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/subscriptions")
    public ResponseEntity<?> addSubscription(@RequestBody Map<String, Object> body) {
        String email = clean(asString(body.get("email")));
        String artist = clean(asString(body.get("artist")));
        String title = clean(asString(body.get("title")));
        String album = clean(asString(body.get("album")));
        String imageValue = clean(asString(body.get("image_url")));
        String titleYearAlbum = clean(asString(body.get("title_year_album")));

        int year = parseYear(body.get("year"));

        if (email.isEmpty() || artist.isEmpty() || title.isEmpty() || album.isEmpty() || year == 0) {
            return ResponseEntity.badRequest().body(ApiResponse.message("Missing subscription data"));
        }

        if (titleYearAlbum.isEmpty()) {
            titleYearAlbum = title + "#" + year + "#" + album;
        }

        String songId = artist + "#" + titleYearAlbum;

        /*
          This stores the image value received from the music result. If your frontend posts back
          a pre-signed URL, that URL will expire later. For a stronger implementation, send and store
          image_key separately. This backend still works best when Music.image_url stores object keys.
        */
        Item subscription = new Item()
                .withPrimaryKey("email", email, "song_key", songId)
                .withString("artist", artist)
                .withString("title", title)
                .withNumber("year", year)
                .withString("album", album)
                .withString("image_url", imageValue)
                .withString("title_year_album", titleYearAlbum);

        subscriptionsTable.putItem(new PutItemSpec().withItem(subscription));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.message("Subscription added"));
    }

    @DeleteMapping("/subscriptions")
    public ResponseEntity<?> removeSubscription(
            @RequestParam String email,
            @RequestParam String artist,
            @RequestParam String title,
            @RequestParam String year,
            @RequestParam String album,
            @RequestParam(required = false) String title_year_album
    ) {
        email = clean(email);
        artist = clean(artist);
        title = clean(title);
        year = clean(year);
        album = clean(album);
        title_year_album = clean(title_year_album);

        if (title_year_album.isEmpty()) {
            title_year_album = title + "#" + year + "#" + album;
        }

        String songId = artist + "#" + title_year_album;

        subscriptionsTable.deleteItem(new DeleteItemSpec()
                .withPrimaryKey("email", email, "song_key", songId));

        return ResponseEntity.ok(ApiResponse.message("Subscription removed"));
    }

    private Map<String, Object> toSubscriptionMap(Item item) {
        Map<String, Object> song = new HashMap<>();

        song.put("artist", item.getString("artist"));
        song.put("title", item.getString("title"));
        song.put("year", item.getInt("year"));
        song.put("album", item.getString("album"));
        String imageUrl = item.getString("image_url");
        if (imageUrl != null && !imageUrl.startsWith("http")) {
              imageUrl = "https://a1-24-music-images-2026.s3.amazonaws.com/artist-images/" + imageUrl;
            }

song.put("image_url", imageUrl);
        song.put("title_year_album", item.getString("title_year_album"));

        return song;
    }

    private int parseYear(Object value) {
        if (value == null) return 0;

        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception e) {
            return 0;
        }
    }

    private String asString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
