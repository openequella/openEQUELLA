package com.tle.common.payment.storefront.entity;

import java.util.ArrayList;
import java.util.List;

import com.tle.common.property.ConfigurationProperties;
import com.tle.common.property.annotation.Property;
import com.tle.common.property.annotation.PropertyDataList;

public class ApprovalsPaymentsSettings implements ConfigurationProperties
{
	private static final long serialVersionUID = 1;

	@Property(key = "payment.storefront.approvalsenabled")
	private boolean enabled;

	@PropertyDataList(key = "payment.storefront.approvals", type = ApprovalsPayments.class)
	private final List<ApprovalsPayments> approvals = new ArrayList<ApprovalsPayments>();

	@PropertyDataList(key = "payment.storefront.payments", type = ApprovalsPayments.class)
	private final List<ApprovalsPayments> payments = new ArrayList<ApprovalsPayments>();

	public ApprovalsPaymentsSettings()
	{
		super();
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	public List<ApprovalsPayments> getApprovals()
	{
		return approvals;
	}

	public List<ApprovalsPayments> getPayments()
	{
		return payments;
	}

	public static class ApprovalsPayments implements ConfigurationProperties
	{
		private static final long serialVersionUID = 1;

		@Property(key = "expression.from")
		private String expressionFrom;
		@Property(key = "expression.to")
		private String expressionTo;

		/**
		 * Reflection only.
		 */
		public ApprovalsPayments()
		{
			//
		}

		@Override
		public int hashCode()
		{
			return expressionFrom.hashCode() + expressionTo.hashCode();
		}

		@Override
		public boolean equals(Object obj)
		{
			if( obj instanceof ApprovalsPayments )
			{
				final ApprovalsPayments other = (ApprovalsPayments) obj;
				if( expressionFrom.equals(other.expressionFrom) && expressionTo.equals(other.expressionTo) )
				{
					return true;
				}
			}
			return false;
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
	}
}
