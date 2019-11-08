package com.amazonaws.logs.destination;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.Map;

class Configuration extends BaseConfiguration {

    public Configuration() {
        super("aws-logs-destination.json");
    }

    public JSONObject resourceSchemaJSONObject() {
        return new JSONObject(new JSONTokener(this.getClass().getClassLoader().getResourceAsStream(schemaFilename)));
    }

    public Map<String, String> resourceDefinedTags(final ResourceModel resourceModel) {
        return null;
    }
}
