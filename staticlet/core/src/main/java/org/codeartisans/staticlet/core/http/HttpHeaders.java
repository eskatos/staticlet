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

public interface HttpHeaders
{

    // Used only from HttpRequest --------------------------------------------------------------------------------------
    String IF_NONE_MATCH = "If-None-Match";
    String IF_MODIFIED_SINCE = "If-Modified-Since"; // This is the only one used for HTTP/1.0 clients
    String IF_MATCH = "If-Match";
    String IF_UNMODIFIED_SINCE = "If-Unmodified-Since";
    String RANGE = "Range";
    String IF_RANGE = "If-Range";
    String ACCEPT_ENCODING = "Accept-Encoding";
    String ACCEPT = "Accept";
    // Used only in HttpResponse --------------------------------------------------------------------------------------
    String ETAG = "ETag"; // HTTP/1.1 only
    String CONTENT_RANGE = "Content-Range"; // HTTP/1.1 only
    String CONTENT_DISPOSITION = "Content-Disposition"; // HTTP/1.1 only
    String ACCEPT_RANGES = "Accept-Ranges"; // HTTP/1.1 only
    String LAST_MODIFIED = "Last-Modified";
    String EXPIRES = "Expires";
    String CONTENT_ENCODING = "Content-Encoding";
    String CONTENT_LENGTH = "Content-Length";
    String CONTENT_TYPE = "Content-Type";
}
