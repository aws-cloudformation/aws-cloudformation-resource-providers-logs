package software.amazon.logs.logstream;

import org.apache.commons.collections.CollectionUtils;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.CloudWatchLogsRequest;

import software.amazon.awssdk.services.cloudwatchlogs.model.*;

import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException;

import software.amazon.cloudformation.exceptions.*;

import software.amazon.cloudformation.proxy.*;

import static java.util.Objects.requireNonNull;

import com.amazonaws.event.request.Progress;
import com.amazonaws.services.kms.model.AlreadyExistsException;
import com.amazonaws.services.kms.model.LimitExceededException;

public abstract class BaseHandlerStd extends BaseHandler<CallbackContext> {

private final CloudWatchLogsClient cloudWatchLogsClient;
public static final int EVENTUAL_CONSISTENCY_DELAY_SECONDS = 10;
private final static String RESOURCE_ALREADY_EXISTS_EXCEPTION = "ResourceAlreadyExistsException";
private final static String RESOURCE_NOT_FOUND_EXCEPTION = "ResourceNotFoundException";


  protected BaseHandlerStd() {
    this(ClientBuilder.getClient());
  }

  protected BaseHandlerStd(CloudWatchLogsClient cloudWatchLogsClient) {
    this.cloudWatchLogsClient = requireNonNull(cloudWatchLogsClient);
  }

  private CloudWatchLogsClient getCloudWatchLogsClient() {
    return cloudWatchLogsClient;
  }

  @Override
  public final ProgressEvent<ResourceModel, CallbackContext> handleRequest(
    final AmazonWebServicesClientProxy proxy,
    final ResourceHandlerRequest<ResourceModel> request,
    final CallbackContext callbackContext,
    final Logger logger) {
    return handleRequest(
      proxy,
      request,
      callbackContext != null ? callbackContext : new CallbackContext(),
      proxy.newProxy(ClientBuilder::getClient),
      logger
    );
  }

  protected abstract ProgressEvent<ResourceModel, CallbackContext> handleRequest(
    final AmazonWebServicesClientProxy proxy,
    final ResourceHandlerRequest<ResourceModel> request,
    final CallbackContext callbackContext,
    final ProxyClient<CloudWatchLogsClient> proxyClient,
    final Logger logger);

  protected ProgressEvent<ResourceModel, CallbackContext> doesResourceExistwithName(final ProxyClient<CloudWatchLogsClient> proxyClient, final ResourceModel model, final CallbackContext callbackContext)
          throws AwsServiceException {
    final DescribeLogStreamsRequest translateToReadRequest = Translator.translateToReadRequest(model);
    final DescribeLogStreamsResponse response;
    try {
      response = proxyClient.injectCredentialsAndInvokeV2(translateToReadRequest, proxyClient.client()::describeLogStreams);
      if(CollectionUtils.isEmpty(response.logStreams())){
        // Log Stream does not exist so return InProgress Event
        return ProgressEvent.progress(model, callbackContext);
      }
      final LogStream logStream = response.logStreams().get(0);
      if(logStream.logStreamName().equals(model.getLogStreamName())){
        // Log Stream does exist so return Failed
        return ProgressEvent.failed(model, callbackContext, HandlerErrorCode.AlreadyExists, String.format("Log Stream Already Exists"));
      }
      return ProgressEvent.progress(model, callbackContext);
    } catch (final AwsServiceException e) {
      BaseHandlerException newException = Translator.translateException(e);
      if (newException instanceof CfnNotFoundException) {
        return ProgressEvent.progress(model, callbackContext);
      }
      throw e;
    }
  }

  protected void logExceptionDetails(Exception e, Logger logger, final String stackId) {
    logger.log(String.format("Stack with ID: %s got exception: %s Message: %s Cause: %s", stackId,
            e.toString(), e.getMessage(), e.getCause()));
  }

  protected void handleException(Exception e, Logger logger, final String stackId) {
    if (e instanceof InvalidParameterException) {
      logExceptionDetails(e, logger, stackId);
      throw new CfnInvalidRequestException(e);
    } else if (e instanceof ResourceNotFoundException) {
      logExceptionDetails(e, logger, stackId);
      throw new CfnNotFoundException(e);
    } else if (e instanceof ServiceUnavailableException) {
      logExceptionDetails(e, logger, stackId);
      throw new CfnServiceInternalErrorException(e);
    } else if (e instanceof AlreadyExistsException){
      logExceptionDetails(e, logger, stackId);
      throw new CfnAlreadyExistsException(e);
    } else if (e instanceof LimitExceededException){
      logExceptionDetails(e, logger, stackId);
      throw new CfnServiceLimitExceededException(e);
    } else if(e instanceof CloudWatchLogsException){
      throw new CfnGeneralServiceException(e);
    }
  }

  //  handleErrors
  protected ProgressEvent<ResourceModel, CallbackContext> handleError(
          final CloudWatchLogsRequest request,
          final Exception e,
          final ProxyClient<CloudWatchLogsClient> proxyClient,
          final ResourceModel resourceModel,
          final CallbackContext callbackContext) {

      BaseHandlerException ex = new CfnNotFoundException(e);
      if (e instanceof InvalidParameterException) {
        ex = new CfnInvalidRequestException(e);
      } else if (e instanceof ResourceNotFoundException) {
        ex = new CfnNotFoundException(e);
      } else if (e instanceof ServiceUnavailableException) {
        ex = new CfnServiceInternalErrorException(e);
      } else if (e instanceof AlreadyExistsException){
        ex = new CfnAlreadyExistsException(e);
      } else if (e instanceof LimitExceededException){
        ex = new CfnServiceLimitExceededException(e);
      } else if (e instanceof CloudWatchLogsException){
        ex = new CfnGeneralServiceException(e);
      }

    return ProgressEvent.failed(resourceModel, callbackContext, ex.getErrorCode(), ex.getMessage());
  }

  //  function to getErrorCode
//  protected static String getErrorCode(Exception e) {
//    if (e instanceof AwsServiceException) {
//      return ((AwsServiceException) e).awsErrorDetails().errorCode();
//    }
//    return e.getMessage();
//  }




}
