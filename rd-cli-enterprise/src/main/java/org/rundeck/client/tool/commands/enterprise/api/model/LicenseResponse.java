package org.rundeck.client.tool.commands.enterprise.api.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.rundeck.client.util.DataOutput;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class LicenseResponse implements DataOutput {
    String company;
    String contactEmail;
    String application;
    List<String> editions;
    List<String> applicationVersion;
    List<String> serverUUIDs;
    int gracePeriod;
    String type;
    String issueDate;
    String validUntil;
    String validSince;
    String graceUntil;
    String licenseId;
    boolean perpetual;
    String invalidCode;
    int remaining;
    String state;
    boolean active;
    boolean shouldWarn;
    String baseUrl;
    String edition;
    String reason;
    String warning;

    @Override
    public Map<?, ?> asMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("company", company);
        map.put("contactEmail", contactEmail);
        map.put("application", application);
        map.put("editions", editions);
        map.put("applicationVersion", applicationVersion);
        map.put("serverUUIDs", serverUUIDs);
        map.put("gracePeriod", gracePeriod);
        map.put("type", type);
        map.put("issueDate", issueDate);
        map.put("validUntil", validUntil);
        map.put("validSince", validSince);
        map.put("graceUntil", graceUntil);
        map.put("licenseId", licenseId);
        map.put("perpetual", perpetual);
        map.put("invalidCode", invalidCode);
        map.put("remaining", remaining);
        map.put("state", state);
        map.put("active", active);
        map.put("shouldWarn", shouldWarn);
        map.put("baseUrl", baseUrl);
        map.put("edition", edition);
        map.put("reason", reason);
        map.put("warning", warning);
        return map;
    }
}
