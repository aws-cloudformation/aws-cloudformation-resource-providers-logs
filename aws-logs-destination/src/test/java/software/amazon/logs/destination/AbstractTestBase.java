package software.amazon.logs.destination;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.Destination;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.ProxyClient;

public class AbstractTestBase {

    protected static final Credentials MOCK_CREDENTIALS;

    protected static final LoggerProxy logger;

    protected static final String TEST_DESTINATION_INPUT = "TestDestinationInput";

    protected static final String TEST_ACCESS_POLICY = "TestAccessPolicy";

    protected static final String TEST_ROLE_ARN = "TestRoleARN";

    protected static final String TEST_TARGET_ARN = "TestTargetARN";

    static {
        MOCK_CREDENTIALS = new Credentials("accessKey", "secretKey", "token");
        logger = new LoggerProxy();
    }

    static ProxyClient<CloudWatchLogsClient> MOCK_PROXY(final AmazonWebServicesClientProxy proxy, final CloudWatchLogsClient sdkClient) {
        return new ProxyClient<CloudWatchLogsClient>() {
            @Override
            public <RequestT extends AwsRequest, ResponseT extends AwsResponse> ResponseT injectCredentialsAndInvokeV2(
                RequestT request,
                Function<RequestT, ResponseT> requestFunction
            ) {
                return proxy.injectCredentialsAndInvokeV2(request, requestFunction);
            }

            @Override
            public <
                RequestT extends AwsRequest, ResponseT extends AwsResponse
            > ResponseInputStream<ResponseT> injectCredentialsAndInvokeV2InputStream(
                RequestT requestT,
                Function<RequestT, ResponseInputStream<ResponseT>> function
            ) {
                throw new UnsupportedOperationException();
            }

            @Override
            public <RequestT extends AwsRequest, ResponseT extends AwsResponse> ResponseBytes<ResponseT> injectCredentialsAndInvokeV2Bytes(
                RequestT requestT,
                Function<RequestT, ResponseBytes<ResponseT>> function
            ) {
                throw new UnsupportedOperationException();
            }

            @Override
            public <
                RequestT extends AwsRequest, ResponseT extends AwsResponse
            > CompletableFuture<ResponseT> injectCredentialsAndInvokeV2Async(
                RequestT request,
                Function<RequestT, CompletableFuture<ResponseT>> requestFunction
            ) {
                throw new UnsupportedOperationException();
            }

            @Override
            public <
                RequestT extends AwsRequest, ResponseT extends AwsResponse, IterableT extends SdkIterable<ResponseT>
            > IterableT injectCredentialsAndInvokeIterableV2(RequestT request, Function<RequestT, IterableT> requestFunction) {
                return proxy.injectCredentialsAndInvokeIterableV2(request, requestFunction);
            }

            @Override
            public CloudWatchLogsClient client() {
                return sdkClient;
            }
        };
    }

    protected Destination getTestDestination() {
        return getTestDestination(true);
    }

    protected Destination getTestDestination(String destinationName) {
        return getTestDestination(true, destinationName);
    }

    protected Destination getTestDestination(boolean withPolicy) {
        return getTestDestination(withPolicy, TEST_DESTINATION_INPUT);
    }

    protected Destination getTestDestination(boolean withPolicy, String destinationName) {
        return Destination
            .builder()
            .destinationName(destinationName)
            .accessPolicy(withPolicy ? TEST_ACCESS_POLICY : null)
            .roleArn(TEST_ROLE_ARN)
            .targetArn(TEST_TARGET_ARN)
            .build();
    }

    protected ResourceModel getTestResourceModel() {
        return getTestResourceModel(TEST_DESTINATION_INPUT);
    }

    protected ResourceModel getTestResourceModel(String destinationName) {
        return ResourceModel
            .builder()
            .destinationName(destinationName)
            .destinationPolicy(TEST_ACCESS_POLICY)
            .roleArn(TEST_ROLE_ARN)
            .targetArn(TEST_TARGET_ARN)
            .build();
    }
}
