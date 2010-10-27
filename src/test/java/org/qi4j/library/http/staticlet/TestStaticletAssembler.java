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
package org.qi4j.library.http.staticlet;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.http.JettyConfiguration;
import static org.qi4j.library.http.Servlets.*;
import org.qi4j.test.EntityTestAssembler;

@Ignore
public class TestStaticletAssembler
        implements Assembler
{

    private final File docRoot;

    public TestStaticletAssembler( File docRoot )
    {
        this.docRoot = docRoot;
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        new EntityTestAssembler().assemble( module );

        // HttpService using embedded Jetty
        module.addServices( HttpService.class ).visibleIn( Visibility.module ).instantiateOnStartup();
        module.addEntities( JettyConfiguration.class ).visibleIn( Visibility.module );

        // Only one assembly at a time for now, need to figure out how to re-run the same test with a different assembly
        if ( true ) {

            // StaticletService Assembly
            module.addEntities( StaticletConfiguration.class ).visibleIn( Visibility.module );
            module.forMixin( StaticletConfiguration.class ).declareDefaults().docRoot().set( docRoot.getAbsolutePath() ); // FIXME DO NOT WORK
            addServlets( serve( "/staticlet/*" ).with( StaticletService.class ) ).to( module );

        } else {

            // Used to test servlet init params for webapps & web.xml support
            Map<String, String> initParams = new HashMap<String, String>();
            initParams.put( "docRoot", docRoot.getAbsolutePath() );
            initParams.put( "directoryListing", "true" );
            addServlets( serve( "/staticlet/*" ).withInitParams( initParams ).with( StaticletService.class ) ).to( module );

        }

    }

}
