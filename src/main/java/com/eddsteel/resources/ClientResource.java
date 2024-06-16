package com.eddsteel.resources;

import com.eddsteel.api.EndpointResponse;
import com.eddsteel.api.ResponseAndBody;
import com.eddsteel.client.WebAppClient;
import org.glassfish.jersey.client.ChunkedInput;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Produces(MediaType.TEXT_PLAIN)
@Path("/check")
public class ClientResource {
    private final WebAppClient client;
    private final int iterations;

    public ClientResource(WebAppClient client, int iterations) {
        this.client = client;
        this.iterations = iterations;
    }

    @Path("")
    @GET
    public String callServer() {
        for (int i = 0; i < iterations; i++) {
            try (ResponseAndBody<EndpointResponse> input = client.getEndpointResponse()) {
                boolean cont;
                do {
                    EndpointResponse next = input.getBody().read();
                    cont = next != null;
                } while (cont);
            }
        }
        return "done";
    }
}
