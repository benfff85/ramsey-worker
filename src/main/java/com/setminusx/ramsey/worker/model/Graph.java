package com.setminusx.ramsey.worker.model;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class Graph {

    private Integer graphId;
    private Integer subgraphSize;
    private Integer vertexCount;
    private String edgeData;
    private Integer cliqueCount;
    private Date identifiedDate;

}
