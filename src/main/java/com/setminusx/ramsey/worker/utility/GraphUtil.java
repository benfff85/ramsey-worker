package com.setminusx.ramsey.worker.utility;

import com.setminusx.ramsey.worker.model.Graph;
import com.setminusx.ramsey.worker.model.WorkUnitEdge;

import java.util.List;

public class GraphUtil {

    private GraphUtil() {
        // Masking default public constructor
    }

    public static void flipEdges(Graph graph, List<WorkUnitEdge> edges) {
        for (WorkUnitEdge edge : edges) {
            graph.flipEdge(edge.getVertexOne(), edge.getVertexTwo());
        }
    }


}
