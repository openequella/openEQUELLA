package com.tle.ant;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.google.common.collect.Sets;

/**
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
public class LanguagePackDiff extends Task
{
	private String previous;
	private String current;
	private String diff;

	@Override
	public void execute() throws BuildException
	{
		File previousDir = new File(previous);
		File currentDir = new File(current);
		File diffDir = new File(diff);
		HashSet<String> propertyNames = Sets.newHashSet(previousDir.list(new PropertyFilter()));
		propertyNames.addAll(Sets.newHashSet(currentDir.list(new PropertyFilter())));

		for( String filename : propertyNames )
		{
			File previousFile = new File(previousDir, filename);
			File currentFile = new File(currentDir, filename);

			PropDiff propDiff = new PropDiff(previousFile.exists() ? loadByFile(previousFile) : new Properties(),
				currentFile.exists() ? loadByFile(currentFile) : new Properties());

			Properties intersect = propDiff.intersect(false);
			Properties onlyIn2 = propDiff.onlyIn(2);
			Properties onlyIn1 = propDiff.onlyIn(1);

			if( onlyIn2.size() > 0 )
			{
				saveByFilename(
					onlyIn2,
					diffDir.getAbsolutePath() + "/" + filename,
					"########################################\n# The following properties were added #\n#########################################");
			}
			if( intersect.size() > 0 )
			{
				saveByFilename(
					intersect,
					diffDir.getAbsolutePath() + "/" + filename,
					"########################################\n# The following properties were updated #\n#########################################");
			}

			if( onlyIn1.size() > 0 )
			{
				saveByFilename(
					onlyIn1,
					diffDir.getAbsolutePath() + "/" + filename,
					"########################################\n# The following properties were removed #\n#########################################");
			}
		}

	}

	/**
	 * Save a Properties object by filename, with given comment.
	 */

	private void saveByFilename(Properties props, String filename, String comment)
	{
		OutputStream bos;
		if( props != null )
		{
			try
			{

				bos = new BufferedOutputStream(new FileOutputStream(filename, true));
				props.store(bos, comment);
				bos.close();
			}
			catch( IOException e )
			{ // either not found or io error
				System.err.println("when saving to " + filename + " : " + e);
			}

		}
	}

	/**
	 * Load a saved Properties object by filename.
	 */
	private static Properties loadByFile(File file)
	{
		Properties props = new Properties();
		BufferedInputStream bis;
		try
		{
			bis = new BufferedInputStream(new FileInputStream(file));
			props.load(bis);
		}
		catch( IOException e )
		{ // either not found or io error
			System.err.println("when opening " + file.getName() + " : " + e);
			return null;
		}
		return props;
	}

	public String getPrevious()
	{
		return previous;
	}

	public void setPrevious(String previous)
	{
		this.previous = previous;
	}

	public String getCurrent()
	{
		return current;
	}

	public void setCurrent(String current)
	{
		this.current = current;
	}

	public String getDiff()
	{
		return diff;
	}

	public void setDiff(String diff)
	{
		this.diff = diff;
	}

	public class PropertyFilter implements FilenameFilter
	{
		@Override
		public boolean accept(File dir, String name)
		{
			return name.endsWith(".properties") && !name.equals("locale.properties");
		}
	}

	public class PropDiff
	{
		//
		// Instance variables
		//
		private Properties p1;
		private Properties p2;

		//
		// Constructor
		//

		/**
		 * Create a PropDiff that will operate on the Properties objects p1 and
		 * p2. Entries in p2 take precedence over p1 (that is, p1 is the default
		 * of p2). The filenames are used when generating comments and as a
		 * prefix when creating output files and are neither modified nor read
		 * here.
		 * 
		 * @param p1 a default or lower precedence Properties object.
		 * @param p2 a higher precedence Properties object than p1.
		 * @param toStdout if true, send results to System.out instead of
		 *            creating property files as output.
		 */
		public PropDiff(Properties p1, Properties p2)
		{
			this.p1 = p1;
			this.p2 = p2;
		}

		/**
		 * Return properties that have the same name and equal or different
		 * values. The returned properties that are common (same name) with or
		 * without the same values, depending on intersectValues. (p1 is default
		 * for p2).
		 * 
		 * @param intersectValues if true, then return props that also have the
		 *            same value. Otherwise, return only properties that have
		 *            different values.
		 * @return properties in p1 and p2 that have the same name with the
		 *         added condition intersectValues.
		 */
		public Properties intersect(boolean intersectValues)
		{
			Properties props = new Properties();
			// scan p1
			for( Enumeration e = p1.propertyNames(); e.hasMoreElements(); )
			{
				String name = (String) e.nextElement();
				String value1 = p1.getProperty(name);
				String value2 = p2.getProperty(name);
				if( value2 != null )
				{ // name is in both
					if( intersectValues )
					{
						if( value1.equals(value2) )
						{ // same value
							props.put(name, value1);
						}
					}
					else
					{
						if( !value1.equals(value2) )
						{ // different value
							props.put(name, value2);
						}
					}
				}
			}
			return props;
		}

		/**
		 * Return properties that are common (same name). The returned
		 * properties that are common to p1 and p2, where p1 is default for p2
		 * (said another way, p2 overrides p1).
		 * 
		 * @return properties that are in both p1 and p2 where p1 is default for
		 *         p2.
		 */
		public Properties commonProps()
		{
			Properties props = new Properties();
			// scan p2
			for( Enumeration e = p2.propertyNames(); e.hasMoreElements(); )
			{
				String name = (String) e.nextElement();
				String value2 = p2.getProperty(name);
				String value1 = p1.getProperty(name);
				if( value1 != null )
				{ // in both
					props.put(name, value2); // value from p2 overrides p1
				}
			}
			return props;
		}

		/**
		 * Return union of properties.
		 * 
		 * @return union of properties, where p2 overrides p1
		 */
		public Properties union()
		{
			Properties props = new Properties();
			// scan p1
			for( Enumeration e = p1.propertyNames(); e.hasMoreElements(); )
			{
				String name = (String) e.nextElement();
				String value = p1.getProperty(name);
				props.put(name, value);
			}
			// scan p2
			for( Enumeration e = p2.propertyNames(); e.hasMoreElements(); )
			{
				String name = (String) e.nextElement();
				String value = p2.getProperty(name);
				props.put(name, value);
			}
			return props;
		}

		/**
		 * Return properties only in one of the Properties objects. The values
		 * of property items are not considered in this case.
		 * 
		 * @param which either 1 or 2.
		 * @return properties only in one of the Properties objects.
		 */
		public Properties onlyIn(int which)
		{
			Properties props = new Properties();
			Properties pp1 = null;
			Properties pp2 = null;
			if( which == 2 )
			{
				pp1 = p1;
				pp2 = p2;
			}
			else
			{
				pp1 = p2;
				pp2 = p1;
			}
			// scan pp2
			for( Enumeration e = pp2.propertyNames(); e.hasMoreElements(); )
			{
				String name = (String) e.nextElement();
				String value2 = pp2.getProperty(name);
				String value1 = pp1.getProperty(name);
				if( value1 == null )
				{ // only in pp2
					props.put(name, value2);
				}
			}
			return props;
		}

	}
}
