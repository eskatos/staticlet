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
package org.qi4j.library.http.staticlet.files;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import org.junit.Ignore;

@Ignore // JUnit wants to test everything with a test name
public final class TestData
{

    public static final String TEXT_PLAIN = "text-plain.txt";
    public static final String DATA_0 = "data_0";
    public static String TEXT_PLAIN_TEXT;

    public static synchronized File deployDocRoot( File buildDirectory )
            throws IOException
    {
        // Unzip staticlet-docroot.zip to ~/target/ --------------------------------------------------------------------
        String testResourcesDirectory = System.getProperty( "testResourcesDirectory" );
        FileUtils.forceMkdir( buildDirectory );
        int buffSize = 1024 * 64;
        ZipFile docRootZip = new ZipFile( new File( testResourcesDirectory, "staticlet-docroot.zip" ) );
        Enumeration<? extends ZipEntry> entries = docRootZip.entries();
        while ( entries.hasMoreElements() ) {
            ZipEntry eachEntry = entries.nextElement();
            File out = new File( buildDirectory, eachEntry.getName() );
            FileUtils.forceMkdir( out.getParentFile() );
            if ( !eachEntry.isDirectory() ) {
                BufferedInputStream input = new BufferedInputStream( docRootZip.getInputStream( eachEntry ) );
                byte[] buffer = new byte[ buffSize ];
                BufferedOutputStream output = new BufferedOutputStream( new FileOutputStream( out ), buffSize );
                while ( input.read( buffer, 0, buffSize ) != -1 ) {
                    output.write( buffer, 0, buffSize );
                }
                output.flush();
                output.close();
                input.close();
            }
        }
        docRootZip.close();

        // Load test docroot content in memory for unit tests ----------------------------------------------------------
        File docRoot = new File( buildDirectory, "staticlet-docroot" );

        // text-plain.txt
        StringWriter sw = new StringWriter();
        IOUtils.copy( new FileInputStream( new File( docRoot, TEXT_PLAIN ) ), sw, "UTF-8" );
        sw.flush();
        TEXT_PLAIN_TEXT = sw.toString().trim();

        return docRoot;
    }

    private TestData()
    {
    }

}
