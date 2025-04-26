package com.setminusx.ramsey.worker.config;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnablePerfLogging {
}