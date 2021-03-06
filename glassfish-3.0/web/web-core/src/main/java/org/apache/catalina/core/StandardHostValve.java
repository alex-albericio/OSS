/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 *
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */




package org.apache.catalina.core;

import java.io.IOException;

// START SJSAS 6324911
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.Reader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.BufferedInputStream;
import javax.servlet.*;
// END SJSAS 6324911

import java.util.logging.*;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
// START GlassFish Issue 1057
import javax.servlet.http.HttpSession;
// END GlassFish Issue 1057
import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.HttpRequest;
import org.apache.catalina.HttpResponse;
import org.apache.catalina.Manager;
import org.apache.catalina.Request;
import org.apache.catalina.Response;
// START GlassFish Issue 1057
import org.apache.catalina.Session;
// END GlassFish Issue 1057
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.ClientAbortException;
import org.apache.catalina.deploy.ErrorPage;
import org.apache.catalina.util.RequestUtil;
import org.apache.catalina.util.ResponseUtil;
import org.apache.catalina.util.StringManager;
import org.apache.catalina.valves.ValveBase;
// START SJSAS 6374691
import org.glassfish.web.valve.GlassFishValve;
// END SJSAS 6374691

/**
 * Valve that implements the default basic behavior for the
 * <code>StandardHost</code> container implementation.
 * <p>
 * <b>USAGE CONSTRAINT</b>:  This implementation is likely to be useful only
 * when processing HTTP requests.
 *
 * @author Craig R. McClanahan
 * @author Remy Maucherat
 * @version $Revision: 1.21 $ $Date: 2007/04/10 17:12:22 $
 */

final class StandardHostValve
    extends ValveBase {


    private static final Logger log = Logger.getLogger(
        StandardHostValve.class.getName());

    private static final ClassLoader standardHostValveClassLoader =
        StandardHostValve.class.getClassLoader();


    // ----------------------------------------------------- Instance Variables


    /**
     * The descriptive information related to this implementation.
     */
    private static final String info =
        "org.apache.catalina.core.StandardHostValve/1.0";

    /**
     * The string manager for this package.
     */
    private static final StringManager sm =
        StringManager.getManager(Constants.Package);


    // START SJSAS 6374691
    private GlassFishValve errorReportValve;
    // END SJSAS 6374691


    // ------------------------------------------------------------- Properties


    /**
     * Return descriptive information about this Valve implementation.
     */
    public String getInfo() {

        return (info);

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Select the appropriate child Context to process this request,
     * based on the specified request URI.  If no matching Context can
     * be found, return an appropriate HTTP error.
     *
     * @param request Request to be processed
     * @param response Response to be produced
     * @param valveContext Valve context used to forward to the next Valve
     *
     * @exception IOException if an input/output error occurred
     * @exception ServletException if a servlet error occurred
     */
    @Override
    public int invoke(Request request, Response response)
            throws IOException, ServletException {

        Context context = preInvoke(request, response);
        if (context == null) {
            return END_PIPELINE;
        }

        // Ask this Context to process this request
        if (context.getPipeline().hasNonBasicValves() ||
                context.hasCustomPipeline()) {
            context.getPipeline().invoke(request, response);
        } else {
            context.getPipeline().getBasic().invoke(request, response);
        }

        return END_PIPELINE;
    }


    /**
     * Tomcat style invocation.
     */
    @Override
    public void invoke(org.apache.catalina.connector.Request request,
                       org.apache.catalina.connector.Response response)
            throws IOException, ServletException {

        Context context = preInvoke(request, response);
        if (context == null) {
            return;
        }

        // Ask this Context to process this request
        if (context.getPipeline().hasNonBasicValves() ||
                context.hasCustomPipeline()) {
            context.getPipeline().invoke(request, response);
        } else {
            context.getPipeline().getBasic().invoke(request, response);
        }

        postInvoke(request, response);
    }


    @Override
    public void postInvoke(Request request, Response response)
        // START SJSAS 6374691
        throws IOException, ServletException
        // END SJSAS 6374691
    {
        try {
            // START SJSAS 6374990
            if (((ServletResponse) response).isCommitted()) {
                return;
            }
            // END SJSAS 6374990

            HttpServletRequest hreq = (HttpServletRequest) request.getRequest();

            // Error page processing
            response.setSuspended(false);

            Throwable t = (Throwable) hreq.getAttribute(
                RequestDispatcher.ERROR_EXCEPTION);

            if (t != null) {
                throwable(request, response, t);
            } else {
                status(request, response);
            }

            // START SJSAS 6374691
            if (errorReportValve != null) {
                errorReportValve.postInvoke(request, response);
            }
            // END SJSAS 6374691
        } finally {
            Thread.currentThread().setContextClassLoader
                (standardHostValveClassLoader);
        }
    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Handle the specified Throwable encountered while processing
     * the specified Request to produce the specified Response.  Any
     * exceptions that occur during generation of the exception report are
     * logged and swallowed.
     *
     * @param request The request being processed
     * @param response The response being generated
     * @param exception The exception that occurred (which possibly wraps
     *  a root cause exception
     */
    protected void throwable(Request request, Response response,
                             Throwable throwable) {
        Context context = request.getContext();
        if (context == null)
            return;
        
        Throwable realError = throwable;
        if (realError instanceof ServletException) {
            realError = ((ServletException) realError).getRootCause();
            if (realError == null) {
                realError = throwable;
            }
        } 

        // If this is an aborted request from a client just log it and return
        if (realError instanceof ClientAbortException ) {
            if (log.isLoggable(Level.FINE)) {
                log.fine(sm.getString("standardHost.clientAbort",
                                      realError.getCause().getMessage()));
            }
            return;
        }

        ErrorPage errorPage = findErrorPage(context, throwable);
        if ((errorPage == null) && (realError != throwable)) {
            errorPage = findErrorPage(context, realError);
        }

        if (errorPage != null) {
            dispatchToErrorPage(request, response, errorPage, throwable,
                                realError, 0);
        } else if (context.getDefaultErrorPage() != null) {
            dispatchToErrorPage(request, response,
                context.getDefaultErrorPage(), throwable, realError, 0);  
        } else {
            // A custom error-page has not been defined for the exception
            // that was thrown during request processing. Check if an
            // error-page for error code 500 was specified and if so, 
            // send that page back as the response.
            ServletResponse sresp = (ServletResponse) response;

            /* GlassFish 6386229
            if (sresp instanceof HttpServletResponse) {
                ((HttpServletResponse) sresp).setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                // The response is an error
                response.setError();

                status(request, response);
            }
            */
            // START GlassFish 6386229
            ((HttpServletResponse) sresp).setStatus(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            // The response is an error
            response.setError();

            status(request, response);
            // END GlassFish 6386229
        }
            

    }


    /**
     * Handle the HTTP status code (and corresponding message) generated
     * while processing the specified Request to produce the specified
     * Response.  Any exceptions that occur during generation of the error
     * report are logged and swallowed.
     *
     * @param request The request being processed
     * @param response The response being generated
     */
    protected void status(Request request, Response response) {

        // Handle a custom error page for this status code
        Context context = request.getContext();
        if (context == null)
            return;

        /*
         * Only look for error pages when isError() is set.
         * isError() is set when response.sendError() is invoked.
         */
        if (!response.isError()) {
            return;
        }

        int statusCode = ((HttpResponse) response).getStatus();
        ErrorPage errorPage = context.findErrorPage(statusCode);
        if (errorPage != null) {
            dispatchToErrorPage(request, response, errorPage, null, null,
                                statusCode);
        } else if (statusCode >= 400 && statusCode < 600 &&
                context.getDefaultErrorPage() != null) {
            dispatchToErrorPage(request, response,
                context.getDefaultErrorPage(), null, null, statusCode);
        }
        // START SJSAS 6324911
        else {
            errorPage = ((StandardHost) getContainer()).findErrorPage(
                                                        statusCode);
            if (errorPage != null) {
                try {
                    handleHostErrorPage(response, errorPage, statusCode);
                } catch (Exception e) {
                    log("Exception processing " + errorPage, e);
                }
            }
        }
        // END SJSAS 6324911
    }


    /**
     * Find and return the ErrorPage instance for the specified exception's
     * class, or an ErrorPage instance for the closest superclass for which
     * there is such a definition.  If no associated ErrorPage instance is
     * found, return <code>null</code>.
     *
     * @param context The Context in which to search
     * @param exception The exception for which to find an ErrorPage
     */
    protected static ErrorPage findErrorPage
        (Context context, Throwable exception) {

        if (exception == null)
            return (null);
        Class<?> clazz = exception.getClass();
        String name = clazz.getName();
        while (!Object.class.equals(clazz)) {
            ErrorPage errorPage = context.findErrorPage(name);
            if (errorPage != null)
                return (errorPage);
            clazz = clazz.getSuperclass();
            if (clazz == null)
                break;
            name = clazz.getName();
        }
        return (null);

    }


    /**
     * Handle an HTTP status code or Java exception by forwarding control
     * to the location included in the specified errorPage object.  It is
     * assumed that the caller has already recorded any request attributes
     * that are to be forwarded to this page.  Return <code>true</code> if
     * we successfully utilized the specified error page location, or
     * <code>false</code> if the default error report should be rendered.
     *
     * @param request The request being processed
     * @param response The response being generated
     * @param errorPage The errorPage directive we are obeying
     */
    protected boolean custom(Request request, Response response,
                             ErrorPage errorPage) {

        if (debug >= 1)
            log("Processing " + errorPage);

        // Validate our current environment
        /* GlassFish 6386229
        if (!(request instanceof HttpRequest)) {
            if (debug >= 1)
                log(" Not processing an HTTP request --> default handling");
            return (false);     // NOTE - Nothing we can do generically
        }
        */
        HttpServletRequest hreq =
            (HttpServletRequest) request.getRequest();
        /* GlassFish 6386229
        if (!(response instanceof HttpResponse)) {
            if (debug >= 1)
                log("Not processing an HTTP response --> default handling");
            return (false);     // NOTE - Nothing we can do generically
        }
        */
        HttpServletResponse hres =
            (HttpServletResponse) response.getResponse();

        ((HttpRequest) request).setPathInfo(errorPage.getLocation());

        try {
            /*
             * Reset the response, while keeping the real error code and
             * message
             */
            response.resetBuffer(true);
            Integer statusCodeObj = (Integer) hreq.getAttribute(
                RequestDispatcher.ERROR_STATUS_CODE);
            int statusCode = statusCodeObj.intValue();
            String message = (String) hreq.getAttribute(
                RequestDispatcher.ERROR_MESSAGE);
            hres.setStatus(statusCode, message);

            // Forward control to the specified location
            ServletContext servletContext =
                request.getContext().getServletContext();
            ApplicationDispatcher dispatcher = (ApplicationDispatcher)
                servletContext.getRequestDispatcher(errorPage.getLocation());
            dispatcher.dispatch(hreq, hres, DispatcherType.ERROR);

            // If we forward, the response is suspended again
            response.setSuspended(false);

            // Indicate that we have successfully processed this custom page
            return (true);

        } catch (Throwable t) {

            // Report our failure to process this custom page
            log("Exception Processing " + errorPage, t);
            return (false);

        }

    }


    /**
     * Log a message on the Logger associated with our Container (if any).
     *
     * @param message Message to be logged
     */
    protected void log(String message) {
        org.apache.catalina.Logger logger = container.getLogger();
        if (logger != null) {
            logger.log(this.toString() + ": " + message);
        } else {
            log.info(this.toString() + ": " + message);
        }
    }


    /**
     * Log a message on the Logger associated with our Container (if any).
     *
     * @param message Message to be logged
     * @param t Associated exception
     */
    protected void log(String message, Throwable t) {
        org.apache.catalina.Logger logger = container.getLogger();
        if (logger != null) {
            logger.log(this.toString() + ": " + message, t,
                org.apache.catalina.Logger.WARNING);
        } else {
            log.log(Level.WARNING, this.toString() + ": " + message, t);
        }
    }


    // START SJSAS 6324911
    /**
     * Copies the contents of the given error page to the response, and
     * updates the status message with the reason string of the error page.
     *
     * @param response The response object
     * @param errorPage The error page whose contents are to be copied
     * @param statusCode The status code
     */
    private void handleHostErrorPage(Response response,
                                     ErrorPage errorPage,
                                     int statusCode)
            throws Exception {

        ServletOutputStream ostream = null;
        PrintWriter writer = null;
        FileReader reader = null;
        BufferedInputStream istream = null;
        IOException ioe = null;

        String message = errorPage.getReason();
        if (message != null) {
            ((HttpResponse) response).reset(statusCode, message);
        }
         
        try {
            ostream = response.getResponse().getOutputStream();
        } catch (IllegalStateException e) {
            // If it fails, we try to get a Writer instead if we're
            // trying to serve a text file
            writer = response.getResponse().getWriter();
        }

        if (writer != null) {
            reader = new FileReader(errorPage.getLocation());
            ioe = ResponseUtil.copy(reader, writer);
            try {
                reader.close();
            } catch (Throwable t) {
                ;
            }
        } else {
            istream = new BufferedInputStream(
                new FileInputStream(errorPage.getLocation()));
            ioe = ResponseUtil.copy(istream, ostream);
            try {
                istream.close();
            } catch (Throwable t) {
                ;
            }
        }

        // Rethrow any exception that may have occurred
        if (ioe != null) {
            throw ioe;
        }
    }
    // END SJSAS 6324911


    // START SJSAS 6374691
    void setErrorReportValve(GlassFishValve errorReportValve) {
        this.errorReportValve = errorReportValve;
    }
    // END SJSAS 6374691


    private Context preInvoke(Request request, Response response)
            throws IOException, ServletException {

        // Select the Context to be used for this Request
        Context context = request.getContext();
        if (context == null) {
            /* S1AS 4878272
            ((HttpServletResponse) response.getResponse()).sendError
                (HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                 sm.getString("standardHost.noContext"));
            */
            // BEGIN S1AS 4878272
            ((HttpServletResponse) response.getResponse()).sendError
                (HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setDetailMessage(sm.getString("standardHost.noContext"));
            // END S1AS 4878272
            return null;
        }

        // Bind the context CL to the current thread
        if( context.getLoader() != null ) {
            // Not started - it should check for availability first
            // This should eventually move to Engine, it's generic.
            Thread.currentThread().setContextClassLoader
                    (context.getLoader().getClassLoader());
        }
                 
        // START GlassFish Issue 1057
        // Update the session last access time for our session (if any)
        HttpServletRequest hreq = (HttpServletRequest) request.getRequest();
        hreq.getSession(false);
        // END GlassFish Issue 1057

        return context;
    }


    private void dispatchToErrorPage(Request request, Response response,
            ErrorPage errorPage, Throwable throwable, Throwable realError,
            int statusCode) {

        response.setAppCommitted(false);

        ServletRequest sreq = request.getRequest();
        ServletResponse sresp = response.getResponse();

        sreq.setAttribute(Globals.DISPATCHER_REQUEST_PATH_ATTR,
                          errorPage.getLocation());
        sreq.setAttribute(RequestDispatcher.ERROR_REQUEST_URI,
                          ((HttpServletRequest) sreq).getRequestURI());
        Wrapper wrapper = request.getWrapper();
        if (wrapper != null) {
            sreq.setAttribute(RequestDispatcher.ERROR_SERVLET_NAME,
                              wrapper.getName());
        }

        if (throwable != null) {
            sreq.setAttribute(RequestDispatcher.ERROR_STATUS_CODE,
                Integer.valueOf(HttpServletResponse.SC_INTERNAL_SERVER_ERROR));
            sreq.setAttribute(RequestDispatcher.ERROR_MESSAGE,
                              throwable.getMessage());
            sreq.setAttribute(RequestDispatcher.ERROR_EXCEPTION,
                              realError);
            sreq.setAttribute(RequestDispatcher.ERROR_EXCEPTION_TYPE,
                              realError.getClass());
        } else {
            sreq.setAttribute(RequestDispatcher.ERROR_STATUS_CODE,
                              Integer.valueOf(statusCode));
            String message = RequestUtil.filter(
                ((HttpResponse) response).getMessage());
            if (message == null) {
                message = "";
            }
            sreq.setAttribute(RequestDispatcher.ERROR_MESSAGE, message);
        }

        if (custom(request, response, errorPage)) {
            try {
                sresp.flushBuffer();
            } catch (IOException e) {
                log("Exception processing " + errorPage, e);
            }
        }
    }
}
