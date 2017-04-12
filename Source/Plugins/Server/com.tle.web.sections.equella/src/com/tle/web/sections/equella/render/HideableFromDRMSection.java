package com.tle.web.sections.equella.render;

import com.tle.web.sections.SectionInfo;

/**
 * An interface for sections which can be instructed to hide or unhide
 * themselves. This facility is referred to when rendering the DRMFilterSection.
 * If the DRMFilterSection is rendered in its 'Accept/Reject/Preview mode, then
 * almost all actions that otherwise appear in the RHS actions column are
 * hidden. The exception is the SearchPrevNextSection. This provides the ability
 * to click,click,click through a sequence of items in a search result list,
 * viewing either the item summary, or the DRM filter screen where appropriate,
 * and in both cases to have the Next Prev buttons rendered and in the same
 * location on screen.<br>
 * Sections implementing this interface require a Model class which implements
 * HideableFromDRMModel
 * 
 * @author larry
 */
public interface HideableFromDRMSection
{
	void showSection(SectionInfo info, boolean show);
}
