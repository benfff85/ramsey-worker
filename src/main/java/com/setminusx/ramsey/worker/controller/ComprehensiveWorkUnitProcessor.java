package com.setminusx.ramsey.worker.controller;

import com.setminusx.ramsey.worker.client.MiddlewareClient;
import com.setminusx.ramsey.worker.model.*;
import com.setminusx.ramsey.worker.service.ComprehensiveCliqueCheckService;
import com.setminusx.ramsey.worker.utility.BitSetMatrixUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.BitSet;
import java.util.List;

import static com.setminusx.ramsey.worker.utility.TimeUtility.now;

/**
 * This class is responsible for processing work units in a comprehensive manner.
 * It uses the BitSet-based logic for clique checking and adjacency matrix construction.
 */
@Slf4j
@Component
public class ComprehensiveWorkUnitProcessor implements WorkUnitProcessor {

    private final ComprehensiveCliqueCheckService cliqueCheckService;
    private final MiddlewareClient middlewareClient;

    public ComprehensiveWorkUnitProcessor(ComprehensiveCliqueCheckService cliqueCheckService, MiddlewareClient middlewareClient) {
        this.cliqueCheckService = cliqueCheckService;
        this.middlewareClient = middlewareClient;
    }

    @Override
    public void process(WorkUnit workUnit) {
        log.info("Processing WorkUnit: {}", workUnit);

        Graph graph = middlewareClient.getGraphById(workUnit.getBaseGraphId());
        int vertexCount = graph.getVertexCount();
        List<WorkUnitEdge> edgesToFlip = workUnit.getEdgesToFlip();

        BitSet[] redAdjacency = BitSetMatrixUtils.buildAdjacencyMatrixFromBitString(graph.getEdgeData(), vertexCount);
        BitSetMatrixUtils.flipEdges(redAdjacency, edgesToFlip);
        List<List<Integer>> derivedGraphCliques = cliqueCheckService.getCliques(vertexCount, redAdjacency);
        log.info("Clique count for derived graph: {}", derivedGraphCliques.size());
        workUnit.setCliqueCount(derivedGraphCliques.size());
        workUnit.setCompletedDate(now());
        workUnit.setStatus(WorkUnitStatus.COMPLETE);
    }
}
