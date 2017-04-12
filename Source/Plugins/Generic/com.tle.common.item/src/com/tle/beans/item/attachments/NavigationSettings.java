package com.tle.beans.item.attachments;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;

import org.hibernate.annotations.AccessType;

@Embeddable
@AccessType("field")
public class NavigationSettings implements Serializable, INavigationSettings
{
	private static final long serialVersionUID = 1L;

	@Transient
	@Deprecated
	private boolean showNextPrev;

	@Column(name = "showSplitOption")
	private boolean showSplitOption;

	@Column(name = "manualNavigation", nullable = false)
	private boolean manualNavigation;

	@Override
	public boolean isShowSplitOption()
	{
		return showSplitOption;
	}

	public void setShowSplitOption(boolean showSplitOption)
	{
		this.showSplitOption = showSplitOption;
	}

	@Override
	public boolean isManualNavigation()
	{
		return manualNavigation;
	}

	public void setManualNavigation(boolean manualNavigation)
	{
		this.manualNavigation = manualNavigation;
	}

	public boolean isShowNextPrev()
	{
		return showNextPrev;
	}

	public void setShowNextPrev(boolean showNextPrev)
	{
		this.showNextPrev = showNextPrev;
	}
}
