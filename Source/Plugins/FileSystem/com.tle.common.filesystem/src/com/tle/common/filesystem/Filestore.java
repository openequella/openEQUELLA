package com.tle.common.filesystem;

import java.nio.file.Path;

import com.tle.annotation.NonNullByDefault;

/**
 * @author Aaron
 *
 */
@NonNullByDefault
public class Filestore
{
	private final String id;
	private final String name;
	private final Path root;

	public Filestore(String id, String name, Path root)
	{
		this.id = id;
		this.name = name;
		this.root = root;
	}

	public String getId()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}

	public Path getRoot()
	{
		return root;
	}

	@SuppressWarnings("nls")
	@Override
	public String toString()
	{
		return "(" + id + ") " + name + ": " + root;
	}
}
