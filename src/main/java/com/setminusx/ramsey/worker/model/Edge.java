package com.setminusx.ramsey.worker.model;

import lombok.Data;

import static com.setminusx.ramsey.worker.model.EdgeColor.BLUE;
import static com.setminusx.ramsey.worker.model.EdgeColor.RED;

@Data
public class Edge {

    private Vertex vertexA;
    private Vertex vertexB;
    private EdgeColor color;

    public void flip() {
        if (BLUE.equals(color)) {
            color = RED;
        } else {
            color = BLUE;
        }
    }
}
