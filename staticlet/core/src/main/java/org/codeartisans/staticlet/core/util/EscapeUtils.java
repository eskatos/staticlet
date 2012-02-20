/*
 * Copyright (c) 2012, Mo Maison. All Rights Reserved.
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

import java.io.UnsupportedEncodingException;

/**
 * Some utility methods for url encoding and html escapes.
 * 
 * It is a shame that standard java library does not include such methods...
 */
public class EscapeUtils
{

    /**
     * @param pathSegment
     * @return escaped path segment, suitable for url
     */
    public static String encodeUrlPath( String pathSegment )
    {
        final StringBuilder sb = new StringBuilder();

        try {
            for ( int i = 0; i < pathSegment.length(); i++ ) {
                final char c = pathSegment.charAt( i );

                if ( ( ( c >= 'A' ) && ( c <= 'Z' ) )
                        || ( ( c >= 'a' ) && ( c <= 'z' ) )
                        || ( ( c >= '0' ) && ( c <= '9' ) )
                        || ( c == '-' ) || ( c == '.' ) || ( c == '_' ) || ( c == '~' ) ) {
                    sb.append( c );
                } else {
                    final byte[] bytes = String.valueOf( c ).getBytes( "UTF-8" );
                    for ( byte b : bytes ) {
                        sb.append( '%' ) //
                                .append( Integer.toHexString( ( b >> 4 ) & 0xf ) ) //
                                .append( Integer.toHexString( b & 0xf ) );
                    }
                }
            }

            return sb.toString();
        } catch ( UnsupportedEncodingException ex ) {
            // does not occur, but in case :
            throw new RuntimeException( ex );
        }
    }

    // cf. OWASP : https://www.owasp.org/index.php/XSS_%28Cross_Site_Scripting%29_Prevention_Cheat_Sheet
    public static String encodeHtml( String aString )
    {
        StringBuilder sb = new StringBuilder();
        for ( int idx = 0; idx < aString.length(); idx++ ) {
            char c = aString.charAt( idx );
            switch ( c ) {
                case '&':
                    sb.append( "&amp;" );
                    break;
                case '<':
                    sb.append( "&lt;" );
                    break;
                case '>':
                    sb.append( "&gt;" );
                    break;
                case '"':
                    sb.append( "&quot;" );
                    break;
                case '\'':
                    sb.append( "&#x27;" ); // &apos; is not recommended
                    break;
                default:
                    sb.append( c );
                    break;
            }
        }
        return sb.toString();
    }

}
