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
package org.codeartisans.staticlet.classic;

import org.codeartisans.staticlet.core.AbstractStaticlet;
import org.codeartisans.staticlet.core.StaticletConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Staticlet
        extends AbstractStaticlet
{

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger( Staticlet.class );

    public Staticlet()
    {
        super();
    }

    @Override
    protected Logger getLogger()
    {
        return LOGGER;
    }

    @Override
    protected StaticletConfiguration getConfiguration()
    {
        String docRootString = getInitParameter( "docRoot" );
        String directoryListingString = getInitParameter( "directoryListing" );
        String bufferSizeString = getInitParameter( "bufferSize" );
        String expireTimeString = getInitParameter( "expireTime" );
        boolean bufferSizePresent = bufferSizeString != null && bufferSizeString.length() > 0;
        boolean expireTimePresent = expireTimeString != null && expireTimeString.length() > 0;
        return new StaticletConfiguration( docRootString,
                                           Boolean.valueOf( directoryListingString ),
                                           bufferSizePresent ? Integer.parseInt( bufferSizeString ) : null,
                                           expireTimePresent ? Long.parseLong( expireTimeString ) : null );
    }

}
