package com.setminusx.ramsey.worker.service;

import com.setminusx.ramsey.worker.controller.ClientRegister;
import com.setminusx.ramsey.worker.dto.WorkUnitDto;
import com.setminusx.ramsey.worker.model.WorkUnitStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;

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

    public WorkUnitService(ClientRegister clientRegister, RestTemplate restTemplate) {
        clientId = clientRegister.getClientId();
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    private void createUri() {
        log.info("Creating URI for work unit fetch");
        workUnitUri = UriComponentsBuilder.fromHttpUrl(workUnitUrl)
                .queryParam("vertexCount", vertexCount)
                .queryParam("subgraphSize", subgraphSize)
                .queryParam("assignedClientId", clientId)
                .queryParam("pageSize", fetchSize)
                .queryParam("status", WorkUnitStatus.ASSIGNED)
                .toUriString();
        log.info("URI for work unit fetch: {}", workUnitUri);
    }

    public List<WorkUnitDto> getWorkUnits() {
        log.info("Fetching {} work units", fetchSize);
        WorkUnitDto[] workUnitDtos = restTemplate.getForObject(workUnitUri, WorkUnitDto[].class);
        if (isNull(workUnitDtos)) {
            log.info("No work units found");
            return Collections.emptyList();
        }
        log.info("Work units fetched");
        return Arrays.asList(workUnitDtos);
    }

    public void publish(WorkUnitDto workUnit) {
        log.info("Saving work unit {}", workUnit.getId());
        restTemplate.postForObject(UriComponentsBuilder.fromHttpUrl(workUnitUrl).toUriString(), singletonList(workUnit), WorkUnitDto[].class);
    }
}
