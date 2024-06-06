package com.eddsteel;

import com.eddsteel.client.WebAppClient;
import com.eddsteel.resources.ClientResource;
import com.eddsteel.resources.ConnectionRequestTimeoutExceptionMapper;
import com.eddsteel.resources.ServerResource;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;

public class WebappApplication extends Application<WebappConfiguration> {
    public static void main(final String[] args) throws Exception {
        new WebappApplication().run(args);
    }

    @Override
    public String getName() {
        return "webapp";
    }

    @Override
    public void initialize(final Bootstrap<WebappConfiguration> bootstrap) {
        // TODO: application initialization
    }

    @Override
    public void run(final WebappConfiguration configuration,
                    final Environment environment) {

        environment.jersey().register(ConnectionRequestTimeoutExceptionMapper.class);
        environment.jersey().register(new ServerResource());
        environment.jersey().register(
                new ClientResource(
                        configuration.getClient().build(environment),
                        configuration.getIterations()));
    }
}
