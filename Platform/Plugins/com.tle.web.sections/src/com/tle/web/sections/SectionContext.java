package com.tle.web.sections;

/**
 * The context of a {@link Section} within a {@link SectionInfo}.
 * <p>
 * Because <code>Section</code>s can be singleton instances, this class
 * represents the mapping of an id to a <code>Section</code>. It also provides
 * easy access to the Model class for the <code>Section</code>.
 * 
 * @author jmaginnis
 */
public interface SectionContext extends SectionId, SectionInfo
{

	/**
	 * @return The <code>SectionInfo</code> that this <code>Section</code> is
	 *         associated with.
	 */
	@Deprecated
	SectionInfo getInfo();

	@Deprecated
	SectionId getSection();
}
