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

package org.rundeck.client.util;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * Retrofit converter factory that can convert xml or json bodies, depending on annotation of the
 * API service method using {@link Xml} or {@link Json}, and has a default format.
 */
public class QualifiedTypeConverterFactory extends Converter.Factory {
    private final Converter.Factory jsonFactory;
    private final Converter.Factory xmlFactory;
    private final Converter.Factory defaultFactory;

    public QualifiedTypeConverterFactory(
            Converter.Factory jsonFactory,
            Converter.Factory xmlFactory, final boolean defaultJson
    )
    {
        this.jsonFactory = jsonFactory;
        this.xmlFactory = xmlFactory;
        this.defaultFactory = defaultJson ? jsonFactory : xmlFactory;
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(
            Type type, Annotation[] annotations,
            Retrofit retrofit
    )
    {
        for (Annotation annotation : annotations) {
            if (annotation instanceof Json) {
                return jsonFactory.responseBodyConverter(type, annotations, retrofit);
            }
            if (annotation instanceof Xml) {
                return xmlFactory.responseBodyConverter(type, annotations, retrofit);
            }
        }
        return defaultFactory.responseBodyConverter(type, annotations, retrofit);
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(
            Type type,
            Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit
    )
    {
        for (Annotation annotation : parameterAnnotations) {
            if (annotation instanceof Json) {
                return jsonFactory.requestBodyConverter(type, parameterAnnotations, methodAnnotations,
                                                        retrofit
                );
            }
            if (annotation instanceof Xml) {
                return xmlFactory.requestBodyConverter(type, parameterAnnotations, methodAnnotations,
                                                       retrofit
                );
            }
        }
        return defaultFactory.requestBodyConverter(type, parameterAnnotations, methodAnnotations,
                                                   retrofit
        );
    }
}
