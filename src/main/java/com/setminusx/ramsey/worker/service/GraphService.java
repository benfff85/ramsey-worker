package com.setminusx.ramsey.worker.service;

import com.setminusx.ramsey.worker.dto.GraphDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Service
public class GraphService {

    @Value("${ramsey.graph.url}")
    private String graphUrl;

    private RestTemplate restTemplate;

    public GraphService(@Autowired RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public GraphDto getGraphById(Integer id) {
        log.info("Fetching graph with id {}", id);
        ResponseEntity<GraphDto> response = restTemplate.exchange(createUri(id), HttpMethod.GET, null, GraphDto.class);
        log.info("Graph fetched");
        return response.getBody();
    }

    private String createUri(Integer id) {
        log.info("Creating URI for graph fetch");
        String workUnitUri = UriComponentsBuilder.fromHttpUrl(graphUrl)
                .pathSegment(id.toString())
                .toUriString();
        log.info("URI for graph fetch: {}", workUnitUri);
        return workUnitUri;
    }

}
