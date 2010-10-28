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

import java.util.Arrays;

public class ETagMatcher
{

    private static final String SPLIT_REGEX = "\\s*,\\s*";
    private static final String WILDCARD = "*";
    private final String headerValue;
    private final boolean matched;

    public ETagMatcher( String matchHeaderValue, String entityTag )
    {
        this.headerValue = matchHeaderValue;
        if ( this.headerValue == null || this.headerValue.length() <= 0 ) {
            this.matched = false;
        } else {
            String[] matchExpressions = this.headerValue.split( SPLIT_REGEX );
            Arrays.sort( matchExpressions );
            this.matched = Arrays.binarySearch( matchExpressions, entityTag ) > -1
                    || Arrays.binarySearch( matchExpressions, WILDCARD ) > -1;
        }
    }

    public String headerValue()
    {
        return headerValue;
    }

    public boolean doMatch()
    {
        return matched;
    }

    public boolean dontMatch()
    {
        return !matched;
    }

}
