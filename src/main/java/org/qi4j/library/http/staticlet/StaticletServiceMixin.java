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
import javax.servlet.ServletException;

import org.codeartisans.staticlet.Staticlet;

import org.qi4j.api.injection.scope.This;

public class StaticletServiceMixin
        extends Staticlet
{

    private static final long serialVersionUID = 1L;
    @This
    private StaticletConfiguration configuration;

    @Override
    public void init()
            throws ServletException
    {
        String docRoot = configuration.docRoot().get();
        Boolean directoryListing = configuration.directoryListing().get();

        if ( true ) {
            System.out.println( "/!\\ /!\\ WARN WARN WARN /!\\ /!\\ FIXME ! Qi4j configuration is flawed, need some code I haven't with me ..." );
            docRoot = System.getProperty( "buildDirectory" ) + File.separator + "staticlet-docroot";
            directoryListing = true;
        }

        initStaticlet( docRoot, directoryListing );
    }

}
