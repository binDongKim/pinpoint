package com.navercorp.pinpoint.collector.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ScatterConfiguration {
    public enum ServerSideScan {
        v1,
        v2 // enable saltkey for fuzzyfilter
    }

    @Value("${collector.scatter.serverside-scan:v1}")
    private String serverSideScan;

    public ServerSideScan getServerSideScan() {
        return ServerSideScan.valueOf(serverSideScan);
    }

    @Override
    public String toString() {
        return "ScatterConfiguration{" +
                "serverSideScan='" + serverSideScan + '\'' +
                '}';
    }
}
