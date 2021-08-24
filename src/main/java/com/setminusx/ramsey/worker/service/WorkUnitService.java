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
import java.util.LinkedList;
import java.util.List;

import static java.util.Objects.isNull;

@Slf4j
@Service
public class WorkUnitService {

    @Value("${ramsey.work-unit.queue.url}")
    private String workUnitUrl;

    @Value("${ramsey.work-unit.queue.fetch-size}")
    private Integer fetchSize;

    @Value("${ramsey.work-unit.queue.publish-size}")
    private Integer publishSize;

    @Value("${ramsey.vertex-count}")
    private Integer vertexCount;

    @Value("${ramsey.subgraph-size}")
    private Integer subgraphSize;

    private final ClientRegister clientRegister;
    private final RestTemplate restTemplate;
    private String workUnitUri;

    private final List<WorkUnitDto> workUnitsToPublish = new LinkedList<>();

    public WorkUnitService(ClientRegister clientRegister, RestTemplate restTemplate) {
        this.clientRegister = clientRegister;
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    private void createUri() {
        log.info("Creating URI for work unit fetch");
        workUnitUri = UriComponentsBuilder.fromHttpUrl(workUnitUrl)
                .queryParam("vertexCount", vertexCount)
                .queryParam("subgraphSize", subgraphSize)
                .queryParam("assignedClientId", clientRegister.getClientId())
                .queryParam("pageSize", fetchSize)
                .queryParam("status", WorkUnitStatus.ASSIGNED)
                .toUriString();
        log.info("URI for work unit fetch: {}", workUnitUri);
    }

    public List<WorkUnitDto> getWorkUnits() {
        log.info("Fetching {} work units", fetchSize);
        WorkUnitDto[] workUnitDtos = restTemplate.getForObject(workUnitUri, WorkUnitDto[].class);
        if (isNull(workUnitDtos) || workUnitDtos.length == 0) {
            log.info("No work units found");
            return Collections.emptyList();
        }
        log.info("Work units fetched");
        return Arrays.asList(workUnitDtos);
    }

    public void publishBatch(WorkUnitDto workUnit) {
        workUnitsToPublish.add(workUnit);
        if (workUnitsToPublish.size() >= publishSize) {
            flushPublishCache();
        }
    }

    public void flushPublishCache() {
        if (!workUnitsToPublish.isEmpty()) {
            log.info("Saving work units");
            restTemplate.postForObject(UriComponentsBuilder.fromHttpUrl(workUnitUrl).toUriString(), workUnitsToPublish, WorkUnitDto[].class);
            workUnitsToPublish.clear();
        }
    }
}
