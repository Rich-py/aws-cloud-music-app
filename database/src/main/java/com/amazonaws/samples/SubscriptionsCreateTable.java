package com.amazonaws.samples;

import java.util.Arrays;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.*;

public class SubscriptionsCreateTable {
    public static void main(String[] args) throws Exception {

        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                .withRegion(Regions.US_EAST_1)
                .withCredentials(new ProfileCredentialsProvider("default"))
                .build();

        DynamoDB dynamoDB = new DynamoDB(client);

        String tableName = "subscriptions";

        try {
            System.out.println("Attempting to create subscriptions table; please wait...");


            Table table = dynamoDB.createTable(tableName,
                    Arrays.asList(
                            new KeySchemaElement("email", KeyType.HASH),
                            new KeySchemaElement("song_key", KeyType.RANGE)
                    ),
                    Arrays.asList(
                            new AttributeDefinition("email", ScalarAttributeType.S),
                            new AttributeDefinition("song_key", ScalarAttributeType.S)
                    ),
                    new ProvisionedThroughput(10L, 10L));

            table.waitForActive();
            System.out.println("Success. Table status: " + table.getDescription().getTableStatus());

        } catch (Exception e) {
            System.err.println("Unable to create subscriptions table.");
            System.err.println(e.getMessage());
        }
    }
}