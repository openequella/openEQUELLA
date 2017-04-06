package com.tle.core.taxonomy.scripting.types;

/**
 * These are retrieved and manipulated with the TaxonomyScriptType
 * 
 * @author aholland
 */
public interface TermScriptType
{
	/**
	 * @return The node name of the term (i.e. not the full path)
	 */
	String getTerm();

	/**
	 * @return The full path to this term.
	 */
	String getFullPath();

	/**
	 * @return true if this node has no child nodes.
	 */
	boolean isLeaf();

	/**
	 * @param attributeKey The key of the custom data field.
	 * @return The value of the custom data (if any)
	 */
	String getData(String attributeKey);
}
