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
package org.codeartisans.staticlet.core.http.etag;

import java.io.File;
import java.io.IOException;

public abstract class AbstractETagger
        implements ETagger
{

    @Override
    public final String tagFile( File file )
            throws IOException
    {
        if ( file.isDirectory() ) {
            return eTagOfDirectory( file );
        }
        return eTagOfFile( file );
    }

    protected abstract String eTagOfFile( File file )
            throws IOException;

    protected abstract String eTagOfDirectory( File directory )
            throws IOException;

}
