package com.badri.RideAllocation.service;

import com.badri.RideAllocation.model.DriverProfile;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;

import java.io.InputStream;
import java.util.List;

@Service
public class ScriptsService {

    private final DynamoDbTable<DriverProfile> driverProfileTable;
    private final ObjectMapper objectMapper;

    public ScriptsService(DynamoDbTable<DriverProfile> driverProfileTable,
                   ObjectMapper objectMapper) {
        this.driverProfileTable = driverProfileTable;
        this.objectMapper = objectMapper;
    }

    public void addDriverProfilesData() {
        try {
            InputStream inputStream =
                    getClass().getClassLoader().getResourceAsStream("drivers.json");

            if (inputStream == null) {
                throw new RuntimeException("drivers.json not found in resources folder");
            }

            List<DriverProfile> drivers =
                    objectMapper.readValue(
                            inputStream,
                            new TypeReference<List<DriverProfile>>() {}
                    );

            drivers.forEach(driverProfileTable::putItem);

            System.out.println("Successfully inserted " + drivers.size() + " drivers.");

        } catch (Exception e) {
            throw new RuntimeException("Failed to load driver data", e);
        }
    }
}