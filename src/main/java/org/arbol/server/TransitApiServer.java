package org.arbol.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.arbol.bussines.StopQuery;
import org.arbol.database.Database;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class TransitApiServer {

    private static final Logger logger = LoggerFactory.getLogger(TransitApiServer.class);

    public static void main(String[] args) throws Exception {
        int port = resolvePort(args);
        Database database = new Database();
        StopQuery stopQuery = new StopQuery(database);

        Server server = new Server(port);
        server.setHandler(new ApiHandler(stopQuery));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                logger.info("Cerrando servidor HTTP...");
                server.stop();
            } catch (Exception e) {
                logger.error("Error al detener Jetty", e);
            }
            database.close();
        }));

        server.start();
        logger.info("Servidor iniciado en http://localhost:{}", port);
        logger.info("Endpoint principal: GET /transitions/grouped");
        server.join();
    }

    private static int resolvePort(String[] args) {
        if (args != null && args.length > 0) {
            return Integer.parseInt(args[0]);
        }

        String envPort = System.getenv("PORT");
        if (envPort != null && !envPort.isBlank()) {
            return Integer.parseInt(envPort);
        }
        return 8080;
    }

    private static final class ApiHandler extends AbstractHandler {
        private final StopQuery stopQuery;
        private final ObjectMapper mapper = new ObjectMapper();

        private ApiHandler(StopQuery stopQuery) {
            this.stopQuery = stopQuery;
        }

        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
            if (!"GET".equalsIgnoreCase(request.getMethod())) {
                return;
            }

            try {
                if ("/health".equals(target)) {
                    writeJson(response, HttpServletResponse.SC_OK, Map.of("status", "ok"));
                    baseRequest.setHandled(true);
                    return;
                }

                if ("/transitions/grouped".equals(target)) {
                    long start = System.currentTimeMillis();
                    List<?> grouped = stopQuery.findGroupedConsecutiveStopEdges();
                    writeJson(response, HttpServletResponse.SC_OK, grouped);
                    logger.info("GET /transitions/grouped -> {} registros en {} ms", grouped.size(), System.currentTimeMillis() - start);
                    baseRequest.setHandled(true);
                    return;
                }

                if ("/stops/active".equals(target)) {
                    List<?> activeStops = stopQuery.findActiveStops();
                    writeJson(response, HttpServletResponse.SC_OK, activeStops);
                    baseRequest.setHandled(true);
                    return;
                }

                if ("/edges/consecutive".equals(target)) {
                    List<?> edges = stopQuery.findConsecutiveStopEdges();
                    logger.info("GET /edges/consecutive -> {}", edges.subList(0,10));
                    writeJson(response, HttpServletResponse.SC_OK, edges);
                    baseRequest.setHandled(true);
                    return;
                }

                writeJson(response, HttpServletResponse.SC_NOT_FOUND, Map.of("error", "Endpoint no encontrado"));
                baseRequest.setHandled(true);
            } catch (Exception e) {
                logger.error("Error manejando request {} {}", request.getMethod(), target, e);
                writeJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Map.of("error", "Error interno", "detail", e.getMessage()));
                baseRequest.setHandled(true);
            }
        }

        private void writeJson(HttpServletResponse response, int status, Object body) throws IOException {
            response.setStatus(status);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            mapper.writeValue(response.getOutputStream(), body);
        }
    }
}


