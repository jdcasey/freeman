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
package org.commonjava.freeman.data;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.commonjava.freeman.conf.FreemanConfig;
import org.commonjava.freeman.model.Command;
import org.commonjava.freeman.util.JRubyLauncher;
import org.commonjava.util.logging.Logger;

public class CommandController
{

    private final Logger logger = new Logger( getClass() );

    private final FreemanConfig conf;

    private final JRubyLauncher launcher;

    public CommandController( final FreemanConfig conf, final JRubyLauncher launcher )
    {
        this.conf = conf;
        this.launcher = launcher;
    }

    private Map<String, Command> getCommands()
    {
        final Map<String, Command> commands = new HashMap<>();

        final File dir = conf.getCommandsDir();
        logger.info( "Checking for commands in: %s", dir );
        if ( dir.isDirectory() )
        {
            final File[] files = dir.listFiles();
            for ( final File file : files )
            {
                if ( file.getName()
                         .startsWith( "." ) )
                {
                    continue;
                }

                if ( file.isDirectory() )
                {
                    final String label = file.getName();
                    final File inHtml = new File( file, label + ".form.html" );
                    final File script = new File( file, label + ".rb" );
                    logger.info( "Adding command %s", label );
                    commands.put( label, new Command( label, inHtml, script ) );
                }
            }
        }

        return commands;
    }

    public List<String> getCommandLabels()
    {
        final List<String> labels = new ArrayList<>( getCommands().keySet() );
        Collections.sort( labels );

        return labels;
    }

    public File getCommandForm( final String label )
    {
        final Command command = getCommands().get( label );
        if ( command != null )
        {
            return command.getInHtml();
        }

        return null;
    }

    public Map<String, Object> runCommandAction( final String label, final Map<String, String> params )
        throws CommandException
    {
        final Command command = getCommands().get( label );
        if ( command == null )
        {
            throw new CommandException( "Cannot find command: '%s'", label );
        }

        final File actionFile = command.getScript();

        final Map<String, Object> map = new HashMap<String, Object>();
        map.putAll( params );

        return launcher.execute( actionFile, map );
    }

}
