package com.setminusx.ramsey.worker.controller;

import com.setminusx.ramsey.worker.dto.WorkUnitDto;
import com.setminusx.ramsey.worker.model.Clique;
import com.setminusx.ramsey.worker.model.EdgeMappedCliqueCollection;
import com.setminusx.ramsey.worker.model.Graph;
import com.setminusx.ramsey.worker.model.WorkUnitStatus;
import com.setminusx.ramsey.worker.service.CliqueCheckService;
import com.setminusx.ramsey.worker.service.GraphService;
import com.setminusx.ramsey.worker.service.TargetedCliqueCheckService;
import com.setminusx.ramsey.worker.service.WorkUnitService;
import com.setminusx.ramsey.worker.utility.GraphUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Queue;

import static java.time.LocalDateTime.now;

@Slf4j
@Component
public class WorkerController {

    @Value("${ramsey.vertex-count}")
    private Short vertexCount;

    private final WorkUnitService workUnitService;
    private final GraphService graphService;
    private final CliqueCheckService cliqueCheckService;
    private final TargetedCliqueCheckService targetedCliqueCheckService;
    private final Queue<WorkUnitDto> workUnits;

    private Graph graph;
    private EdgeMappedCliqueCollection cliqueCollection;


    public WorkerController(WorkUnitService workUnitService, GraphService graphService, CliqueCheckService cliqueCheckService, TargetedCliqueCheckService targetedCliqueCheckService, @Qualifier("workUnitQueue") Queue<WorkUnitDto> workUnits) {
        this.workUnitService = workUnitService;
        this.graphService = graphService;
        this.cliqueCheckService = cliqueCheckService;
        this.targetedCliqueCheckService = targetedCliqueCheckService;
        this.workUnits = workUnits;
    }

    @PostConstruct
    public void init() {
        graph = new Graph(vertexCount);
        cliqueCollection = new EdgeMappedCliqueCollection(vertexCount);
    }

    @Scheduled(fixedDelay = 10000)
    public void process() {
        List<Clique> derivedGraphCliques;
        WorkUnitDto workUnit;

        workUnits.addAll(workUnitService.getWorkUnits());
        while (!workUnits.isEmpty()) {
            workUnit = workUnits.poll();
            log.info("Processing WorkUnit: {}", workUnit);

            if (!workUnit.getBaseGraphId().equals(graph.getId())) {
                processBaseGraph(graphService.getGraphById(workUnit.getBaseGraphId()).getEdgeData(), workUnit.getBaseGraphId());
            }
            workUnit.setProcessingStartedDate(now());

            log.debug("Flipping edges");
            GraphUtil.flipEdges(graph, workUnit.getEdgesToFlip());

            log.debug("Checking for cliques in derived graph");
            derivedGraphCliques = targetedCliqueCheckService.getCliques(graph, workUnit.getEdgesToFlip());
            enrichWorkUnit(derivedGraphCliques, workUnit);
            workUnitService.publishBatch(workUnit);

            log.debug("Reverting base graph");
            GraphUtil.flipEdges(graph, workUnit.getEdgesToFlip());

            if (workUnits.isEmpty()) {
                log.info("Work unit queue is empty, attempting to fetch more work units.");
                workUnitService.flushPublishCache();
                workUnits.addAll(workUnitService.getWorkUnits());
            }

        }

        log.warn("Worker out of work, sleeping and trying again. Consider increasing the work unit creation rate.");

    }

    private void enrichWorkUnit(List<Clique> derivedGraphCliques, WorkUnitDto workUnit) {
        log.debug("Enriching work unit with analysis results");
        Integer cliqueCount = (derivedGraphCliques.size() + cliqueCollection.size()) - cliqueCollection.getCountOfCliquesContainingEdges(workUnit.getEdgesToFlip());

        log.info("Clique count for derived graph: {}", cliqueCount);
        workUnit.setCliqueCount(cliqueCount);
        workUnit.setCompletedDate(now());
        workUnit.setStatus(WorkUnitStatus.COMPLETE);
    }

    private void processBaseGraph(String edgeData, Integer graphId) {
        log.info("Base graph for this work unit not yet analyzed");
        log.info("Applying coloring");
        graph.applyColoring(edgeData, graphId);
        log.info("Checking for cliques in base graph");
        cliqueCollection.setCliques(cliqueCheckService.getCliques(graph));
        log.info("Clique count for base graph: {}", cliqueCollection.size());
    }

}
