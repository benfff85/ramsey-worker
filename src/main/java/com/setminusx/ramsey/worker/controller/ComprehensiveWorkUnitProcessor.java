package com.setminusx.ramsey.worker.controller;

import com.setminusx.ramsey.worker.client.MiddlewareClient;
import com.setminusx.ramsey.worker.config.RamseyConfig;
import com.setminusx.ramsey.worker.model.*;
import com.setminusx.ramsey.worker.utility.UtilityGraph;
import com.setminusx.ramsey.worker.service.CliqueCheckServiceComprehensiveBitSet;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import static com.setminusx.ramsey.worker.utility.TimeUtility.now;


@Slf4j
@Component
public class ComprehensiveWorkUnitProcessor implements WorkUnitProcessor {

    private final CliqueCheckServiceComprehensiveBitSet cliqueCheckService;
    private final RamseyConfig ramseyConfig;
    private final MiddlewareClient middlewareClient;
    private UtilityGraph utilityGraph;


    public ComprehensiveWorkUnitProcessor(CliqueCheckServiceComprehensiveBitSet cliqueCheckService, RamseyConfig ramseyConfig, MiddlewareClient middlewareClient) {
        this.cliqueCheckService = cliqueCheckService;
        this.middlewareClient = middlewareClient;
        this.ramseyConfig = ramseyConfig;
    }

    @PostConstruct
    public void init() {
        utilityGraph = new UtilityGraph(ramseyConfig.getVertexCount());
    }

    private BitSet[] buildAdjacencyMatrix(List<Vertex> vertices, EdgeColor color) {
        int n = vertices.size();
        BitSet[] adjacency = new BitSet[n];
        for (int i = 0; i < n; i++) {
            adjacency[i] = new BitSet(n);
        }
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j && vertices.get(i).getEdgeColor(vertices.get(j)).equals(color)) {
                    adjacency[i].set(j);
                }
            }
        }
        return adjacency;
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
        List<Vertex> vertices = utilityGraph.getVertices();
        BitSet[] redAdjacency = buildAdjacencyMatrix(vertices, EdgeColor.RED);
        BitSet[] blueAdjacency = buildAdjacencyMatrix(vertices, EdgeColor.BLUE);
        List<Clique> derivedGraphCliques = new ArrayList<>();
        derivedGraphCliques.addAll(cliqueCheckService.getCliques(vertices, redAdjacency));
        derivedGraphCliques.addAll(cliqueCheckService.getCliques(vertices, blueAdjacency));
        log.info("Clique count for derived graph: {}", derivedGraphCliques.size());

        workUnit.setCliqueCount(derivedGraphCliques.size());
        workUnit.setCompletedDate(now());
        workUnit.setStatus(WorkUnitStatus.COMPLETE);


        log.debug("Reverting base graph");
        utilityGraph.flipsEdges(workUnit.getEdgesToFlip());

    }

}
