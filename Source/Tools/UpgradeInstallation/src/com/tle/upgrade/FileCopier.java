package com.tle.upgrade;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * @author Aaron
 */
public class FileCopier
{
	private final File src;
	private final File dest;
	private final boolean blowUp;

	/**
	 * @param src
	 * @param dest
	 * @param blowUp Blow up if src does not exist
	 */
	public FileCopier(File src, File dest, boolean blowUp)
	{
		this.src = src;
		this.dest = dest;
		this.blowUp = blowUp;
	}

	public void rename() throws IOException
	{
		if( !src.exists() && !blowUp )
		{
			return;
		}
		Files.move(src.toPath(), dest.toPath());
	}

	public void copy() throws IOException
	{
		if( !src.exists() && !blowUp )
		{
			return;
		}

		dest.getParentFile().mkdirs();
		final Path srcPath = src.toPath();
		final Path destPath = dest.toPath();
		if( Files.isDirectory(srcPath) )
		{
			Files.walkFileTree(srcPath, new CopyDirVisitor(srcPath, destPath, StandardCopyOption.REPLACE_EXISTING));
		}
		else
		{
			Files.copy(srcPath, destPath, StandardCopyOption.REPLACE_EXISTING);
		}
	}

	public static class CopyDirVisitor extends SimpleFileVisitor<Path>
	{
		private final Path fromPath;
		private final Path toPath;
		private final CopyOption copyOption;

		public CopyDirVisitor(Path fromPath, Path toPath, CopyOption copyOption)
		{
			this.fromPath = fromPath;
			this.toPath = toPath;
			this.copyOption = copyOption;
		}

		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException
		{
			Path targetPath = toPath.resolve(fromPath.relativize(dir));
			if( !Files.exists(targetPath) )
			{
				Files.createDirectory(targetPath);
			}
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
		{
			Files.copy(file, toPath.resolve(fromPath.relativize(file)), copyOption);
			return FileVisitResult.CONTINUE;
		}
	}
}
