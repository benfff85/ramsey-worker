package com.setminusx.ramsey.worker.service;

import com.setminusx.ramsey.worker.dto.WorkUnitDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Queue;

@Slf4j
@Service
public class QueueFeederService {

    @Value("${ramsey.work-unit.queue.feeder.minimum-count}")
    private Integer queueMinimum;

    private WorkUnitService workUnitService;
    private Queue<WorkUnitDto> queue;

    public QueueFeederService(WorkUnitService workUnitService, @Qualifier("workUnitQueue") Queue<WorkUnitDto> queue) {
        this.workUnitService = workUnitService;
        this.queue = queue;
    }

    @Scheduled(fixedRateString = "${ramsey.work-unit.queue.feeder.frequency-in-millis}")
    public void feedQueue() {
        if (queue.size() < queueMinimum) {
            log.info("Work unit queue size is {}, fetching more work units as this is below the defined minimum threshold of {}", queue.size(), queueMinimum);
            queue.addAll(workUnitService.getWorkUnits());
        }
    }

}
