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
package org.codeartisans.staticlet.core.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import javax.servlet.ServletContext;

import org.slf4j.Logger;

public class IOService
{

    private final ServletContext servletContext;
    private final Logger logger;

    public IOService( ServletContext servletContext, Logger logger )
    {
        this.servletContext = servletContext;
        this.logger = logger;
    }

    public String getMimeType( String filename )
    {
        return servletContext.getMimeType( filename );
    }

    public void close( Closeable resource )
    {
        if ( resource != null ) {
            try {
                resource.close();
            } catch ( IOException ignored ) {
                logger.debug( "Unable to close resource, client aborted request ?", ignored );
            }
        }
    }

    /**
     * Copy the given byte range of the given input to the given output.
     *
     * @param input         The input to copy the given range to the given output for.
     * @param output        The output to copy the given range from the given input for.
     * @param start         Start of the byte range.
     * @param length        Length of the byte range.
     * @param bufferSize    Size of the copy buffer.
     *
     * @throws IOException  If something fails at I/O level.
     */
    public void copy( RandomAccessFile input, OutputStream output,
                      long start, long length, int bufferSize )
            throws IOException
    {
        byte[] buffer = new byte[ bufferSize ];
        int read;

        if ( input.length() == length ) {
            // Full write
            while ( ( read = input.read( buffer ) ) > 0 ) {
                output.write( buffer, 0, read );
            }
        } else {
            // Partial write
            input.seek( start );
            long toRead = length;

            while ( ( read = input.read( buffer ) ) > 0 ) {
                if ( ( toRead -= read ) > 0 ) {
                    output.write( buffer, 0, read );
                } else {
                    output.write( buffer, 0, ( int ) toRead + read );
                    break;
                }
            }
        }
    }

}
