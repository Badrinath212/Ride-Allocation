package com.badri.RideAllocation.config;

import com.badri.RideAllocation.model.Ride;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.net.URI;

@Configuration
public class AwsConfig {
    @Bean
    public DynamoDbClient getDynamoDbClientConnection() {
        try {
            DynamoDbClient ddc = DynamoDbClient.builder()
                    .endpointOverride(URI.create("http://localhost:4566"))
                    .region(Region.AP_SOUTH_1)
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create("test","test")
                    ))
                    .build();
            return ddc;
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }
    @Bean
    public DynamoDbEnhancedClient getDynamoDbEnhancedClient(DynamoDbClient ddc) {
        try {
            DynamoDbEnhancedClient ddcEnhanced = DynamoDbEnhancedClient.builder()
                    .dynamoDbClient(ddc)
                    .build();
            return ddcEnhanced;
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    @Bean
    public DynamoDbTable<Ride> rideTable(DynamoDbEnhancedClient ddcEnhanced) {
        try {
            DynamoDbTable<Ride> rideTable = ddcEnhanced.table("Ride", TableSchema.fromBean(Ride.class));
            return rideTable;
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    @Bean
    public SqsClient sqsClient() {
        try {
            SqsClient sqsClient = SqsClient.builder()
                    .endpointOverride(URI.create("http://localhost:4566"))
                    .region(Region.AP_SOUTH_1)
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create("test","test")
                    ))
                    .build();
            return sqsClient;
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }
}
