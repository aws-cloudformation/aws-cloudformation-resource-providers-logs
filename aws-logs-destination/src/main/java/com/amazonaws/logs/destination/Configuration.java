package com.amazonaws.logs.destination;

import java.io.InputStream;
import org.json.JSONObject;
import org.json.JSONTokener;

class Configuration extends BaseConfiguration {

    public Configuration() {
        super("aws-logs-destination.json");
    }

    public JSONObject resourceSchemaJSONObject() {
        return new JSONObject(new JSONTokener(this.getClass().getClassLoader().getResourceAsStream(schemaFilename)));
    }

}
