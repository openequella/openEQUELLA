package com.tle.web.sections.equella.render;

/**
 * Simple Model interface for MOdels associated with Sections which implement
 * HideableFromDRMSection
 * 
 * @author larry
 */
public interface HideableFromDRMModel
{
	boolean isHide();

	void setHide(boolean hide);
}
