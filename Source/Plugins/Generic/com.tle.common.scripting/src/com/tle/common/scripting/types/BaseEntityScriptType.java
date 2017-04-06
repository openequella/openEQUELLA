package com.tle.common.scripting.types;


/**
 * A super-interface for entity types. E.g. collection, schema, taxonomy...
 * 
 * @author Aaron
 */
public interface BaseEntityScriptType
{
	/**
	 * @return The UUID of the entity
	 */
	String getUniqueID();

	/**
	 * @return This function is an alias for getUniqueID()
	 */
	String getUuid();

	/**
	 * @return The name of the entity in the current user's language
	 */
	String getName();

	/**
	 * @return The name of the entity in the current user's language
	 */
	String getDescription();
}
