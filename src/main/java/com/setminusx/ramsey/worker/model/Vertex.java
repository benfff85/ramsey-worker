package com.setminusx.ramsey.worker.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Vertex {

    private short id;
    private List<Edge> edges;

    public Vertex(short id, short vertexCount) {
        this.id = id;
        edges = new ArrayList<>(vertexCount);
    }

    public void addEdge(Edge edge) {
        edges.add(edge);
    }

    public Edge getEdge(short vertexId) {
        return edges.get(vertexId);
    }

    public Edge getEdge(Vertex vertex) {
        return edges.get(vertex.getId());
    }

    public EdgeColor getEdgeColor(Vertex vertex) {
        return getEdge(vertex).getColor();
    }

}
