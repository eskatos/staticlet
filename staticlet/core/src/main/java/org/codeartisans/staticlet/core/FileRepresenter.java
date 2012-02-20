/*
 * Copyright (c) 2010, Paul Merlin. All Rights Reserved.
 * Copyright (c) 2012, Mo Maison. All Rights Reserved.
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

import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPOutputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.codeartisans.staticlet.core.http.HttpHeaders;
import org.codeartisans.staticlet.core.http.HttpVersion;
import org.codeartisans.staticlet.core.util.EscapeUtils;
import org.codeartisans.staticlet.core.util.IOService;

import org.slf4j.Logger;

public class FileRepresenter
{

    static class ByteRange
    {

        long start;
        long end;
        long length;
        long total;

        ByteRange( long start, long end, long total )
        {
            this.start = start;
            this.end = end;
            this.length = end - start + 1;
            this.total = total;
        }

    }

    private static final String MULTIPART_BOUNDARY = "MULTIPART_BYTERANGES";
    private final StaticletConfiguration configuration;
    private final Logger logger;
    private final StaticRequest staticRequest;
    private final IOService io;
    private ByteRange fullRange;
    private boolean acceptsGzip;
    private String contentType;
    private String disposition = "inline";

    FileRepresenter( StaticletConfiguration configuration, Logger logger, IOService io, StaticRequest staticRequest )
    {
        this.configuration = configuration;
        this.logger = logger;
        this.io = io;
        this.staticRequest = staticRequest;
    }

    void represent()
            throws EarlyHttpStatusException, IOException
    {
        // Preparing ranges, the full Range represents the complete file.
        // This is done before handling content type in order to work with a clean response
        List<ByteRange> ranges = processRangeHeaders();

        // Handling gzip, content type and disposition
        handleAcceptHeaders();

        // Apply http idoms to response based on the FileRequest
        initializeResponse();

        // Sending requested file (part(s)) to client
        sendRequestedFileParts( ranges );
    }

    /**
     * rfc2616 - 14.35 Range & 14.27 - If-Range
     */
    private List<ByteRange> processRangeHeaders()
            throws EarlyHttpStatusException
    {
        long fileLength = staticRequest.file.length();
        List<ByteRange> ranges = new ArrayList<ByteRange>();
        fullRange = new ByteRange( 0, fileLength - 1, fileLength );

        switch ( staticRequest.protocol ) {
            case HTTP_1_0:

                // No range support in HTTP/1.0
                ranges.add( fullRange );
                break;

            case HTTP_1_1:

                // Validate and process Range and If-Range headers.
                String range = staticRequest.httpRequest.getHeader( HttpHeaders.RANGE );
                long length = staticRequest.file.length();
                if ( range != null ) {
                    // Range header should match format "bytes=n-n,n-n,n-n...". If not, then return the full file
                    if ( !range.matches( "^bytes=\\d*-\\d*(,\\d*-\\d*)*$" ) ) {
                        ranges.add( fullRange );
                    } else {
                        // Range header format is valid
                        String ifRange = staticRequest.httpRequest.getHeader( HttpHeaders.IF_RANGE );
                        if ( ifRange != null && !ifRange.equals( staticRequest.entityTag ) ) {
                            try {
                                long ifRangeTime = staticRequest.httpRequest.getDateHeader( HttpHeaders.IF_RANGE ); // Throws IAE if invalid.
                                if ( ifRangeTime != -1 && ifRangeTime < staticRequest.lastModified ) {
                                    ranges.add( fullRange );
                                }
                            } catch ( IllegalArgumentException ignore ) {
                                ranges.add( fullRange );
                            }
                        }
                        if ( ranges.isEmpty() ) {
                            for ( String part : range.substring( 6 ).split( "," ) ) {
                                // Assuming a file with length of 100, the following examples returns bytes at:
                                // 50-80 (50 to 80), 40- (40 to length=100), -20 (length-20=80 to length=100).
                                long start = sublong( part, 0, part.indexOf( "-" ) );
                                long end = sublong( part, part.indexOf( "-" ) + 1, part.length() );
                                if ( start == -1 ) {
                                    start = length - end;
                                    end = length - 1;
                                } else if ( end == -1 || end > length - 1 ) {
                                    end = length - 1;
                                }
                                // Check if Range boundaries are valid. If not, then return 416.
                                if ( start > end ) {
                                    EarlyHttpStatusException earlyStatus = new EarlyHttpStatusException( HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE, "Requested range not satisfiable" );
                                    earlyStatus.headers.put( HttpHeaders.CONTENT_RANGE, "bytes */" + length ); // Required in 416.
                                    throw earlyStatus;
                                }
                                ranges.add( new ByteRange( start, end, length ) );
                            }
                        }
                    }
                }
                break;
        }
        return ranges;
    }

    @SuppressWarnings( "LocalVariableHidesMemberVariable" )
    private void handleAcceptHeaders()
    {
        // Get content type by file name and set default GZIP support and content disposition.
        contentType = io.getMimeType( staticRequest.file.getName() );

        // If content type is unknown, then set the default value.
        // For all content types, see: http://www.w3schools.com/media/media_mimeref.asp
        // To add new content types, add new mime-mapping entry in web.xml.
        if ( contentType == null ) {
            contentType = "application/octet-stream";
        }

        // Expect for text. If content type is supported by the browser and expand content type with the one and
        // right character encoding.
        if ( contentType.startsWith( "text" ) ) {
            if ( staticRequest.protocol == HttpVersion.HTTP_1_1 ) {
                String acceptEncoding = staticRequest.httpRequest.getHeader( HttpHeaders.ACCEPT_ENCODING );
                acceptsGzip = acceptEncoding != null && accepts( acceptEncoding, "gzip" );
            }
            contentType += ";charset=UTF-8";
        } // Else, expect for images, determine content disposition. If content type is supported by
        // the browser, then set to inline, else attachment which will pop a 'save as' dialogue.
        else if ( staticRequest.protocol == HttpVersion.HTTP_1_1 && !contentType.startsWith( "image" ) ) {
            String accept = staticRequest.httpRequest.getHeader( HttpHeaders.ACCEPT );
            disposition = accept != null && accepts( accept, contentType ) ? "inline" : "attachment";
        }
    }

    private void initializeResponse()
    {
        // Initialize response.
        staticRequest.httpResponse.reset();
        staticRequest.httpResponse.setBufferSize( configuration.getBufferSize() );
        if ( staticRequest.protocol == HttpVersion.HTTP_1_1 ) {
            // cf. Rfc6266 :
            staticRequest.httpResponse.setHeader( HttpHeaders.CONTENT_DISPOSITION,
                                                  disposition + "; filename*=UTF-8''" + EscapeUtils.encodeUrlPath( staticRequest.file.getName() ) );
            // This would also work :
            // staticRequest.httpResponse.setHeader( HttpHeaders.CONTENT_DISPOSITION, disposition );
            staticRequest.httpResponse.setHeader( HttpHeaders.ACCEPT_RANGES, "bytes" );
            staticRequest.httpResponse.setHeader( HttpHeaders.ETAG, staticRequest.entityTag );
        }
        staticRequest.httpResponse.setDateHeader( HttpHeaders.LAST_MODIFIED, staticRequest.lastModified );
        staticRequest.httpResponse.setDateHeader( HttpHeaders.EXPIRES, System.currentTimeMillis() + configuration.getExpireTime() );
    }

    private void sendRequestedFileParts( List<ByteRange> ranges )
            throws IOException
    {
        if ( ranges.isEmpty() || ranges.get( 0 ) == fullRange ) {

            // Return full file.
            sendFullFile();

        } else if ( ranges.size() == 1 ) {

            // Return single part of file.
            sendSinglePart( ranges.get( 0 ) );

        } else {

            // Return multiple parts of file.
            sendMultipleParts( ranges );

        }
    }

    private void sendFullFile()
            throws IOException
    {
        RandomAccessFile input = null;
        OutputStream output = null;
        try {
            input = new RandomAccessFile( staticRequest.file, "r" );
            output = staticRequest.httpResponse.getOutputStream();

            ByteRange range = fullRange;
            staticRequest.httpResponse.setContentType( contentType );
            if ( staticRequest.protocol == HttpVersion.HTTP_1_1 ) {
                staticRequest.httpResponse.setHeader( HttpHeaders.CONTENT_RANGE, "bytes " + range.start + "-" + range.end + "/" + range.total );
            }
            if ( staticRequest.writeBody ) {
                if ( acceptsGzip ) {
                    staticRequest.httpResponse.setHeader( HttpHeaders.CONTENT_ENCODING, "gzip" );
                    output = new GZIPOutputStream( output, configuration.getBufferSize() );
                } else {
                    staticRequest.httpResponse.setHeader( HttpHeaders.CONTENT_LENGTH, String.valueOf( range.length ) );
                }
                // Copy full range.
                io.copy( input, output, range.start, range.length, configuration.getBufferSize() );
            }

        } finally {
            io.close( output );
            io.close( input );
        }
    }

    // Only used with HTTP/1.1
    private void sendSinglePart( ByteRange range )
            throws IOException
    {
        RandomAccessFile input = null;
        OutputStream output = null;
        try {
            input = new RandomAccessFile( staticRequest.file, "r" );
            output = staticRequest.httpResponse.getOutputStream();

            staticRequest.httpResponse.setContentType( contentType );
            staticRequest.httpResponse.setHeader( HttpHeaders.CONTENT_RANGE, "bytes " + range.start + "-" + range.end + "/" + range.total );
            staticRequest.httpResponse.setHeader( HttpHeaders.CONTENT_LENGTH, String.valueOf( range.length ) );
            staticRequest.httpResponse.setStatus( HttpServletResponse.SC_PARTIAL_CONTENT ); // 206.
            if ( staticRequest.writeBody ) {
                // Copy single part range of multi part range.
                io.copy( input, output, range.start, range.length, configuration.getBufferSize() );
            }

        } finally {
            io.close( output );
            io.close( input );
        }
    }

    // Only used with HTTP/1.1
    private void sendMultipleParts( List<ByteRange> ranges )
            throws IOException
    {
        RandomAccessFile input = null;
        OutputStream output = null;
        try {
            input = new RandomAccessFile( staticRequest.file, "r" );
            output = staticRequest.httpResponse.getOutputStream();

            staticRequest.httpResponse.setContentType( "multipart/byteranges; boundary=" + MULTIPART_BOUNDARY );
            staticRequest.httpResponse.setStatus( HttpServletResponse.SC_PARTIAL_CONTENT ); // 206.
            if ( staticRequest.writeBody ) {
                // Cast back to ServletOutputStream to get the easy println methods.
                ServletOutputStream sos = ( ServletOutputStream ) output;
                // Copy multi part range.
                for ( ByteRange eachRange : ranges ) {
                    // Add multipart boundary and header fields for every range.
                    sos.println();
                    sos.println( "--" + MULTIPART_BOUNDARY );
                    sos.println( HttpHeaders.CONTENT_TYPE + ": " + contentType );
                    sos.println( HttpHeaders.CONTENT_RANGE + ": bytes " + eachRange.start + "-" + eachRange.end + "/" + eachRange.total );
                    // Copy single part range of multi part range.
                    io.copy( input, output, eachRange.start, eachRange.length, configuration.getBufferSize() );
                }
                // End with multipart boundary.
                sos.println();
                sos.println( "--" + MULTIPART_BOUNDARY + "--" );
            }

        } finally {
            io.close( output );
            io.close( input );
        }
    }

    /**
     * Returns true if the given accept header accepts the given value.
     *
     * @param acceptHeader  The accept header.
     * @param toAccept      The value to be accepted.
     *
     * @return True if the given accept header accepts the given value.
     */
    private static boolean accepts( String acceptHeader, String toAccept )
    {
        String[] acceptExpressions = acceptHeader.split( "\\s*(,|;)\\s*" );
        Arrays.sort( acceptExpressions );
        return Arrays.binarySearch( acceptExpressions, toAccept ) > -1
                || Arrays.binarySearch( acceptExpressions, toAccept.replaceAll( "/.*$", "/*" ) ) > -1
                || Arrays.binarySearch( acceptExpressions, "*/*" ) > -1;
    }

    /**
     * Returns a substring of the given string value from the given begin index to the given end
     * index as a long. If the substring is empty, then -1 will be returned
     *
     * @param value         The string value to return a substring as long for.
     * @param beginIndex    The begin index of the substring to be returned as long.
     * @param endIndex      The end index of the substring to be returned as long.
     *
     * @return A substring of the given string value as long or -1 if substring is empty.
     */
    private static long sublong( String value, int beginIndex, int endIndex )
    {
        String substring = value.substring( beginIndex, endIndex );
        return ( substring.length() > 0 ) ? Long.parseLong( substring ) : -1;
    }

}
