package com.setminusx.ramsey.worker.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@ToString(onlyExplicitlyIncluded = true)
public class WorkUnit {

    @ToString.Include
    private Integer id;
    private Integer baseGraphId;
    private Integer stageId;
    @ToString.Include
    private List<WorkUnitEdge> edgesToFlip;
    private WorkUnitStatus status;
    private Integer cliqueCount;
    private LocalDateTime createdDate;
    private LocalDateTime assignedDate;
    private LocalDateTime processingStartedDate;
    private LocalDateTime completedDate;
    private String assignedClient;
    private WorkUnitPriority priority;
    @ToString.Include
    private WorkUnitAnalysisType workUnitAnalysisType;

}
