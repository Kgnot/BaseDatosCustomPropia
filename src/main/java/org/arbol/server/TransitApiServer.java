package org.arbol.server;

import org.arbol.bussines.StopQuery;
import org.arbol.database.Database;
import org.arbol.server.controller.ExportsController;
import org.arbol.server.controller.HealthController;
import org.arbol.server.controller.QueryController;
import org.arbol.server.controller.TripsController;
import org.arbol.server.http.ApiController;
import org.arbol.server.router.ControllerRouter;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class TransitApiServer {

    private static final Logger logger = LoggerFactory.getLogger(TransitApiServer.class);

    public static void main(String[] args) throws Exception {
        int port = resolvePort(args);
        Database database = new Database();
        StopQuery stopQuery = new StopQuery(database);

        List<ApiController> controllers = List.of(
                new HealthController(stopQuery),
                new QueryController(stopQuery),
                new TripsController(stopQuery),
                new ExportsController(stopQuery)
        );

        Server server = new Server(port);
        server.setHandler(new ApiHandler(new ControllerRouter(controllers)));

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
        logger.info("Endpoints: /health, /stops/active, /transitions/grouped, /trips, /trip-stop-edges, /exports/*");
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
        private final ControllerRouter router;

        private ApiHandler(ControllerRouter router) {
            this.router = router;
        }

        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
            if (!"GET".equalsIgnoreCase(request.getMethod())) {
                return;
            }

            try {
                router.route(target, baseRequest, request, response);
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"error\":\"Error interno\"}");
                baseRequest.setHandled(true);
            }
        }
    }
}
