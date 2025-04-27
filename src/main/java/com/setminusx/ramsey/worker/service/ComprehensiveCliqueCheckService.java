package com.setminusx.ramsey.worker.service;

import com.setminusx.ramsey.worker.config.RamseyConfig;
import com.setminusx.ramsey.worker.utility.BitSetMatrixUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

@Component
public class ComprehensiveCliqueCheckService {
    private final Short subgraphSize;

    public ComprehensiveCliqueCheckService(RamseyConfig ramseyConfig) {
        this.subgraphSize = ramseyConfig.getSubgraphSize();
    }

    public List<List<Integer>> getCliques(int vertexCount, BitSet[] adjacency) {
        List<List<Integer>> cliques = new ArrayList<>();
        // RED cliques
        BitSet R = new BitSet(vertexCount);
        BitSet P = new BitSet(vertexCount);
        BitSet X = new BitSet(vertexCount);
        P.set(0, vertexCount);
        bronKerbosch(R, P, X, adjacency, cliques);
        // BLUE cliques (inverted adjacency)
        BitSetMatrixUtils.invertAdjacencyMatrixInPlace(adjacency);
        R.clear(); P.set(0, vertexCount); X.clear();
        bronKerbosch(R, P, X, adjacency, cliques);
        return cliques;
    }

    private void bronKerbosch(BitSet R, BitSet P, BitSet X, BitSet[] adjacency, List<List<Integer>> cliques) {
        if (R.cardinality() == subgraphSize) {
            List<Integer> cliqueVertices = new ArrayList<>();
            for (int v = R.nextSetBit(0); v >= 0; v = R.nextSetBit(v + 1)) {
                cliqueVertices.add(v);
            }
            cliques.add(cliqueVertices);
            return;
        }
        if (R.cardinality() + P.cardinality() < subgraphSize) return;
        if (P.isEmpty()) return;
        BitSet candidates = (BitSet) P.clone();
        for (int v = candidates.nextSetBit(0); v >= 0; v = candidates.nextSetBit(v + 1)) {
            R.set(v);
            BitSet newP = (BitSet) P.clone();
            newP.and(adjacency[v]);
            bronKerbosch(R, newP, X, adjacency, cliques);
            R.clear(v);
            P.clear(v);
            X.set(v);
        }
    }
}
