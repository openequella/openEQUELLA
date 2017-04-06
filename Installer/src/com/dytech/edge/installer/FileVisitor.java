package com.dytech.edge.installer;

import com.dytech.common.io.FileWrapper;

/**
 * @author Nicholas Read
 */
public interface FileVisitor
{
	public boolean visit(FileWrapper f);
}
