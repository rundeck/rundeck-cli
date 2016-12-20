package org.rundeck.client.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by greg on 7/19/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ACLPolicyValidation extends ErrorResponse {
    public boolean valid;
    public List<ValidationError> policies;

    public Map<String, Object> toMap() {
        if (null == policies || policies.size() < 1) {
            return null;
        }
        Map<String, Object> result = new HashMap<>();
        policies.forEach(err -> result.put(err.policy, err.errors));
        return result;

    }

    public static class ValidationError {
        public String policy;
        public List<String> errors;
    }

}
