package org.arbol.server.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.arbol.bussines.StopQuery;
import org.arbol.server.http.AbstractJsonController;
import org.eclipse.jetty.server.Request;

import java.io.IOException;

public class StopTimeController extends AbstractJsonController {

    public final static String PATH_STOP_TIMES = "/stop-times";

    protected StopTimeController(StopQuery stopQuery) {
        super(stopQuery);
    }

    @Override
    public boolean supports(String target) {
        return false;
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {

    }
}
