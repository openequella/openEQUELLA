/*
 * Created on Oct 18, 2005
 */
package com.dytech.gui.file;

import java.io.File;

import com.dytech.common.io.FileExtensionFilter;

/**
 * Adapts java.io.FileFilter or javax.swing.filechooser.FileFilter to implement
 * both interfaces.
 * 
 * @author Nicholas Read
 */
public class FileFilterAdapter extends javax.swing.filechooser.FileFilter
{
	private java.io.FileFilter filter;
	private String description;

	public FileFilterAdapter(java.io.FileFilter filter, String description)
	{
		this.filter = filter;
		this.description = description;
	}

	@Override
	public boolean accept(File f)
	{
		return filter.accept(f);
	}

	@Override
	public String getDescription()
	{
		return description;
	}

	public static FileFilterAdapter IMAGES()
	{
		return new FileFilterAdapter(FileExtensionFilter.IMAGES(), "Image Files");
	}

	public static FileFilterAdapter HTML()
	{
		return new FileFilterAdapter(FileExtensionFilter.HTML(), "HTML Files");
	}

	public static FileFilterAdapter ZIP()
	{
		return new FileFilterAdapter(FileExtensionFilter.ZIP(), "ZIP Archives");
	}

	public static FileFilterAdapter XML()
	{
		return new FileFilterAdapter(FileExtensionFilter.XML(), "XML Files");
	}

	public static FileFilterAdapter XSLT()
	{
		return new FileFilterAdapter(FileExtensionFilter.XSLT(), "XSL Transformations");
	}
}
