package com.eddsteel.api;

import org.glassfish.jersey.client.ChunkedInput;

import java.io.Closeable;
import javax.ws.rs.core.Response;

public class ResponseAndBody<T> implements Closeable {

    private final Response response;
    private final ChunkedInput<T> body;

    public ResponseAndBody(Response response, ChunkedInput<T> body) {
        this.response = response;
        this.body = body;
    }

    public ChunkedInput<T> getBody() {
        return body;
    }

    public void close() {
        try {
            body.close();
        } finally {
            response.close();
        }
    }
}
