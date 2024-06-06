package com.eddsteel;

import com.eddsteel.client.WebAppClient;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.core.setup.Environment;

public class ClientConfiguration {
    private String endpoint = "http://localhost:8080/";
    private boolean compress = false;

    public WebAppClient build(final Environment environment) {
        return WebAppClient.build(
            new JerseyClientBuilder(environment),
                endpoint,
                compress,
                "test");
    }

    @JsonProperty
    public String getEndpoint() {
        return endpoint;
    }

    @JsonProperty
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    @JsonProperty
    public boolean isCompress() {
        return compress;
    }

    @JsonProperty
    public void setCompress(boolean compress) {
        this.compress = compress;
    }
}
