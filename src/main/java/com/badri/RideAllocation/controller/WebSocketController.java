package com.badri.RideAllocation.controller;

import com.badri.RideAllocation.dto.RideResponseDto;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import tools.jackson.databind.ObjectMapper;

@Controller
public class WebSocketController {

    private final ObjectMapper objectMapper;
    private final SqsClient sqsClient;

    public WebSocketController(ObjectMapper objectMapper, SqsClient sqsClient) {
        this.objectMapper = objectMapper;
        this.sqsClient = sqsClient;
    }

    @MessageMapping("/ride-response")
    @SendTo("/topic/")
    public void handleRideRequest(RideResponseDto dto) {
        System.out.println("message from driver " + dto.toString());

        String jsonPayload = objectMapper.writeValueAsString(dto);
        String queueUrl = "http://sqs.ap-south-1.localhost.localstack.cloud:4566/000000000000/ride-process";

        SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                .messageBody(jsonPayload)
                .queueUrl(queueUrl)
                .build();

        sqsClient.sendMessage(sendMessageRequest);

        System.out.println("ride response sent to sqs queue");
    }
}
