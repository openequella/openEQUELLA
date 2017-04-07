package com.tle.jpfclasspath.model;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

@SuppressWarnings("nls")
public class JarPluginModelImpl extends AbstractPluginModel
{
	private RepoModel repoModel;

	public JarPluginModelImpl(RepoModel repoModel, IResource jarFile)
	{
		super(jarFile);
		this.repoModel = repoModel;
	}

	@Override
	public String toString()
	{
		return "Jar plugin:" + underlyingResource.toString();
	}

	@Override
	public String getRegistryName()
	{
		return underlyingResource.getProject().getName();
	}

	@Override
	protected InputStream getInputStream() throws CoreException
	{
		URI uri = ((IFile) underlyingResource).getLocationURI();
		try
		{
			return URIUtil.toJarURI(uri, JPFProject.MANIFEST_PATH).toURL().openStream();
		}
		catch( IOException e )
		{
			return null;
		}
	}

	@Override
	public List<IClasspathEntry> createClasspathEntries()
	{
		IPath srcJar = null;
		if( underlyingResource.getFileExtension().equals("jar") )
		{
			String name = underlyingResource.getName();
			IFile srcJarFile = underlyingResource.getProject().getFile(
				"lib-src/" + name.substring(0, name.length() - 4) + "-sources.jar");
			if( srcJarFile.exists() )
			{
				srcJar = srcJarFile.getFullPath();
			}
		}
		return Arrays.asList(JavaCore.newLibraryEntry(underlyingResource.getFullPath(), srcJar, null));
	}

	@Override
	public IJavaProject getJavaProject()
	{
		return null;
	}

	@Override
	public boolean isJarModel()
	{
		return true;
	}

	public RepoModel getRepoModel()
	{
		return repoModel;
	}
}
