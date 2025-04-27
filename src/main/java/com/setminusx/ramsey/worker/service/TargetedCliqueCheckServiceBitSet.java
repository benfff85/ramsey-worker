package com.setminusx.ramsey.worker.service;

import com.setminusx.ramsey.worker.config.EnablePerfLogging;
import com.setminusx.ramsey.worker.model.EdgeMappedCliqueCollectionBitSet;
import com.setminusx.ramsey.worker.model.WorkUnitEdge;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class TargetedCliqueCheckServiceBitSet {

    // In-place inversion: flips all off-diagonal bits in the adjacency matrix
    public void invertAdjacencyMatrixInPlace(BitSet[] adjacency) {
        int n = adjacency.length;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    adjacency[i].flip(j);
                }
            }
        }
    }

    @EnablePerfLogging
    // Main method for a derived graph: count new cliques containing a flipped edge, by starting recursion with the two vertices of each flipped edge
    public int getNewCliques(BitSet[] derivedAdjacency, int vertexCount, int cliqueSize, List<WorkUnitEdge> flippedEdges, EdgeMappedCliqueCollectionBitSet baseCliqueCollection) {
        int newCliqueCount = 0;
        // RED (current adjacency)
        for (WorkUnitEdge edge : flippedEdges) {
            int v1 = edge.getVertexOne();
            int v2 = edge.getVertexTwo();
            if (derivedAdjacency[v1].get(v2)) {
                BitSet R = new BitSet(vertexCount);
                R.set(v1); R.set(v2);
                BitSet P = (BitSet) derivedAdjacency[v1].clone();
                P.and(derivedAdjacency[v2]);
                P.clear(v1); P.clear(v2);
                BitSet X = new BitSet(vertexCount);
                newCliqueCount += bronKerboschCount(R, P, X, derivedAdjacency, cliqueSize);
            }
        }
        // BLUE (inverted adjacency)
        invertAdjacencyMatrixInPlace(derivedAdjacency);
        for (WorkUnitEdge edge : flippedEdges) {
            int v1 = edge.getVertexOne();
            int v2 = edge.getVertexTwo();
            if (derivedAdjacency[v1].get(v2)) {
                BitSet R = new BitSet(vertexCount);
                R.set(v1); R.set(v2);
                BitSet P = (BitSet) derivedAdjacency[v1].clone();
                P.and(derivedAdjacency[v2]);
                P.clear(v1); P.clear(v2);
                BitSet X = new BitSet(vertexCount);
                newCliqueCount += bronKerboschCount(R, P, X, derivedAdjacency, cliqueSize);
            }
        }
        // Restore adjacency to original (invert again)
        invertAdjacencyMatrixInPlace(derivedAdjacency);
        return newCliqueCount;
    }

    // Bron-Kerbosch variant: returns count of cliques of size k found from R
    private int bronKerboschCount(BitSet R, BitSet P, BitSet X, BitSet[] adjacency, int cliqueSize) {
        if (R.cardinality() == cliqueSize) {
            return 1;
        }
        if (R.cardinality() + P.cardinality() < cliqueSize) return 0;
        if (P.isEmpty()) return 0;
        int count = 0;
        BitSet candidates = (BitSet) P.clone();
        for (int v = candidates.nextSetBit(0); v >= 0; v = candidates.nextSetBit(v + 1)) {
            R.set(v);
            BitSet newP = (BitSet) P.clone();
            newP.and(adjacency[v]);
            count += bronKerboschCount(R, newP, X, adjacency, cliqueSize);
            R.clear(v);
            P.clear(v);
            X.set(v);
        }
        return count;
    }
}