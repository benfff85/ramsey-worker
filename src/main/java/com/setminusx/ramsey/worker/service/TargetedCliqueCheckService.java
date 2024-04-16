package com.setminusx.ramsey.worker.service;

import com.setminusx.ramsey.worker.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@Component
public class TargetedCliqueCheckService {

    private Graph graph;
    private short vertexCount;
    private final List<Vertex> connectedVertices = new ArrayList<>();
    private Integer cliqueCount;

    @Value("${ramsey.subgraph-size}")
    private Short subgraphSize;


    public Integer getCliques(Graph graph, List<WorkUnitEdge> compromisedEdges) {
        this.graph = graph;
        vertexCount = (short) graph.getVertices().size();
        connectedVertices.clear();
        cliqueCount = 0;
        Vertex v1;
        Vertex v2;
        EdgeColor color;

        for (WorkUnitEdge compromisedEdge : compromisedEdges) {
            v1 = graph.getVertexById(compromisedEdge.getVertexOne());
            v2 = graph.getVertexById(compromisedEdge.getVertexTwo());
            color = v1.getEdgeColor(v2);
            connectedVertices.add(v1);
            connectedVertices.add(v2);
            for (Vertex v3 : graph.getVertices()) {
                if (!v3.equals(v1) && !v3.equals(v2) && color.equals(v3.getEdgeColor(v1)) && color.equals(v3.getEdgeColor(v2))) {
                    connectedVertices.add(v3);
                    findCliqueRecursive(connectedVertices, color);
                    connectedVertices.remove(v3);
                }
            }
            connectedVertices.clear();
        }

        return cliqueCount;

    }


    private void findCliqueRecursive(List<Vertex> connectedVertices, EdgeColor color) {

        // Loop through all vertices starting with the one after the last vertex in the chain
        for (short i = (short) (connectedVertices.getLast().getId() + 1); i < vertexCount; i++) {
            // If the vertex being considered is connected
            if (i != connectedVertices.get(0).getId() && i != connectedVertices.get(1).getId() && isConnected(connectedVertices, graph.getVertexById(i), color)) {
                connectedVertices.add(graph.getVertexById(i));
                // If this and makes a completed clique add it to the clique collection
                if (connectedVertices.size() == subgraphSize) {
                    cliqueCount++;
                }
                // Otherwise if there are enough possible options left to form a clique proceed with
                // search. Optimize by adding second condition above.
                else {
                    findCliqueRecursive(connectedVertices, color);
                }
                // Remove this vertex from the chain and try the next at this level
                connectedVertices.remove(graph.getVertexById(i));
            }
        }
        // Once all have been tried at this level return
    }


    private static boolean isConnected(List<Vertex> connectedVertices, Vertex vertex, EdgeColor color) {
        for (Vertex connectedVertex : connectedVertices) {
            if (!connectedVertex.getEdgeColor(vertex).equals(color)) {
                return false;
            }
        }
        return true;
    }

}
