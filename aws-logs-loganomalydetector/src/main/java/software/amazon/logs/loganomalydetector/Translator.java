package software.amazon.logs.loganomalydetector;

import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.CreateLogAnomalyDetectorRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.ListLogAnomalyDetectorsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.ListLogAnomalyDetectorsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.GetLogAnomalyDetectorRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.GetLogAnomalyDetectorResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.UpdateLogAnomalyDetectorRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteLogAnomalyDetectorRequest;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.HashSet;

/**
 * This class is a centralized placeholder for
 * - api request construction
 * - object translation to/from aws sdk
 * - resource model construction for read/list handlers
 */

public class Translator {

    /**
     * Request to create a resource
     *
     * @param model resource model
     * @return awsRequest the aws service request to create a resource
     */
    static CreateLogAnomalyDetectorRequest translateToCreateRequest(final ResourceModel model) {
        return CreateLogAnomalyDetectorRequest.builder()
                .logGroupArnList(model.getLogGroupArnList())
                .detectorName(model.getDetectorName())
                .evaluationFrequency(model.getEvaluationFrequency())
                .anomalyVisibilityTime(model.getAnomalyVisibilityTime() == null ? null : model.getAnomalyVisibilityTime().longValue())
                .filterPattern(model.getFilterPattern())
                .kmsKeyId(model.getKmsKeyId())
                .build();
    }

    /**
     * Request to read a resource
     *
     * @param model resource model
     * @return awsRequest the aws service request to describe a resource
     */
    static GetLogAnomalyDetectorRequest translateToReadRequest(final ResourceModel model) {
        return GetLogAnomalyDetectorRequest.builder()
                .anomalyDetectorArn(model.getAnomalyDetectorArn())
                .build();
    }

    /**
     * Translates resource object from sdk into a resource model
     *
     * @param awsResponse the aws service describe resource response
     * @return model resource model
     */
    static ResourceModel translateFromReadResponse(final GetLogAnomalyDetectorResponse awsResponse, String arn) {
        Set<String> logGroupArnList = new HashSet<>(awsResponse.logGroupArnList());

        return ResourceModel.builder()
                .anomalyDetectorArn(arn)
                .kmsKeyId(awsResponse.kmsKeyId())
                .detectorName(awsResponse.detectorName())
                .logGroupArnList(logGroupArnList)
                .filterPattern(awsResponse.filterPattern())
                .anomalyVisibilityTime((double) awsResponse.anomalyVisibilityTime())
                .evaluationFrequency(awsResponse.evaluationFrequency().toString())
                .anomalyDetectorStatus(awsResponse.anomalyDetectorStatus().toString())
                .creationTimeStamp((double) awsResponse.creationTimeStamp())
                .lastModifiedTimeStamp((double) awsResponse.lastModifiedTimeStamp())
                .build();
    }

    /**
     * Request to delete a resource
     *
     * @param model resource model
     * @return awsRequest the aws service request to delete a resource
     */
    static DeleteLogAnomalyDetectorRequest translateToDeleteRequest(final ResourceModel model) {
        return DeleteLogAnomalyDetectorRequest.builder()
                .anomalyDetectorArn(model.getAnomalyDetectorArn())
                .build();
    }

    /**
     * Request to update properties of a previously created resource
     *
     * @param model resource model
     * @return awsRequest the aws service request to modify a resource
     */
    static UpdateLogAnomalyDetectorRequest translateToUpdateRequest(final ResourceModel model) {
        return UpdateLogAnomalyDetectorRequest.builder()
                .anomalyDetectorArn(model.getAnomalyDetectorArn())
                .evaluationFrequency(model.getEvaluationFrequency())
                .anomalyVisibilityTime(model.getAnomalyVisibilityTime() == null ? null : model.getAnomalyVisibilityTime().longValue())
                .filterPattern(model.getFilterPattern())
                .build();
    }

    /**
     * Request to list resources
     *
     * @param nextToken token passed to the aws service list resources request
     * @return awsRequest the aws service request to list resources within aws account
     */
    static ListLogAnomalyDetectorsRequest translateToListRequest(final String nextToken) {
        return ListLogAnomalyDetectorsRequest.builder()
                .nextToken(nextToken)
                .build();
    }

    /**
     * Translates resource objects from sdk into a resource model (primary identifier only)
     *
     * @param awsResponse the aws service describe resource response
     * @return list of resource models
     */
    static List<ResourceModel> translateFromListRequest(final ListLogAnomalyDetectorsResponse awsResponse) {
        return streamOfOrEmpty(awsResponse.anomalyDetectors())
                .map(resource -> ResourceModel.builder()
                        .anomalyDetectorArn(resource.anomalyDetectorArn())
                        .build())
                .collect(Collectors.toList());
    }

    private static <T> Stream<T> streamOfOrEmpty(final Collection<T> collection) {
        return Optional.ofNullable(collection)
                .map(Collection::stream)
                .orElseGet(Stream::empty);
    }

    /**
     * Request to add tags to a resource
     *
     * @param model resource model
     * @return awsRequest the aws service request to create a resource
     */
    static AwsRequest tagResourceRequest(final ResourceModel model, final Map<String, String> addedTags) {
        final AwsRequest awsRequest = null;
        // TODO: construct a request
        // e.g. https://github.com/aws-cloudformation/aws-cloudformation-resource-providers-logs/blob/2077c92299aeb9a68ae8f4418b5e932b12a8b186/aws-logs-loggroup/src/main/java/com/aws/logs/loggroup/Translator.java#L39-L43
        return awsRequest;
    }

    /**
     * Request to add tags to a resource
     *
     * @param model resource model
     * @return awsRequest the aws service request to create a resource
     */
    static AwsRequest untagResourceRequest(final ResourceModel model, final Set<String> removedTags) {
        final AwsRequest awsRequest = null;
        // TODO: construct a request
        // e.g. https://github.com/aws-cloudformation/aws-cloudformation-resource-providers-logs/blob/2077c92299aeb9a68ae8f4418b5e932b12a8b186/aws-logs-loggroup/src/main/java/com/aws/logs/loggroup/Translator.java#L39-L43
        return awsRequest;
    }
}
