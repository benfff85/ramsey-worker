package com.setminusx.ramsey.worker.controller;

import com.setminusx.ramsey.worker.client.MiddlewareClient;
import com.setminusx.ramsey.worker.config.RamseyConfig;
import com.setminusx.ramsey.worker.model.WorkUnit;
import com.setminusx.ramsey.worker.model.EdgeMappedCliqueCollection;
import com.setminusx.ramsey.worker.utility.UtilityGraph;
import com.setminusx.ramsey.worker.model.WorkUnitStatus;
import com.setminusx.ramsey.worker.service.CliqueCheckService;
import com.setminusx.ramsey.worker.service.TargetedCliqueCheckService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.setminusx.ramsey.worker.utility.TimeUtility.now;


@Slf4j
@Component
public class TargetedWorkUnitProcessor implements WorkUnitProcessor {

    private final CliqueCheckService cliqueCheckService;
    private final TargetedCliqueCheckService targetedCliqueCheckService;
    private final RamseyConfig ramseyConfig;
    private final MiddlewareClient middlewareClient;
    private UtilityGraph utilityGraph;
    private EdgeMappedCliqueCollection cliqueCollection;


    public TargetedWorkUnitProcessor(CliqueCheckService cliqueCheckService, TargetedCliqueCheckService targetedCliqueCheckService, RamseyConfig ramseyConfig, MiddlewareClient middlewareClient) {
        this.cliqueCheckService = cliqueCheckService;
        this.targetedCliqueCheckService = targetedCliqueCheckService;
        this.ramseyConfig = ramseyConfig;
        this.middlewareClient = middlewareClient;
    }

    @PostConstruct
    public void init() {
        utilityGraph = new UtilityGraph(ramseyConfig.getVertexCount());
        cliqueCollection = new EdgeMappedCliqueCollection(ramseyConfig.getVertexCount());
    }


    @Override
    public void process(WorkUnit workUnit) {
            log.info("Processing WorkUnit: {}", workUnit);

            if (!workUnit.getBaseGraphId().equals(utilityGraph.getId())) {
                processBaseGraph(middlewareClient.getGraphById(workUnit.getBaseGraphId()).getEdgeData(), workUnit.getBaseGraphId());
            }
            workUnit.setProcessingStartedDate(now());

            log.debug("Flipping edges");
            utilityGraph.flipsEdges(workUnit.getEdgesToFlip());

            log.debug("Checking for cliques in derived graph");
            int derivedGraphCliqueCount = targetedCliqueCheckService.getCliques(utilityGraph, workUnit.getEdgesToFlip());
            enrichWorkUnit(derivedGraphCliqueCount, workUnit);

            log.debug("Reverting base graph");
            utilityGraph.flipsEdges(workUnit.getEdgesToFlip());

    }

    private void processBaseGraph(String edgeData, Integer graphId) {
        log.info("Base graph for this work unit not yet analyzed");
        log.info("Applying coloring");
        utilityGraph.applyColoring(edgeData, graphId);
        log.info("Checking for cliques in base graph");
        cliqueCollection.setCliques(cliqueCheckService.getCliques(utilityGraph));
        log.info("Clique count for base graph: {}", cliqueCollection.size());
    }

    private void enrichWorkUnit(int derivedGraphCliqueCount, WorkUnit workUnit) {
        log.debug("Enriching work unit with analysis results");
        Integer cliqueCount = (derivedGraphCliqueCount + cliqueCollection.size()) - cliqueCollection.getCountOfCliquesContainingEdges(workUnit.getEdgesToFlip());

        log.info("Clique count for derived graph: {}", cliqueCount);
        workUnit.setCliqueCount(cliqueCount);
        workUnit.setCompletedDate(now());
        workUnit.setStatus(WorkUnitStatus.COMPLETE);
    }

}
