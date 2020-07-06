package software.amazon.logs.destination;

import software.amazon.awssdk.services.cloudwatchlogs.model.CloudWatchLogsException;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeDestinationsResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class ListHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger) {
        ProgressEvent<ResourceModel, CallbackContext> progressEvent = null;

        try {
            DescribeDestinationsResponse awsResponse =
                    proxy.injectCredentialsAndInvokeV2(Translator.translateToListRequest(request.getNextToken()),
                            ClientBuilder.getClient()::describeDestinations);
            progressEvent = ProgressEvent.<ResourceModel, CallbackContext>builder().resourceModels(
                    Translator.translateFromListResponse(awsResponse))
                    .nextToken(awsResponse.nextToken())
                    .status(OperationStatus.SUCCESS)
                    .build();
        } catch (CloudWatchLogsException e) {
            Translator.translateException(e);
        }
        return progressEvent;
    }

}
