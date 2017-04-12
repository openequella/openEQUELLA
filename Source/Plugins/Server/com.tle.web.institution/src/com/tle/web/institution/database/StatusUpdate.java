package com.tle.web.institution.database;

import java.util.List;
import java.util.Map;

import com.tle.web.sections.ajax.AbstractDOMResult;
import com.tle.web.sections.ajax.FullAjaxCaptureResult;

public class StatusUpdate extends AbstractDOMResult
{
	private Map<String, FullAjaxCaptureResult> updates;
	private List<FullAjaxCaptureResult> rows;
	private boolean finished;

	public StatusUpdate(AbstractDOMResult result)
	{
		super(result);
	}

	public List<FullAjaxCaptureResult> getRows()
	{
		return rows;
	}

	public void setRows(List<FullAjaxCaptureResult> rows)
	{
		this.rows = rows;
	}

	public boolean isFinished()
	{
		return finished;
	}

	public void setFinished(boolean finished)
	{
		this.finished = finished;
	}

	public Map<String, FullAjaxCaptureResult> getUpdates()
	{
		return updates;
	}

	public void setUpdates(Map<String, FullAjaxCaptureResult> updates)
	{
		this.updates = updates;
	}
}