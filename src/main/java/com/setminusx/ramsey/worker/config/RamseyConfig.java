package com.setminusx.ramsey.worker.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ramsey")
public class RamseyConfig {

    private Short vertexCount;
    private Short subgraphSize;
    private Integer campaignId;
    private String clientId;

    private Mw mw;
    private Client client;
    private Graph graph;
    private WorkUnit workUnit;
    private Campaign campaign;
    private Stage stage;

    @Data
    public static class Mw {
        private String host;
    }

    @Data
    public static class Client {
        private String url;
        private Registration registration;

        @Data
        public static class Registration {
            private PhoneHome phoneHome;

            @Data
            public static class PhoneHome {
                private Long frequencyInMillis;
            }

        }
    }

    @Data
    public static class Graph {
        private String url;
    }

    @Data
    public static class WorkUnit {
        private Queue queue;

        @Data
        public static class Queue {
            private String url;
            private Integer fetchSize;
            private Integer publishSize;
        }
    }

    @Data
    public static class Campaign {
        private String url;
    }

    @Data
    public static class Stage {
        private String url;
    }

}