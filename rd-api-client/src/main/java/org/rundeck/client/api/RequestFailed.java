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

package org.rundeck.client.api;

/**
 * Http request failure
 */
public class RequestFailed extends RuntimeException {
    private final int statusCode;
    private final String status;

    public RequestFailed(final int statusCode, final String status) {
        this.statusCode = statusCode;
        this.status = status;
    }

    public RequestFailed(final String message, final int statusCode, final String status) {
        super(message);
        this.statusCode = statusCode;
        this.status = status;
    }

    public RequestFailed(final String message, final Throwable cause, final int statusCode, final String status) {
        super(message, cause);
        this.statusCode = statusCode;
        this.status = status;
    }

    public RequestFailed(final Throwable cause, final int statusCode, final String status) {
        super(cause);
        this.statusCode = statusCode;
        this.status = status;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getStatus() {
        return status;
    }
}
