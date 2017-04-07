package com.dytech.common.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

import junit.framework.TestCase;

public class FileUtilsTest extends TestCase
{
	private final File tempFolder = new File(new File(System.getProperty("java.io.tmpdir")), "FileUtilsTest");
	private File srcFolder;

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();

		srcFolder = new File(tempFolder, "source");
		if( !srcFolder.mkdirs() )
		{
			// Try creating a new file to see if it's lacking inodes or
			// something
			File f = new File(tempFolder, "test.txt");
			f.delete();
			f.createNewFile();

			throw new IOException("Couldn't create directory for " + srcFolder + " for unknown reasons :(");
		}
	}

	@Override
	protected void tearDown() throws Exception
	{
		super.tearDown();
		FileUtils.delete(tempFolder.toPath());
	}

	public void testDeleteFile() throws Exception
	{
		File destFile = new File(tempFolder, "deleteFileTest.txt");
		Files.write(destFile.toPath(), "abc".getBytes(), StandardOpenOption.CREATE_NEW);

		if( !Files.exists(destFile.toPath(), LinkOption.NOFOLLOW_LINKS) )
		{
			throw new AssertionError("File not created");
		}

		FileUtils.delete(destFile.toPath());

		if( Files.exists(destFile.toPath(), LinkOption.NOFOLLOW_LINKS) )
		{
			throw new AssertionError("File not deleted");
		}
	}

	public void testDeleteFolder() throws Exception
	{
		File destFolder = new File(tempFolder, "deleteTest");
		FileUtils.delete(destFolder.toPath());
		destFolder.mkdirs();
		buildFolders(destFolder);

		if( !Files.exists(destFolder.toPath(), LinkOption.NOFOLLOW_LINKS) )
		{
			throw new AssertionError("Folder not created");
		}

		FileUtils.delete(destFolder.toPath());

		if( Files.exists(destFolder.toPath(), LinkOption.NOFOLLOW_LINKS) )
		{
			throw new AssertionError("Folder not deleted");
		}
	}

	public void testFileSize() throws Exception
	{
		File destFolder = new File(tempFolder, "fileSizeTest");
		FileUtils.delete(destFolder);
		destFolder.mkdirs();

		Path target = new File(destFolder, "getsize.txt").toPath();
		Files.write(target, "0123456789".getBytes("UTF-8"), StandardOpenOption.CREATE_NEW);

		final long size = FileUtils.fileSize(target);
		if( size != 10 )
		{
			throw new AssertionError("File size expected to be 10, but was " + size);
		}

		byte[] bytes = new byte[1000];
		Path target2 = new File(destFolder, "getsize2.txt").toPath();
		Files.write(target2, bytes, StandardOpenOption.CREATE_NEW);

		final long size2 = FileUtils.fileSize(target2);
		if( size2 != 1000 )
		{
			throw new AssertionError("File size expected to be 1000, but was " + size2);
		}
	}

	public void testCount() throws Exception
	{
		File destFolder = new File(tempFolder, "countTest");
		FileUtils.delete(destFolder);
		destFolder.mkdirs();
		buildFolders(destFolder);

		long startNew = System.nanoTime();
		long newCount = FileUtils.countFiles(destFolder.toPath());
		long endNew = System.nanoTime();

		long startOld = System.nanoTime();
		long oldCount = FileUtils.oldCountFiles(destFolder);
		long endOld = System.nanoTime();

		long durNew = endNew - startNew;
		long durOld = endOld - startOld;

		System.out.println("New count: " + newCount);
		System.out.println("Old count: " + oldCount);

		System.out.println("Old method: " + durOld);
		System.out.println("New method: " + durNew);
		System.out.println("Old faster? " + (durNew > durOld));

		destFolder = new File(tempFolder, "countTestBigger");
		FileUtils.delete(destFolder);
		destFolder.mkdirs();
		buildFolders(destFolder);
		for( int i = 0; i < 100; i++ )
		{
			File subFolder = new File(destFolder, "sub" + i);
			subFolder.mkdirs();

			for( int j = 0; j < 20; j++ )
			{
				File subFile = new File(subFolder, "subfile" + j + ".txt");
				Files.write(subFile.toPath(), "abc".getBytes(), StandardOpenOption.CREATE_NEW);
			}
		}

		startNew = System.nanoTime();
		newCount = FileUtils.countFiles(destFolder.toPath());
		endNew = System.nanoTime();

		startOld = System.nanoTime();
		oldCount = FileUtils.oldCountFiles(destFolder);
		endOld = System.nanoTime();

		durNew = endNew - startNew;
		durOld = endOld - startOld;

		System.out.println("New count: " + newCount);
		System.out.println("Old count: " + oldCount);

		System.out.println("Old method: " + durOld);
		System.out.println("New method: " + durNew);
		System.out.println("Old faster? " + (durNew > durOld));
	}

	/**
	 * See buildFolders for structure
	 */
	public void testGrep() throws Exception
	{
		File destFolder = new File(tempFolder, "grepTest");
		FileUtils.delete(destFolder);
		destFolder.mkdirs();
		buildFolders(destFolder);

		doGrep(destFolder, "**/middle*", false, 4);
		doGrep(destFolder, "**/middle*", true, 3);
		doGrep(destFolder, "**/*", true, 11);
		doGrep(destFolder, "**/*", false, 13);
		doGrep(destFolder, "*", false, 4);
		doGrep(destFolder, "*", true, 3);
		doGrep(destFolder, "*/*", false, 6);
		doGrep(destFolder, "**/muddle*", true, 1);
		doGrep(destFolder, "**/bottom*", true, 2);
		doGrep(destFolder, "**/top*", true, 2);
		doGrep(destFolder, "all*", true, 1);
		doGrep(destFolder, "**/all*", true, 3);
		// the old Grep return 1 for "all*", and return 3 for "**/all*"
		// doGrep(destFolder, "all*", true, 3);
		doGrep(destFolder, "**/*folder", false, 2);

		FileUtils.delete(destFolder);
		destFolder = null;
	}

	//@formatter:off
	/*
	 * [targetFolder]
	 *   |- top1.txt
	 *   |- top2.txt
	 *   |- all1.txt
	 *   |- middlefolder
	 *        |- middle1.txt
	 *        |- middle2.txt
	 *        |- middle3.txt
	 *        |- muddle4.txt
	 *        |- all2.txt
	 *        |- bottomfolder
	 *              |- bottom1.txt
	 *              |- bottom2.txt
	 *              |- all3.txt
	 */
	//@formatter:on
	private void buildFolders(File targetFolder) throws Exception
	{
		Path top1 = new File(targetFolder, "top1.txt").toPath();
		Files.write(top1, "abc".getBytes(), StandardOpenOption.CREATE_NEW);
		Files.copy(top1, new File(targetFolder, "top2.txt").toPath());
		Files.copy(top1, new File(targetFolder, "all1.txt").toPath());

		Path middleFolder = new File(targetFolder, "middlefolder").toPath();
		Files.createDirectory(middleFolder);

		Path middle1 = new File(middleFolder.toFile(), "middle1.txt").toPath();
		Files.write(middle1, "def".getBytes(), StandardOpenOption.CREATE_NEW);
		Files.copy(middle1, new File(middleFolder.toFile(), "middle2.txt").toPath());
		Files.copy(middle1, new File(middleFolder.toFile(), "middle3.txt").toPath());
		Files.copy(middle1, new File(middleFolder.toFile(), "muddle4.txt").toPath()); // deliberate
																						// typo!
		Files.copy(middle1, new File(middleFolder.toFile(), "all2.txt").toPath());

		Path bottomFolder = new File(middleFolder.toFile(), "bottomfolder").toPath();
		Files.createDirectory(bottomFolder);

		Path bottom1 = new File(bottomFolder.toFile(), "bottom1.txt").toPath();
		Files.write(bottom1, "ghi".getBytes());
		Files.copy(bottom1, new File(bottomFolder.toFile(), "bottom2.txt").toPath());
		Files.copy(bottom1, new File(bottomFolder.toFile(), "all3.txt").toPath());
	}

	private void doGrep(File folder, String pattern, boolean filesOnly, int expectedCount) throws Exception
	{
		List<String> newGrep = FileUtils.grep(folder.toPath(), pattern, filesOnly);

		if( newGrep.size() != expectedCount )
		{
			throw new AssertionError("Expected " + expectedCount + " for " + pattern + ", found " + newGrep.size());
		}
	}
}
