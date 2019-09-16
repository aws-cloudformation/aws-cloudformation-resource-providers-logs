package com.amazonaws.logs.logstream;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.cloudwatchlogs.model.CreateLogStreamRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteLogStreamRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogStreamsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogStreamsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.LogStream;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class TranslatorTest {
    private static final ResourceModel RESOURCE_MODEL = ResourceModel.builder()
        .logStreamName("Stream")
        .logGroupName("LogGroup")
        .build();

    private static final LogStream LOG_STREAM = LogStream.builder()
        .logStreamName("Stream")
        .build();

    @Test
    public void translateToReadRequest() {
        final DescribeLogStreamsRequest request = DescribeLogStreamsRequest.builder()
            .logStreamNamePrefix("Stream")
            .logGroupName("LogGroup")
            .build();

        assertThat(Translator.translateToReadRequest(RESOURCE_MODEL)).isEqualToComparingFieldByField(request);
    }

    @Test
    public void translateToListRequest() {
        final DescribeLogStreamsRequest request = DescribeLogStreamsRequest.builder()
            .limit(50)
            .logGroupName("LogGroup")
            .nextToken("token")
            .build();

        assertThat(Translator.translateToListRequest(RESOURCE_MODEL, "token")).isEqualToComparingFieldByField(request);
    }

    @Test
    public void translateToCreateRequest() {
        final CreateLogStreamRequest request = CreateLogStreamRequest.builder()
            .logStreamName("Stream")
            .logGroupName("LogGroup")
            .build();

        assertThat(Translator.translateToCreateRequest(RESOURCE_MODEL)).isEqualToComparingFieldByField(request);
    }

    @Test
    public void translateToDeleteRequest() {
        final DeleteLogStreamRequest request = DeleteLogStreamRequest.builder()
            .logStreamName("Stream")
            .logGroupName("LogGroup")
            .build();

        assertThat(Translator.translateToDeleteRequest(RESOURCE_MODEL)).isEqualToComparingFieldByField(request);
    }

    @Test
    public void translateForRead() {
        final DescribeLogStreamsResponse response = DescribeLogStreamsResponse.builder()
            .logStreams(Collections.singletonList(LOG_STREAM))
            .build();

        assertThat(Translator.translateForRead(response, "LogGroup"))
            .isEqualToComparingFieldByField(RESOURCE_MODEL);
    }

    @Test
    public void translateForList() {
        final DescribeLogStreamsResponse response = DescribeLogStreamsResponse.builder()
            .logStreams(Collections.singletonList(LOG_STREAM))
            .build();

        assertThat(Translator.translateForList(response, "LogGroup"))
            .containsExactly(RESOURCE_MODEL);
    }

    @Test
    public void buildResourceAlreadyExistsErrorMessage() {
        final String expected = "Resource of type 'AWS::Logs::LogStream' with identifier 'ID' already exists.";
        assertThat(Translator.buildResourceAlreadyExistsErrorMessage("ID")).isEqualTo(expected);
    }

    @Test
    public void buildResourceDoesNotExistErrorMessage() {
        final String expected = "Resource of type 'AWS::Logs::LogStream' with identifier 'ID' was not found.";
        assertThat(Translator.buildResourceDoesNotExistErrorMessage("ID")).isEqualTo(expected);
    }
}
