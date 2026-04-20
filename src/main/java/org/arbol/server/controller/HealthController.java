package org.arbol.server.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.arbol.bussines.StopQuery;
import org.arbol.server.http.AbstractJsonController;
import org.eclipse.jetty.server.Request;

import java.io.IOException;
import java.util.Map;

@SuppressWarnings("unused")
public class HealthController extends AbstractJsonController {

    public static final String PATH = "/health";

    public HealthController(StopQuery stopQuery) {
        super(stopQuery);
    }

    @Override
    public boolean supports(String target) {
        return PATH.equals(target);
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        writeJson(response, HttpServletResponse.SC_OK, Map.of("status", "ok"));
        baseRequest.setHandled(true);
    }
}


