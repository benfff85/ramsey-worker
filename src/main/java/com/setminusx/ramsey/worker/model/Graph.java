package com.setminusx.ramsey.worker.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

import static com.setminusx.ramsey.worker.model.EdgeColor.BLUE;
import static com.setminusx.ramsey.worker.model.EdgeColor.RED;

@Slf4j
@Data
public class Graph {

    private List<Vertex> vertices;
    private List<Edge> edges;
    private Integer id;
    private String edgeData;

    public Graph(short vertexCount) {

        Edge edge;
        Vertex vertexA;
        Vertex vertexB;

        vertices = new ArrayList<>(vertexCount);
        edges = new ArrayList<>();

        for (short i = 0; i < vertexCount; i++) {
            vertices.add(new Vertex(i, vertexCount));
        }

        for (short i = 0; i < vertexCount; i++) {
            vertexA = vertices.get(i);
            vertexA.addEdge(null);
            for (short j = (short) (i + 1); j < vertexCount; j++) {
                vertexB = vertices.get(j);
                edge = new Edge();
                edges.add(edge);
                edge.setVertexA(vertexA);
                edge.setVertexB(vertexB);
                vertexA.addEdge(edge);
                vertexB.addEdge(edge);
            }
        }

        log.info("Graph initialized, vertex count: {}, edge count {}", vertices.size(), edges.size());
    }

    public void applyColoring(String edgeData, Integer id) {
        this.edgeData = edgeData;
        this.id = id;

        if (edgeData.length() != edges.size()) {
            log.warn("Applying coloring to graph of a different size");
        }

        for (int index = 0; index < edgeData.length(); index++) {
            if (edgeData.charAt(index) == '0') {
                edges.get(index).setColor(BLUE);
            } else {
                edges.get(index).setColor(RED);
            }
        }
        log.debug("Coloring applied");

    }

    public void flipEdge(short vertexOne, short vertexTwo) {
        vertices.get(vertexOne).getEdge(vertexTwo).flip();
    }

    public Vertex getVertexById(short id) {
        return vertices.get(id);
    }
}
