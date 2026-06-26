package com.example.musicbackend;

import com.amazonaws.HttpMethod;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.Date;

@Service
public class S3Service {

    private final AmazonS3 s3Client;
    private final String bucketName;

    public S3Service() {
        String region = System.getenv().getOrDefault("AWS_REGION", Regions.US_EAST_1.getName());
        this.bucketName = System.getenv("S3_BUCKET_NAME");

        if (this.bucketName == null || this.bucketName.trim().isEmpty()) {
            throw new IllegalStateException("S3_BUCKET_NAME environment variable is not set.");
        }

        this.s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .build();
    }

    public String generatePresignedUrl(String imageValue) {
        if (imageValue == null || imageValue.trim().isEmpty()) {
            return "";
        }

        String objectKey = extractObjectKey(imageValue.trim());

        Date expiration = new Date();
        expiration.setTime(expiration.getTime() + (1000L * 60L * 15L)); // 15 minutes

        URL url = s3Client.generatePresignedUrl(
                bucketName,
                objectKey,
                expiration,
                HttpMethod.GET
        );

        return url.toString();
    }

    /*
      Supports either:
      1. DynamoDB storing only the object key, e.g. "Taylor_Swift.jpg"
      2. DynamoDB storing an S3 URL, e.g. "https://bucket.s3.amazonaws.com/Taylor_Swift.jpg"

      Best practice for your report: store object keys in DynamoDB, not public URLs.
    */
    private String extractObjectKey(String imageValue) {
        if (!imageValue.startsWith("http://") && !imageValue.startsWith("https://")) {
            return imageValue;
        }

        try {
            URL url = new URL(imageValue);
            String path = url.getPath();

            if (path.startsWith("/")) {
                path = path.substring(1);
            }

            return path;

        } catch (Exception e) {
            return imageValue;
        }
    }
}
