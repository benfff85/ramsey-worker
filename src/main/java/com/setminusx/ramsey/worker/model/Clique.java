package com.setminusx.ramsey.worker.model;

import lombok.Data;

import java.util.List;

@Data
public class Clique {

    private final List<Vertex> vertices;

    public Clique(List<Vertex> vertices) {
        this.vertices = vertices;
    }

    public boolean isValid() {
        EdgeColor color = vertices.get(0).getEdgeColor(vertices.get(1));
        for (int i = 0; i < 8; i++) {
            for (int j = i + 1; j < 8; j++) {
                if (!color.equals(vertices.get(i).getEdgeColor(vertices.get(j)))) {
                    return false;
                }
            }
        }
        return true;
    }

}
