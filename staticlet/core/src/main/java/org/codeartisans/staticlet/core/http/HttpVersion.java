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
package org.codeartisans.staticlet.core.http;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codeartisans.staticlet.core.EarlyHttpStatusException;

public enum HttpVersion
{

    HTTP_1_0( "HTTP/1.0" ),
    HTTP_1_1( "HTTP/1.1" );
    String protocol;

    private HttpVersion( String protocol )
    {
        this.protocol = protocol;
    }

    public String protocol()
    {
        return protocol;
    }

    public static HttpVersion fromRequestProtocol( HttpServletRequest httpRequest )
            throws EarlyHttpStatusException
    {
        String requestProtocol = httpRequest.getProtocol();
        if ( HTTP_1_1.protocol.equals( requestProtocol ) ) {
            return HTTP_1_1;
        } else if ( HTTP_1_0.protocol.equals( requestProtocol ) ) {
            return HTTP_1_0;
        }
        throw new EarlyHttpStatusException( HttpServletResponse.SC_BAD_REQUEST, "Unknown HTTP protocol: " + requestProtocol );
    }

}
