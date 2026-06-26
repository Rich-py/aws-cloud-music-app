package com.amazonaws.samples;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.*;

import java.util.Arrays;

public class MusicCreateTable {
    public static void main(String[] args) throws Exception {

        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                .withRegion(Regions.US_EAST_1)
                .withCredentials(new ProfileCredentialsProvider("default"))
                .build();

        String tableName = "Music";

        try {
            System.out.println("Attempting to create table; please wait...");


            CreateTableRequest request = new CreateTableRequest()
                    .withTableName(tableName)


                    .withKeySchema(
                            new KeySchemaElement("title", KeyType.HASH),
                            new KeySchemaElement("song_key", KeyType.RANGE)
                    )


                    .withAttributeDefinitions(
                            new AttributeDefinition("title", ScalarAttributeType.S),
                            new AttributeDefinition("song_key", ScalarAttributeType.S),
                            new AttributeDefinition("artist", ScalarAttributeType.S),
                            new AttributeDefinition("year", ScalarAttributeType.N),
                            new AttributeDefinition("album", ScalarAttributeType.S)
                    )


                    .withGlobalSecondaryIndexes(
                            new GlobalSecondaryIndex()
                                    .withIndexName("artist-year-index")
                                    .withKeySchema(
                                            new KeySchemaElement("artist", KeyType.HASH),
                                            new KeySchemaElement("year", KeyType.RANGE)
                                    )
                                    .withProjection(new Projection().withProjectionType(ProjectionType.ALL))
                                    .withProvisionedThroughput(new ProvisionedThroughput(10L, 10L))
                    )


                    .withLocalSecondaryIndexes(
                            new LocalSecondaryIndex()
                                    .withIndexName("title-album-index")
                                    .withKeySchema(
                                            new KeySchemaElement("title", KeyType.HASH),
                                            new KeySchemaElement("album", KeyType.RANGE)
                                    )
                                    .withProjection(new Projection().withProjectionType(ProjectionType.ALL))
                    )

                    .withProvisionedThroughput(new ProvisionedThroughput(10L, 10L));

            client.createTable(request);

            System.out.println("Table creation request sent.");

        } catch (Exception e) {
            System.err.println("Unable to create table:");
            System.err.println(e.getMessage());
        }
    }
}