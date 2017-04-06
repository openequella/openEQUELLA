package com.tle.web.payment.storefront.section.workflow;

import java.io.Serializable;
import java.util.Map;

import com.google.common.collect.Maps;

public class SelectedApproval implements Serializable
{
	private int approvalIndex;
	private String expressionFrom;
	private String expressionTo;
	private boolean approval;
	private final Map<String, Object> validationErrors = Maps.newHashMap();

	public SelectedApproval()
	{
		super();
	}

	public SelectedApproval(int approvalIndex, String expressionFrom, String expressionTo, boolean approval)
	{
		this.approvalIndex = approvalIndex;
		this.expressionFrom = expressionFrom;
		this.expressionTo = expressionTo;
		this.approval = approval;
	}

	public int getApprovalIndex()
	{
		return approvalIndex;
	}

	public void setApprovalIndex(int approvalIndex)
	{
		this.approvalIndex = approvalIndex;
	}

	public String getExpressionFrom()
	{
		return expressionFrom;
	}

	public void setExpressionFrom(String expressionFrom)
	{
		this.expressionFrom = expressionFrom;
	}

	public String getExpressionTo()
	{
		return expressionTo;
	}

	public void setExpressionTo(String expressionTo)
	{
		this.expressionTo = expressionTo;
	}

	public boolean isApproval()
	{
		return approval;
	}

	public void setApproval(boolean approval)
	{
		this.approval = approval;
	}

	public Map<String, Object> getValidationErrors()
	{
		return validationErrors;
	}
}
