package com.eddsteel.resources;

import com.eddsteel.api.EndpointResponse;
import org.glassfish.jersey.server.ChunkedOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Arrays;


@Produces(MediaType.APPLICATION_JSON)
@Path("/")
public class ServerResource {
    private static Logger LOGGER = LoggerFactory.getLogger(ServerResource.class);
    private static final String DELIMITER = "\r\n";
    @Path("")
    @POST
    public ChunkedOutput<EndpointResponse> rootEndpoint()  {
        ChunkedOutput<EndpointResponse> output = new ChunkedOutput<>(EndpointResponse.class, DELIMITER);
        try {
            output.write(new EndpointResponse("test", Arrays.asList(1, 2, 3), 3L));
        } catch (IOException ioe){
            LOGGER.error("Failed to write", ioe);
        } finally {
            try {
                output.close();
            } catch (IOException ioe) {
                LOGGER.error("Failed to close exception", ioe);
            }
        }

        LOGGER.info("Success");

        return output;
    }
}