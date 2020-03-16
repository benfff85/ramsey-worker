package com.setminusx.ramsey.worker.config;

import com.setminusx.ramsey.worker.dto.WorkUnitDto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

@Configuration
public class WorkUnitQueueConfig {

    @Bean(name = "workUnitQueue")
    public Queue<WorkUnitDto> linkedBlockingQueue() {
        return new LinkedBlockingQueue<>();
    }

}


