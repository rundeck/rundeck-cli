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
import com.fasterxml.jackson.annotation.JsonProperty;
import org.rundeck.client.util.Xml;

import javax.xml.bind.annotation.*;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@XmlRootElement()
@Xml

public class ErrorResponse
        implements ErrorDetail
{

    @XmlAttribute(name = "error")
    @JsonProperty("error")
    public String errorString;
    @JsonProperty("message")
    public String messageString;
    @JsonProperty("messages")
    public List<String> messages;
    @JsonProperty("errorCode")
    public String errorCodeJson;

    @XmlAttribute
    public int apiversion;

    static class Error {
        @XmlAttribute
        String code;
        @XmlElement(name = "message")
        String messageString;
        @XmlElementWrapper(name = "messages")
        @XmlElement(name = "message")
        public List<Message> messageList;

        @XmlElement(name = "message")
        public Message message;

        String getSingleMessage(){
            if(null!=messageList ){
                if(messageList.size()==1) {
                    return messageList.get(0).message;
                }else{
                    return messageList.toString();
                }
            }else if(null!=message){
                return message.message;
            }else{
                return messageString;
            }
        }
        static class Message{
            @XmlValue
            String message;

            @Override
            public String toString() {
                return message;
            }
        }
    }

    @XmlElement
    Error error;

    public String getErrorCode() {
        return null != error ? error.code : errorCodeJson;
    }



    public String toCodeString() {
        if (null != getErrorCode()) {
            return String.format(
                    "[code: %s; APIv%d]",
                    getErrorCode(),
                    apiversion
            );
        }
        return "";
    }


    @Override
    public String getErrorMessage() {
        return error != null ? error.getSingleMessage() : null != messages ? messages.toString() : messageString;
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
