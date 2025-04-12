package com.setminusx.ramsey.worker.controller;

import com.setminusx.ramsey.worker.model.WorkUnit;

public interface WorkUnitProcessor {
    void process(WorkUnit workUnit);
}
