package org.arbol.server.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.arbol.bussines.StopQuery;
import org.arbol.server.http.AbstractJsonController;
import org.eclipse.jetty.server.Request;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class TripsController extends AbstractJsonController {

    public static final String PATH_TRIPS = "/trips";
    public static final String PATH_TRIP_STOP_EDGES = "/trip-stop-edges";
    public static final String PATH_TRIPS_PREFIX = "/trips/";
    public static final String PATH_TRIP_STOP_EDGES_SUFFIX = "/trip-stop-edges";

    public TripsController(StopQuery stopQuery) {
        super(stopQuery);
    }

    @Override
    public boolean supports(String target) {
        return PATH_TRIPS.equals(target)
                || PATH_TRIP_STOP_EDGES.equals(target)
                || isTripStopEdgesByPath(target);
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (PATH_TRIPS.equals(target)) {
            handleTripIds(request, response, baseRequest);
            return;
        }

        if (PATH_TRIP_STOP_EDGES.equals(target)) {
            handleTripStopEdgesByQuery(request, response, baseRequest);
            return;
        }

        if (isTripStopEdgesByPath(target)) {
            handleTripStopEdgesByPath(target, request, response, baseRequest);
            return;
        }

        writeNotFound(response);
    }

    private void handleTripIds(HttpServletRequest request, HttpServletResponse response, Request baseRequest) throws IOException {
        Pagination pagination = parsePagination(request);
        int total = stopQuery.countTripIds();
        int fromIndex = clampOffset(pagination.offset(), total);
        int toIndex = Math.min(fromIndex + pagination.limit(), total);

        List<String> tripIds = stopQuery.findTripIds(fromIndex, pagination.limit());
        writePageResponse(response, pagination.limit(), total, toIndex, tripIds, null);
        baseRequest.setHandled(true);
    }

    private void handleTripStopEdgesByQuery(HttpServletRequest request, HttpServletResponse response, Request baseRequest) throws IOException {
        String tripId = request.getParameter("tripId");
        if (tripId == null || tripId.isBlank()) {
            writeBadRequest(response);
            baseRequest.setHandled(true);
            return;
        }

        handleTripStopEdgesInternal(tripId, request, response, baseRequest);
    }

    private void handleTripStopEdgesByPath(String target, HttpServletRequest request, HttpServletResponse response, Request baseRequest) throws IOException {
        String tripId = extractTripIdFromPath(target);
        if (tripId == null || tripId.isBlank()) {
            writeBadRequest(response);
            baseRequest.setHandled(true);
            return;
        }

        handleTripStopEdgesInternal(tripId, request, response, baseRequest);
    }

    private void handleTripStopEdgesInternal(String tripId, HttpServletRequest request, HttpServletResponse response, Request baseRequest) throws IOException {
        if (!stopQuery.tripExists(tripId)) {
            writeJson(response, HttpServletResponse.SC_NOT_FOUND, Map.of("error", "tripId no encontrado", "tripId", tripId));
            baseRequest.setHandled(true);
            return;
        }

        Pagination pagination = parsePagination(request);
        List<?> edges = stopQuery.findConsecutiveStopEdgesByTripId(tripId);
        int total = edges.size();

        int fromIndex = clampOffset(pagination.offset(), total);
        int toIndex = Math.min(fromIndex + pagination.limit(), total);
        List<?> items = edges.subList(fromIndex, toIndex);

        writePageResponse(response, pagination.limit(), total, toIndex, items, tripId);
        baseRequest.setHandled(true);
    }

    private boolean isTripStopEdgesByPath(String target) {
        return target.startsWith(PATH_TRIPS_PREFIX) && target.endsWith(PATH_TRIP_STOP_EDGES_SUFFIX);
    }

    private String extractTripIdFromPath(String target) {
        if (!isTripStopEdgesByPath(target)) {
            return null;
        }

        String middle = target.substring(PATH_TRIPS_PREFIX.length(), target.length() - PATH_TRIP_STOP_EDGES_SUFFIX.length());
        if (middle.startsWith("/")) {
            middle = middle.substring(1);
        }
        return middle.isBlank() ? null : middle;
    }
}


