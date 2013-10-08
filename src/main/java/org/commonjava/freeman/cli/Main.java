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
package org.commonjava.freeman.cli;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import org.commonjava.freeman.conf.FreemanConfig;
import org.commonjava.freeman.conf.RendererConfig;
import org.commonjava.freeman.data.CommandController;
import org.commonjava.freeman.infra.render.ContentRenderer;
import org.commonjava.freeman.infra.render.RenderingEngine;
import org.commonjava.freeman.infra.render.json.JsonRenderer;
import org.commonjava.freeman.infra.render.tmpl.JRubyERBRenderer;
import org.commonjava.freeman.rest.CommandHandler;
import org.commonjava.freeman.rest.StaticContentHandler;
import org.commonjava.freeman.util.ContentType;
import org.commonjava.freeman.util.JRubyLauncher;
import org.commonjava.vertx.vabr.ApplicationRouter;
import org.commonjava.vertx.vabr.RouteCollection;
import org.commonjava.vertx.vabr.RouteHandler;
import org.commonjava.web.json.ser.JsonSerializer;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultVertx;
import org.vertx.java.platform.Verticle;

public class Main
    extends Verticle
{

    public static void main( final String[] args )
        throws IOException
    {
        new Main( args ).run();
    }

    private boolean canStart = false;

    private final FreemanConfig config;

    public Main( final String[] args )
    {
        config = new FreemanConfig();
        final CmdLineParser parser = new CmdLineParser( config );
        try
        {
            parser.parseArgument( args );
            canStart = true;
        }
        catch ( final CmdLineException e )
        {
            System.out.printf( "ERROR: %s", e.getMessage() );
            printUsage( parser, e );
        }

        if ( config.isHelp() )
        {
            printUsage( parser, null );
            canStart = false;
        }
    }

    private static void printUsage( final CmdLineParser parser, final Exception error )
    {
        if ( error != null )
        {
            System.err.println( "Invalid option(s): " + error.getMessage() );
            System.err.println();
        }

        System.err.println( "Usage: $0 [OPTIONS] [<target-path>]" );
        System.err.println();
        System.err.println();
        // If we are running under a Linux shell COLUMNS might be available for the width
        // of the terminal.
        parser.setUsageWidth( ( System.getenv( "COLUMNS" ) == null ? 100 : Integer.valueOf( System.getenv( "COLUMNS" ) ) ) );
        parser.printUsage( System.err );
        System.err.println();
    }

    public void run()
        throws IOException
    {
        if ( !canStart )
        {
            return;
        }

        start();
        final Vertx v = new DefaultVertx();
        setVertx( v );

        final Map<String, String> rawTemplateConf = new HashMap<>();
        rawTemplateConf.put( "command@" + ContentType.TEXT_HTML.value(), "groovy/html/command.groovy" );
        rawTemplateConf.put( "command@" + ContentType.TEXT_PLAIN.value(), "groovy/plain/command.groovy" );

        final RendererConfig templateConfig = new RendererConfig( rawTemplateConf, config );
        final JsonSerializer serializer = new JsonSerializer(/* new PrettyPrintingAdapter() */);
        final JRubyLauncher launcher = new JRubyLauncher();

        final Set<ContentRenderer> renderers = new HashSet<>();
        renderers.add( new JRubyERBRenderer( templateConfig, launcher ) );
        renderers.add( new JsonRenderer( serializer ) );

        final RenderingEngine engine = new RenderingEngine( renderers );

        final Set<RouteHandler> handlers = new HashSet<RouteHandler>()
        {
            private static final long serialVersionUID = 1L;

            {
                add( new CommandHandler( new CommandController( config, launcher ), engine ) );
            }
        };

        final ServiceLoader<RouteCollection> collections = ServiceLoader.load( RouteCollection.class );
        final ApplicationRouter router = new ApplicationRouter( handlers, collections );
        router.noMatch( new StaticContentHandler( config ) );

        final String listen = config.getListen();
        vertx.createHttpServer()
             .requestHandler( router )
             .listen( config.getPort(), listen );

        System.out.printf( "Listening for requests on %s:%s\n\n", config.getListen(), config.getPort() );

        synchronized ( this )
        {
            try
            {
                wait();
            }
            catch ( final InterruptedException e )
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
