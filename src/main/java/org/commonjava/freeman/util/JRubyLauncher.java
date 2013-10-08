package org.commonjava.freeman.util;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.io.FileUtils;
import org.commonjava.freeman.data.CommandException;
import org.commonjava.freeman.infra.render.RenderingException;

public class JRubyLauncher
{

    @SuppressWarnings( "unchecked" )
    public Map<String, Object> execute( final String script, final Map<String, Object> ctx )
        throws CommandException
    {
        final ScriptEngineManager sem = new ScriptEngineManager();
        final ScriptEngine engine = sem.getEngineByName( "jruby" );
        final ScriptContext context = engine.getContext();

        for ( final Entry<String, Object> entry : ctx.entrySet() )
        {
            final String key = entry.getKey();
            final Object value = entry.getValue();

            context.setAttribute( key, value, ScriptContext.ENGINE_SCOPE );
        }

        try
        {
            return (Map<String, Object>) engine.eval( script );

            //            return (String) engine.getBindings( ScriptContext.ENGINE_SCOPE )
            //                                  .get( "output" );
        }
        catch ( final ScriptException e )
        {
            throw new CommandException( "Failed to execute script: %s", e, e.getMessage() );
        }
    }

    public String erb( final String script, final Map<String, Object> ctx )
        throws RenderingException
    {
        final ScriptEngineManager sem = new ScriptEngineManager();
        final ScriptEngine engine = sem.getEngineByName( "jruby" );
        final ScriptContext context = engine.getContext();

        final StringBuilder sb = new StringBuilder();
        sb.append( "require 'erb'\n\nCONTENT=<<-EOC\n" )
          .append( script )
          .append( "\nEOC" )
          .append( "\n\nresult = ERB.new(CONTENT).result(binding)\n" );

        for ( final Entry<String, Object> entry : ctx.entrySet() )
        {
            final String key = entry.getKey();
            final Object value = entry.getValue();

            context.setAttribute( key, value, ScriptContext.ENGINE_SCOPE );
        }

        try
        {
            engine.eval( sb.toString() );
            return (String) engine.getBindings( ScriptContext.ENGINE_SCOPE )
                                  .get( "result" );
        }
        catch ( final ScriptException e )
        {
            throw new RenderingException( "Failed to execute script: %s", e, e.getMessage() );
        }
    }

    public Map<String, Object> execute( final File actionFile, final Map<String, Object> map )
        throws CommandException
    {
        try
        {
            return execute( FileUtils.readFileToString( actionFile ), map );
        }
        catch ( final IOException e )
        {
            throw new CommandException( "Failed to read script: %s. Reason: %s", e, actionFile, e.getMessage() );
        }
    }

}
