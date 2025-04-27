package com.setminusx.ramsey.worker.service;

import com.setminusx.ramsey.worker.config.EnablePerfLogging;
import com.setminusx.ramsey.worker.model.EdgeMappedCliqueCollectionBitSet;
import com.setminusx.ramsey.worker.model.WorkUnitEdge;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TargetedCliqueCheckServiceBitSet {

    // Build a clique collection mapping edges to the cliques they appear in
    public EdgeMappedCliqueCollectionBitSet buildCliqueCollection(BitSet[] adjacency, int vertexCount, int cliqueSize) {
        EdgeMappedCliqueCollectionBitSet collection = new EdgeMappedCliqueCollectionBitSet((short) vertexCount);
        List<List<Integer>> cliques = findAllCliques(adjacency, vertexCount, cliqueSize);
        collection.setCliques(cliques);
        return collection;
    }

    // Find all cliques of size k in the provided adjacency
    public List<List<Integer>> findAllCliques(BitSet[] adjacency, int vertexCount, int cliqueSize) {
        List<List<Integer>> cliques = new ArrayList<>();
        BitSet R = new BitSet(vertexCount);
        BitSet P = new BitSet(vertexCount);
        BitSet X = new BitSet(vertexCount);
        P.set(0, vertexCount);
        bronKerbosch(R, P, X, adjacency, cliques, cliqueSize);
        return cliques;
    }

    // Bron-Kerbosch with clique size cutoff
    private void bronKerbosch(BitSet R, BitSet P, BitSet X, BitSet[] adjacency, List<List<Integer>> cliques, int cliqueSize) {
        if (R.cardinality() == cliqueSize) {
            List<Integer> clique = new ArrayList<>();
            for (int v = R.nextSetBit(0); v >= 0; v = R.nextSetBit(v + 1)) {
                clique.add(v);
            }
            cliques.add(clique);
            return;
        }
        if (R.cardinality() + P.cardinality() < cliqueSize) return;
        if (P.isEmpty()) return;
        BitSet candidates = (BitSet) P.clone();
        for (int v = candidates.nextSetBit(0); v >= 0; v = candidates.nextSetBit(v + 1)) {
            R.set(v);
            BitSet newP = (BitSet) P.clone();
            newP.and(adjacency[v]);
            bronKerbosch(R, newP, X, adjacency, cliques, cliqueSize);
            R.clear(v);
            P.clear(v);
            X.set(v);
        }
    }

    // Main method for a derived graph: count new cliques containing a flipped edge
    public int getCliques(BitSet[] derivedAdjacency, int vertexCount, int cliqueSize, List<WorkUnitEdge> flippedEdges, EdgeMappedCliqueCollectionBitSet cliqueCollection) {
        // Find all cliques in the derived graph containing any of the flipped edges
        List<List<Integer>> newCliques = findAllCliquesContainingEdges(derivedAdjacency, vertexCount, cliqueSize, flippedEdges);
        return newCliques.size();
    }

    // Find all cliques in the graph that contain at least one of the specified edges
    private List<List<Integer>> findAllCliquesContainingEdges(BitSet[] adjacency, int vertexCount, int cliqueSize, List<WorkUnitEdge> edges) {
        Set<String> edgeSet = new HashSet<>();
        for (WorkUnitEdge edge : edges) {
            int i = edge.getVertexOne();
            int j = edge.getVertexTwo();
            edgeSet.add(i < j ? i + "," + j : j + "," + i);
        }
        List<List<Integer>> cliques = new ArrayList<>();
        List<List<Integer>> allCliques = findAllCliques(adjacency, vertexCount, cliqueSize);
        for (List<Integer> clique : allCliques) {
            // Check if clique contains any flipped edge
            outer:
            for (int i = 0; i < clique.size(); i++) {
                for (int j = i + 1; j < clique.size(); j++) {
                    String key = clique.get(i) < clique.get(j)
                            ? clique.get(i) + "," + clique.get(j)
                            : clique.get(j) + "," + clique.get(i);
                    if (edgeSet.contains(key)) {
                        cliques.add(clique);
                        break outer;
                    }
                }
            }
        }
        return cliques;
    }

    // In-place inversion: flips all off-diagonal bits in the adjacency matrix
    private void invertAdjacencyMatrixInPlace(BitSet[] adjacency) {
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
    // Main method for a derived graph: count new cliques containing a flipped edge, but only those not in the base graph
    public int getNewCliques(BitSet[] derivedAdjacency, int vertexCount, int cliqueSize, List<WorkUnitEdge> flippedEdges, EdgeMappedCliqueCollectionBitSet baseCliqueCollection) {
        Set<String> baseCliqueSet = new HashSet<>();
        for (List<Integer> clique : baseCliqueCollection.getCliquesAsVertexIdLists()) {
            baseCliqueSet.add(cliqueKey(clique));
        }
        Set<String> foundNewCliques = new HashSet<>();
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
                bronKerboschNewCliques(R, P, X, derivedAdjacency, foundNewCliques, baseCliqueSet, cliqueSize);
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
                bronKerboschNewCliques(R, P, X, derivedAdjacency, foundNewCliques, baseCliqueSet, cliqueSize);
            }
        }
        // Restore adjacency to original (invert again)
        invertAdjacencyMatrixInPlace(derivedAdjacency);
        return foundNewCliques.size();
    }

    // Helper: Bron-Kerbosch for new cliques only
    private void bronKerboschNewCliques(BitSet R, BitSet P, BitSet X, BitSet[] adjacency, Set<String> foundNewCliques, Set<String> baseCliqueSet, int cliqueSize) {
        if (R.cardinality() == cliqueSize) {
            List<Integer> clique = new ArrayList<>();
            for (int v = R.nextSetBit(0); v >= 0; v = R.nextSetBit(v + 1)) clique.add(v);
            String key = cliqueKey(clique);
            if (!baseCliqueSet.contains(key)) foundNewCliques.add(key);
            return;
        }
        if (R.cardinality() + P.cardinality() < cliqueSize) return;
        if (P.isEmpty()) return;
        BitSet candidates = (BitSet) P.clone();
        for (int v = candidates.nextSetBit(0); v >= 0; v = candidates.nextSetBit(v + 1)) {
            R.set(v);
            BitSet newP = (BitSet) P.clone();
            newP.and(adjacency[v]);
            bronKerboschNewCliques(R, newP, X, adjacency, foundNewCliques, baseCliqueSet, cliqueSize);
            R.clear(v);
            P.clear(v);
            X.set(v);
        }
    }

    // Helper: canonical string key for a clique (sorted vertex ids)
    private String cliqueKey(List<Integer> clique) {
        List<Integer> sorted = new ArrayList<>(clique);
        Collections.sort(sorted);
        return sorted.toString();
    }
}