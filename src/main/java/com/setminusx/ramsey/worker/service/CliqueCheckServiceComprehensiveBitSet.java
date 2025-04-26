package com.setminusx.ramsey.worker.service;

import com.setminusx.ramsey.worker.config.EnablePerfLogging;
import com.setminusx.ramsey.worker.config.RamseyConfig;
import com.setminusx.ramsey.worker.model.Clique;
import com.setminusx.ramsey.worker.model.Vertex;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

@Component
public class CliqueCheckServiceComprehensiveBitSet {
    private final Short subgraphSize;

    public CliqueCheckServiceComprehensiveBitSet(RamseyConfig ramseyConfig) {
        this.subgraphSize = ramseyConfig.getSubgraphSize();
    }

    @EnablePerfLogging
    public List<Clique> getCliques(List<Vertex> vertices, BitSet[] adjacency) {
        List<Clique> cliques = new ArrayList<>();
        int n = vertices.size();
        BitSet R = new BitSet(n);
        BitSet P = new BitSet(n);
        BitSet X = new BitSet(n);
        P.set(0, n);
        bronKerbosch(R, P, X, adjacency, cliques, vertices);
        return cliques;
    }

    private void bronKerbosch(BitSet R, BitSet P, BitSet X, BitSet[] adjacency, List<Clique> cliques, List<Vertex> vertices) {
        if (R.cardinality() == subgraphSize) {
            List<Vertex> cliqueVertices = new ArrayList<>();
            for (int v = R.nextSetBit(0); v >= 0; v = R.nextSetBit(v + 1)) {
                cliqueVertices.add(vertices.get(v));
            }
            cliques.add(new Clique(cliqueVertices));
            return; // Do not continue expanding this clique
        }
        if (R.cardinality() + P.cardinality() < subgraphSize) return; // pruning
        if (P.isEmpty()) return; // No more candidates to expand

        BitSet candidates = (BitSet) P.clone();
        for (int v = candidates.nextSetBit(0); v >= 0; v = candidates.nextSetBit(v + 1)) {
            R.set(v);
            BitSet newP = (BitSet) P.clone();
            newP.and(adjacency[v]);
            bronKerbosch(R, newP, X, adjacency, cliques, vertices);
            R.clear(v);
            P.clear(v);
            X.set(v);
        }
    }
}
