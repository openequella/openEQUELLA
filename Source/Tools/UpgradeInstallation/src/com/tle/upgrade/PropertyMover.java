package com.tle.upgrade;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

public class PropertyMover
{
	private File dest;
	private File source;
	private String prefix;
	private Pattern pattern;

	public PropertyMover(File source, File dest, Pattern pattern, String prefix)
	{
		this.source = source;
		this.dest = dest;
		this.pattern = pattern;
		this.prefix = prefix;
	}

	public void move(UpgradeResult result) throws IOException, ConfigurationException
	{
		final Map<String, String> optionMap = new HashMap<String, String>();

		LineFileModifier modifier = new LineFileModifier(source, result)
		{

			@Override
			protected String processLine(String line)
			{
				Matcher matcher = pattern.matcher(line);
				if( matcher.matches() )
				{
					boolean comment = matcher.group(1).trim().startsWith("#"); //$NON-NLS-1$
					if( !comment )
					{
						optionMap.put(prefix + matcher.group(2), matcher.group(3));
					}
					return null;
				}
				return line;
			}
		};
		modifier.update();

		if( dest != null )
		{
			if( !optionMap.isEmpty() )
			{
				new PropertyFileModifier(dest)
				{
					@Override
					protected boolean modifyProperties(PropertiesConfiguration props)
					{
						for( String key : optionMap.keySet() )
						{
							String value = optionMap.get(key);
							props.setProperty(key, value);
						}
						return true;
					}
				}.updateProperties();
			}
		}

	}
}
