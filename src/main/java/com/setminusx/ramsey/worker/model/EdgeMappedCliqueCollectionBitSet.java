package com.setminusx.ramsey.worker.model;

import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class EdgeMappedCliqueCollectionBitSet {
    private final Map<WorkUnitEdge, Integer> edgeMap;
    private Integer cliqueCount;
    private List<List<Integer>> cliques;

    public EdgeMappedCliqueCollectionBitSet(Short vertexCount) {
        edgeMap = new HashMap<>();
        cliqueCount = 0;

        WorkUnitEdge edge;
        for (short i = 0; i < vertexCount; i++) {
            for (short j = (short) (i + 1); j < vertexCount; j++) {
                edge = new WorkUnitEdge();
                edge.setVertexOne(i);
                edge.setVertexTwo(j);
                edgeMap.put(new WorkUnitEdge(i, j), 0);
            }
        }
    }

    public Integer size() {
        return cliqueCount;
    }

    // Accepts List<List<Integer>> where each inner list is a clique of vertex ids
    public void setCliques(List<List<Integer>> inputCliques) {
        this.cliques = new ArrayList<>(inputCliques);
        cliqueCount = inputCliques.size();

        // Reset edgeMap counts
        for (WorkUnitEdge edge : edgeMap.keySet()) {
            edgeMap.put(edge, 0);
        }

        for (List<Integer> clique : inputCliques) {
            int cliqueSize = clique.size();
            for (int i = 0; i < cliqueSize; i++) {
                int vertexAId = clique.get(i);
                for (int j = i + 1; j < cliqueSize; j++) {
                    int vertexBId = clique.get(j);
                    WorkUnitEdge edge = new WorkUnitEdge((short) vertexAId, (short) vertexBId);
                    edgeMap.put(edge, edgeMap.getOrDefault(edge, 0) + 1);
                }
            }
        }
    }

    // Note this should only be called with one edge or two edges when they are of differing colors.
    // If called for two edges of the same color and both edges are in the same clique, the clique will
    // be accounted for twice in the sum returned.
    public int getCountOfCliquesContainingEdges(List<WorkUnitEdge> edges) {
        int sum = 0;
        for (WorkUnitEdge edge : edges) {
            sum += edgeMap.getOrDefault(edge, 0);
        }
        return sum;
    }

    public List<List<Integer>> getCliquesAsVertexIdLists() {
        return cliques != null ? cliques : List.of();
    }
}
