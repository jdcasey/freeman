/*******************************************************************************
 * Copyright (C) 2013 John Casey.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.commonjava.freeman.rest;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.commonjava.freeman.conf.FreemanConfig;
import org.commonjava.util.logging.Logger;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;

public class StaticContentHandler
    implements Handler<HttpServerRequest>
{

    private final Logger logger = new Logger( getClass() );

    private final File staticBasedir;

    public StaticContentHandler( final FreemanConfig mainConf )
    {
        this.staticBasedir = mainConf.getContentDir();
    }

    @Override
    public void handle( final HttpServerRequest req )
    {
        final String path = req.absoluteURI()
                               .getPath();
        req.response()
           .setStatusCode( 200 );

        logger.info( "Serving static path: '%s'", path );

        final File f = new File( staticBasedir, path );
        if ( !f.exists() )
        {
            final String resource = "static/" + path;
            //            logger.info( "Path does not exist in static directory: %s; checking for classpath resource: '%s'", staticBasedir, resource );

            final InputStream stream = Thread.currentThread()
                                             .getContextClassLoader()
                                             .getResourceAsStream( resource );
            if ( stream == null )
            {
                //                logger.info( "No dice" );
                req.response()
                   .setStatusMessage( "Not found" )
                   .setStatusCode( 404 )
                   .end();
            }
            else
            {
                //                logger.info( "Sending classpath resource: %s", resource );
                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try
                {
                    IOUtils.copy( stream, baos );
                }
                catch ( final IOException e )
                {
                    logger.error( "Error reading static resource from classpath: %s. Reason: %s", e, path, e.getMessage() );
                    req.response()
                       .setStatusCode( 500 )
                       .setStatusMessage( "Error reading static resource from classpath: " + path )
                       .end();
                    return;
                }

                try
                {
                    final int len = baos.toByteArray().length;
                    final Buffer buf = new Buffer( baos.toByteArray() );
                    //                    logger.info( "Send: %d bytes", len );
                    req.response()
                       .putHeader( "Content-Length", Integer.toString( len ) )
                       .end( buf );
                }
                finally
                {
                    IOUtils.closeQuietly( stream );
                }
            }
        }
        else if ( f.isDirectory() )
        {
            //            logger.info( "Requested file is actually a directory!" );
            req.response()
               .setStatusMessage( "Content is a directory" )
               .setStatusCode( 404 )
               .end();
        }
        else
        {
            //            logger.info( "Sending freeki file resource: %s", f.getAbsolutePath() );
            req.response()
               .sendFile( f.getAbsolutePath() );
        }
    }
}
