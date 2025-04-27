package com.setminusx.ramsey.worker.controller;

import com.setminusx.ramsey.worker.client.MiddlewareClient;
import com.setminusx.ramsey.worker.config.RamseyConfig;
import com.setminusx.ramsey.worker.model.*;
import com.setminusx.ramsey.worker.service.ComprehensiveCliqueCheckService;
import com.setminusx.ramsey.worker.service.TargetedCliqueCheckServiceBitSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.BitSet;
import java.util.List;

import static com.setminusx.ramsey.worker.utility.TimeUtility.now;

@Slf4j
@Component
public class TargetedWorkUnitProcessorBitSet implements WorkUnitProcessor {

    private final TargetedCliqueCheckServiceBitSet targetedCliqueCheckService;
    private final ComprehensiveCliqueCheckService comprehensiveCliqueCheckService;
    private final RamseyConfig ramseyConfig;
    private final MiddlewareClient middlewareClient;
    private EdgeMappedCliqueCollectionBitSet cliqueCollection;
    private BitSet[] baseAdjacency;
    private int baseVertexCount;
    private int baseGraphId = -1;

    public TargetedWorkUnitProcessorBitSet(TargetedCliqueCheckServiceBitSet targetedCliqueCheckService, ComprehensiveCliqueCheckService comprehensiveCliqueCheckService, RamseyConfig ramseyConfig, MiddlewareClient middlewareClient) {
        this.targetedCliqueCheckService = targetedCliqueCheckService;
        this.comprehensiveCliqueCheckService = comprehensiveCliqueCheckService;
        this.middlewareClient = middlewareClient;
        this.ramseyConfig = ramseyConfig;
    }

    @Override
    public void process(WorkUnit workUnit) {
        log.info("Processing WorkUnit: {}", workUnit);

        if (baseAdjacency == null || !workUnit.getBaseGraphId().equals(baseGraphId)) {
            Graph graph = middlewareClient.getGraphById(workUnit.getBaseGraphId());
            baseVertexCount = graph.getVertexCount();
            baseAdjacency = buildAdjacencyMatrixFromEdgeData(graph.getEdgeData(), baseVertexCount);
            baseGraphId = graph.getGraphId();
            // Use comprehensiveCliqueCheckService to get all cliques
            List<List<Integer>> cliques = comprehensiveCliqueCheckService.getCliques(baseVertexCount, baseAdjacency);
            cliqueCollection = new EdgeMappedCliqueCollectionBitSet((short) baseVertexCount);
            cliqueCollection.setCliques(cliques);
        }
        workUnit.setProcessingStartedDate(now());

        // Flip edges for this work unit
        BitSet[] derivedAdjacency = cloneAdjacencyMatrix(baseAdjacency);
        for (WorkUnitEdge edge : workUnit.getEdgesToFlip()) {
            int i = edge.getVertexOne();
            int j = edge.getVertexTwo();
            derivedAdjacency[i].flip(j);
            derivedAdjacency[j].flip(i);
        }

        // Compute the derived clique count using the targeted BitSet logic
        int brokenCliques = cliqueCollection.getCountOfCliquesContainingEdges(workUnit.getEdgesToFlip());
        int newCliques = targetedCliqueCheckService.getNewCliques(
                derivedAdjacency,
                baseVertexCount,
                ramseyConfig.getSubgraphSize(),
                workUnit.getEdgesToFlip(),
                cliqueCollection
        );
        int cliqueCount = cliqueCollection.size() - brokenCliques + newCliques;
        enrichWorkUnit(cliqueCount, workUnit);
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

    private BitSet[] cloneAdjacencyMatrix(BitSet[] adjacency) {
        BitSet[] clone = new BitSet[adjacency.length];
        for (int i = 0; i < adjacency.length; i++) {
            clone[i] = (BitSet) adjacency[i].clone();
        }
        return clone;
    }

    private void enrichWorkUnit(int cliqueCount, WorkUnit workUnit) {
        log.debug("Enriching work unit with analysis results");
        log.info("Clique count for derived graph: {}", cliqueCount);
        workUnit.setCliqueCount(cliqueCount);
        workUnit.setCompletedDate(now());
        workUnit.setStatus(WorkUnitStatus.COMPLETE);
    }
}