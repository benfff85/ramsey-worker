package com.setminusx.ramsey.worker.controller;

import com.setminusx.ramsey.worker.dto.WorkUnitDto;
import com.setminusx.ramsey.worker.model.Clique;
import com.setminusx.ramsey.worker.model.Graph;
import com.setminusx.ramsey.worker.model.WorkUnitStatus;
import com.setminusx.ramsey.worker.service.CliqueCheckService;
import com.setminusx.ramsey.worker.service.GraphService;
import com.setminusx.ramsey.worker.service.WorkUnitService;
import com.setminusx.ramsey.worker.utility.GraphUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class WorkerController {

    @Value("${ramsey.vertex-count}")
    private Short vertexCount;

    private final WorkUnitService workUnitService;
    private final GraphService graphService;
    private final CliqueCheckService cliqueCheckService;

    private List<WorkUnitDto> workUnits;

    private Graph graph;
    private List<Clique> baseGraphCliques;
    private List<Clique> derivedGraphCliques;


    public WorkerController(WorkUnitService workUnitService, GraphService graphService, CliqueCheckService cliqueCheckService) {
        this.workUnitService = workUnitService;
        this.graphService = graphService;
        this.cliqueCheckService = cliqueCheckService;
    }

    @PostConstruct
    public void init() {
        graph = new Graph(vertexCount);
    }

    @Scheduled(fixedDelay = 10000)
    public void process() {

        workUnits = workUnitService.getWorkUnits();
        for (WorkUnitDto workUnit : workUnits) {
            log.info("Processing WorkUnit: {}", workUnit);
            workUnit.setProcessingStartedDate(new Date());

            if (!workUnit.getBaseGraphId().equals(graph.getId())) {
                log.info("Base graph for this work unit not yet analyzed");
                log.info("Applying coloring");
                graph.applyColoring(graphService.getGraphById(workUnit.getBaseGraphId()).getEdgeData(), workUnit.getBaseGraphId());
                log.info("Checking for cliques in base graph");
                baseGraphCliques = cliqueCheckService.getCliques(graph);
                log.info("Clique count for base graph: {}", baseGraphCliques.size());
            }

            log.info("Flipping edges");
            GraphUtil.flipEdges(graph, workUnit.getEdgesToFlip());

            log.info("Checking for cliques in derived graph");
            derivedGraphCliques = cliqueCheckService.getCliques(graph);

            log.info("Clique count for derived graph: {}", baseGraphCliques.size());
            workUnit.setCliqueCount(derivedGraphCliques.size());
            workUnit.setCompletedDate(new Date());
            workUnit.setStatus(WorkUnitStatus.COMPLETE);
            workUnitService.publish(workUnit);

            log.info("Reverting base graph");
            GraphUtil.flipEdges(graph, workUnit.getEdgesToFlip());

            // fetch more WUs if needed

        }


    }


}
