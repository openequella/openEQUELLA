package com.tle.web.payment.section.pay;

import com.tle.web.payment.section.pay.AbstractCheckoutSection.AbstractCheckoutModel;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;

public abstract class AbstractCheckoutSection<M extends AbstractCheckoutModel> extends AbstractPrototypeSection<M>
	implements
		HtmlRenderer
{
	public static class AbstractCheckoutModel
	{
		@Bookmarked(name = "gatewayUuid", supported = true, parameter = "gatewayUuid")
		protected String gatewayUuid;
		@Bookmarked(name = "checkoutUuid", supported = true, parameter = "checkoutUuid")
		protected String checkoutUuid;
		/**
		 * Store front URL
		 */
		@Bookmarked(name = "returnUrl", supported = true, parameter = "returnUrl")
		protected String returnUrl;
		@Bookmarked(name = "c")
		protected boolean confirm;

		public String getGatewayUuid()
		{
			return gatewayUuid;
		}

		public void setGatewayUuid(String gatewayUuid)
		{
			this.gatewayUuid = gatewayUuid;
		}

		public String getCheckoutUuid()
		{
			return checkoutUuid;
		}

		public void setCheckoutUuid(String checkoutUuid)
		{
			this.checkoutUuid = checkoutUuid;
		}

		public String getReturnUrl()
		{
			return returnUrl;
		}

		public void setReturnUrl(String returnUrl)
		{
			this.returnUrl = returnUrl;
		}

		public boolean isConfirm()
		{
			return confirm;
		}

		public void setConfirm(boolean confirm)
		{
			this.confirm = confirm;
		}
	}
}
