/**
 * 
 */
package com.tle.web.selection.section;

/**
 * Simple wrapper for RuntimeException that allows a particular Handler
 * configured in the plugin to be directed to places where this particular
 * exception is thrown. It is a potential weakness of the default section-error
 * handlers that if an error originates in the root, the handler will endlessly
 * attempt to rebuild the root (and presumably re-cause and re-throw the error).
 * See Redmine #7607
 * 
 * @author larry
 */
public class RootSelectionException extends RuntimeException
{
	public RootSelectionException(Throwable thrown)
	{
		super(thrown);
	}
}
