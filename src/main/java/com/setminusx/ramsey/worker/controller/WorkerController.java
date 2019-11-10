package com.setminusx.ramsey.worker.controller;

import com.setminusx.ramsey.worker.dto.WorkUnitDto;
import com.setminusx.ramsey.worker.service.GraphService;
import com.setminusx.ramsey.worker.service.WorkUnitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Slf4j
@Component
public class WorkerController {

    @Value("${ramsey.vertex-count}")
    private Integer vertexCount;

    @Value("${ramsey.subgraph-size}")
    private Integer subgraphSize;

    private final WorkUnitService workUnitService;
    private final GraphService graphService;

    private List<WorkUnitDto> workUnits;


    public WorkerController(@Autowired WorkUnitService workUnitService, @Autowired GraphService graphService) {
        this.workUnitService = workUnitService;
        this.graphService = graphService;
    }

    @PostConstruct
    public void init() {
        workUnits = workUnitService.getWorkUnits();
        for (WorkUnitDto workUnit : workUnits) {
            log.info("WorkUnit: {}", workUnit);
        }

        log.error("GRAPH::: {}", graphService.getGraphById(workUnits.get(0).getBaseGraphId()));


    }


}
