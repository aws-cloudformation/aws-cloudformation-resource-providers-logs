package com.aws.logs.metricfilter;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.Map;

class Configuration extends BaseConfiguration {

    public Configuration() {
        super("aws-logs-metricfilter.json");
    }

    public JSONObject resourceSchemaJSONObject() {
        return new JSONObject(new JSONTokener(this.getClass().getClassLoader().getResourceAsStream(schemaFilename)));
    }

    public Map<String, String> resourceDefinedTags(final ResourceModel resourceModel) {
        return null;
    }
}
