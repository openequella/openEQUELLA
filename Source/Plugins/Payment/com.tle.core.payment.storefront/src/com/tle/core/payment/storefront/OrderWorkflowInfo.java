package com.tle.core.payment.storefront;

/**
 * @author Aaron
 */
public class OrderWorkflowInfo
{
	private boolean reject;
	private boolean submitPay;
	private boolean submitApprove;
	private boolean cart;
	private boolean pay;
	private boolean redraft;

	public boolean isReject()
	{
		return reject;
	}

	public void setReject(boolean reject)
	{
		this.reject = reject;
	}

	public boolean isSubmitPay()
	{
		return submitPay;
	}

	public void setSubmitPay(boolean submitPay)
	{
		this.submitPay = submitPay;
	}

	public boolean isSubmitApprove()
	{
		return submitApprove;
	}

	public void setSubmitApprove(boolean submitApprove)
	{
		this.submitApprove = submitApprove;
	}

	public boolean isCart()
	{
		return cart;
	}

	public void setCart(boolean cart)
	{
		this.cart = cart;
	}

	public boolean isPay()
	{
		return pay;
	}

	public void setPay(boolean pay)
	{
		this.pay = pay;
	}

	public boolean isRedraft()
	{
		return redraft;
	}

	public void setRedraft(boolean redraft)
	{
		this.redraft = redraft;
	}
}
