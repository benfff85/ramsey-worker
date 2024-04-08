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

import static com.setminusx.ramsey.worker.model.WorkUnitAnalysisType.COMPREHENSIVE;
import static com.setminusx.ramsey.worker.model.WorkUnitAnalysisType.TARGETED;


@Slf4j
@Component
public class WorkUnitRouter {

    private final WorkUnitService workUnitService;
    private final Queue<WorkUnitDto> workUnits;
    private final Map<WorkUnitAnalysisType, WorkUnitProcessor> processorMap;

    public WorkUnitRouter(WorkUnitService workUnitService, TargetedWorkUnitProcessor targetedWorkUnitProcessor, ComprehensiveWorkUnitProcessor comprehensiveWorkUnitProcessor, @Qualifier("workUnitQueue") Queue<WorkUnitDto> workUnits) {
        this.workUnitService = workUnitService;
        this.workUnits = workUnits;
        this.processorMap = Map.of(TARGETED, targetedWorkUnitProcessor, COMPREHENSIVE, comprehensiveWorkUnitProcessor);
    }

    @Scheduled(fixedDelay = 10000)
    public void process() {
        WorkUnitDto workUnit;

        workUnits.addAll(workUnitService.getWorkUnits());
        while (!workUnits.isEmpty()) {
            workUnit = workUnits.poll();
            processorMap.get(workUnit.getWorkUnitAnalysisType()).process(workUnit);
            workUnitService.publishBatch(workUnit);
            if (workUnits.isEmpty()) {
                log.info("Work unit queue is empty, attempting to fetch more work units.");
                workUnitService.flushPublishCache();
                workUnits.addAll(workUnitService.getWorkUnits());
            }
        }

        log.warn("Worker out of work, sleeping and trying again. Consider increasing the work unit creation rate.");

    }

}
