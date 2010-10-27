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
package org.qi4j.library.staticlet;

import java.io.File;
import java.io.IOException;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.staticlet.files.TestData;
import org.qi4j.test.AbstractQi4jTest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StaticletServiceTest
        extends AbstractQi4jTest
{

    private static final Logger LOGGER = LoggerFactory.getLogger( StaticletServiceTest.class );
    private static final int DEFAULT_PORT = 8080;
    private static File docRoot;

    @BeforeClass
    public static void beforeClass()
            throws IOException
    {
        docRoot = TestData.deployDocRoot( new File( System.getProperty( "buildDirectory" ) ) );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        new TestStaticletAssembler( docRoot ).assemble( module );
    }

    private DefaultHttpClient httpClient;
    private BasicResponseHandler strResponseHandler;
    private HttpHost host;

    @Before
    public void before()
    {
        httpClient = new DefaultHttpClient();
        strResponseHandler = new BasicResponseHandler()
        {

            @Override
            public String handleResponse( HttpResponse response )
                    throws HttpResponseException, IOException
            {
                String strResponse = super.handleResponse( response );
                LOGGER.debug( "BasicResponseHandler got: {}", strResponse );
                return strResponse;
            }

        };
        host = new HttpHost( "localhost", DEFAULT_PORT );
    }

    @Test
    public void test404()
            throws IOException
    {
        HttpGet get = new HttpGet( "/staticlet/do-no-exist" );
        HttpResponse response = httpClient.execute( host, get );
        StatusLine status = response.getStatusLine();
        assertEquals( "Was expecting a 404 but got: " + status.getProtocolVersion().toString() + " " + status.getStatusCode() + " " + status.getReasonPhrase(), 404, status.getStatusCode() );
    }

    @Test
    public void testTextPlain()
            throws IOException
    {
        HttpGet get = new HttpGet( "/staticlet/" + TestData.TEXT_PLAIN );
        String test = httpClient.execute( host, get, strResponseHandler ).trim();
        assertEquals( TestData.TEXT_PLAIN_TEXT, test );
    }

    @Test
    public void testData_0()
            throws IOException
    {
        HttpGet get = new HttpGet( "/staticlet/" + TestData.DATA_0 );
        String test = httpClient.execute( host, get, strResponseHandler ).trim();
        assertEquals( "", test );
    }

    @Test
    @Ignore
    public void testDirectoryListing()
            throws IOException, InterruptedException
    {
        HttpGet get = new HttpGet( "/staticlet/" );
        String test = httpClient.execute( host, get, strResponseHandler ).trim();
        Thread.sleep( 10000000 );
    }

    private static void assertEquals( String msg, Object expected, Object was )
    {
        LOGGER.debug( "Expected: '{}' - Is: '{}'", expected, was );
        Assert.assertEquals( msg, expected, was );
    }

    private static void assertEquals( Object expected, Object was )
    {
        LOGGER.debug( "Expected: '{}' - Is: '{}'", expected, was );
        Assert.assertEquals( expected, was );
    }

}
