/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.internal.resource.transport.http;


import java.nio.charset.Charset;

import cz.msebera.android.httpclient.annotation.Contract;
import cz.msebera.android.httpclient.annotation.ThreadingBehavior;
import cz.msebera.android.httpclient.auth.AuthScheme;
import cz.msebera.android.httpclient.auth.AuthSchemeFactory;
import cz.msebera.android.httpclient.auth.AuthSchemeProvider;
import cz.msebera.android.httpclient.params.HttpParams;
import cz.msebera.android.httpclient.protocol.HttpContext;

@Contract(threading = ThreadingBehavior.IMMUTABLE)
@SuppressWarnings("deprecation")
public class HttpHeaderSchemeFactory implements AuthSchemeFactory, AuthSchemeProvider {

    public HttpHeaderSchemeFactory(final Charset charset) {
        super();
    }

    public HttpHeaderSchemeFactory() {
        this(null);
    }

    @Override
    public AuthScheme newInstance(final HttpParams params) {
        return new HttpHeaderAuthScheme();
    }

    @Override
    public AuthScheme create(final HttpContext context) {
        return new HttpHeaderAuthScheme();
    }

}
