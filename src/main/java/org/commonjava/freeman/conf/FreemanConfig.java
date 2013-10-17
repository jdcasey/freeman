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
package org.commonjava.freeman.conf;

import java.io.File;

import org.kohsuke.args4j.Option;

public class FreemanConfig
{

    private static final String USER_HOME = System.getProperty( "user.home" );

    public static final File DEFAULT_CONTENT_BASEDIR = new File( USER_HOME, "freeman" );

    public static final String DEFAULT_BRANDING_SUBPATH = "branding";

    public static final String DEFAULT_COMMANDS_SUBPATH = "commands";

    public static final String DEFAULT_STATIC_SUBPATH = "static";

    public static final String DEFAULT_LISTEN = "localhost";

    public static final Integer DEFAULT_PORT = 8080;

    @Option( name = "-h", aliases = "--help", usage = "Show this help screen" )
    private Boolean help;

    @Option( name = "-p", aliases = "--port", usage = "Port to listen on (default: 8080)" )
    private Integer port;

    @Option( name = "-l", aliases = "--listen", usage = "Host or IP address to listen on (default: localhost)" )
    private String listen;

    @Option( name = "-c", aliases = "--content", usage = "Content directory (default: $HOME/freeman)" )
    private File contentBasedir;

    @Option( name = "-s", aliases = "--static", usage = "Static content directory (default: $HOME/freeman/static)" )
    private File staticDir;

    @Option( name = "-b", aliases = "--branding", usage = "Branding content directory (default: $HOME/freeman/branding)" )
    private File brandingDir;

    @Option( name = "-C", aliases = "--commands", usage = "Commands directory (default: $HOME/freeman/templates)" )
    private File commandsDir;

    public FreemanConfig()
    {
    }

    public FreemanConfig( final File storageDir )
    {
        this( storageDir, null, null );
    }

    public FreemanConfig( final File contentDir, final File brandingDir, final File templatesDir )
    {
        this.contentBasedir = contentDir;
        this.brandingDir = brandingDir;
        this.commandsDir = templatesDir;
    }

    public File getContentBasedir()
    {
        return contentBasedir == null ? DEFAULT_CONTENT_BASEDIR : contentBasedir;
    }

    public void setContentBasedir( final File contentBasedir )
    {
        this.contentBasedir = contentBasedir;
    }

    public File getStaticDir()
    {
        return staticDir == null ? new File( getContentBasedir(), DEFAULT_STATIC_SUBPATH ) : staticDir;
    }

    public void setContentDir( final File contentDir )
    {
        this.contentBasedir = contentDir;
    }

    public File getBrandingDir()
    {
        return brandingDir == null ? new File( getContentBasedir(), DEFAULT_BRANDING_SUBPATH ) : brandingDir;
    }

    public void setBrandingDir( final File brandingDir )
    {
        this.brandingDir = brandingDir;
    }

    public File getCommandsDir()
    {
        return commandsDir == null ? new File( getContentBasedir(), DEFAULT_COMMANDS_SUBPATH ) : commandsDir;
    }

    public void setCommandsDir( final File commandsDir )
    {
        this.commandsDir = commandsDir;
    }

    public boolean isHelp()
    {
        return help == null ? false : help;
    }

    public void setHelp( final boolean help )
    {
        this.help = help;
    }

    public String getListen()
    {
        return listen == null ? DEFAULT_LISTEN : listen;
    }

    public void setListen( final String listen )
    {
        this.listen = listen;
    }

    public int getPort()
    {
        return port == null ? DEFAULT_PORT : port;
    }

    public void setPort( final int port )
    {
        this.port = port;
    }

    public void overrideWith( final FreemanConfig overrides )
    {
        if ( overrides.help != null )
        {
            this.help = overrides.help;
        }

        if ( overrides.brandingDir != null )
        {
            this.brandingDir = overrides.brandingDir;
        }

        if ( overrides.contentBasedir != null )
        {
            this.contentBasedir = overrides.contentBasedir;
        }

        if ( overrides.listen != null )
        {
            this.listen = overrides.listen;
        }

        if ( overrides.port != null )
        {
            this.port = overrides.port;
        }

        if ( overrides.commandsDir != null )
        {
            this.commandsDir = overrides.commandsDir;
        }
    }

}
