package com.setminusx.ramsey.worker.service;

import com.setminusx.ramsey.worker.model.Clique;
import com.setminusx.ramsey.worker.model.EdgeColor;
import com.setminusx.ramsey.worker.model.Graph;
import com.setminusx.ramsey.worker.model.Vertex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.setminusx.ramsey.worker.model.EdgeColor.BLUE;
import static com.setminusx.ramsey.worker.model.EdgeColor.RED;

@Component
public class CliqueCheckService {

    private Graph graph;
    private short vertexCount;
    private final List<Vertex> connectedVertices = new ArrayList<>();
    private final List<Clique> cliques = new ArrayList<>();

    @Value("${ramsey.subgraph-size}")
    private Short subgraphSize;


    public List<Clique> getCliques(Graph graph) {
        this.graph = graph;
        vertexCount = (short) graph.getVertices().size();
        connectedVertices.clear();
        cliques.clear();

        for (int i = 0; i < vertexCount; i++) {
            connectedVertices.add(graph.getVertices().get(i));
            findCliqueRecursive(connectedVertices, RED);
            connectedVertices.clear();
        }

        for (int i = 0; i < vertexCount; i++) {
            connectedVertices.add(graph.getVertices().get(i));
            findCliqueRecursive(connectedVertices, BLUE);
            connectedVertices.clear();
        }

        return cliques;

    }


    private void findCliqueRecursive(List<Vertex> connectedVertices, EdgeColor color) {

        // Loop through all vertices starting with the one after the last vertex in the chain
        for (short i = (short) (connectedVertices.get(connectedVertices.size() - 1).getId() + 1); i < vertexCount; i++) {
            // If the vertex being considered is connected
            if (isConnected(connectedVertices, graph.getVertexById(i), color)) {
                connectedVertices.add(graph.getVertexById(i));
                // If this and makes a completed clique add it to the clique collection
                if (connectedVertices.size() == subgraphSize) {
                    cliques.add(new Clique(new ArrayList<>(connectedVertices)));
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
            if (connectedVertex.getEdgeColor(vertex).equals(color)) {
                return false;
            }
        }
        return true;
    }

}
