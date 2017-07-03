/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
