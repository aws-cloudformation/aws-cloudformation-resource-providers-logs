package com.aws.logs.metricfilter;

import org.json.JSONObject;

public class ResourceModelExtensions {

    public static JSONObject getPrimaryIdentifier(final ResourceModel model) {
        final JSONObject identifier = new JSONObject();
        identifier.append("LogGroupName", model.getLogGroupName());
        identifier.append("FilterName", model.getFilterName());
        return identifier;
    }
}
