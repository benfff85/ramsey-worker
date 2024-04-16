package com.setminusx.ramsey.worker.model;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class EdgeMappedCliqueCollection {

    private final Map<WorkUnitEdge, Integer> edgeMap;
    private Integer cliqueCount;

    public EdgeMappedCliqueCollection(Short vertexCount) {
        edgeMap = new HashMap<>();
        cliqueCount = 0;

        WorkUnitEdge edge;
        for (short i = 0; i < vertexCount; i++) {
            for (short j = (short) (i + 1); j < vertexCount; j++) {
                edge = new WorkUnitEdge();
                edge.setVertexOne(i);
                edge.setVertexTwo(j);
                edgeMap.put(edge, 0);
            }
        }
    }

    public Integer size() {
        return cliqueCount;
    }

    public void setCliques(List<Clique> inputCliques) {

        cliqueCount = inputCliques.size();

        for (WorkUnitEdge edge : edgeMap.keySet()) {
            edgeMap.replace(edge, 0);
        }

        short vertexAId;
        WorkUnitEdge edge = new WorkUnitEdge();
        int cliqueSize = inputCliques.getFirst().vertices().size();
        for (Clique clique : inputCliques) {
            for (short i = 0; i < cliqueSize; i++) {
                vertexAId = clique.vertices().get(i).getId();
                for (short j = (short) (i + 1); j < cliqueSize; j++) {
                    edge.setVertexOne(vertexAId);
                    edge.setVertexTwo(clique.vertices().get(j).getId());
                    edgeMap.replace(edge, edgeMap.get(edge) + 1);
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
            sum += edgeMap.get(edge);
        }
        return sum;
    }
}
