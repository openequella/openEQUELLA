package com.tle.core.payment.scripting.objects;

import java.util.List;

import com.tle.common.scripting.ScriptObject;
import com.tle.core.payment.scripting.types.CatalogueScriptType;

/**
 * Available in scripts as the 'catalogue' variable
 * 
 * @author larry
 */
public interface CatalogueScriptObject extends ScriptObject
{
	String DEFAULT_VARIABLE = "catalogue"; //$NON-NLS-1$

	/**
	 * Return all catalogues in the system (enabled or otherwise)
	 */
	List<CatalogueScriptType> listAllCatalogues();

	/**
	 * The list of catalogues this item appears in due to being added to the
	 * whitelist
	 */
	List<CatalogueScriptType> listWhitelistCatalogues();

	/**
	 * The list of catalogues this item appears in due to being added to the
	 * blacklist
	 */
	List<CatalogueScriptType> listBlacklistCatalogues();

	/**
	 * The list of catalogues this item appears in due to being found in the
	 * associated dynamic collection
	 */
	List<CatalogueScriptType> listDynamicCatalogues();

	/**
	 * Add the current item to the whitelist of the supplied catalogue
	 * 
	 * @param catalogue The catalogue to add to
	 */
	void addToWhitelist(CatalogueScriptType catalogue);

	/**
	 * Add the current item to the blacklist of the supplied catalogue
	 * 
	 * @param catalogue The catalogue to exclude from
	 */
	void addToBlacklist(CatalogueScriptType catalogue);

	/**
	 * Remove the current item from the whitelist of the supplied catalogue.
	 * Note that the item may still appear in the catalogue if it matches the
	 * dynamic collection of the catalogue. Use addToBlacklist to exclude from
	 * the catalogue.
	 * 
	 * @param catalogue The catalogue to remove from manual additions
	 */
	void removeFromWhitelist(CatalogueScriptType catalogue);

	/**
	 * Remove the current item from the whitelist of the supplied catalogue
	 * 
	 * @param catalogue The catalogue to un-exclude from
	 */
	void removeFromBlacklist(CatalogueScriptType catalogue);

	/**
	 * Get a catalogue by UUID
	 * 
	 * @param uuid The UUID of the catalogue to obtain
	 * @return The catalogue with the UUID of uuid, or null if no match
	 */
	CatalogueScriptType getCatalogueByUuid(String uuid);
}
