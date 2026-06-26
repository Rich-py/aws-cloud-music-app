package com.amazonaws.samples;

import java.io.File;
import java.util.Iterator;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;


public class LoadMusicData {
    public static void main(String[] args) throws Exception {

        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                .withRegion(Regions.US_EAST_1).withCredentials(new ProfileCredentialsProvider("default"))
                .build();

        DynamoDB dynamoDB = new DynamoDB(client);

        Table table = dynamoDB.getTable("Music");

        JsonParser parser = new JsonFactory().createParser(new File("C:/Sem 3/Cloud Computing/A2/2026a2_songs.json"));

        JsonNode rootNode = new ObjectMapper().readTree(parser);
        Iterator<JsonNode> iter = rootNode.path("songs").iterator();

        ObjectNode currentNode;
            while (iter.hasNext()) {
                currentNode = (ObjectNode) iter.next();

                String title = currentNode.path("title").asText();
                String artist = currentNode.path("artist").asText();
                int year = currentNode.path("year").asInt();
                String album = currentNode.path("album").asText();
                String imageUrl = currentNode.path("img_url").asText();

                // Create unique sort key
                String songKey = artist + "#" + year + "#" + album;

                try {
                    table.putItem(new Item()
                            .withPrimaryKey("title", title, "song_key", songKey)
                            .withString("artist", artist)
                            .withNumber("year", year)
                            .withString("album", album)
                            .withString("image_url", imageUrl));

                    System.out.println("PutItem succeeded: " + artist + " - " + title);

                } catch (Exception e) {
                    System.err.println("Unable to add song: " + artist + " - " + songKey);
                    System.err.println(e.getMessage());
                    break;
                }
            }
            parser.close();
        }
    }
