package com.armandow.rest.http;

import com.armandow.rest.enums.CharsetType;
import com.armandow.rest.enums.ContentType;
import com.armandow.rest.enums.MethodType;
import com.armandow.rest.exceptions.AuthorizationException;
import com.armandow.rest.exceptions.CustomException;
import com.armandow.rest.exceptions.OperationExecutionException;
import com.armandow.rest.exceptions.OperationNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Map;

@Slf4j
public class RESTClient {
    private static final HttpClient httpClient = createHttpClient();
    
    private RESTClient() {
        // RESTClient
    }

    private static HttpClient createHttpClient() {
        HttpClient client;

        try {
            var context = SSLContext.getInstance("TLSv1.3");
            context.init(null, null, null);

            client = HttpClient.newBuilder()
                    .sslContext(context)
                    .connectTimeout(Duration.ofSeconds(75))
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            log.warn("createHttpClient", e);
            client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(30))
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();
        }

        return client;
    }
    
    public static Object execute(
            String uri, 
            MethodType method, 
            ContentType contentType, 
            CharsetType charsetType,
            Object content, 
            Map<String, String> headers, 
            ContentType produces
    ) throws IOException, InterruptedException, OperationNotFoundException, OperationExecutionException, AuthorizationException, CustomException {
        log.trace("{}: {}", method.toString(), uri);
        var requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .setHeader("Accept-Charset", charsetType.toString());
        
        switch (method) {
            case POST -> requestBuilder.POST(HttpRequest.BodyPublishers.ofString(content.toString()));
            case PUT -> requestBuilder.PUT(HttpRequest.BodyPublishers.ofString(content.toString()));
            case GET -> requestBuilder.GET();
            case DELETE -> requestBuilder.DELETE();
            default -> throw new OperationNotFoundException("Not supported");
        }
        
        if ( contentType != null ) {
            var strContentType = contentType + (charsetType.toString() == null ? "" : ";charset=" + charsetType);
            requestBuilder.setHeader("Content-Type", strContentType);
        }
        
        if ( headers != null ) {
            for ( var h : headers.entrySet() ) {
                requestBuilder.setHeader(h.getKey(), h.getValue());
            }
        }

        if ( produces == ContentType.APPLICATION_PDF ||
                produces == ContentType.APPLICATION_OCTET_STREAM ||
                produces == ContentType.IMAGE_JPG ||
                produces == ContentType.IMAGE_PNG ||
                produces == ContentType.APPLICATION_ZIP ) {
            return httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofByteArray()).body();
        }

        var response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        var responseCode = response.statusCode();
        
        Object responseData;
        if ( produces == ContentType.APPLICATION_JSON ) {
            try {
                responseData = new JSONObject(response.body());
            } catch (JSONException je) {
                responseData = je.getMessage();
            }
        } else {
            responseData = response.body();
        }

        return switch (responseCode) {
            case 200 -> responseData;
            case 400 -> throw new OperationExecutionException("Bad request: " + uri, responseData);
            case 401 -> throw new AuthorizationException("Unauthorized " + uri, responseData);
            case 405 -> throw new OperationNotFoundException("Method Not Allowed " + uri, responseData);
            case 409 -> throw new OperationExecutionException("Conflict " + uri, responseData);
            case 503 -> throw new OperationExecutionException("Service Unavailable " + uri, responseData);
            default -> throw new CustomException("", responseData, responseCode);
        };
    }
}
