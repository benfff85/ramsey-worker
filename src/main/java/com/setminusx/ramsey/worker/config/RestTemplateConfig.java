package com.setminusx.ramsey.worker.config;

import com.setminusx.ramsey.worker.utility.ContentTypeInterceptor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.interceptors(List.of(new ContentTypeInterceptor())).build();
    }

}


