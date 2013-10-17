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

import static org.apache.commons.lang.StringUtils.join;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.commonjava.freeman.data.CommandController;
import org.commonjava.freeman.data.CommandException;
import org.commonjava.freeman.infra.render.RenderingEngine;
import org.commonjava.freeman.infra.render.RenderingException;
import org.commonjava.freeman.util.ContentType;
import org.commonjava.util.logging.Logger;
import org.commonjava.vertx.vabr.Method;
import org.commonjava.vertx.vabr.RouteHandler;
import org.commonjava.vertx.vabr.anno.Route;
import org.commonjava.vertx.vabr.anno.Routes;
import org.commonjava.web.json.model.Listing;
import org.vertx.java.core.VoidHandler;
import org.vertx.java.core.http.HttpServerRequest;

public class CommandHandler
    implements RouteHandler
{

    private final Logger logger = new Logger( getClass() );

    private final CommandController controller;

    private final RenderingEngine render;

    public CommandHandler( final CommandController controller, final RenderingEngine render )
    {
        this.controller = controller;
        this.render = render;
    }

    /* @formatter:off */
    @Routes( {
        @Route( path="/commands", method=Method.GET )
    } )
    /* @formatter:on */
    public void list( final HttpServerRequest req )
        throws Exception
    {
        final List<String> labels = controller.getCommandLabels();
        logger.info( "Rendering %d commands:\n\n  - %s", labels.size(), join( labels, "\n  - " ) );

        final String json = render.render( new Listing<String>( labels ), ContentType.APPLICATION_JSON, Collections.<String, String> emptyMap() );

        req.response()
           .setStatusCode( 200 )
           .putHeader( "Content-Length", Integer.toString( json.length() ) )
           .write( json )
           .end();
    }

    /* @formatter:off */
    @Routes( {
        @Route( path="/commands/:command", method=Method.GET )
    } )
    /* @formatter:on */
    public void get( final HttpServerRequest req )
        throws Exception
    {
        final String template = req.params()
                                   .get( PathParameter.command.name() );

        //        logger.info( "Loading HTML form for template: '%s'\nURI: '%s'", template, req.absoluteURI() );
        final File html = controller.getCommandForm( template );
        if ( !html.exists() )
        {
            req.response()
               .setStatusCode( 404 )
               .setStatusMessage( "Not Found" )
               .end();
        }
        else
        {
            req.response()
               .setStatusCode( 200 )
               .sendFile( html.getAbsolutePath() );
        }
    }

    /* @formatter:off */
    @Routes( {
        @Route( path="/commands/:command", method=Method.POST )
    } )
    /* @formatter:on */
    public void post( final HttpServerRequest req )
        throws Exception
    {
        req.expectMultiPart( true );

        req.endHandler( new VoidHandler()
        {
            @Override
            protected void handle()
            {
                final String template = req.params()
                                           .get( PathParameter.command.name() );

                final Map<String, String> params = new HashMap<>();
                for ( final Map.Entry<String, String> entry : req.params() )
                {
                    final String key = entry.getKey();
                    if ( !params.containsKey( key ) )
                    {
                        //                        logger.info( "PARAMS+ (path) %s = %s", key, entry.getValue() );
                        params.put( key, entry.getValue() );
                    }
                }

                for ( final Map.Entry<String, String> entry : req.formAttributes() )
                {
                    final String key = entry.getKey();
                    if ( !params.containsKey( key ) )
                    {
                        //                        logger.info( "PARAMS+ (form) %s = %s", key, entry.getValue() );
                        params.put( key, entry.getValue() );
                    }
                }

                try
                {
                    final Map<String, Object> result = controller.runCommandAction( template, params );
                    if ( result == null )
                    {
                        req.response()
                           .setStatusCode( 200 )
                           .setStatusMessage( "OK" )
                           .end();
                    }
                    else
                    {
                        String json;
                        try
                        {
                            json = render.render( result, ContentType.APPLICATION_JSON, params );
                        }
                        catch ( final RenderingException e )
                        {
                            req.response()
                               .setStatusCode( 500 )
                               .setStatusMessage( e.getMessage() )
                               .write( join( result.entrySet(), "\n" ) )
                               .end();

                            return;
                        }

                        req.response()
                           .setStatusCode( 200 );

                        req.response()
                           .headers()
                           .add( "Content-Length", Integer.toString( json.length() ) );

                        req.response()
                           .headers()
                           .add( "Content-Type", ContentType.APPLICATION_JSON.value() );

                        req.response()
                           .write( json )
                           .end();
                    }
                }
                catch ( final CommandException e )
                {
                    req.response()
                       .setStatusCode( 500 )
                       .setStatusMessage( e.getMessage() )
                       .end();
                }
            }
        } );
    }

}
