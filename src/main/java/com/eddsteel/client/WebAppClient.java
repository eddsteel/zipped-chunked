package com.eddsteel.client;

import com.eddsteel.api.EndpointResponse;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.client.JerseyClientBuilder;
import org.glassfish.jersey.client.ChunkedInput;
import org.glassfish.jersey.client.ClientProperties;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class WebAppClient {

    private final Client client;
    private final String endpoint;

    private WebAppClient(Client client, String endpoint) {
        this.client = client;
        this.endpoint = endpoint;
    }

    private final GenericType<ChunkedInput<EndpointResponse>> chunkedInputGenericType = new GenericType(new ParameterizedType() {
        @Override
        public Type[] getActualTypeArguments() {
            return new Type[] { EndpointResponse.class };
        }

        @Override
        public Type getRawType() {
            return ChunkedInput.class;
        }

        @Override
        public Type getOwnerType() {
            return ChunkedInput.class;
        }
    });

    public static WebAppClient build(
            JerseyClientBuilder clientBuilder,
            String endpoint,
            boolean compress,
            String name) {
        var client = clientBuilder
                .withProperty(ClientProperties.CONNECT_TIMEOUT, 5000)
                .withProperty(ClientProperties.READ_TIMEOUT, 60000)
                .using(new JerseyClientConfiguration() {
                    @Override
                    public boolean isGzipEnabled() {
                        return compress;
                    }
                })
                .build(name);
        return new WebAppClient(client, endpoint);
    }

    public ChunkedInput<EndpointResponse> getEndpointResponse() {
        ChunkedInput<EndpointResponse> response = client
                .target(endpoint)
                .request()
                .post(Entity.entity("{}", MediaType.APPLICATION_JSON_TYPE),
                        chunkedInputGenericType);
        return response;
    }
}
