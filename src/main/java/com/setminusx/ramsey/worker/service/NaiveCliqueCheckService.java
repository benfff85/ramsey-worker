package com.setminusx.ramsey.worker.service;

import com.setminusx.ramsey.worker.config.RamseyConfig;
import com.setminusx.ramsey.worker.model.*;
import com.setminusx.ramsey.worker.utility.UtilityGraph;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.util.Combinations;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.stream;

@Slf4j
@Component
public class NaiveCliqueCheckService {

    private final List<Vertex> vertices = new ArrayList<>();
    private final List<Clique> cliques = new ArrayList<>();
    private final Short subgraphSize;

    public NaiveCliqueCheckService(RamseyConfig ramseyConfig) {
        this.subgraphSize = ramseyConfig.getSubgraphSize();
    }


    public List<Clique> getCliques(UtilityGraph utilityGraph) {
        short vertexCount = (short) utilityGraph.getVertices().size();
        cliques.clear();

        Combinations combinations = new Combinations(vertexCount, subgraphSize);
        for (int[] combination : combinations) {
            log.info("Combination: {}", combination);
            vertices.clear();
            stream(combination).forEach(index -> vertices.add(utilityGraph.getVertices().get(index)));
            if(isClique(vertices)) {
                log.info("Clique found: {}", vertices);
                cliques.add(new Clique(new ArrayList<>(vertices)));
            }
        }

        return cliques;

    }


    private boolean isClique(List<Vertex> vertices) {
        EdgeColor color = vertices.get(0).getEdgeColor(vertices.get(1));
        for (Vertex vertex : vertices) {
            for (Vertex otherVertex : vertices) {
                if (!vertex.equals(otherVertex) && !vertex.getEdgeColor(otherVertex).equals(color)) {
                    return false;
                }
            }
        }
        return true;
    }

}
