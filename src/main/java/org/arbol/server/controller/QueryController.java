package org.arbol.server.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.arbol.bussines.StopQuery;
import org.arbol.server.http.AbstractJsonController;
import org.eclipse.jetty.server.Request;

import java.io.IOException;
import java.util.List;

public class QueryController extends AbstractJsonController {

    public static final String PATH_TRANSITIONS_GROUPED = "/transitions/grouped";
    public static final String PATH_STOPS_ACTIVE = "/stops/active";
    public static final String PATH_EDGES_CONSECUTIVE = "/edges/consecutive";

    public QueryController(StopQuery stopQuery) {
        super(stopQuery);
    }

    @Override
    public boolean supports(String target) {
        return PATH_TRANSITIONS_GROUPED.equals(target)
                || PATH_STOPS_ACTIVE.equals(target)
                || PATH_EDGES_CONSECUTIVE.equals(target);
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        switch (target) {
            case PATH_TRANSITIONS_GROUPED -> handleGroupedTransitions(response, baseRequest);
            case PATH_STOPS_ACTIVE -> handleActiveStops(response, baseRequest);
            case PATH_EDGES_CONSECUTIVE -> handleConsecutiveEdges(response, baseRequest);
            default -> writeNotFound(response);
        }
    }

    private void handleGroupedTransitions(HttpServletResponse response, Request baseRequest) throws IOException {
        List<?> grouped = stopQuery.findGroupedConsecutiveStopEdges();
        writeJson(response, HttpServletResponse.SC_OK, grouped);
        baseRequest.setHandled(true);
    }

    private void handleActiveStops(HttpServletResponse response, Request baseRequest) throws IOException {
        List<?> activeStops = stopQuery.findActiveStops();
        writeJson(response, HttpServletResponse.SC_OK, activeStops);
        baseRequest.setHandled(true);
    }

    private void handleConsecutiveEdges(HttpServletResponse response, Request baseRequest) throws IOException {
        List<?> edges = stopQuery.findConsecutiveStopEdges();
        writeJson(response, HttpServletResponse.SC_OK, edges);
        baseRequest.setHandled(true);
    }
}



