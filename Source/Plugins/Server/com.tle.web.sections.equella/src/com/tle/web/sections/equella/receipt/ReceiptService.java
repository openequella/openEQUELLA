package com.tle.web.sections.equella.receipt;

import com.tle.web.sections.render.Label;

public interface ReceiptService
{
	/**
	 * Set a receipt for the current user.
	 */
	void setReceipt(Label receipt);

	/**
	 * Retrieves and forgets the receipt for the current user.
	 * 
	 * @return <code>null</code> if no receipt has been set.
	 */
	Label getReceipt();
}
