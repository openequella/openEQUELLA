package com.tle.core.usermanagement.standard.service;

import java.util.Date;

import com.tle.beans.item.Item;

/**
 * @author Nicholas Read
 */
public interface SharePassService
{
	boolean isEnabled();

	String add(Item item, String email, Date expiry);

	void removeExpiredPasses();

	String activatePasses(String token);
}
