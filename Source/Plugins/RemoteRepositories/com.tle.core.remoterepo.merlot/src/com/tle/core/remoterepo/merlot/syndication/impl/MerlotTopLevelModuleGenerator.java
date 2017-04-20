package com.tle.core.remoterepo.merlot.syndication.impl;

import java.util.Collections;
import java.util.Set;

import org.jdom2.Element;
import org.jdom2.Namespace;

import com.rometools.rome.feed.module.Module;
import com.rometools.rome.io.ModuleGenerator;
import com.tle.core.remoterepo.merlot.syndication.MerlotTopLevelModule;

/**
 * @author aholland
 */
public class MerlotTopLevelModuleGenerator implements ModuleGenerator
{
	private static final Set<Namespace> NAMESPACES = Collections.unmodifiableSet(Collections
		.singleton(MerlotTopLevelModule.NAMESPACE));

	@Override
	public void generate(Module module, Element element)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String getNamespaceUri()
	{
		return MerlotTopLevelModule.URI;
	}

	@Override
	public Set<Namespace> getNamespaces()
	{
		return NAMESPACES;
	}
}
