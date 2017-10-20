/*
 * Copyright 2017 Rundeck, Inc. (http://rundeck.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.rundeck.client.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.rundeck.client.util.Xml;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

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
                "%s%n%s",
                getErrorMessage() != null ? getErrorMessage() : "(no message)",
                toCodeString()
        );
    }
}
