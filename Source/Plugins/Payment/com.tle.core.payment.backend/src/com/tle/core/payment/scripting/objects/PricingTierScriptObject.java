package com.tle.core.payment.scripting.objects;

import java.util.List;

import com.tle.common.scripting.ScriptObject;
import com.tle.common.scripting.types.ItemScriptType;
import com.tle.core.payment.scripting.types.PurchaseTierScriptType;
import com.tle.core.payment.scripting.types.SubscriptionTierScriptType;

/**
 * Referenced by the 'tier' variable in script
 * 
 * @author larry
 */
public interface PricingTierScriptObject extends ScriptObject
{
	String DEFAULT_VARIABLE = "tier"; //$NON-NLS-1$

	/**
	 * List all available purchase tiers
	 * 
	 * @return A list of enabled PurchaseTierScriptType
	 */
	List<PurchaseTierScriptType> listAllPurchaseTiers();

	/**
	 * List all available subscription tiers
	 * 
	 * @return A list of enabled SubscriptionTierScriptType
	 */
	List<SubscriptionTierScriptType> listAllSubscriptionTiers();

	// Arrays work better in Javascript

	/**
	 * Same as listAllPurchaseTiers, but an array is returned
	 * 
	 * @return An array of PurchaseTierScriptType which can be assigned to this
	 *         item
	 */
	PurchaseTierScriptType[] getAllPurchaseTiers();

	/**
	 * Same as listAllSubscriptionTiers, but an array is returned
	 * 
	 * @return An array of SubscriptionTierScriptType which can be assigned to
	 *         this item
	 */
	SubscriptionTierScriptType[] getAllSubscriptionTiers();

	/**
	 * Get a purchase tier by UUID
	 * 
	 * @param uuid The uuid of a purchase tier
	 * @return The purchase tier with the supplied uuid, or null if no match
	 */
	PurchaseTierScriptType getPurchaseTier(String uuid);

	/**
	 * Get a subscription tier by UUID
	 * 
	 * @param uuid The uuid of a subscription tier
	 * @return The subscription tier with the supplied uuid, or null if no match
	 */
	SubscriptionTierScriptType getSubscriptionTier(String uuid);

	/**
	 * @return The assigned purchase tier of the current item, or null if none
	 *         assigned
	 */
	PurchaseTierScriptType getPurchaseTier();

	/**
	 * Sets the assigned purchase tier of the current item
	 * 
	 * @param tier The purchase tier to assign
	 */
	void setPurchaseTier(PurchaseTierScriptType tier);

	/**
	 * Sets the assigned purchase tier of the current item
	 * 
	 * @param purchaseTierUuid The UUID of the purchase tier to assign
	 */
	void setPurchaseTierByUuid(String purchaseTierUuid);

	/**
	 * @return The assigned subscription tier of the current item, or null if
	 *         none assigned
	 */
	SubscriptionTierScriptType getSubscriptionTier();

	/**
	 * Sets the assigned subscription tier of the current item
	 * 
	 * @param tier The subscription tier to assign
	 */
	void setSubscriptionTier(SubscriptionTierScriptType tier);

	/**
	 * Sets the assigned subscription tier of the current item
	 * 
	 * @param subscriptionTierUuid The UUID of the subscription tier to assign
	 */
	void setSubscriptionTierByUuid(String subscriptionTierUuid);

	/**
	 * @return true if the current item is allowed to be obtained for free
	 */
	boolean isFreeAllowed();

	/**
	 * @param free The current item is allowed to be obtained for free
	 */
	void setFreeAllowed(boolean free);

	/**
	 * Assign a purchase tier to an item
	 * 
	 * @param item The item to assign the tier to
	 * @param tier The purchase tier to assign to the item
	 */
	void setPurchaseTier(ItemScriptType item, PurchaseTierScriptType tier);

	/**
	 * @param item The item to obtain the assigned purchase tier of
	 * @return The purchase tier assigned to the supplied item, or null if none
	 *         assigned
	 */
	PurchaseTierScriptType getPurchaseTier(ItemScriptType item);

	/**
	 * @param item The item to assign the tier to
	 * @param tier The subscription tier to assign to the item
	 */
	void setSubscriptionTier(ItemScriptType item, SubscriptionTierScriptType tier);

	/**
	 * @param item The item to obtain the assigned subscription tier of
	 * @return The subscription tier assigned to the supplied item, or null if
	 *         none assigned
	 */
	SubscriptionTierScriptType getSubscriptionTier(ItemScriptType item);

	/**
	 * Allow an item to be obtained for free
	 * 
	 * @param item The item to assign the free option to
	 * @param free Supplied item is allowed to be obtained for free
	 */
	void setFreeAllowed(ItemScriptType item, boolean free);

	/**
	 * Determine if an item is allowed to be obtained for free
	 * 
	 * @param item The item to read the free option of
	 * @return true if the supplied item is allowed to be obtained for free
	 */
	boolean isFreeAllowed(ItemScriptType item);

	/**
	 * @return The value of the global setting that determines if 'free' is an
	 *         assignable option
	 */
	boolean isFreeEnabled();

	/**
	 * @return The value of the global setting that determines if purchase tiers
	 *         are an assignable option
	 */
	boolean isPurchaseEnabled();

	/**
	 * @return The value of the global setting that determines if subscription
	 *         tiers are an assignable option
	 */
	boolean isSubscriptionEnabled();
}
