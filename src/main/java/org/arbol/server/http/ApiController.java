package org.arbol.server.http;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;

import java.io.IOException;

public interface ApiController {
    boolean supports(String target);

    void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException;
}

