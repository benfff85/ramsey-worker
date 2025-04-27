package com.setminusx.ramsey.worker.utility;

import com.setminusx.ramsey.worker.model.WorkUnitEdge;

import java.util.BitSet;
import java.util.List;

public class BitSetMatrixUtils {

    // In-place inversion: flips all off-diagonal bits in the adjacency matrix
    public static void invertAdjacencyMatrixInPlace(BitSet[] adjacency) {
        int n = adjacency.length;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    adjacency[i].flip(j);
                }
            }
        }
    }

    // Deep clone of BitSet adjacency matrix
    public static BitSet[] cloneAdjacencyMatrix(BitSet[] adjacency) {
        int n = adjacency.length;
        BitSet[] clone = new BitSet[n];
        for (int i = 0; i < n; i++) {
            clone[i] = (BitSet) adjacency[i].clone();
        }
        return clone;
    }

    // Create a new BitSet adjacency matrix of given size
    public static BitSet[] createAdjacencyMatrix(int vertexCount) {
        BitSet[] matrix = new BitSet[vertexCount];
        for (int i = 0; i < vertexCount; i++) {
            matrix[i] = new BitSet(vertexCount);
        }
        return matrix;
    }

    // Create a new BitSet adjacency matrix from a bitstring (e.g., "001010...")
    public static BitSet[] buildAdjacencyMatrixFromBitString(String bitString, int vertexCount) {
        BitSet[] adjacency = createAdjacencyMatrix(vertexCount);
        int edgeIndex = 0;
        for (int i = 0; i < vertexCount; i++) {
            for (int j = i + 1; j < vertexCount; j++) {
                if (bitString.charAt(edgeIndex) == '1') {
                    adjacency[i].set(j);
                    adjacency[j].set(i);
                }
                edgeIndex++;
            }
        }
        return adjacency;
    }

    // Flip the given edges in the adjacency matrix (undirected)
    public static void flipEdges(BitSet[] adjacency, List<WorkUnitEdge> edges) {
        for (WorkUnitEdge edge : edges) {
            int i = edge.getVertexOne();
            int j = edge.getVertexTwo();
            adjacency[i].flip(j);
            adjacency[j].flip(i);
        }
    }

}
