package com.eddsteel.resources;

import io.dropwizard.jersey.errors.ErrorMessage;
import org.apache.hc.core5.http.ConnectionRequestTimeoutException;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class ConnectionRequestTimeoutExceptionMapper implements ExceptionMapper<ProcessingException> {

    @Override
    public Response toResponse(ProcessingException e) {
        if (e.getCause() instanceof ConnectionRequestTimeoutException) {
            return Response
                    .status(500)
                    .entity(new ErrorMessage(500, "Connections exhausted"))
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .build();
        } else {
            return Response.status(500).build();
        }
    }
}
