package com.example.musicbackend;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AwsConfig {

    @Bean
    public AmazonDynamoDB amazonDynamoDB() {
        String region = System.getenv().getOrDefault("AWS_REGION", Regions.US_EAST_1.getName());

        return AmazonDynamoDBClientBuilder.standard()
                .withRegion(region)
                .build();
    }

    @Bean
    public DynamoDB dynamoDB(AmazonDynamoDB client) {
        return new DynamoDB(client);
    }
}
