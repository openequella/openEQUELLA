package com.tle.web.sections;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.generic.AbstractPrototypeSection;

/**
 * This interface holds the registered Id of a {@link Section}. Generally
 * {@code Section} classes themselves will implement this.
 * 
 * @see AbstractPrototypeSection
 * @author jmaginnis
 */
@NonNullByDefault
public interface SectionId
{
	/**
	 * Returns the section id.
	 * 
	 * @return The id
	 */
	// TODO: is it really nullable?
	@Nullable
	String getSectionId();

	/**
	 * @return
	 */
	@Nullable
	SectionTree getTree();

	/**
	 * @return The <code>Section</code> associated with this
	 *         <code>SectionContext</code>s id.
	 */
	@Nullable
	Section getSectionObject();

}
