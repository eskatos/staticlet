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
package org.codeartisans.staticlet;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codeartisans.staticlet.http.etag.ETagger;
import org.codeartisans.staticlet.util.RequestLogger;
import org.codeartisans.staticlet.http.etag.HexMD5ETagger;
import org.codeartisans.staticlet.util.IOService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A servlet implementing GET & HEAD for HTTP/1.0 & HTTP/1.1 on top of a filesystem.
 */
public class Staticlet
        extends HttpServlet
{

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger( Staticlet.class );
    private StaticletConfiguration configuration;

    @Override
    public void init()
            throws ServletException
    {
        String docRootString = getInitParameter( "docRoot" );
        String directoryListingString = getInitParameter( "directoryListing" );
        String bufferSizeString = getInitParameter( "bufferSize" );
        String expireTimeString = getInitParameter( "expireTime" );
        boolean bufferSizePresent = bufferSizeString != null && bufferSizeString.length() > 0;
        boolean expireTimePresent = expireTimeString != null && expireTimeString.length() > 0;
        initStaticlet( docRootString,
                       Boolean.valueOf( directoryListingString ),
                       bufferSizePresent ? Integer.parseInt( bufferSizeString ) : null,
                       expireTimePresent ? Long.parseLong( expireTimeString ) : null );
    }

    protected final void initStaticlet( String docRoot, Boolean directoryListing, Integer bufferSize, Long expireTime )
            throws ServletException
    {
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
        if ( directoryListing == null ) {
            directoryListing = Boolean.FALSE;
        }
        if ( bufferSize == null ) {
            bufferSize = 10240; // ..bytes = 10KB.
        }
        if ( expireTime == null ) {
            expireTime = 604800000L; // ..ms = 1 week.
        }
        configuration = new StaticletConfiguration( docRoot, directoryListing, bufferSize, expireTime );
    }

    @Override
    public void destroy()
    {
        super.destroy();
    }

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

        Logger logger = new RequestLogger( LOGGER, UUID.randomUUID().toString() );

        try {

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
