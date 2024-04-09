package com.setminusx.ramsey.worker.controller;

import com.setminusx.ramsey.worker.dto.GraphDto;
import com.setminusx.ramsey.worker.dto.WorkUnitDto;
import com.setminusx.ramsey.worker.model.Clique;
import com.setminusx.ramsey.worker.model.Graph;
import com.setminusx.ramsey.worker.model.WorkUnitStatus;
import com.setminusx.ramsey.worker.service.GraphService;
import com.setminusx.ramsey.worker.service.NaiveCliqueCheckService;
import com.setminusx.ramsey.worker.utility.GraphUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.setminusx.ramsey.worker.utility.TimeUtility.now;


@Slf4j
@Component
public class NaiveWorkUnitProcessor implements WorkUnitProcessor {

    @Value("${ramsey.vertex-count}")
    private Short vertexCount;
    private final GraphService graphService;
    private final NaiveCliqueCheckService naiveCliqueCheckService;
    private Graph graph;


    public NaiveWorkUnitProcessor(GraphService graphService, NaiveCliqueCheckService naiveCliqueCheckService) {
        this.graphService = graphService;
        this.naiveCliqueCheckService = naiveCliqueCheckService;
    }

    @PostConstruct
    public void init() {
        graph = new Graph(vertexCount);
    }

    @Override
    public void process(WorkUnitDto workUnit) {
            log.info("Processing WorkUnit: {}", workUnit);

            if (!workUnit.getBaseGraphId().equals(graph.getId())) {
                GraphDto graphDto = graphService.getGraphById(workUnit.getBaseGraphId());
                graph.applyColoring(graphDto.getEdgeData(), graphDto.getGraphId());
            }
            workUnit.setProcessingStartedDate(now());

            log.debug("Flipping edges");
            GraphUtil.flipEdges(graph, workUnit.getEdgesToFlip());

            log.debug("Checking for cliques in derived graph");
            List<Clique> derivedGraphCliques = naiveCliqueCheckService.getCliques(graph);
            workUnit.setCliqueCount(derivedGraphCliques.size());
            workUnit.setCompletedDate(now());
            workUnit.setStatus(WorkUnitStatus.COMPLETE);

            log.debug("Reverting base graph");
            GraphUtil.flipEdges(graph, workUnit.getEdgesToFlip());

    }

}
