package com.setminusx.ramsey.worker.dto;

import com.setminusx.ramsey.worker.model.WorkUnitEdge;
import com.setminusx.ramsey.worker.model.WorkUnitPriority;
import com.setminusx.ramsey.worker.model.WorkUnitStatus;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class WorkUnitDto {

    private Integer id;
    private Integer subgraphSize;
    private Integer vertexCount;
    private Integer baseGraphId;
    private List<WorkUnitEdge> edgesToFlip;
    private WorkUnitStatus status;
    private Integer cliqueCount;
    private Date createdDate;
    private Date assignedDate;
    private Date processingStartedDate;
    private Date completedDate;
    private String assignedClient;
    private WorkUnitPriority priority;

}
