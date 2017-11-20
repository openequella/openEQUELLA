/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.xslt.service.impl;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;
import java.util.WeakHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.dytech.devlib.BadCharacterFilterReader;
import com.dytech.devlib.Md5;
import com.dytech.devlib.PropBagEx;
import com.google.common.io.Closeables;
import com.tle.common.Pair;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.common.institution.CurrentInstitution;
import com.tle.core.filesystem.InstitutionFile;
import com.tle.core.guice.Bind;
import com.tle.core.services.FileSystemService;
import com.tle.core.xslt.service.XsltService;

/**
 * @author Nicholas Read
 */
@Bind(XsltService.class)
@Singleton
public final class XsltServiceImpl implements XsltService
{
	private final Map<String, Pair<Long, Templates>> xsltCache;
	private final Map<String, Templates> stringXsltCache;

	private final TransformerFactory factory;

	@Inject
	private FileSystemService fileSystemService;

	public XsltServiceImpl()
	{
		xsltCache = new WeakHashMap<String, Pair<Long, Templates>>();
		stringXsltCache = new WeakHashMap<String, Templates>();
		factory = TransformerFactory.newInstance();
	}

	@Override
	public String transform(final FileHandle handle, final String xslt, final Reader input)
	{
		return transform(handle, xslt, input, null);
	}

	@Override
	public String transform(final FileHandle handle, final String xslt, final Reader input, final URIResolver resolver)
	{
		StreamSource source = null;
		try
		{
			source = getSource(input);
			return transformFromFileHandle(handle, xslt, source, resolver, false);
		}
		finally
		{
			closeSource(source);
		}
	}

	@Override
	public String transform(final FileHandle handle, final String xslt, final PropBagEx input,
		boolean omitXmlDeclaration)
	{
		return transform(handle, xslt, input, null, omitXmlDeclaration);
	}

	@Override
	public String transform(final FileHandle handle, final String xslt, final PropBagEx input,
		final URIResolver resolver, boolean omitXmlDeclaration)
	{
		StreamSource source = null;
		try
		{
			// PropBag is safe, it will never give us bad XML chars, hence there
			// is no need to wrap with BadCharacterFilterReader, it would just
			// be overhead
			source = new StreamSource(new StringReader(input.toString()));
			return transformFromFileHandle(handle, xslt, source, resolver, omitXmlDeclaration);
		}
		finally
		{
			closeSource(source);
		}
	}

	@Override
	public String onceOffTransform(final InputStream xslt, final InputStream input)
	{
		StreamSource xsltSource = null;
		StreamSource inputSource = null;
		Thread currentThread = Thread.currentThread();
		ClassLoader oldLoader = currentThread.getContextClassLoader();
		currentThread.setContextClassLoader(getClass().getClassLoader());
		try
		{
			xsltSource = getSource(xslt);
			final Transformer templates = factory.newTransformer(xsltSource);
			final StringWriter writer = new StringWriter();
			inputSource = getSource(input);
			templates.transform(inputSource, new StreamResult(writer));
			return writer.toString();
		}
		catch( final TransformerException ex )
		{
			throw new RuntimeException("Error compiling XSLT", ex);
		}
		finally
		{
			closeSource(xsltSource);
			closeSource(inputSource);
			currentThread.setContextClassLoader(oldLoader);
		}
	}

	/**
	 * Performs the XSLT transformation, possibly using a pre-compiled version.
	 * Note: Does not close the supplied source
	 */
	private String transformFromFileHandle(final FileHandle handle, final String xslt, final StreamSource source,
		final URIResolver resolver, boolean omitXmlDeclaration)
	{
		StreamSource xsltStream = null;
		Thread currentThread = Thread.currentThread();
		ClassLoader oldLoader = currentThread.getContextClassLoader();
		currentThread.setContextClassLoader(getClass().getClassLoader());
		try
		{
			Pair<Long, Templates> compiledXslt = null;
			Templates templates = null;
			final String key = getKey(handle, xslt);
			synchronized( xsltCache )
			{
				final long modified = fileSystemService.lastModified(handle, xslt);
				compiledXslt = xsltCache.get(key);
				if( compiledXslt != null )
				{
					if( compiledXslt.getFirst() != modified )
					{
						compiledXslt = null;
					}
					else
					{
						templates = compiledXslt.getSecond();
					}
				}

				if( compiledXslt == null )
				{
					xsltStream = getSource(fileSystemService.read(handle, xslt));
					templates = factory.newTemplates(xsltStream);
					xsltCache.put(key, new Pair<Long, Templates>(modified, templates));
				}
			}
			// Not sure it can be
			if( templates == null )
			{
				throw new Error("templates is null");
			}
			Transformer transformersMoreThanMeetsTheEye = templates.newTransformer();
			transformersMoreThanMeetsTheEye.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
				omitXmlDeclaration ? "yes" : "no");
			return doTransform(transformersMoreThanMeetsTheEye, source, resolver);
		}
		catch( final Exception ex )
		{
			throw new RuntimeException("Error compiling XSLT", ex);
		}
		finally
		{
			closeSource(xsltStream);
			currentThread.setContextClassLoader(oldLoader);
		}
	}

	@Override
	public String transformFromXsltString(String xslt, PropBagEx input)
	{
		StreamSource xsltStream = null;
		StreamSource inputSource = null;
		String cacheKey = new Md5(xslt).getStringDigest();
		Thread currentThread = Thread.currentThread();
		ClassLoader oldLoader = currentThread.getContextClassLoader();
		currentThread.setContextClassLoader(getClass().getClassLoader());

		try
		{
			Templates templates = null;
			Templates compiledXslt = null;

			synchronized( stringXsltCache )
			{
				compiledXslt = stringXsltCache.get(cacheKey);
				if( compiledXslt != null )
				{
					templates = compiledXslt;
				}

				if( compiledXslt == null )
				{
					xsltStream = getSource(new StringReader(xslt));
					templates = factory.newTemplates(xsltStream);
					stringXsltCache.put(cacheKey, templates);
				}
			}

			inputSource = getSource(new StringReader(input.toString()));
			// Not sure it can be
			if( templates == null )
			{
				throw new Error("templates is null");
			}
			return doTransform(templates.newTransformer(), inputSource, null);
		}
		catch( Exception ex )
		{
			throw new RuntimeException("Error compiling XSLT", ex);
		}
		finally
		{
			closeSource(inputSource);
			currentThread.setContextClassLoader(oldLoader);
		}
	}

	/**
	 * Performs the XSLT transformation.
	 */
	private String doTransform(final Transformer transformer, final Source input, final URIResolver resolver)
	{
		final StringWriter writer = new StringWriter();
		final StreamResult output = new StreamResult(writer);

		if( resolver != null )
		{
			transformer.setURIResolver(resolver);
		}

		try
		{
			transformer.transform(input, output);
		}
		catch( final TransformerException ex )
		{
			throw new RuntimeException("Error transforming XSLT", ex);
		}

		return writer.toString();
	}

	private String getKey(final FileHandle handle, final String xslt)
	{
		if( handle instanceof InstitutionFile )
		{
			final InstitutionFile institutionHandle = (InstitutionFile) handle;
			institutionHandle.setInstitution(CurrentInstitution.get());
		}
		return handle.getAbsolutePath() + ':' + xslt;
	}

	private StreamSource getSource(final Reader reader)
	{
		return new StreamSource(new BadCharacterFilterReader(reader));
	}

	private StreamSource getSource(final InputStream stream)
	{
		return getSource(new InputStreamReader(stream));
	}

	private void closeSource(final StreamSource source)
	{
		if( source != null )
		{
			try
			{
				// one of these should be non-null
				Closeables.close(source.getReader(), true);
				Closeables.close(source.getInputStream(), true);
			}
			catch( Throwable th )
			{
				// Ignore
			}
		}
	}

	@Override
	public void cacheXslt(String xslt)
	{
		final String cacheKey = new Md5(xslt).getStringDigest();
		synchronized( stringXsltCache )
		{
			if( !stringXsltCache.containsKey(cacheKey) )
			{
				try
				{
					stringXsltCache.put(cacheKey, factory.newTemplates(getSource(new StringReader(xslt))));
				}
				catch( final TransformerException ex )
				{
					throw new RuntimeException("Error compiling XSLT", ex);
				}
			}
		}
	}
}
