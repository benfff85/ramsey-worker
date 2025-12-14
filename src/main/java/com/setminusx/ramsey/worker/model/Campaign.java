package com.setminusx.ramsey.worker.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
