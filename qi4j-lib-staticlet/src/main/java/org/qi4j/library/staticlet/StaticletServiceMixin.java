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

import org.codeartisans.staticlet.core.AbstractStaticlet;

import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.This;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StaticletServiceMixin
        extends AbstractStaticlet
{

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger( StaticletServiceMixin.class );
    @This
    private Configuration<StaticletConfiguration> configRef;

    public StaticletServiceMixin()
    {
        super();
    }

    @Override
    protected Logger getLogger()
    {
        return LOGGER;
    }

    @Override
    protected org.codeartisans.staticlet.core.StaticletConfiguration getConfiguration()
    {
        StaticletConfiguration configuration = configRef.configuration();
        String docRoot = configuration.docRoot().get();
        Boolean directoryListing = configuration.directoryListing().get();
        Integer bufferSize = configuration.bufferSize().get();
        Long expireTime = configuration.expireTime().get();
        return new org.codeartisans.staticlet.core.StaticletConfiguration( docRoot, directoryListing, bufferSize, expireTime );
    }

}
