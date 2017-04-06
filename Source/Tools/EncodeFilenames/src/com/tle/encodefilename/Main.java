package com.tle.encodefilename;

import java.io.File;

@SuppressWarnings("nls")
public class Main
{
	private boolean dryRun;
	private File root;

	public Main(File root, boolean dryRun)
	{
		this.root = root;
		this.dryRun = dryRun;
	}

	public static void main(String[] args)
	{
		boolean run = false;
		boolean help = false;
		String dir = null;
		for( String arg : args )
		{
			if( arg.startsWith("-") )
			{
				if( arg.equals("-r") )
				{
					run = true;
				}
				else if( arg.equals("-h") )
				{
					help = true;
				}
			}
			else
			{
				dir = arg;
			}
		}
		if( help || dir == null )
		{
			printHelp();
		}
		File root = new File(dir);
		if( !root.exists() )
		{
			System.err.println("Dir '" + dir + "' does not exist!");
			System.exit(1);
		}
		new Main(root, !run).execute();
	}

	private void execute()
	{
		recurse(root);
	}

	private void recurse(File dir)
	{
		File[] fileList = dir.listFiles();
		if( fileList != null )
		{
			for( File file : fileList )
			{
				if( file.isDirectory() )
				{
					recurse(file);
				}
				else
				{
					String srcName = file.getName();
					String destName = fileencode(srcName);
					if( !destName.equals(srcName) )
					{
						System.out.println(file.getAbsolutePath() + " to " + destName);
						if( !dryRun )
						{
							file.renameTo(new File(dir, destName));
						}
					}
				}
			}
		}

	}

	public static String fileencode(String szStr)
	{
		StringBuilder szOut = new StringBuilder();
		for( int i = 0; i < szStr.length(); i++ )
		{
			char ch = szStr.charAt(i);
			switch( ch )
			{
				case ':':
				case '*':
				case '?':
				case '"':
				case '<':
				case '>':
				case '|':
				case '%':
					szOut.append('%');
					int intval = ch;
					szOut.append(String.format("%02x", intval)); //$NON-NLS-1$
					break;
				default:
					szOut.append(ch);
			}
		}
		return szOut.toString();
	}

	private static void printHelp()
	{
		System.err.println("Usage: [options] directory");
		System.err.println("Options:");
		System.err.println("-r   Actually do the renaming (not a dry run)");
		System.exit(1);
	}
}
