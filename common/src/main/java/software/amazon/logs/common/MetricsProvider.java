package software.amazon.logs.common;

import java.util.Optional;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import software.amazon.cloudwatchlogs.emf.logger.MetricsLogger;

/**
 * Factory class for creating an EMF MetricsLogger instance
 */
@Slf4j
public final class MetricsProvider {

    private static final String NAMESPACE_PREFIX = "CFNRegistry";
    private static Optional<String> resourceName = Optional.empty();

    private MetricsProvider() {}

    /**
     * Initializes the resource name for the metric factory
     *
     * @param name of the resource to be used for the metric namespace
     */
    public static void initialize(@NonNull final String name) {
        if (resourceName.isPresent()) {
            log.warn("MetricsFactory is already initialized");
        }

        resourceName = Optional.of(name);
    }

    /**
     * Gets a new MetricsLogger instance
     *
     * @return an EMF MetricsLogger object
     */
    public static MetricsLogger getMetrics() {
        final MetricsLogger metrics = new MetricsLogger();

        setNamespace(metrics);
        metrics.resetDimensions(false);
        metrics.setFlushPreserveDimensions(false);

        return metrics;
    }

    private static void setNamespace(final MetricsLogger metrics) {
        final String metricNamespace = getMetricNamespace();

        try {
            metrics.setNamespace(metricNamespace);
        } catch (final Throwable ex) {
            log.error("Failed to assign namespace [{}] to MetricsLogger instance", metricNamespace, ex);
        }
    }

    private static String getMetricNamespace() {
        return resourceName.map(name -> String.format("%s/%s", NAMESPACE_PREFIX, name)).orElse(NAMESPACE_PREFIX);
    }
}
