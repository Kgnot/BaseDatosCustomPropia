package org.arbol.server.router;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.arbol.server.http.ApiController;
import org.eclipse.jetty.server.Request;

import java.io.IOException;
import java.util.List;

@SuppressWarnings("unused")
public class ControllerRouter {

    private final List<ApiController> controllers;

    public ControllerRouter(List<ApiController> controllers) {
        this.controllers = List.copyOf(controllers);
    }

    public void route(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        for (ApiController controller : controllers) {
            if (controller.supports(target)) {
                controller.handle(target, baseRequest, request, response);
                return;
            }
        }

        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"error\":\"Endpoint no encontrado\"}");
        baseRequest.setHandled(true);
    }
}


