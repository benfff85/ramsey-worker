package com.setminusx.ramsey.worker.service;

import com.setminusx.ramsey.worker.client.MiddlewareClient;
import com.setminusx.ramsey.worker.config.RamseyConfig;
import com.setminusx.ramsey.worker.controller.ClientRegister;
import com.setminusx.ramsey.worker.model.WorkUnit;
import com.setminusx.ramsey.worker.model.WorkUnitStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Slf4j
@Service
public class WorkUnitService {

    private final Integer fetchSize;
    private final Integer publishSize;
    private final String clientId;

    private final MiddlewareClient middlewareClient;
    private final List<WorkUnit> workUnitsToPublish = new LinkedList<>();

    public WorkUnitService(ClientRegister clientRegister, RamseyConfig ramseyConfig, MiddlewareClient middlewareClient) {
        this.middlewareClient = middlewareClient;
        this.fetchSize = ramseyConfig.getWorkUnit().getQueue().getFetchSize();
        this.publishSize = ramseyConfig.getWorkUnit().getQueue().getPublishSize();
        this.clientId = ramseyConfig.getClientId();
    }


    public List<WorkUnit> getWorkUnits() {
        log.info("Fetching work units, fetchSize: {}", fetchSize);
        List<WorkUnit> workUnits = middlewareClient.getWorkUnitsByAssignedClientAndStatus(clientId, WorkUnitStatus.ASSIGNED, fetchSize);
        if (workUnits.isEmpty()) {
            log.info("No work units found");
            return Collections.emptyList();
        }
        log.info("Work units fetched, count: {}", workUnits.size());
        return workUnits;
    }

    public void publishBatch(WorkUnit workUnit) {
        workUnitsToPublish.add(workUnit);
        if (workUnitsToPublish.size() >= publishSize) {
            flushPublishCache();
        }
    }

    public void flushPublishCache() {
        if (!workUnitsToPublish.isEmpty()) {
            log.info("Saving work units, count: {}", workUnitsToPublish.size());
            middlewareClient.updateWorkUnits(workUnitsToPublish);
            workUnitsToPublish.clear();
        }
    }
}
