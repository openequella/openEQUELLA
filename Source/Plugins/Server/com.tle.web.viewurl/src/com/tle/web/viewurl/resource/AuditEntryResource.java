package com.tle.web.viewurl.resource;

import java.util.List;

import com.tle.web.viewurl.AttachmentDetail;
import com.tle.web.viewurl.ViewAuditEntry;
import com.tle.web.viewurl.ViewableResource;

public class AuditEntryResource extends AbstractWrappedResource
{

	private final ViewAuditEntry auditEntry;

	public AuditEntryResource(ViewableResource resource, ViewAuditEntry viewAuditEntry)
	{
		super(resource);
		this.auditEntry = viewAuditEntry;
	}

	@Override
	public ViewAuditEntry getViewAuditEntry()
	{
		return auditEntry;
	}

	@Override
	public List<AttachmentDetail> getCommonAttachmentDetails()
	{
		// TODO Auto-generated method stub
		return null;
	}
}
