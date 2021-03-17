package software.amazon.logs.querydefinition;

import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteQueryDefinitionRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeQueryDefinitionsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutQueryDefinitionRequest;

final class Translator {

    static PutQueryDefinitionRequest translateToCreateRequest(final ResourceModel model) {
        return PutQueryDefinitionRequest.builder()
                .name(model.getName())
                .queryString(model.getQueryString())
                .queryDefinitionId(model.getQueryDefinitionId())
                .logGroupNames(model.getLogGroupNames())
                .build();
    }

    static DeleteQueryDefinitionRequest translateToDeleteRequest(final ResourceModel model) {
        return DeleteQueryDefinitionRequest.builder()
                .queryDefinitionId(model.getQueryDefinitionId())
                .build();
    }

    static DescribeQueryDefinitionsRequest translateToReadRequest(final ResourceModel model, final String nextToken) {
        return DescribeQueryDefinitionsRequest.builder()
                .queryDefinitionNamePrefix(model.getName())
                .nextToken(nextToken)
                .maxResults(1000)
                .build();
    }

    static DescribeQueryDefinitionsRequest translateToListRequest(final String nextToken) {
        return DescribeQueryDefinitionsRequest.builder()
                .nextToken(nextToken)
                .maxResults(1000)
                .build();
    }
}
