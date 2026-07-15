package com.badri.RideAllocation.config;

import com.badri.RideAllocation.model.*;
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
            return ddcEnhanced.table("ride", TableSchema.fromBean(Ride.class));
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    @Bean
    public DynamoDbTable<DriverProfile> driverProfileTable(DynamoDbEnhancedClient ddcEnhanced) {
        try {
            return ddcEnhanced.table("driver-profile", TableSchema.fromBean(DriverProfile.class));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    @Bean
    public DynamoDbTable<DriverRejectionEvents> driverRejectionEvents(DynamoDbEnhancedClient ddcEnhanced) {
        try {
            return ddcEnhanced.table("driver-rejection-events", TableSchema.fromBean(DriverRejectionEvents.class));
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    @Bean
    public DynamoDbTable<DailyRideAnalytics> dailyRideAnalytics(DynamoDbEnhancedClient ddcEnhanced) {
        try {
            return ddcEnhanced.table("daily-analytics", TableSchema.fromBean(DailyRideAnalytics.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    public DynamoDbTable<HourlyRideAnalytics> hourlyRideAnalytics(DynamoDbEnhancedClient ddcEnhanced) {
        try {
            return ddcEnhanced.table("hourly-analytics", TableSchema.fromBean(HourlyRideAnalytics.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    public DynamoDbTable<DailyDriverAnalytics> dailyDriverAnalytics(DynamoDbEnhancedClient ddcEnhanced) {
        try {
            return ddcEnhanced.table("daily-driver-analytics", TableSchema.fromBean(DailyDriverAnalytics.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    public DynamoDbTable<HourlyDriverAnalytics> hourlyDriverAnalytics(DynamoDbEnhancedClient ddcEnhanced) {
        try {
            return ddcEnhanced.table("hourly-driver-analytics", TableSchema.fromBean(HourlyDriverAnalytics.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    public SqsClient sqsClient() {
        try {
            return SqsClient.builder()
                    .endpointOverride(URI.create("http://localhost:4566"))
                    .region(Region.AP_SOUTH_1)
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create("test","test")
                    ))
                    .build();
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }
}
