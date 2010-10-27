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
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codeartisans.staticlet.http.etag.ETagger;
import org.codeartisans.staticlet.http.etag.ETagMatcher;
import org.codeartisans.staticlet.http.HttpHeaders;
import org.codeartisans.staticlet.http.HttpVersion;
import org.codeartisans.staticlet.util.IOService;

import org.slf4j.Logger;

public class StaticRequest
{

    static final long DEFAULT_EXPIRE_TIME = 604800000L; // ..ms = 1 week.
    // Configuration
    private final Boolean directoryListing;
    private final String docRoot;
    // Context
    private final IOService io;
    private final ETagger entityTagger;
    private final Logger logger;
    final ServletContext servletContext;
    final HttpServletRequest httpRequest;
    final HttpServletResponse httpResponse;
    final boolean writeBody;
    final int bufferSize;
    // Data
    String uri;
    String pathInfo;
    File file;
    String entityTag;
    long lastModified;
    String fileName;
    HttpVersion protocol;

    StaticRequest( Boolean directoryListing, String docRoot,
                   IOService io, ETagger entityTagger, Logger logger, ServletContext servletContext,
                   HttpServletRequest httpRequest, HttpServletResponse httpResponse,
                   boolean writeBody, int bufferSize )
    {
        this.directoryListing = directoryListing;
        this.docRoot = docRoot;
        this.io = io;
        this.entityTagger = entityTagger;
        this.logger = logger;
        this.servletContext = servletContext;
        this.httpRequest = httpRequest;
        this.httpResponse = httpResponse;
        this.writeBody = writeBody;
        this.bufferSize = bufferSize;
    }

    @SuppressWarnings( "LocalVariableHidesMemberVariable" )
    StaticRequest validateRequest()
            throws EarlyHttpStatusException, UnsupportedEncodingException, IOException
    {
        if ( logger.isDebugEnabled() ) {
            StringBuilder sb = new StringBuilder();
            sb.append( httpRequest.getRemoteUser() ).append( "@" ).append( httpRequest.getRemoteAddr() ).append( " " );
            sb.append( httpRequest.getMethod() ).append( "(" ).append( httpRequest.getProtocol() ).append( ") " ).append( httpRequest.getRequestURL() );
            logger.debug( sb.toString() );
        }

        this.protocol = HttpVersion.fromRequestProtocol( httpRequest );
        logger.trace( "Handling a {} request", this.protocol.protocol() );

        String requestURI = httpRequest.getRequestURI();
        String requestPathInfo = httpRequest.getPathInfo();
        if ( requestPathInfo == null ) {
            logger.debug( "Request path info is null, overrided with \"/\"" );
            requestPathInfo = "/";
        }

        File file = new File( docRoot, URLDecoder.decode( requestPathInfo, "UTF-8" ) );
        if ( file.getName().startsWith( "." ) ) {
            logger.debug( "Requested path {}, leads to a hidden file {}, have'nt checked if it exists, 404", requestPathInfo, file );
            throw new EarlyHttpStatusException( HttpServletResponse.SC_NOT_FOUND, "Requested path does not exists" );
        }

        if ( !file.exists() ) {
            logger.debug( "Requested path {}, leads to a non existant file {}, 404", requestPathInfo, file );
            throw new EarlyHttpStatusException( HttpServletResponse.SC_NOT_FOUND, "Requested path does not exists" );
        }

        logger.debug( "Requested uri {} with path {} leads to the file {}", new Object[]{ requestURI, requestPathInfo, file } );

        this.uri = noTrailingSlash( requestURI );
        this.pathInfo = requestPathInfo;
        this.file = file;
        this.entityTag = entityTagger.tagFile( file );

        return this;
    }

    void handleConditional()
            throws EarlyHttpStatusException
    {
//        if ( HttpVersion.HTTP_1_0.lessEquals( protocol ) ) {
//            handleConditionalHttp1_0();
//        } else {
//            handleConditionalHttp1_1();
//        }

        switch ( protocol ) {
            case HTTP_1_0:
                handleConditionalHttp1_0();
                break;
            case HTTP_1_1:
                handleConditionalHttp1_1();
                break;
        }
    }

    /**
     * rfc1945 - HTTP/1.0
     */
    private void handleConditionalHttp1_0()
            throws EarlyHttpStatusException
    {
        if ( logger.isTraceEnabled() ) {
            logger.trace( "HTTP/1.0 conditional header: If-Modified-Since({})", httpRequest.getHeader( HttpHeaders.IF_MODIFIED_SINCE ) );
        }

        // rfc1945 - 10.9 If-Modified-Since

        // A conditional GET method requests that the identified resource be
        // transferred only if it has been modified since the date given by the
        // If-Modified-Since header. The algorithm for determining this includes
        // the following cases:

        // a) If the request would normally result in anything other than
        //    a 200 (ok) status, or if the passed If-Modified-Since date
        //    is invalid, the response is exactly the same as for a
        //    normal GET. A date which is later than the server's current
        //    time is invalid.

        // b) If the resource has been modified since the
        //    If-Modified-Since date, the response is exactly the same as
        //    for a normal GET.

        // c) If the resource has not been modified since a valid
        //    If-Modified-Since date, the server shall return a 304 (not
        //    modified) response.
        try {
            long ifModifiedSince = httpRequest.getDateHeader( HttpHeaders.IF_MODIFIED_SINCE );
            if ( ifModifiedSince != -1 && ifModifiedSince > lastModified ) {
                logger.debug( "If-Modified-Since is greater than the file lastModified date, 304" );
                EarlyHttpStatusException earlyStatus = new EarlyHttpStatusException( HttpServletResponse.SC_NOT_MODIFIED, "Not Modified" );
                earlyStatus.headers.put( HttpHeaders.ETAG, entityTag );
                throw earlyStatus;
            }
        } catch ( IllegalArgumentException ignored ) {
            logger.trace( "Ignoring invalid If-Modified-Since header value ({})", ignored.toString() );
        }
    }

    /**
     * rfc2616 - HTTP/1.1
     *
     *      [ ... ]
     *
     *      The result of a request having both an If-None-Match header field and
     *      either an If-Match or an If-Unmodified-Since header fields is
     *      undefined by this specification.
     *
     *      [ ... ]
     *
     *      The result of a request having both an If-Unmodified-Since header
     *      field and either an If-None-Match or an If-Modified-Since header
     *      fields is undefined by this specification.
     *
     *      [ ... ]
     *
     *      The result of a request having both an If-Modified-Since header field
     *      and either an If-Match or an If-Unmodified-Since header fields is
     *      undefined by this specification.
     *
     *      [ ... ]
     *
     * @throws EarlyHttpStatusException When 304 NotModified or 412 PreconditionFailed
     */
    private void handleConditionalHttp1_1()
            throws EarlyHttpStatusException
    {
        if ( logger.isTraceEnabled() ) {
            Object[] conditionalHeaders = new Object[]{
                httpRequest.getHeader( HttpHeaders.IF_NONE_MATCH ),
                httpRequest.getHeader( HttpHeaders.IF_MODIFIED_SINCE ),
                httpRequest.getHeader( HttpHeaders.IF_MATCH ),
                httpRequest.getHeader( HttpHeaders.IF_UNMODIFIED_SINCE ), };
            logger.trace( "HTTP/1.1 conditional headers: If-None-Match({}) If-Modified-Since({}) If-Match({}) If-Unmodified-Since({})", conditionalHeaders );
        }

        // rfc2616 - 14.26 If-None-Match

        // This one is particular because it is related to If-Modified-Since, Range and If-Range headers.
        // Nothing interesting to copy paste here without being several KB large, see the rfc.

        ETagMatcher ifNoneMatch = new ETagMatcher( httpRequest.getHeader( HttpHeaders.IF_NONE_MATCH ), entityTag );
        if ( ifNoneMatch.doMatch() ) {
            logger.debug( "Got If-None-Match header and ETag {} matches, 304", entityTag );
            EarlyHttpStatusException earlyStatus = new EarlyHttpStatusException( HttpServletResponse.SC_NOT_MODIFIED, "NotModified" );
            earlyStatus.headers.put( HttpHeaders.ETAG, entityTag );
            throw earlyStatus;
        }

        if ( ifNoneMatch.headerValue() == null || ( ifNoneMatch.headerValue() != null && ifNoneMatch.doMatch() ) ) {

            // rfc2616 - 14.25 If-Modified-Since

            // If the passed If-Modified-Since date is invalid, the response is
            // exactly the same as for a normal GET. A date which is later than
            // the server's current time is invalid.

            // If the variant has been modified since the If-Modified-Since
            // date, the response is exactly the same as for a normal GET.

            // If the variant has not been modified since a valid If-
            // Modified-Since date, the server SHOULD return a 304 (Not
            // Modified) response.

            try {
                long ifModifiedSince = httpRequest.getDateHeader( HttpHeaders.IF_MODIFIED_SINCE );
                if ( ifModifiedSince != -1 && ifModifiedSince > lastModified ) {
                    logger.debug( "No If-None-Match header, If-Modified-Since is greater than the file lastModified date, 304" );
                    EarlyHttpStatusException earlyStatus = new EarlyHttpStatusException( HttpServletResponse.SC_NOT_MODIFIED, "Not Modified" );
                    earlyStatus.headers.put( HttpHeaders.ETAG, entityTag );
                    throw earlyStatus;
                }
            } catch ( IllegalArgumentException ignored ) {
                logger.trace( "Ignoring invalid If-Modified-Since header value ({})", ignored.toString() );
            }
        } else {
            if ( logger.isTraceEnabled() ) {
                try {
                    long ifModifiedSince = httpRequest.getDateHeader( HttpHeaders.IF_MODIFIED_SINCE );
                    if ( ifModifiedSince != -1 ) {
                        logger.trace( "Ignoring If-Modified-Since '{}' because If-None-Match was present and matched", ifModifiedSince );
                    }
                } catch ( IllegalArgumentException ex ) {
                    logger.trace( "Ignoring invalid If-Modified-Since because If-None-Match was present and matched .. request is weird" );
                }
            }
        }


        // rfc2616 - 14.24 If-Match

        // If none of the entity tags match, or if "*" is given and no current
        // entity exists, the server MUST NOT perform the requested method, and
        // MUST return a 412 (Precondition Failed) response.

        ETagMatcher ifMatch = new ETagMatcher( httpRequest.getHeader( HttpHeaders.IF_MATCH ), entityTag );
        if ( ifMatch.headerValue() != null && ifMatch.dontMatch() ) {
            logger.debug( "If-Match header did not match entityTag, 412" );
            throw new EarlyHttpStatusException( HttpServletResponse.SC_PRECONDITION_FAILED, "Precondition Failed" );
        }


        // rfc2616 - 14.28 If-Unmodified-Since

        // If the requested variant has been modified since the specified time,
        // the server MUST NOT perform the requested operation, and MUST return
        // a 412 (Precondition Failed).
        // If the specified date is invalid, the header is ignored.

        try {
            long ifUnmodifiedSince = httpRequest.getDateHeader( HttpHeaders.IF_UNMODIFIED_SINCE );
            if ( ifUnmodifiedSince != -1 && ifUnmodifiedSince <= lastModified ) {
                logger.debug( "Requested entity has been modified since If-Unmodified-Since, 412" );
                throw new EarlyHttpStatusException( HttpServletResponse.SC_PRECONDITION_FAILED, "Precondition Failed" );
            }
        } catch ( IllegalArgumentException ignored ) {
            logger.trace( "Ignoring invalid If-Unmodified-Since header value ({})", ignored.toString() );
        }
    }

    void represent()
            throws EarlyHttpStatusException, IOException
    {
        if ( file.isDirectory() ) {

            // Directory request -----------------------------------------------------------------------------------

            if ( !directoryListing ) {
                throw new EarlyHttpStatusException( HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized" );
            }
            new DirectoryRepresenter( logger, this ).represent();

        } else {

            // File request ----------------------------------------------------------------------------------------

            new FileRepresenter( logger, io, this, bufferSize ).represent();

        }
    }

    private String noTrailingSlash( String uri )
    {
        String noTrailingSlash = uri;
        if ( uri.endsWith( "/" ) ) {
            noTrailingSlash = uri.substring( 0, uri.length() - 1 );
            logger.trace( "Removed trailing slash: before '{}' after '{}'", uri, noTrailingSlash );
        }
        return noTrailingSlash;
    }

}
