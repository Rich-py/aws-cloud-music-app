package com.example.musicbackend;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.ScanOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class MusicController {

    private final Table musicTable;
    private final S3Service s3Service;

    public MusicController(DynamoDB dynamoDB, S3Service s3Service) {
        this.musicTable = dynamoDB.getTable("Music");
        this.s3Service = s3Service;
    }

    @GetMapping("/music")
    public Map<String, Object> queryMusic(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String year,
            @RequestParam(required = false) String artist,
            @RequestParam(required = false) String album
    ) {
        title = clean(title);
        year = clean(year);
        artist = clean(artist);
        album = clean(album);

        List<Map<String, Object>> results;

        /*
          If artist is provided, use DynamoDB Query on the Music table partition key.
          Otherwise, use Scan because title/year/album alone are non-key attributes unless
          you extend this controller to use your GSI for title-based searches.
        */
        if (!artist.isEmpty()) {
            results = queryByArtistAndFilter(artist, title, year, album);
        } else {
            results = scanAndFilter(title, year, album);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("results", results);
        return response;
    }

    private List<Map<String, Object>> queryByArtistAndFilter(
            String artist,
            String title,
            String year,
            String album
    ) {
        QuerySpec spec = new QuerySpec()
                .withKeyConditionExpression("artist = :artist")
                .withValueMap(new ValueMap().withString(":artist", artist));

        ItemCollection<QueryOutcome> items = musicTable.query(spec);
        return filterItems(items, title, year, album);
    }

    private List<Map<String, Object>> scanAndFilter(
            String title,
            String year,
            String album
    ) {
        ScanSpec spec = new ScanSpec();
        ItemCollection<ScanOutcome> items = musicTable.scan(spec);
        return filterItems(items, title, year, album);
    }

    private List<Map<String, Object>> filterItems(
            Iterable<Item> items,
            String title,
            String year,
            String album
    ) {
        List<Map<String, Object>> results = new ArrayList<>();

        for (Item item : items) {
            if (!matches(item, title, year, album)) {
                continue;
            }

            results.add(toSongMap(item));
        }

        return results;
    }

    private boolean matches(Item item, String title, String year, String album) {
        if (!title.isEmpty() && !equalsIgnoreCase(item.getString("title"), title)) {
            return false;
        }

        if (!year.isEmpty() && !String.valueOf(item.getInt("year")).equals(year)) {
            return false;
        }

        if (!album.isEmpty() && !equalsIgnoreCase(item.getString("album"), album)) {
            return false;
        }

        return true;
    }

    private Map<String, Object> toSongMap(Item item) {
        Map<String, Object> song = new HashMap<>();

        song.put("artist", item.getString("artist"));
        song.put("title", item.getString("title"));
        song.put("year", item.getInt("year"));
        song.put("album", item.getString("album"));

        /*
          DynamoDB may contain either:
          - image_url = object key, e.g. "Taylor_Swift.jpg"  Recommended
          - image_url = S3 URL, e.g. "https://bucket.s3.amazonaws.com/Taylor_Swift.jpg"
          S3Service converts either value into a temporary pre-signed URL.
        */
        String imageUrl = item.getString("image_url");
  
        if (imageUrl != null && !imageUrl.startsWith("http")) {
             imageUrl = "https://a1-24-music-images-2026.s3.amazonaws.com/artist-images/" + imageUrl;
         }
         song.put("image_url", imageUrl);

        if (item.hasAttribute("title_year_album")) {
            song.put("title_year_album", item.getString("title_year_album"));
        } else {
            song.put("title_year_album",
                    item.getString("title") + "#" + item.getInt("year") + "#" + item.getString("album"));
        }

        return song;
    }

    private boolean equalsIgnoreCase(String a, String b) {
        if (a == null || b == null) return false;
        return a.trim().equalsIgnoreCase(b.trim());
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
