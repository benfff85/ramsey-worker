package com.setminusx.ramsey.worker.controller;

import com.setminusx.ramsey.worker.client.MiddlewareClient;
import com.setminusx.ramsey.worker.config.RamseyConfig;
import com.setminusx.ramsey.worker.model.Graph;
import com.setminusx.ramsey.worker.model.WorkUnit;
import com.setminusx.ramsey.worker.model.Clique;
import com.setminusx.ramsey.worker.utility.UtilityGraph;
import com.setminusx.ramsey.worker.model.WorkUnitStatus;
import com.setminusx.ramsey.worker.service.NaiveCliqueCheckService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.setminusx.ramsey.worker.utility.TimeUtility.now;


@Slf4j
@Component
public class NaiveWorkUnitProcessor implements WorkUnitProcessor {

    private final NaiveCliqueCheckService naiveCliqueCheckService;
    private final RamseyConfig ramseyConfig;
    private final MiddlewareClient middlewareClient;
    private UtilityGraph utilityGraph;


    public NaiveWorkUnitProcessor(NaiveCliqueCheckService naiveCliqueCheckService, RamseyConfig ramseyConfig, MiddlewareClient middlewareClient) {
        this.naiveCliqueCheckService = naiveCliqueCheckService;
        this.middlewareClient = middlewareClient;
        this.ramseyConfig = ramseyConfig;
    }

    @PostConstruct
    public void init() {
        utilityGraph = new UtilityGraph(ramseyConfig.getVertexCount());
    }

    @Override
    public void process(WorkUnit workUnit) {
            log.info("Processing WorkUnit: {}", workUnit);

            if (!workUnit.getBaseGraphId().equals(utilityGraph.getId())) {
                Graph graph = middlewareClient.getGraphById(workUnit.getBaseGraphId());
                utilityGraph.applyColoring(graph.getEdgeData(), graph.getGraphId());
            }
            workUnit.setProcessingStartedDate(now());

            log.debug("Flipping edges");
            utilityGraph.flipsEdges(workUnit.getEdgesToFlip());

            log.debug("Checking for cliques in derived graph");
            List<Clique> derivedGraphCliques = naiveCliqueCheckService.getCliques(utilityGraph);
            workUnit.setCliqueCount(derivedGraphCliques.size());
            workUnit.setCompletedDate(now());
            workUnit.setStatus(WorkUnitStatus.COMPLETE);

            log.debug("Reverting base graph");
            utilityGraph.flipsEdges(workUnit.getEdgesToFlip());

    }

}
