package org.rundeck.client.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.rundeck.client.util.Xml;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

/**
 * Created by greg on 5/22/16.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@Root(strict = false)
@Xml

public class ErrorResponse implements ErrorDetail {

    @Attribute
    public String error;

    @Attribute
    public int apiversion;

    @Attribute(name = "code")
    @Path("error")
    public String errorCode;

    @Element
    @Path("error")
    public String message;

    public String toCodeString() {
        if (null != errorCode) {
            return String.format(
                    "[code: %s; APIv%d]",
                    errorCode,
                    apiversion
            );
        }
        return "";
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String getErrorMessage() {
        return message != null ? message : error;
    }

    @Override
    public int getApiVersion() {
        return apiversion;
    }


    @Override
    public String toString() {
        return String.format(
                "%s%n%s%n",
                getErrorMessage() != null ? getErrorMessage() : "(no message)",
                toCodeString()
        );
    }
}
