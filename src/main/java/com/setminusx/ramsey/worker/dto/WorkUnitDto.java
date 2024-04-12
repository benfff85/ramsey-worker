package com.setminusx.ramsey.worker.dto;

import com.setminusx.ramsey.worker.model.WorkUnitAnalysisType;
import com.setminusx.ramsey.worker.model.WorkUnitEdge;
import com.setminusx.ramsey.worker.model.WorkUnitPriority;
import com.setminusx.ramsey.worker.model.WorkUnitStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@ToString(onlyExplicitlyIncluded = true)
public class WorkUnitDto {

    @ToString.Include
    private Integer id;
    private Integer subgraphSize;
    private Integer vertexCount;
    private Integer baseGraphId;
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
