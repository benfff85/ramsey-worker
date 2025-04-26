package com.setminusx.ramsey.worker.controller;

import com.setminusx.ramsey.worker.client.MiddlewareClient;
import com.setminusx.ramsey.worker.model.*;
import com.setminusx.ramsey.worker.service.CliqueCheckServiceComprehensiveBitSet;
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

    private final CliqueCheckServiceComprehensiveBitSet cliqueCheckService;
    private final MiddlewareClient middlewareClient;

    public ComprehensiveWorkUnitProcessor(CliqueCheckServiceComprehensiveBitSet cliqueCheckService, MiddlewareClient middlewareClient) {
        this.cliqueCheckService = cliqueCheckService;
        this.middlewareClient = middlewareClient;
    }

    private BitSet[] buildAdjacencyMatrixFromEdgeData(String edgeData, int vertexCount) {
        BitSet[] adjacency = new BitSet[vertexCount];
        for (int i = 0; i < vertexCount; i++) {
            adjacency[i] = new BitSet(vertexCount);
        }
        int edgeIndex = 0;
        for (int i = 0; i < vertexCount; i++) {
            for (int j = i + 1; j < vertexCount; j++) {
                if (edgeData.charAt(edgeIndex) == '1') {
                    adjacency[i].set(j);
                    adjacency[j].set(i);
                }
                edgeIndex++;
            }
        }
        return adjacency;
    }

    @Override
    public void process(WorkUnit workUnit) {
        log.info("Processing WorkUnit: {}", workUnit);

        Graph graph = middlewareClient.getGraphById(workUnit.getBaseGraphId());
        int vertexCount = graph.getVertexCount();
        String edgeData = graph.getEdgeData();
        List<WorkUnitEdge> edgesToFlip = workUnit.getEdgesToFlip();

        // Flip the specified edges in edgeData
        char[] edgeDataArr = edgeData.toCharArray();
        for (WorkUnitEdge edge : edgesToFlip) {
            int i = edge.getVertexOne();
            int j = edge.getVertexTwo();
            int edgeIndex = (i * vertexCount - (i * (i + 1)) / 2) + (j - i - 1);
            edgeDataArr[edgeIndex] = (edgeDataArr[edgeIndex] == '1') ? '0' : '1';
        }
        String flippedEdgeData = new String(edgeDataArr);

        BitSet[] redAdjacency = buildAdjacencyMatrixFromEdgeData(flippedEdgeData, vertexCount);
        List<List<Integer>> derivedGraphCliques = cliqueCheckService.getCliques(vertexCount, redAdjacency);
        log.info("Clique count for derived graph: {}", derivedGraphCliques.size());
        workUnit.setCliqueCount(derivedGraphCliques.size());
        workUnit.setCompletedDate(now());
        workUnit.setStatus(WorkUnitStatus.COMPLETE);
    }
}
