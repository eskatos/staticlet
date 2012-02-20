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

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.text.MessageFormat;
import org.codeartisans.staticlet.core.util.EscapeUtils;

import org.slf4j.Logger;

public class DirectoryRepresenter
{

    static class HTML
    {

        private static final String DOC_HEAD = "<html><head><title>{0}</title></head><body>";
        private static final String BODY_START = "<ul>\n";
        static final String DIRLIST_HEADER = DOC_HEAD + BODY_START;
        static final String DIRLIST_HEADER_TRACE = DOC_HEAD + "<p><small><strong>trace:</strong> requestPathInfo=\"{1}\" file=\"{2}\"</small></p>" + BODY_START;
        static final String DIRLIST_DIR = "<li class=\"directory\"><a href=\"{0}\">{1}</a>/</li>\n";
        static final String DIRLIST_FILE = "<li class=\"file\"><a href=\"{0}\">{1}</a></li>\n";
        static final String DIRLIST_FOOTER = "</ul></body></html>";
    }

    private final Logger log;
    private final StaticRequest staticRequest;

    DirectoryRepresenter( Logger log, StaticRequest staticRequest )
    {
        this.log = log;
        this.staticRequest = staticRequest;
    }

    void represent()
            throws IOException
    {
        staticRequest.httpResponse.setContentType( "text/html; charset=UTF-8" );
        Writer out = staticRequest.httpResponse.getWriter();
        String htmlName = EscapeUtils.encodeHtml( staticRequest.file.getName() );
        if ( log.isTraceEnabled() ) {
            log.warn( "Logger trace level is activated, will output sensitive data to the browser (local file path)" );
            // TODO escape html :
            out.write( MessageFormat.format( HTML.DIRLIST_HEADER_TRACE, htmlName, staticRequest.pathInfo, staticRequest.file ) );
        } else {
            out.write( MessageFormat.format( HTML.DIRLIST_HEADER, htmlName ) );
        }

        boolean atJailRoot = atJailRoot();

        if ( !atJailRoot ) {
            out.write( MessageFormat.format( HTML.DIRLIST_DIR, parentPath(), ".." ) );
        }

        for ( File eachFile : staticRequest.file.listFiles() ) {

            String urlPath = EscapeUtils.encodeUrlPath( eachFile.getName() );
            htmlName = EscapeUtils.encodeHtml( eachFile.getName() );
            if ( eachFile.isDirectory() ) {
                out.write( MessageFormat.format( HTML.DIRLIST_DIR, staticRequest.uri + "/" + urlPath, htmlName ) );
            } else {
                out.write( MessageFormat.format( HTML.DIRLIST_FILE, staticRequest.uri + "/" + urlPath, htmlName ) );
            }
        }

        out.write( HTML.DIRLIST_FOOTER );
        out.flush();
    }

    private boolean atJailRoot()
    {
        boolean atJailRoot = staticRequest == null || "/".equals( staticRequest.pathInfo.trim() );
        log.trace( "atJailRoot? {}", atJailRoot );
        return atJailRoot;
    }

    private String parentPath()
    {
        int indexOfLastSlash = staticRequest.uri.lastIndexOf( '/' );
        String parentPath = "/";
        if ( indexOfLastSlash > 0 ) {
            parentPath = staticRequest.uri.substring( 0, indexOfLastSlash );
        }
        log.trace( "Parent path is {}", parentPath );
        return parentPath;
    }

}
