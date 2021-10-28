/*
 * Licensed to Diennea S.r.l. under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Diennea S.r.l. licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.bkvm.bookkeeper.restapi;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class HttpRequestUtils {

    public static BookieApiResponse sendGetRequest(String httpServerUri, String path) throws IOException, InterruptedException, URISyntaxException, ExecutionException {
        BookieApiResponse gcResponse;
        URI uri = buildUri(httpServerUri, path);
        HttpGet request = new HttpGet(uri);
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                gcResponse = getResponse(response);
            }
        }
        return gcResponse;
    }

    public static BookieApiResponse sendPostRequest(String httpServerUri, String path, Map<String, String> headers) throws IOException, InterruptedException, URISyntaxException, ExecutionException {
        BookieApiResponse gcResponse;
        URI uri = buildUri(httpServerUri, path);
        HttpPost request = new HttpPost(uri);
        if (headers != null) {
            headers.forEach((k, v) -> request.addHeader(k, v));
        }
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                gcResponse = getResponse(response);
            }
        }
        return gcResponse;
    }

    public static BookieApiResponse sendPutRequest(String httpServerUri, String path, Map<String, String> headers) throws IOException, InterruptedException, URISyntaxException, ExecutionException {
        BookieApiResponse gcResponse;
        URI uri = buildUri(httpServerUri, path);
        HttpPut request = new HttpPut(uri);
        if (headers != null) {
            headers.forEach((k, v) -> request.addHeader(k, v));
        }
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                gcResponse = getResponse(response);
            }
        }
        return gcResponse;
    }

    private static URI buildUri(String httpServerUri, String path) throws URISyntaxException {
        URI uri = new URI(httpServerUri);
        return new URIBuilder()
                .setScheme(uri.getScheme())
                .setHost(uri.getHost())
                .setPort(uri.getPort())
                .setPath(path)
                .build();
    }

    private static BookieApiResponse getResponse(HttpResponse response) throws IOException {
        BookieApiResponse gcResponse = new BookieApiResponse();
        String message = EntityUtils.toString(response.getEntity());
        gcResponse.setStatusCode(response.getStatusLine().getStatusCode());
        gcResponse.setMessage(message);
        return gcResponse;
    }
}
