package software.amazon.logs.destination;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.cloudwatchlogs.model.CloudWatchLogsException;
import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteDestinationRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeDestinationsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeDestinationsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.InvalidParameterException;
import software.amazon.awssdk.services.cloudwatchlogs.model.OperationAbortedException;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutDestinationPolicyRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutDestinationRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException;
import software.amazon.awssdk.services.cloudwatchlogs.model.ServiceUnavailableException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnResourceConflictException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Translator {

    static PutDestinationRequest translateToPutDestinationRequest(final ResourceModel model) {
        return PutDestinationRequest.builder()
                .destinationName(model.getDestinationName())
                .roleArn(model.getRoleArn())
                .targetArn(model.getTargetArn())
                .build();
    }

    static DescribeDestinationsRequest translateToReadRequest(final ResourceModel model) {
        return DescribeDestinationsRequest.builder()
                .destinationNamePrefix(model.getDestinationName())
                .build();
    }

    static ResourceModel translateFromReadResponse(final DescribeDestinationsResponse awsResponse) {
        return streamOfOrEmpty(awsResponse.destinations()).findAny()
                .map(Translator::translateLogDestination)
                .orElse(null);
    }

    static DeleteDestinationRequest translateToDeleteRequest(final ResourceModel model) {
        return DeleteDestinationRequest.builder()
                .destinationName(model.getDestinationName())
                .build();
    }

    static PutDestinationPolicyRequest translateToPutDestinationPolicyRequest(final ResourceModel model) {
        return PutDestinationPolicyRequest.builder()
                .destinationName(model.getDestinationName())
                .accessPolicy(model.getDestinationPolicy())
                .build();
    }

    static DescribeDestinationsRequest translateToListRequest(final String nextToken) {
        return DescribeDestinationsRequest.builder()
                .limit(50)
                .nextToken(nextToken)
                .build();
    }

    static List<ResourceModel> translateFromListResponse(final DescribeDestinationsResponse awsResponse) {
        return streamOfOrEmpty(awsResponse.destinations()).map(destination -> ResourceModel.builder()
                .destinationName(destination.destinationName())
                .destinationPolicy(destination.accessPolicy())
                .roleArn(destination.roleArn())
                .targetArn(destination.targetArn())
                .build())
                .collect(Collectors.toList());
    }

    private static ResourceModel translateLogDestination(
            final software.amazon.awssdk.services.cloudwatchlogs.model.Destination destination) {
        return ResourceModel.builder()
                .arn(destination.arn())
                .roleArn(destination.roleArn())
                .destinationName(destination.destinationName())
                .destinationPolicy(destination.accessPolicy())
                .targetArn(destination.targetArn())
                .build();
    }

    private static <T> Stream<T> streamOfOrEmpty(final Collection<T> collection) {
        return Optional.ofNullable(collection)
                .map(Collection::stream)
                .orElseGet(Stream::empty);
    }

    static void translateException(AwsServiceException exception) {
        if (exception instanceof InvalidParameterException) {
            throw new CfnInvalidRequestException(ResourceModel.TYPE_NAME, exception);
        } else if (exception instanceof ServiceUnavailableException) {
            throw new CfnServiceInternalErrorException(ResourceModel.TYPE_NAME, exception);
        } else if (exception instanceof OperationAbortedException) {
            throw new CfnResourceConflictException(exception);
        } else if (exception instanceof ResourceNotFoundException) {
            throw new CfnNotFoundException(exception);
        } else if (exception instanceof CloudWatchLogsException) {
            throw new CfnGeneralServiceException(exception);
        }
    }

}
