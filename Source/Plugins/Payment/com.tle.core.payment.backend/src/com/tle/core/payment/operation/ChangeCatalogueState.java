package com.tle.core.payment.operation;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

/**
 * @author Aaron
 */
public class ChangeCatalogueState implements Serializable
{
	private final List<ChangeCatalogueAssignment> assignments;

	public ChangeCatalogueState(ChangeCatalogueAssignment assignment)
	{
		this.assignments = Collections.singletonList(assignment);
	}

	public ChangeCatalogueState(List<ChangeCatalogueAssignment> assignments)
	{
		this.assignments = Lists.newArrayList(assignments);
	}

	public List<ChangeCatalogueAssignment> getAssignments()
	{
		return assignments;
	}

	public static class ChangeCatalogueAssignment implements Serializable
	{
		private final boolean add;
		private final Long catalogueId;
		private final boolean blacklist;

		public ChangeCatalogueAssignment(boolean add, Long catalogueId, boolean blacklist)
		{
			this.add = add;
			this.catalogueId = catalogueId;
			this.blacklist = blacklist;
		}

		public boolean isAdd()
		{
			return add;
		}

		public Long getCatalogueId()
		{
			return catalogueId;
		}

		public boolean isBlacklist()
		{
			return blacklist;
		}
	}
}
