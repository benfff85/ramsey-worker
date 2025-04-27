package com.setminusx.ramsey.worker.controller;

import com.setminusx.ramsey.worker.client.MiddlewareClient;
import com.setminusx.ramsey.worker.config.RamseyConfig;
import com.setminusx.ramsey.worker.model.*;
import com.setminusx.ramsey.worker.service.ComprehensiveCliqueCheckService;
import com.setminusx.ramsey.worker.service.TargetedCliqueCheckServiceBitSet;
import com.setminusx.ramsey.worker.utility.BitSetMatrixUtils;
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
            baseAdjacency = BitSetMatrixUtils.buildAdjacencyMatrixFromBitString(graph.getEdgeData(), baseVertexCount);
            baseGraphId = graph.getGraphId();
            // Use comprehensiveCliqueCheckService to get all cliques
            List<List<Integer>> cliques = comprehensiveCliqueCheckService.getCliques(baseVertexCount, baseAdjacency);
            cliqueCollection = new EdgeMappedCliqueCollectionBitSet((short) baseVertexCount);
            cliqueCollection.setCliques(cliques);
        }
        workUnit.setProcessingStartedDate(now());

        // Flip edges for this work unit
        BitSet[] derivedAdjacency = BitSetMatrixUtils.cloneAdjacencyMatrix(baseAdjacency);
        BitSetMatrixUtils.flipEdges(derivedAdjacency, workUnit.getEdgesToFlip());

        // Compute the derived clique count using the targeted BitSet logic
        int brokenCliques = cliqueCollection.getCountOfCliquesContainingEdges(workUnit.getEdgesToFlip());
        int newCliques = targetedCliqueCheckService.getNewCliques(
                derivedAdjacency,
                baseVertexCount,
                ramseyConfig.getSubgraphSize(),
                workUnit.getEdgesToFlip()
        );
        int cliqueCount = cliqueCollection.size() - brokenCliques + newCliques;
        enrichWorkUnit(cliqueCount, workUnit);
    }

    private void enrichWorkUnit(int cliqueCount, WorkUnit workUnit) {
        log.debug("Enriching work unit with analysis results");
        log.info("Clique count for derived graph: {}", cliqueCount);
        workUnit.setCliqueCount(cliqueCount);
        workUnit.setCompletedDate(now());
        workUnit.setStatus(WorkUnitStatus.COMPLETE);
    }
}