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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.codeartisans.staticlet.core.util.IOService;

import org.slf4j.Logger;

/**
 * Produces ETag with content MD5 for files, "name + lastModified" MD5 for directories.
 */
public class HexMD5ETagger
        extends AbstractETagger
        implements ETagger
{

    private static final int BUFFER_SIZE = 128;
    private final Logger logger;
    private final IOService io;

    public HexMD5ETagger( Logger logger, IOService io )
    {
        this.logger = logger;
        this.io = io;
    }

    @Override
    protected String eTagOfFile( File file )
            throws IOException
    {
        FileInputStream fis = null;
        try {

            fis = new FileInputStream( file );
            return hexMD5( fis );

        } finally {
            io.close( fis );
        }
    }

    @Override
    protected String eTagOfDirectory( File directory )
            throws IOException
    {
        String data = directory.getName() + "__" + directory.lastModified();
        return hexMD5( new ByteArrayInputStream( data.getBytes( "UTF-8" ) ) );
    }

    private String hexMD5( InputStream data )
            throws IOException
    {
        try {

            MessageDigest digest = MessageDigest.getInstance( "MD5" );
            byte[] buffer = new byte[ BUFFER_SIZE ];
            int length = 0;
            while ( ( length = data.read( buffer ) ) != -1 ) {
                digest.update( buffer, 0, length );
            }
            byte[] hashBytes = digest.digest();

            return new BigInteger( 1, hashBytes ).toString( 16 );

        } catch ( NoSuchAlgorithmException ex ) {
            logger.debug( "Unable to create ETag using MD5 hash: {}", ex.toString() );
            throw new InternalError( "Running JVM does not support MD5 out of the box! Cannot continue" );
        }
    }

}
