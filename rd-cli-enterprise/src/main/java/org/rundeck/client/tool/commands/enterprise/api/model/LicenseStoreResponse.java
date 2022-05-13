package org.rundeck.client.tool.commands.enterprise.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import org.rundeck.client.util.DataOutput;

import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter @Setter
public class LicenseStoreResponse
        implements DataOutput
{
    String error;
    String message;
    String licenseAgreement;

    @Override
    public Map<?, ?> asMap() {
        HashMap<String, String> map = new HashMap<>();
        map.put("error", error);
        map.put("message", message);
        map.put("licenseAgreement", licenseAgreement);
        return map;
    }
}
