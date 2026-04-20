package org.arbol.server.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.arbol.bussines.StopQuery;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractJsonController implements ApiController {

    protected static final int DEFAULT_LIMIT = 10_000;
    protected static final int MAX_LIMIT = 50_000;

    protected final StopQuery stopQuery;
    protected final ObjectMapper mapper = new ObjectMapper();

    protected AbstractJsonController(StopQuery stopQuery) {
        this.stopQuery = stopQuery;
    }

    protected void writeJson(HttpServletResponse response, int status, Object body) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        mapper.writeValue(response.getOutputStream(), body);
    }

    protected void writeNotFound(HttpServletResponse response) throws IOException {
        writeJson(response, HttpServletResponse.SC_NOT_FOUND, Map.of("error", "Endpoint no encontrado"));
    }

    protected void writeBadRequest(HttpServletResponse response) throws IOException {
        writeJson(response, HttpServletResponse.SC_BAD_REQUEST, Map.of("error", "tripId es requerido"));
    }

    protected Pagination parsePagination(HttpServletRequest request) {
        return new Pagination(
                parseLimit(request.getParameter("limit")),
                parseCursorToOffset(request.getParameter("cursor"))
        );
    }

    protected int parseLimit(String rawLimit) {
        if (rawLimit == null || rawLimit.isBlank()) {
            return DEFAULT_LIMIT;
        }

        int parsed = Integer.parseInt(rawLimit);
        if (parsed <= 0) {
            throw new IllegalArgumentException("limit debe ser mayor a 0");
        }

        return Math.min(parsed, MAX_LIMIT);
    }

    protected int clampOffset(int offset, int total) {
        return Math.clamp(offset, 0, total);
    }

    protected int parseCursorToOffset(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return 0;
        }

        try {
            String decoded = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            int offset = Integer.parseInt(decoded);
            if (offset < 0) {
                throw new IllegalArgumentException("cursor inválido");
            }
            return offset;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("cursor inválido");
        }
    }

    protected String encodeOffsetCursor(int offset) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(String.valueOf(offset).getBytes(StandardCharsets.UTF_8));
    }

    protected void writePageResponse(HttpServletResponse response, int limit, int total, int toIndex, List<?> items, String tripId) throws IOException {
        Map<String, Object> body = new LinkedHashMap<>();
        if (tripId != null) {
            body.put("tripId", tripId);
        }
        body.put("limit", limit);
        body.put("count", items.size());
        body.put("total", total);
        body.put("hasMore", toIndex < total);
        if (toIndex < total) {
            body.put("nextCursor", encodeOffsetCursor(toIndex));
        }
        body.put("items", items);

        writeJson(response, HttpServletResponse.SC_OK, body);
    }

    protected record Pagination(int limit, int offset) {
    }
}


