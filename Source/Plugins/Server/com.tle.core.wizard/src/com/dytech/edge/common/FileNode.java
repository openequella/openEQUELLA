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

package com.dytech.edge.common;

import java.util.List;

/**
 * A class for representing a file in a hierarchy.
 */
public class FileNode
{
	private String name;
	private String fullpath;
	private List<FileNode> files;
	private boolean actualfile;
	private int indent;
	private int length;
	private FileNode original;
	private boolean folder;

	public FileNode(String name, int length, List<FileNode> files, boolean folder)
	{
		this.name = name;
		this.length = length;
		this.files = files;
		this.folder = folder;
		actualfile = true;
	}

	public FileNode(FileNode orig)
	{
		actualfile = false;
		original = orig;
		name = orig.getName();
		length = orig.getLength();
	}

	/**
	 * @return Returns the indent.
	 */
	public int getIndent()
	{
		return indent;
	}

	/**
	 * @param indent The indent to set.
	 */
	public void setIndent(int indent)
	{
		this.indent = indent;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @return Returns the original.
	 */
	public FileNode getOriginal()
	{
		return original;
	}

	/**
	 * @return Returns the actualfile.
	 */
	public boolean isActualfile()
	{
		return actualfile;
	}

	/**
	 * @return Returns the files.
	 */
	public List<FileNode> getFiles()
	{
		return files;
	}

	/**
	 * @param files The files to set.
	 */
	public void setFiles(List<FileNode> files)
	{
		this.files = files;
	}

	/**
	 * @return Returns the fullpath.
	 */
	public String getFullpath()
	{
		return fullpath;
	}

	/**
	 * @param fullpath The fullpath to set.
	 */
	public void setFullpath(String fullpath)
	{
		this.fullpath = fullpath;
	}

	/**
	 * @return Returns the length.
	 */
	public int getLength()
	{
		return length;
	}

	public boolean isFolder()
	{
		return folder;
	}
}
