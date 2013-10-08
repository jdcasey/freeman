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
package org.commonjava.freeman.model;

import java.io.File;

public class Command
{

    private final String label;

    private final File html;

    private final File script;

    public Command( final String label, final File html, final File script )
    {
        this.label = label;
        this.html = html;
        this.script = script;
    }

    public String getLabel()
    {
        return label;
    }

    public File getHtml()
    {
        return html;
    }

    public File getScript()
    {
        return script;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( label == null ) ? 0 : label.hashCode() );
        return result;
    }

    @Override
    public boolean equals( final Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        if ( obj == null )
        {
            return false;
        }
        if ( getClass() != obj.getClass() )
        {
            return false;
        }
        final Command other = (Command) obj;
        if ( label == null )
        {
            if ( other.label != null )
            {
                return false;
            }
        }
        else if ( !label.equals( other.label ) )
        {
            return false;
        }
        return true;
    }

}
