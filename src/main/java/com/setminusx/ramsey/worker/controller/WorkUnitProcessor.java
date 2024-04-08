package com.setminusx.ramsey.worker.controller;

import com.setminusx.ramsey.worker.dto.WorkUnitDto;

public interface WorkUnitProcessor {
    void process(WorkUnitDto workUnit);
}
