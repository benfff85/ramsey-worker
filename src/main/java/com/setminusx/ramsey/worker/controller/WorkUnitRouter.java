package com.setminusx.ramsey.worker.controller;

import com.setminusx.ramsey.worker.dto.WorkUnitDto;
import com.setminusx.ramsey.worker.model.WorkUnitAnalysisType;
import com.setminusx.ramsey.worker.service.WorkUnitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Queue;

import static com.setminusx.ramsey.worker.model.WorkUnitAnalysisType.*;

@Slf4j
@Component
public class WorkUnitRouter {

    private final WorkUnitService workUnitService;
    private final Queue<WorkUnitDto> workUnits;
    private final Map<WorkUnitAnalysisType, WorkUnitProcessor> processorMap;

    public WorkUnitRouter(WorkUnitService workUnitService, TargetedWorkUnitProcessor targetedWorkUnitProcessor, ComprehensiveWorkUnitProcessor comprehensiveWorkUnitProcessor, NaiveWorkUnitProcessor naiveWorkUnitProcessor, @Qualifier("workUnitQueue") Queue<WorkUnitDto> workUnits) {
        this.workUnitService = workUnitService;
        this.workUnits = workUnits;
        this.processorMap = Map.of(
                NAIVE, naiveWorkUnitProcessor,
                TARGETED, targetedWorkUnitProcessor,
                COMPREHENSIVE, comprehensiveWorkUnitProcessor);
    }

    @Scheduled(fixedDelayString = "${ramsey.work-unit.router.frequency-in-millis}")
    public void process() {

        workUnits.addAll(workUnitService.getWorkUnits());
        if(workUnits.isEmpty()) {
            log.warn("Worker out of work, sleeping and trying again. Consider increasing the work unit creation rate.");
            return;
        }

        WorkUnitDto workUnit;
        while (!workUnits.isEmpty()) {
            workUnit = workUnits.poll();
            processorMap.get(workUnit.getWorkUnitAnalysisType()).process(workUnit);
            workUnitService.publishBatch(workUnit);
        }

        workUnitService.flushPublishCache();

    }

}
