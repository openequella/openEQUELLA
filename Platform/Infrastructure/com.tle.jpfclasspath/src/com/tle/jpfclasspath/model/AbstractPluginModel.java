package com.tle.jpfclasspath.model;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.xml.sax.SAXException;

import com.tle.jpfclasspath.parser.ManifestParser;
import com.tle.jpfclasspath.parser.ModelPluginFragment;
import com.tle.jpfclasspath.parser.ModelPluginManifest;

public abstract class AbstractPluginModel implements IPluginModel
{
	private ModelPluginManifest parsedManifest;
	protected IResource underlyingResource;
	private static final ManifestParser parser = new ManifestParser();

	protected AbstractPluginModel(IResource manifest)
	{
		this.underlyingResource = manifest;
		parse();
	}

	private void parse()
	{
		try
		{
			InputStream inputStream = getInputStream();
			if( inputStream != null )
			{
				parsedManifest = parser.parseManifest(inputStream);
			}
			else
			{
				parsedManifest = null;
			}
		}
		catch( ParserConfigurationException | SAXException | IOException | CoreException e )
		{
			parsedManifest = null;
		}
	}

	protected abstract InputStream getInputStream() throws CoreException;

	@Override
	public void reload()
	{
		parse();
	}

	@Override
	public IResource getUnderlyingResource()
	{
		return underlyingResource;
	}

	@Override
	public boolean isFragmentModel()
	{
		return parsedManifest instanceof ModelPluginFragment;
	}

	@Override
	public ModelPluginManifest getParsedManifest()
	{
		return parsedManifest;
	}
}
