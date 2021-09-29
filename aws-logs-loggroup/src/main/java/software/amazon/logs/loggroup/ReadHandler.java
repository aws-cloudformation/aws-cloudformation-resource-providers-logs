package software.amazon.logs.loggroup;

import software.amazon.awssdk.services.cloudwatchlogs.model.CloudWatchLogsException;
import software.amazon.awssdk.services.cloudwatchlogs.model.ListTagsLogGroupResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogGroupsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException;
import software.amazon.awssdk.services.cloudwatchlogs.model.LogGroup;

import java.util.Objects;

public class ReadHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();

        if (model == null || model.getLogGroupName() == null) {
            throwNotFoundException(model);
        }

        DescribeLogGroupsResponse response = null;
        LogGroup matchingLogGroup = null;
        String nextToken = null;
        // Keep paginating until requested log group is found
        do {
            try {
                response = proxy.injectCredentialsAndInvokeV2(Translator.translateToReadRequest(model, nextToken),
                        ClientBuilder.getClient()::describeLogGroups);
            } catch (final ResourceNotFoundException e) {
                throwNotFoundException(model);
            }

            matchingLogGroup = Translator.getMatchingLogGroup(response, model.getLogGroupName());

            // If log group found, break out of loop
            if (matchingLogGroup != null) {
                break;
            }

            nextToken = response.nextToken();
        } while (nextToken != null);

        // If paginated all log groups, still cannot find it
        if (matchingLogGroup == null) {
            throwNotFoundException(model);
        }

        ListTagsLogGroupResponse tagsResponse = null;
        try {
            tagsResponse = proxy.injectCredentialsAndInvokeV2(Translator.translateToListTagsLogGroupRequest(model.getLogGroupName()),
                    ClientBuilder.getClient()::listTagsLogGroup);
        } catch (final CloudWatchLogsException e) {
            if (Translator.ACCESS_DENIED_ERROR_CODE.equals(e.awsErrorDetails().errorCode())) {
                // fail silently, if there is no permission to list tags
                logger.log(e.getMessage());
            } else {
                throw e;
            }
        }

        ResourceModel modelFromReadResult = Translator.translateForReadResponse(matchingLogGroup, tagsResponse);

        return ProgressEvent.defaultSuccessHandler(modelFromReadResult);
    }

    private void throwNotFoundException(final ResourceModel model) {
        final ResourceModel nullSafeModel = model == null ? ResourceModel.builder().build() : model;
        throw new software.amazon.cloudformation.exceptions.ResourceNotFoundException(ResourceModel.TYPE_NAME,
            Objects.toString(nullSafeModel.getPrimaryIdentifier()));
    }
}
