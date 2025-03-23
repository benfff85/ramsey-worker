package com.setminusx.ramsey.worker.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Campaign {

    private Integer campaignId;
    private Integer subgraphSize;
    private Integer vertexCount;
    private Strategy strategy;
    private Status status;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    public enum Status {
        ACTIVE,
        INACTIVE
    }

    public enum Strategy {
        COMPREHENSIVE_EDGE_PAIR_MUTATION
    }

}
