package com.setminusx.ramsey.worker.service;

import com.setminusx.ramsey.worker.controller.ClientRegister;
import com.setminusx.ramsey.worker.dto.WorkUnitDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import java.util.List;

@Slf4j
@Service
public class WorkUnitService {

    @Value("${ramsey.work-unit.queue.url}")
    private String workUnitUrl;

    @Value("${ramsey.work-unit.fetch.size}")
    private Integer fetchSize;

    @Value("${ramsey.vertex-count}")
    private Integer vertexCount;

    @Value("${ramsey.subgraph-size}")
    private Integer subgraphSize;

    private String clientId;
    private RestTemplate restTemplate;
    private String workUnitUri;

    public WorkUnitService(@Autowired ClientRegister clientRegister, @Autowired RestTemplate restTemplate) {
        clientId = clientRegister.getClientId();
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    private void createUri() {
        log.info("Creating URI for work unit fetch");
        workUnitUri = UriComponentsBuilder.fromHttpUrl(workUnitUrl)
                .queryParam("vertexCount", vertexCount)
                .queryParam("subgraphSize", subgraphSize)
                .queryParam("clientId", clientId)
                .queryParam("pageSize", fetchSize)
                .toUriString();
        log.info("URI for work unit fetch: {}", workUnitUri);
    }

    public List<WorkUnitDto> getWorkUnits() {
        log.info("Fetching {} work units", fetchSize);
        ResponseEntity<List<WorkUnitDto>> response = restTemplate.exchange(workUnitUri, HttpMethod.POST, null,  new ParameterizedTypeReference<List<WorkUnitDto>>(){});
        log.info("Work units fetched");
        return response.getBody();
    }

}
