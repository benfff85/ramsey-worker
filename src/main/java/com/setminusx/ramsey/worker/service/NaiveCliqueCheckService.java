package com.setminusx.ramsey.worker.service;

import com.setminusx.ramsey.worker.model.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.util.Combinations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.stream;

@Slf4j
@Component
public class NaiveCliqueCheckService {

    private final List<Vertex> vertices = new ArrayList<>();
    private final List<Clique> cliques = new ArrayList<>();

    @Value("${ramsey.subgraph-size}")
    private Short subgraphSize;


    public List<Clique> getCliques(Graph graph) {
        short vertexCount = (short) graph.getVertices().size();
        cliques.clear();

        Combinations combinations = new Combinations(vertexCount, subgraphSize);
        for (int[] combination : combinations) {
            log.info("Combination: {}", combination);
            vertices.clear();
            // TODO reinitialize vertices instead of clearing
            stream(combination).forEach(index -> vertices.add(graph.getVertices().get(index)));
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
