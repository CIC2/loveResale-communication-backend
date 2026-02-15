package com.resale.homeflycommunication.utils.log;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoggingInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {

        logRequest(request, body);

        ClientHttpResponse response = execution.execute(request, body);
        response = new BufferingClientHttpResponseWrapper(response);

        logResponse(response);

        return response;
    }

    private void logRequest(HttpRequest request, byte[] body) {
        System.out.println("====== REQUEST ======");
        System.out.println("URI: " + request.getURI());
        System.out.println("Method: " + request.getMethod());
        System.out.println("Headers: " + request.getHeaders());
        System.out.println("Body: " + new String(body));
    }

    private void logResponse(ClientHttpResponse response) throws IOException {
        String body = new String(response.getBody().readAllBytes());
        System.out.println("====== RESPONSE ======");
        System.out.println("Status: " + response.getRawStatusCode());
        System.out.println("Headers: " + response.getHeaders());
        System.out.println("Body: " + body);
    }
}


