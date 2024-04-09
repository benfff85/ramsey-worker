package com.setminusx.ramsey.worker.controller;

import com.setminusx.ramsey.worker.dto.GraphDto;
import com.setminusx.ramsey.worker.dto.WorkUnitDto;
import com.setminusx.ramsey.worker.model.Clique;
import com.setminusx.ramsey.worker.model.Graph;
import com.setminusx.ramsey.worker.model.WorkUnitStatus;
import com.setminusx.ramsey.worker.service.CliqueCheckService;
import com.setminusx.ramsey.worker.service.GraphService;
import com.setminusx.ramsey.worker.utility.GraphUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.setminusx.ramsey.worker.utility.TimeUtility.now;


@Slf4j
@Component
public class ComprehensiveWorkUnitProcessor implements WorkUnitProcessor {

    @Value("${ramsey.vertex-count}")
    private Short vertexCount;
    private final GraphService graphService;
    private final CliqueCheckService cliqueCheckService;
    private Graph graph;


    public ComprehensiveWorkUnitProcessor(GraphService graphService, CliqueCheckService cliqueCheckService) {
        this.graphService = graphService;
        this.cliqueCheckService = cliqueCheckService;
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
            List<Clique> derivedGraphCliques = cliqueCheckService.getCliques(graph);
            workUnit.setCliqueCount(derivedGraphCliques.size());
            workUnit.setCompletedDate(now());
            workUnit.setStatus(WorkUnitStatus.COMPLETE);

            log.debug("Reverting base graph");
            GraphUtil.flipEdges(graph, workUnit.getEdgesToFlip());

    }

}
