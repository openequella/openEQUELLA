package com.tle.ant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Text;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import com.google.common.io.Closeables;

/**
 * http://java.sun.com/j2se/1.4.2/docs/guide/jws/downloadservletguide.html
 */
public class JnlpVersionGenerator extends Task
{
	private File jnlp;
	private File base;
	private File dest;
	private SAXBuilder builder = Helper.createSAXBuilder();

	public JnlpVersionGenerator()
	{
		super();
	}

	@Override
	public void execute() throws BuildException
	{
		try
		{
			if( jnlp == null )
			{
				versionFile();
			}
			else
			{
				jnlp();
			}
		}
		catch( Exception e )
		{
			throw new BuildException(e.getMessage(), e);
		}
	}

	/*
	 * <jnlp-versions> <resource> <pattern> <name>application.jar</name>
	 * <version-id>1.1</version-id> </pattern> <file>application.jar</file>
	 * </resource> </jnlp-versions>
	 */
	private void versionFile() throws Exception
	{
		Document doc = new Document();
		Element root = new Element("jnlp-versions"); //$NON-NLS-1$
		doc.setRootElement(root);
		for( File file : base.listFiles() )
		{
			if( file.getName().endsWith(".jar") ) //$NON-NLS-1$
			{
				Element resource = new Element("resource"); //$NON-NLS-1$
				Element pattern = new Element("pattern"); //$NON-NLS-1$

				Element name = new Element("name"); //$NON-NLS-1$
				name.setContent(new Text(file.getName()));

				Element version = new Element("version-id"); //$NON-NLS-1$
				version.setContent(new Text(getMd5Digest(file)));

				pattern.addContent(name);
				pattern.addContent(version);

				Element fileNode = new Element("file"); //$NON-NLS-1$
				fileNode.setContent(new Text(file.getName()));

				resource.addContent(pattern);
				resource.addContent(fileNode);

				root.addContent(resource);
			}
		}

		OutputStream out = new FileOutputStream(new File(base, "version.xml")); //$NON-NLS-1$
		try
		{
			new XMLOutputter(Format.getPrettyFormat()).output(doc, out);
		}
		finally
		{
			Closeables.closeQuietly(out);
		}
	}

	private void jnlp() throws Exception
	{
		Document doc = builder.build(jnlp);

		@SuppressWarnings("unchecked")
		List<Element> list = doc.getRootElement().getChild("resources").getChildren(); //$NON-NLS-1$
		for( Element e : list )
		{
			if( e.getName().equals("jar") ) //$NON-NLS-1$
			{
				File f = new File(base, e.getAttributeValue("href")); //$NON-NLS-1$
				if( f.exists() )
				{
					e.setAttribute("version", getMd5Digest(f)); //$NON-NLS-1$
				}
			}
		}

		OutputStream out = new FileOutputStream(dest);
		try
		{
			new XMLOutputter(Format.getPrettyFormat()).output(doc, out);
		}
		finally
		{
			Closeables.closeQuietly(out);
		}
	}

	private String getMd5Digest(File f) throws IOException
	{
		InputStream in = new FileInputStream(f);
		try
		{
			return DigestUtils.md5Hex(in);
		}
		finally
		{
			Closeables.closeQuietly(in);
		}
	}

	public void setDest(File dest)
	{
		this.dest = dest;
	}

	public void setBase(File base)
	{
		this.base = base;
	}

	public void setJnlp(File jnlp)
	{
		this.jnlp = jnlp;
	}
}
