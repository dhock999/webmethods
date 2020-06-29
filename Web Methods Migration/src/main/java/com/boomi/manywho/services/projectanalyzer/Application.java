package com.boomi.manywho.services.projectanalyzer;

import javax.ws.rs.ApplicationPath;

import com.manywho.sdk.services.servers.EmbeddedServer;
import com.manywho.sdk.services.servers.undertow.UndertowServer;

@ApplicationPath("/")
public class Application {
    public static void main(String[] args) throws Exception {
        EmbeddedServer server = new UndertowServer();
        server.addModule(new ApplicationModule());
        server.setApplication(Application.class);
        server.start();//"",8084);
    }
}