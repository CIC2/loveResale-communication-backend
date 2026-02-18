package com.resale.loveresalecommunication.utils.log;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class BufferingClientHttpResponseWrapper implements ClientHttpResponse {

    private final ClientHttpResponse response;
    private final byte[] body;

    public BufferingClientHttpResponseWrapper(ClientHttpResponse response) throws IOException {
        this.response = response;
        this.body = response.getBody().readAllBytes();
    }

    @Override
    public HttpHeaders getHeaders() {
        return response.getHeaders();
    }

    @Override
    public InputStream getBody() {
        return new ByteArrayInputStream(body);
    }

    @Override
    public HttpStatusCode getStatusCode() throws IOException {
        return response.getStatusCode();   // correct implementation
    }

    @Deprecated // required by interface, safe to keep
    @Override
    public int getRawStatusCode() throws IOException {
        return response.getRawStatusCode();
    }

    @Deprecated // required by interface, safe to keep
    @Override
    public String getStatusText() throws IOException {
        return response.getStatusText();
    }

    @Override
    public void close() {
        response.close();
    }
}


