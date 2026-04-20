package org.arbol.server.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.arbol.bussines.StopQuery;
import org.arbol.server.http.AbstractJsonController;
import org.eclipse.jetty.server.Request;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ExportsController extends AbstractJsonController {

    public static final String PATH_EXPORTS_STATUS = "/exports/status";
    public static final String PATH_EXPORTS_TRIP_STOP_EDGES = "/exports/trip-stop-edges";

    public ExportsController(StopQuery stopQuery) {
        super(stopQuery);
    }

    @Override
    public boolean supports(String target) {
        return PATH_EXPORTS_STATUS.equals(target) || PATH_EXPORTS_TRIP_STOP_EDGES.equals(target);
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (PATH_EXPORTS_STATUS.equals(target)) {
            writeJson(response, HttpServletResponse.SC_OK, Map.of("service", "trip-stop-edges-exporter", "status", "ok"));
            baseRequest.setHandled(true);
            return;
        }

        if (PATH_EXPORTS_TRIP_STOP_EDGES.equals(target)) {
            Pagination pagination = parsePagination(request);
            List<?> edges = stopQuery.findConsecutiveStopEdges();
            int total = edges.size();

            int fromIndex = clampOffset(pagination.offset(), total);
            int toIndex = Math.min(fromIndex + pagination.limit(), total);
            List<?> items = edges.subList(fromIndex, toIndex);

            writePageResponse(response, pagination.limit(), total, toIndex, items, null);
            baseRequest.setHandled(true);
            return;
        }

        writeNotFound(response);
    }
}


