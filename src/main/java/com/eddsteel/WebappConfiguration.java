package com.eddsteel;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.core.Configuration;

public class WebappConfiguration extends Configuration {

    private ClientConfiguration client;
    private int iterations;

    @JsonProperty
    public ClientConfiguration getClient() {
        return client;
    }

    @JsonProperty
    public void setClient(ClientConfiguration client) {
        this.client = client;
    }

    @JsonProperty
    public int getIterations() {
        return iterations;
    }

    @JsonProperty
    public void setIterations(int iterations) {
        this.iterations = iterations;
    }
}
