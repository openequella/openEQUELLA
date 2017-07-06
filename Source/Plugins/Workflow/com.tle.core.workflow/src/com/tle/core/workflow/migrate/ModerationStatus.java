package com.tle.core.workflow.migrate;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.hibernate.annotations.AccessType;

import com.tle.core.workflow.migrate.CreateWorkflowTables.FakeItem;
import com.tle.core.workflow.migrate.beans.WorkflowNodeStatus;

@Entity(name = "ModerationStatus")
@AccessType("field")
public class ModerationStatus
{
	@Id
	long id;
	@OneToOne(mappedBy = "moderation")
	FakeItem item;
	Boolean needsReset;
	Boolean deletedModerating;
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	Set<WorkflowNodeStatus> statuses;
}