package com.aws.logs.metricfilter;

import java.io.InputStream;

class Configuration extends BaseConfiguration {

    public Configuration() {
        super("aws-logs-metricfilter.json");
    }

    public InputStream resourceSchema() {
        return this.getClass().getClassLoader().getResourceAsStream(schemaFilename);
    }

}
