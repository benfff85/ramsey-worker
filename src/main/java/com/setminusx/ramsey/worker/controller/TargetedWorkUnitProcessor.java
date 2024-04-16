package com.setminusx.ramsey.worker.controller;

import com.setminusx.ramsey.worker.dto.WorkUnitDto;
import com.setminusx.ramsey.worker.model.EdgeMappedCliqueCollection;
import com.setminusx.ramsey.worker.model.Graph;
import com.setminusx.ramsey.worker.model.WorkUnitStatus;
import com.setminusx.ramsey.worker.service.CliqueCheckService;
import com.setminusx.ramsey.worker.service.GraphService;
import com.setminusx.ramsey.worker.service.TargetedCliqueCheckService;
import com.setminusx.ramsey.worker.utility.GraphUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static com.setminusx.ramsey.worker.utility.TimeUtility.now;


@Slf4j
@Component
public class TargetedWorkUnitProcessor implements WorkUnitProcessor {

    @Value("${ramsey.vertex-count}")
    private Short vertexCount;
    private final GraphService graphService;
    private final CliqueCheckService cliqueCheckService;
    private final TargetedCliqueCheckService targetedCliqueCheckService;
    private Graph graph;
    private EdgeMappedCliqueCollection cliqueCollection;


    public TargetedWorkUnitProcessor(GraphService graphService, CliqueCheckService cliqueCheckService, TargetedCliqueCheckService targetedCliqueCheckService) {
        this.graphService = graphService;
        this.cliqueCheckService = cliqueCheckService;
        this.targetedCliqueCheckService = targetedCliqueCheckService;
    }

    @PostConstruct
    public void init() {
        graph = new Graph(vertexCount);
        cliqueCollection = new EdgeMappedCliqueCollection(vertexCount);
    }


    @Override
    public void process(WorkUnitDto workUnit) {
            log.info("Processing WorkUnit: {}", workUnit);

            if (!workUnit.getBaseGraphId().equals(graph.getId())) {
                processBaseGraph(graphService.getGraphById(workUnit.getBaseGraphId()).getEdgeData(), workUnit.getBaseGraphId());
            }
            workUnit.setProcessingStartedDate(now());

            log.debug("Flipping edges");
            GraphUtil.flipEdges(graph, workUnit.getEdgesToFlip());

            log.debug("Checking for cliques in derived graph");
            Integer derivedGraphCliqueCount = targetedCliqueCheckService.getCliques(graph, workUnit.getEdgesToFlip());
            enrichWorkUnit(derivedGraphCliqueCount, workUnit);

            log.debug("Reverting base graph");
            GraphUtil.flipEdges(graph, workUnit.getEdgesToFlip());

    }

    private void processBaseGraph(String edgeData, Integer graphId) {
        log.info("Base graph for this work unit not yet analyzed");
        log.info("Applying coloring");
        graph.applyColoring(edgeData, graphId);
        log.info("Checking for cliques in base graph");
        cliqueCollection.setCliques(cliqueCheckService.getCliques(graph));
        log.info("Clique count for base graph: {}", cliqueCollection.size());
    }

    private void enrichWorkUnit(Integer derivedGraphCliqueCount, WorkUnitDto workUnit) {
        log.debug("Enriching work unit with analysis results");
        Integer cliqueCount = (derivedGraphCliqueCount + cliqueCollection.size()) - cliqueCollection.getCountOfCliquesContainingEdges(workUnit.getEdgesToFlip());

        log.info("Clique count for derived graph: {}", cliqueCount);
        workUnit.setCliqueCount(cliqueCount);
        workUnit.setCompletedDate(now());
        workUnit.setStatus(WorkUnitStatus.COMPLETE);
    }

}
