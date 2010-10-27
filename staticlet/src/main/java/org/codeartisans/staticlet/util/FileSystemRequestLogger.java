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
package org.codeartisans.staticlet.util;

import org.slf4j.Logger;
import org.slf4j.Marker;

public class FileSystemRequestLogger
        implements Logger
{

    private final Logger log;
    private final CharSequence prefix;

    public FileSystemRequestLogger( Logger delegate, CharSequence requestId )
    {
        this.log = delegate;
        this.prefix = new StringBuilder().append( requestId ).append( " " ).toString();
    }

    @Override
    public String getName()
    {
        return log.getName();
    }

    @Override
    public boolean isTraceEnabled()
    {
        return log.isTraceEnabled();
    }

    @Override
    public void trace( String msg )
    {
        log.trace( prefix + msg );
    }

    @Override
    public void trace( String format, Object arg )
    {
        log.trace( prefix + format, arg );
    }

    @Override
    public void trace( String format, Object arg1, Object arg2 )
    {
        log.trace( prefix + format, arg1, arg2 );
    }

    @Override
    public void trace( String format, Object[] argArray )
    {
        log.trace( prefix + format, argArray );
    }

    @Override
    public void trace( String msg, Throwable t )
    {
        log.trace( prefix + msg, t );
    }

    @Override
    public boolean isTraceEnabled( Marker marker )
    {
        return log.isTraceEnabled( marker );
    }

    @Override
    public void trace( Marker marker, String msg )
    {
        log.trace( marker, prefix + msg );
    }

    @Override
    public void trace( Marker marker, String format, Object arg )
    {
        log.trace( marker, prefix + format, arg );
    }

    @Override
    public void trace( Marker marker, String format, Object arg1, Object arg2 )
    {
        log.trace( marker, prefix + format, arg1, arg2 );
    }

    @Override
    public void trace( Marker marker, String format, Object[] argArray )
    {
        log.trace( marker, prefix + format, argArray );
    }

    @Override
    public void trace( Marker marker, String msg, Throwable t )
    {
        log.trace( marker, prefix + msg, t );
    }

    @Override
    public boolean isDebugEnabled()
    {
        return log.isDebugEnabled();
    }

    @Override
    public void debug( String msg )
    {
        log.debug( prefix + msg );
    }

    @Override
    public void debug( String format, Object arg )
    {
        log.debug( prefix + format, arg );
    }

    @Override
    public void debug( String format, Object arg1, Object arg2 )
    {
        log.debug( prefix + format, arg1, arg2 );
    }

    @Override
    public void debug( String format, Object[] argArray )
    {
        log.debug( prefix + format, argArray );
    }

    @Override
    public void debug( String msg, Throwable t )
    {
        log.debug( prefix + msg, t );
    }

    @Override
    public boolean isDebugEnabled( Marker marker )
    {
        return log.isDebugEnabled( marker );
    }

    @Override
    public void debug( Marker marker, String msg )
    {
        log.debug( marker, prefix + msg );
    }

    @Override
    public void debug( Marker marker, String format, Object arg )
    {
        log.debug( marker, prefix + format, arg );
    }

    @Override
    public void debug( Marker marker, String format, Object arg1, Object arg2 )
    {
        log.debug( marker, prefix + format, arg1, arg2 );
    }

    @Override
    public void debug( Marker marker, String format, Object[] argArray )
    {
        log.debug( marker, prefix + format, argArray );
    }

    @Override
    public void debug( Marker marker, String msg, Throwable t )
    {
        log.debug( marker, prefix + msg, t );
    }

    @Override
    public boolean isInfoEnabled()
    {
        return log.isInfoEnabled();
    }

    @Override
    public void info( String msg )
    {
        log.info( prefix + msg );
    }

    @Override
    public void info( String format, Object arg )
    {
        log.info( prefix + format, arg );
    }

    @Override
    public void info( String format, Object arg1, Object arg2 )
    {
        log.info( prefix + format, arg1, arg2 );
    }

    @Override
    public void info( String format, Object[] argArray )
    {
        log.info( prefix + format, argArray );
    }

    @Override
    public void info( String msg, Throwable t )
    {
        log.info( prefix + msg, t );
    }

    @Override
    public boolean isInfoEnabled( Marker marker )
    {
        return log.isInfoEnabled( marker );
    }

    @Override
    public void info( Marker marker, String msg )
    {
        log.info( marker, prefix + msg );
    }

    @Override
    public void info( Marker marker, String format, Object arg )
    {
        log.info( marker, prefix + format, arg );
    }

    @Override
    public void info( Marker marker, String format, Object arg1, Object arg2 )
    {
        log.info( marker, prefix + format, arg1, arg2 );
    }

    @Override
    public void info( Marker marker, String format, Object[] argArray )
    {
        log.info( marker, prefix + format, argArray );
    }

    @Override
    public void info( Marker marker, String msg, Throwable t )
    {
        log.info( marker, prefix + msg, t );
    }

    @Override
    public boolean isWarnEnabled()
    {
        return log.isWarnEnabled();
    }

    @Override
    public void warn( String msg )
    {
        log.warn( prefix + msg );
    }

    @Override
    public void warn( String format, Object arg )
    {
        log.warn( prefix + format, arg );
    }

    @Override
    public void warn( String format, Object arg1, Object arg2 )
    {
        log.warn( prefix + format, arg1, arg2 );
    }

    @Override
    public void warn( String format, Object[] argArray )
    {
        log.warn( prefix + format, argArray );
    }

    @Override
    public void warn( String msg, Throwable t )
    {
        log.warn( prefix + msg, t );
    }

    @Override
    public boolean isWarnEnabled( Marker marker )
    {
        return log.isWarnEnabled( marker );
    }

    @Override
    public void warn( Marker marker, String msg )
    {
        log.warn( marker, prefix + msg );
    }

    @Override
    public void warn( Marker marker, String format, Object arg )
    {
        log.warn( marker, prefix + format, arg );
    }

    @Override
    public void warn( Marker marker, String format, Object arg1, Object arg2 )
    {
        log.warn( marker, prefix + format, arg1, arg2 );
    }

    @Override
    public void warn( Marker marker, String format, Object[] argArray )
    {
        log.warn( marker, prefix + format, argArray );
    }

    @Override
    public void warn( Marker marker, String msg, Throwable t )
    {
        log.warn( marker, prefix + msg, t );
    }

    @Override
    public boolean isErrorEnabled()
    {
        return log.isErrorEnabled();
    }

    @Override
    public void error( String msg )
    {
        log.error( prefix + msg );
    }

    @Override
    public void error( String format, Object arg )
    {
        log.error( prefix + format, arg );
    }

    @Override
    public void error( String format, Object arg1, Object arg2 )
    {
        log.error( prefix + format, arg1, arg2 );
    }

    @Override
    public void error( String format, Object[] argArray )
    {
        log.error( prefix + format, argArray );
    }

    @Override
    public void error( String msg, Throwable t )
    {
        log.error( prefix + msg, t );
    }

    @Override
    public boolean isErrorEnabled( Marker marker )
    {
        return log.isErrorEnabled( marker );
    }

    @Override
    public void error( Marker marker, String msg )
    {
        log.error( marker, prefix + msg );
    }

    @Override
    public void error( Marker marker, String format, Object arg )
    {
        log.error( marker, prefix + format, arg );
    }

    @Override
    public void error( Marker marker, String format, Object arg1, Object arg2 )
    {
        log.error( marker, prefix + format, arg1, arg2 );
    }

    @Override
    public void error( Marker marker, String format, Object[] argArray )
    {
        log.error( marker, prefix + format, argArray );
    }

    @Override
    public void error( Marker marker, String msg, Throwable t )
    {
        log.error( marker, prefix + msg, t );
    }

}
