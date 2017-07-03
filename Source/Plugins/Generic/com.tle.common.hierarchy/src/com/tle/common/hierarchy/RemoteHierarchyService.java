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

package com.tle.common.hierarchy;

import java.io.Serializable;
import java.util.List;

import com.tle.beans.hierarchy.HierarchyPack;
import com.tle.beans.hierarchy.HierarchyTreeNode;
import com.tle.common.beans.exception.ValidationError;

/**
 * @author Nicholas Read
 */
public interface RemoteHierarchyService
{
	/**
	 * @param parentTopicID use less than or equal to zero for root tree nodes.
	 */
	List<HierarchyTreeNode> listTreeNodes(long parentTopicID);

	HierarchyPack getHierarchyPack(long topicID);

	long add(HierarchyTreeNode parent, String name, boolean inheritConstraints);

	long addToRoot(String name, boolean inheritConstraints);

	void edit(HierarchyPack pack);

	void delete(HierarchyTreeNode node);

	void move(long childID, long newParentID, int offset);

	/**
	 * @return a task UUID
	 */
	String exportTopic(HierarchyTreeNode node, boolean useSecurity);

	ExportStatus getExportStatus(String taskUuid);

	/**
	 * @return a task UUID
	 */
	String importTopic(String xml, HierarchyTreeNode topicInto, boolean newids, boolean useSecurity);

	/**
	 * @return a task UUID
	 */
	String importRootTopic(String xml, boolean newids, boolean useSecurity);

	ImportStatus getImportStatus(String taskUuid);

	static class AbstractStatus implements Serializable
	{
		private final int done;
		private final int total;

		protected AbstractStatus(int done, int total)
		{
			this.done = done;
			this.total = total;
		}

		public int getDone()
		{
			return done;
		}

		public int getTotal()
		{
			return total;
		}
	}

	final static class ExportStatus extends AbstractStatus
	{
		private String url;

		public static ExportStatus progress(int done, int total)
		{
			return new ExportStatus(done, total, null);
		}

		public static ExportStatus finished(String url)
		{
			return new ExportStatus(1, 1, url);
		}

		private ExportStatus(int done, int total, String url)
		{
			super(done, total);
			this.url = url;
		}

		public String getDownloadUrl()
		{
			return url;
		}
	}

	final static class ImportStatus extends AbstractStatus
	{
		private final ValidationError error;

		public static ImportStatus progress(int done, int total)
		{
			return new ImportStatus(done, total, null);
		}

		public static ImportStatus finished()
		{
			return new ImportStatus(1, 1, null);
		}

		public static ImportStatus error(ValidationError error)
		{
			return new ImportStatus(-1, -1, error);
		}

		private ImportStatus(int done, int total, ValidationError error)
		{
			super(done, total);
			this.error = error;
		}

		public boolean isFinished()
		{
			return getDone() >= getTotal();
		}

		public boolean isError()
		{
			return error != null;
		}

		public ValidationError getError()
		{
			return error;
		}
	}
}
