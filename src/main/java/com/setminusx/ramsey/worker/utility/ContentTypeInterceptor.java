package com.setminusx.ramsey.worker.utility;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

public class ContentTypeInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public @NotNull ClientHttpResponse intercept(HttpRequest request, byte @NotNull [] body, ClientHttpRequestExecution execution) throws IOException {
        request.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        return execution.execute(request, body);
    }
}

