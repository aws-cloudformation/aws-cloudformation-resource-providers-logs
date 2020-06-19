package software.amazon.logs.destination;

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

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

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

    static ProxyClient<CloudWatchLogsClient> MOCK_PROXY(final AmazonWebServicesClientProxy proxy,
            final CloudWatchLogsClient sdkClient) {
        return new ProxyClient<CloudWatchLogsClient>() {

            @Override
            public <RequestT extends AwsRequest, ResponseT extends AwsResponse> ResponseT injectCredentialsAndInvokeV2(
                    RequestT request,
                    Function<RequestT, ResponseT> requestFunction) {
                return proxy.injectCredentialsAndInvokeV2(request, requestFunction);
            }

            @Override
            public <RequestT extends AwsRequest, ResponseT extends AwsResponse> ResponseInputStream<ResponseT> injectCredentialsAndInvokeV2InputStream(
                    RequestT requestT,
                    Function<RequestT, ResponseInputStream<ResponseT>> function) {
                throw new UnsupportedOperationException();
            }

            @Override
            public <RequestT extends AwsRequest, ResponseT extends AwsResponse> ResponseBytes<ResponseT> injectCredentialsAndInvokeV2Bytes(
                    RequestT requestT,
                    Function<RequestT, ResponseBytes<ResponseT>> function) {
                throw new UnsupportedOperationException();
            }

            @Override
            public <RequestT extends AwsRequest, ResponseT extends AwsResponse> CompletableFuture<ResponseT> injectCredentialsAndInvokeV2Async(
                    RequestT request,
                    Function<RequestT, CompletableFuture<ResponseT>> requestFunction) {
                throw new UnsupportedOperationException();
            }

            @Override
            public <RequestT extends AwsRequest, ResponseT extends AwsResponse, IterableT extends SdkIterable<ResponseT>> IterableT injectCredentialsAndInvokeIterableV2(
                    RequestT request,
                    Function<RequestT, IterableT> requestFunction) {
                return proxy.injectCredentialsAndInvokeIterableV2(request, requestFunction);
            }

            @Override
            public CloudWatchLogsClient client() {
                return sdkClient;
            }

        };
    }

    protected Destination getTestDestination() {
        return Destination.builder()
                .destinationName(TEST_DESTINATION_INPUT)
                .accessPolicy(TEST_ACCESS_POLICY)
                .roleArn(TEST_ROLE_ARN)
                .targetArn(TEST_TARGET_ARN)
                .build();
    }

    protected ResourceModel getTestResourceModel() {
        return ResourceModel.builder()
                .destinationName(TEST_DESTINATION_INPUT)
                .destinationPolicy(TEST_ACCESS_POLICY)
                .roleArn(TEST_ROLE_ARN)
                .targetArn(TEST_TARGET_ARN)
                .build();
    }

}
