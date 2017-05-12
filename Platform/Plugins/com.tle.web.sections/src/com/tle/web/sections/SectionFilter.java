package com.tle.web.sections;

import com.tle.web.sections.header.HeaderHelper;

/**
 * Filter a Sections request/forward.
 * <p>
 * Upon {@link SectionInfo} creation, a list of prioritized
 * {@code SectionFilter}s have a chance run and manipulate the
 * {@code SectionInfo}.
 * <p>
 * Usually this manipulation involves adding a {@link SectionTree} to wrap the
 * existing tree(s).
 * <p>
 * The requested page's {@code SectionTree} will always be wrapped by at least
 * one {@code SectionTree} which is responsible for implementing the
 * {@link HeaderHelper} and rendering any header/footer and navigation.
 * 
 * @author jmaginnis
 */
public interface SectionFilter
{
	/**
	 * Filter this request/forward.
	 * 
	 * @param info The {@code SectionInfo} to filter
	 */
	void filter(MutableSectionInfo info);
}
