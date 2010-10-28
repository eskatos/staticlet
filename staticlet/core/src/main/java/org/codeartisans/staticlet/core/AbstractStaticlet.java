/*
 * Copyright (c) 2010, Paul Merlin. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.codeartisans.staticlet.core;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codeartisans.staticlet.core.http.etag.ETagger;
import org.codeartisans.staticlet.core.http.etag.HexMD5ETagger;
import org.codeartisans.staticlet.core.util.IOService;
import org.codeartisans.staticlet.core.util.RequestLogger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A servlet implementing GET & HEAD for HTTP/1.0 & HTTP/1.1 on top of a filesystem.
 */
public abstract class AbstractStaticlet
        extends HttpServlet
{

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger( AbstractStaticlet.class.getPackage().getName() );
    private StaticletConfiguration configuration;

    // Subclasses contract ---------------------------------------------------------------------------------------------
    /**
     * @return The StaticletConfiguration to use
     */
    protected abstract StaticletConfiguration getConfiguration();

    /**
     * By default the org.codeartisans.staticlet.core logger is used for
     * requests logging. You can override this method to provide a logger of your choice.
     *
     * @return The Logger to use for requests logging
     */
    protected Logger getLogger()
    {
        return LOGGER;
    }

    // Lifecycle -------------------------------------------------------------------------------------------------------
    /**
     * WARNING if you override this method remember to call super.destroy().
     */
    @Override
    public void init()
            throws ServletException
    {
        beforeInit();
        super.init();
        StaticletConfiguration config = getConfiguration();
        String docRoot = config.getDocRoot();
        if ( docRoot == null || docRoot.length() <= 0 ) {
            throw new ServletException( "docRoot is required" );
        }
        File path = new File( docRoot );
        if ( !path.exists() ) {
            throw new ServletException( "'" + docRoot + "' does not exist" );
        } else if ( !path.isDirectory() ) {
            throw new ServletException( "'" + docRoot + "' is not a directory" );
        } else if ( !path.canRead() ) {
            throw new ServletException( "'" + docRoot + "' is not readable" );
        }
        Boolean directoryListing = config.isDirectoryListing();
        if ( directoryListing == null ) {
            directoryListing = Boolean.FALSE;
        }
        Integer bufferSize = config.getBufferSize();
        if ( bufferSize == null ) {
            bufferSize = 10240; // ..bytes = 10KB.
        }
        Long expireTime = config.getExpireTime();
        if ( expireTime == null ) {
            expireTime = 604800000L; // ..ms = 1 week.
        }
        this.configuration = new StaticletConfiguration( docRoot, directoryListing, bufferSize, expireTime );
        afterInit();
    }

    /**
     * WARNING if you override this method remember to call super.destroy().
     */
    @Override
    public void destroy()
    {
        beforeDestroy();
        configuration = null;
        super.destroy();
    }

    /**
     * Hook called before servlet init.
     *
     * @throws ServletException
     */
    protected void beforeInit()
            throws ServletException
    {
    }

    /**
     * Hook called after servlet init.
     *
     * @throws ServletException
     */
    protected void afterInit()
            throws ServletException
    {
    }

    /**
     * Hook called before servlet destroy.
     */
    protected void beforeDestroy()
    {
    }

    // Request processing ----------------------------------------------------------------------------------------------
    @Override
    protected final void doHead( HttpServletRequest httpRequest, HttpServletResponse httpResponse )
            throws ServletException, IOException
    {
        processRequest( httpRequest, httpResponse, false );
    }

    @Override
    protected final void doGet( HttpServletRequest httpRequest, HttpServletResponse httpResponse )
            throws ServletException, IOException
    {
        processRequest( httpRequest, httpResponse, true );
    }

    private void processRequest( HttpServletRequest httpRequest, HttpServletResponse httpResponse, boolean writeBody )
            throws IOException
    {

        Logger logger = new RequestLogger( getLogger(), UUID.randomUUID().toString() );

        try {

            // Ensure configuration ------------------------------------------------------------------------------------

            if ( configuration == null ) {
                logger.error( "Improperly configured, see logs outputed during servlet init, 500" );
                throw new EarlyHttpStatusException( HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Improperly configured" );
            }

            // Set up FileSystemRequest and its dependencies -----------------------------------------------------------

            ServletContext servletContext = getServletContext();
            IOService io = new IOService( servletContext, logger );
            ETagger eTagger = new HexMD5ETagger( logger, io );
            StaticRequest fsRequest = new StaticRequest( configuration,
                                                         logger, io, eTagger,
                                                         httpRequest, httpResponse,
                                                         writeBody );


            // Interaction ---------------------------------------------------------------------------------------------

            fsRequest.validateRequest();
            fsRequest.handleConditional();
            fsRequest.represent();

        } catch ( EarlyHttpStatusException earlyStatus ) {

            // A http early status as been raised ----------------------------------------------------------------------

            for ( Map.Entry<String, String> eachHeader : earlyStatus.headers.entrySet() ) {
                httpResponse.setHeader( eachHeader.getKey(), eachHeader.getValue() );
            }
            httpResponse.sendError( earlyStatus.status, earlyStatus.reason );

        }
    }

}
