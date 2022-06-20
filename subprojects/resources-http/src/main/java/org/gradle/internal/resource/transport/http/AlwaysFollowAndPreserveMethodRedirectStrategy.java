/*
 * Copyright 2015 the original author or authors.
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


import java.net.URI;

import cz.msebera.android.httpclient.HttpEntityEnclosingRequest;
import cz.msebera.android.httpclient.HttpRequest;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.ProtocolException;
import cz.msebera.android.httpclient.client.methods.HttpDelete;
import cz.msebera.android.httpclient.client.methods.HttpEntityEnclosingRequestBase;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.methods.HttpHead;
import cz.msebera.android.httpclient.client.methods.HttpOptions;
import cz.msebera.android.httpclient.client.methods.HttpPatch;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.client.methods.HttpPut;
import cz.msebera.android.httpclient.client.methods.HttpTrace;
import cz.msebera.android.httpclient.client.methods.HttpUriRequest;
import cz.msebera.android.httpclient.impl.client.DefaultRedirectStrategy;
import cz.msebera.android.httpclient.protocol.HttpContext;

/**
 * A class which makes httpclient follow redirects for all http methods.
 * This has been introduced to overcome a regression caused by switching to apache httpclient as the transport mechanism for publishing (https://issues.gradle.org/browse/GRADLE-3312)
 * The rational for httpclient not following redirects, by default, can be found here: https://issues.apache.org/jira/browse/HTTPCLIENT-860
 */
public class AlwaysFollowAndPreserveMethodRedirectStrategy extends DefaultRedirectStrategy {

    public AlwaysFollowAndPreserveMethodRedirectStrategy() {
    }

    @Override
    protected boolean isRedirectable(String method) {
        return true;
    }

    @Override
    public HttpUriRequest getRedirect(HttpRequest request, HttpResponse response, HttpContext context) throws ProtocolException {
        URI uri = this.getLocationURI(request, response, context);
        String method = request.getRequestLine().getMethod();
        if (method.equalsIgnoreCase(HttpHead.METHOD_NAME)) {
            return new HttpHead(uri);
        } else if (method.equalsIgnoreCase(HttpPost.METHOD_NAME)) {
            return this.copyEntity(new HttpPost(uri), request);
        } else if (method.equalsIgnoreCase(HttpPut.METHOD_NAME)) {
            return this.copyEntity(new HttpPut(uri), request);
        } else if (method.equalsIgnoreCase(HttpDelete.METHOD_NAME)) {
            return new HttpDelete(uri);
        } else if (method.equalsIgnoreCase(HttpTrace.METHOD_NAME)) {
            return new HttpTrace(uri);
        } else if (method.equalsIgnoreCase(HttpOptions.METHOD_NAME)) {
            return new HttpOptions(uri);
        } else if (method.equalsIgnoreCase(HttpPatch.METHOD_NAME)) {
            return this.copyEntity(new HttpPatch(uri), request);
        } else {
            return new HttpGet(uri);
        }
    }

    private HttpUriRequest copyEntity(HttpEntityEnclosingRequestBase redirect, HttpRequest original) {
        if (original instanceof HttpEntityEnclosingRequest) {
            redirect.setEntity(((HttpEntityEnclosingRequest) original).getEntity());
        }
        return redirect;
    }
}
