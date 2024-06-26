package com.setminusx.ramsey.worker.service;

import com.setminusx.ramsey.worker.dto.GraphDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Service
public class GraphService {

    @Value("${ramsey.graph.url}")
    private String graphUrl;

    private final RestTemplate restTemplate;

    public GraphService(@Autowired RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public GraphDto getGraphById(Integer id) {
        log.info("Fetching graph with graphId: {}", id);
        GraphDto graphDto = restTemplate.getForObject(createUri(id), GraphDto.class);
        log.info("Graph fetched with graphId: {}", id);
        return graphDto;
    }

    private String createUri(Integer id) {
        String workUnitUri = UriComponentsBuilder.fromHttpUrl(graphUrl)
                .pathSegment(id.toString())
                .toUriString();
        log.info("URI for graph fetch: {}", workUnitUri);
        return workUnitUri;
    }

}
