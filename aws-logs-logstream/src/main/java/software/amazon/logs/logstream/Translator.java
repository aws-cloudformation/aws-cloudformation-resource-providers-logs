package software.amazon.logs.logstream;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.cloudwatchlogs.model.*;

import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException;
import software.amazon.cloudformation.exceptions.*;
import software.amazon.cloudformation.proxy.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class is a centralized placeholder for
 *  - api request construction
 *  - object translation to/from aws sdk
 *  - resource model construction for read/list handlers
 */

public class Translator {

  public static BaseHandlerException translateException(final AwsServiceException e) {
    if (e instanceof LimitExceededException) {
      return new CfnServiceLimitExceededException(e);
    }
    if (e instanceof OperationAbortedException) {
      return new CfnResourceConflictException(e);
    }
    if (e instanceof InvalidParameterException) {
      return new CfnInvalidRequestException(e);
    } else if (e instanceof ResourceNotFoundException) {
      return new CfnNotFoundException(e);
    } else if (e instanceof ServiceUnavailableException) {
      return new CfnServiceInternalErrorException(e);
    } else
    return new CfnGeneralServiceException(e);
  }

  static ResourceModel translateLogStream (final software.amazon.awssdk.services.cloudwatchlogs.model.LogStream logStream) {
    return ResourceModel.builder()
            .logStreamName(logStream.logStreamName())
            .build();
  }

  static software.amazon.awssdk.services.cloudwatchlogs.model.LogStream translateToSDK (final ResourceModel model) {
    return software.amazon.awssdk.services.cloudwatchlogs.model.LogStream.builder()
            .logStreamName(model.getLogStreamName())
            .build();
  }

  static CreateLogStreamRequest translateToCreateRequest(final ResourceModel model) {
    return CreateLogStreamRequest.builder()
            .logStreamName(model.getLogStreamName())
            .logGroupName(model.getLogGroupName())
            .build();
  }

  static DescribeLogStreamsRequest translateToReadRequest(final ResourceModel model) {
    return DescribeLogStreamsRequest.builder()
            .logStreamNamePrefix(model.getLogStreamName())
            .logGroupName(model.getLogGroupName())
            .build();

  }

  /**
   * Translates resource object from sdk into a resource model
   * @param awsResponse the aws service describe resource response
   * @return model resource model
   */
  static ResourceModel translateFromReadResponse(final DescribeLogStreamsResponse response, final ResourceModel model) {
    if(response != null && response.hasLogStreams()){
      LogStream logStream = response.logStreams().get(0);

      return ResourceModel.builder()
              .logGroupName(model.getLogGroupName())
              .logStreamName(logStream.logStreamName())
              .build();
    }
    return null;
  }

    static DeleteLogStreamRequest translateToDeleteRequest (final ResourceModel model){
      return DeleteLogStreamRequest.builder()
              .logGroupName(model.getLogGroupName())
              .logStreamName(model.getLogStreamName())
              .build();
    }

    static List<ResourceModel> translateFromListResponse (final DescribeLogStreamsResponse awsResponse, final ResourceModel model){
      return streamOfOrEmpty(awsResponse.logStreams())
              .map(logStream -> ResourceModel.builder()
                      .logStreamName(logStream.logStreamName())
                      .logGroupName(model.getLogGroupName())
                      .build())
              .collect(Collectors.toList());
    }

    static DescribeLogStreamsRequest translateToListRequest (final ResourceModel model, final String nextToken) {
      return DescribeLogStreamsRequest.builder()
              .logGroupName(model.getLogGroupName())
              .nextToken(nextToken)
              .limit(50)
              .build();
    }

    private static <T > Stream < T > streamOfOrEmpty( final Collection<T> collection){
      return Optional.ofNullable(collection)
              .map(Collection::stream)
              .orElseGet(Stream::empty);
    }
}

