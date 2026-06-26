package com.amazonaws.samples;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;

public class AddUserData {
    public static void main(String[] args) throws Exception {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                .withRegion(Regions.US_EAST_1).withCredentials(new ProfileCredentialsProvider("default"))
                .build();

        DynamoDB dynamoDB = new DynamoDB(client);

        Table table = dynamoDB.getTable("login");


            try {
                System.out.println("Adding a new item...");
                table.putItem(new Item().withPrimaryKey("email", "s41127240@student.rmit.edu.au").withString("user_name", "ShaunDsouza0").withString("password", "012345"));
                table.putItem(new Item().withPrimaryKey("email", "s41127241@student.rmit.edu.au").withString("user_name", "ShaunDsouza1").withString("password", "123456"));
                table.putItem(new Item().withPrimaryKey("email", "s41127242@student.rmit.edu.au").withString("user_name", "ShaunDsouza2").withString("password", "234567"));
                table.putItem(new Item().withPrimaryKey("email", "s41127243@student.rmit.edu.au").withString("user_name", "ShaunDsouza3").withString("password", "901234"));
                table.putItem(new Item().withPrimaryKey("email", "s41451370@student.rmit.edu.au").withString("user_name", "VigneshRam0").withString("password", "345678"));
                table.putItem(new Item().withPrimaryKey("email", "s41239310@student.rmit.edu.au").withString("user_name", "AnirudhBashyam0").withString("password", "456789"));
                table.putItem(new Item().withPrimaryKey("email", "s41451371@student.rmit.edu.au").withString("user_name", "VigneshRam1").withString("password", "567890"));
                table.putItem(new Item().withPrimaryKey("email", "s41239311@student.rmit.edu.au").withString("user_name", "AnirudhBashyam1").withString("password", "678901"));
                table.putItem(new Item().withPrimaryKey("email", "s41344350@student.rmit.edu.au").withString("user_name", "RichardPaul0").withString("password", "789012"));
                table.putItem(new Item().withPrimaryKey("email", "s41344351@student.rmit.edu.au").withString("user_name", "RichardPaul1").withString("password", "890123"));
                System.out.println("10 records inserted successfully.");

        }
            catch (Exception e) {
            System.err.println("Unable to add user records.");
            System.err.println(e.getMessage());
        }

}
}
