package com.tle.beans;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;

import com.thoughtworks.xstream.annotations.XStreamOmitField;
import com.tle.beans.item.Item;
import com.tle.common.workflow.node.WorkflowItem;

@Entity
@AccessType("field")
public class TaskHistory
{
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Index(name = "th_item")
	@ManyToOne(fetch = FetchType.LAZY)
	@XStreamOmitField
	private Item item;

	@Index(name = "th_task")
	@ManyToOne(fetch = FetchType.LAZY)
	private WorkflowItem task;

	@Index(name = "th_entry")
	private Date entryDate;

	@Index(name = "th_exit")
	private Date exitDate;

	public TaskHistory()
	{

	}

	public TaskHistory(Item item, WorkflowItem task, Date entry, Date exit)
	{
		this.item = item;
		this.task = task;
		this.entryDate = entry;
		this.exitDate = exit;
	}

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public WorkflowItem getTask()
	{
		return task;
	}

	public void setTask(WorkflowItem task)
	{
		this.task = task;
	}

	public Date getEntryDate()
	{
		return entryDate;
	}

	public void setEntryDate(Date entryDate)
	{
		this.entryDate = entryDate;
	}

	public Date getExitDate()
	{
		return exitDate;
	}

	public void setExitDate(Date exitDate)
	{
		this.exitDate = exitDate;
	}

	public Item getItem()
	{
		return item;
	}

	public void setItem(Item item)
	{
		this.item = item;
	}
}
