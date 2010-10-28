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
package org.codeartisans.staticlet;

public class StaticletConfiguration
{

    private final String docRoot;
    private final Boolean directoryListing;
    private final Integer bufferSize;
    private final Long expireTime;

    public StaticletConfiguration( String docRoot, Boolean directoryListing, Integer bufferSize, Long expireTime )
    {
        this.docRoot = docRoot;
        this.directoryListing = directoryListing;
        this.bufferSize = bufferSize;
        this.expireTime = expireTime;
    }

    public String getDocRoot()
    {
        return docRoot;
    }

    public boolean isDirectoryListing()
    {
        return directoryListing;
    }

    public int getBufferSize()
    {
        return bufferSize;
    }

    public long getExpireTime()
    {
        return expireTime;
    }

}
