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
package org.commonjava.freeman.infra.render.tmpl;

import static org.commonjava.freeman.infra.render.RenderUtils.getTemplateKey;
import static org.commonjava.freeman.util.ContentType.TEXT_HTML;
import static org.commonjava.freeman.util.ContentType.TEXT_PLAIN;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.codehaus.groovy.control.CompilationFailedException;
import org.commonjava.freeman.conf.RendererConfig;
import org.commonjava.freeman.infra.render.ContentRenderer;
import org.commonjava.freeman.infra.render.RenderingException;
import org.commonjava.freeman.util.ContentType;
import org.commonjava.freeman.util.JRubyLauncher;

public class JRubyERBRenderer
    implements ContentRenderer
{

    private static final ContentType[] TYPES = { TEXT_HTML, TEXT_PLAIN };

    private final RendererConfig config;

    private final JRubyLauncher launcher;

    public JRubyERBRenderer( final RendererConfig config, final JRubyLauncher launcher )
    {
        this.launcher = launcher;
        this.config = config;
    }

    @Override
    public ContentType[] getContentTypes()
    {
        return TYPES;
    }

    @Override
    public String render( final Object data, final Map<String, String> requestParams, final ContentType type )
        throws RenderingException
    {
        final String path = config.getTemplate( type, getTemplateKey( data ) );
        //        System.out.printf( "Using template: %s\n", path );
        try
        {
            final Map<String, Object> map = new HashMap<String, Object>();
            map.put( "data", data );
            map.put( "params", requestParams );

            String template;
            final File templateFile = new File( config.getBrandingDir(), path );
            if ( templateFile.exists() && !templateFile.isDirectory() )
            {
                template = FileUtils.readFileToString( templateFile );
            }
            else
            {
                final InputStream stream = Thread.currentThread()
                                                 .getContextClassLoader()
                                                 .getResourceAsStream( path );

                template = stream == null ? null : IOUtils.toString( stream );
            }

            if ( template == null )
            {
                throw new RenderingException( "Failed to locate template: %s", path );
            }

            final String html = launcher.erb( template, map );

            return html;
        }
        catch ( CompilationFailedException | IOException e )
        {
            throw new RenderingException( "Failed to load template: %s. Reason: %s", e, path, e.getMessage() );
        }
    }

}
