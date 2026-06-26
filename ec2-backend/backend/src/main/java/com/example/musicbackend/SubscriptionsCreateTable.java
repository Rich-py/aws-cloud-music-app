package com.example.musicbackend;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;

import java.util.Arrays;

public class SubscriptionsCreateTable {

    public static void main(String[] args) throws Exception {
        String region = System.getenv().getOrDefault("AWS_REGION", Regions.US_EAST_1.getName());

        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                .withRegion(region)
                .build();

        DynamoDB dynamoDB = new DynamoDB(client);
        String tableName = "subscriptions";

        try {
            System.out.println("Creating subscriptions table...");

            Table table = dynamoDB.createTable(
                    tableName,
                    Arrays.asList(
                            new KeySchemaElement("email", KeyType.HASH),
                            new KeySchemaElement("song_id", KeyType.RANGE)
                    ),
                    Arrays.asList(
                            new AttributeDefinition("email", ScalarAttributeType.S),
                            new AttributeDefinition("song_id", ScalarAttributeType.S)
                    ),
                    new ProvisionedThroughput(5L, 5L)
            );

            table.waitForActive();
            System.out.println("Table created: " + table.getDescription().getTableStatus());

        } catch (Exception e) {
            System.err.println("Unable to create subscriptions table.");
            System.err.println(e.getMessage());
        }
    }
}
