/*
 * Copyright 2019 Apereo
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

package com.tle.web.sections.result.util;

import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.ProcessedLabel;

@SuppressWarnings("nls")
public class IconLabel implements ProcessedLabel
{
	public enum Icon
	{
		NONE(null), PREV("icon-arrow-left"), NEXT("icon-arrow-right", false), ADD("icon-plus"), DELETE("icon-remove"),
		EDIT("icon-pencil"), SELECT("icon-thumbs-up"), UNSELECT("icon-ban-circle"), THUMBS_UP("icon-thumbs-up"),
		THUMBS_DOWN("icon-thumbs-down"), DOWN("icon-chevron-down"), UP("icon-chevron-up"), SEARCH("icon-search"), USER(
			"icon-user"), SAVE("icon-ok"), EMAIL("icon-envelope"), UPLOAD("icon-upload"), DOWNLOAD("icon-download"),
		WRENCH("icon-wrench"), WARNING("icon-warning-sign"), ZOOM_IN("icon-zoom-in"), ZOOM_OUT("icon-zoom-out"),
		ROTATE_LEFT("icon-arrow-left"), ROTATE_RIGHT("icon-arrow-right", false), PRINT("icon-print"), REFRESH(
			"icon-refresh"), JUMP_TO_FIRST("icon-fast-backward"), JUMP_TO_LAST("icon-fast-forward", false), CART(
			"icon-shopping-cart"), BACK("icon-circle-arrow-left"), COG("icon-cog"), BELL("icon-bell"), TASKS(
			"icon-tasks"), OFF("icon-off"), FLAG("icon-flag"), MINUS("icon-minus"), MOVE("icon-move"), FULLSCREEN(
			"icon-fullscreen"), WAIT("icon-time"), INFO("icon-info-sign"), FAVOURITES("icon-star"), FAVOURITES_EMPTY(
			"icon-star-empty"), HIERACHY("icon-th-list"), COMMENT("icon-comment"), IMAGE("icon-picture"), VIDEO(
			"icon-film");

		private final String cssClass;
		private final boolean before;

		private Icon(String cssClass)
		{
			this(cssClass, true);
		}

		private Icon(String cssClass, boolean before)
		{
			this.cssClass = cssClass;
			this.before = before;
		}

		public String getCssClass()
		{
			return cssClass;
		}

		public boolean isBefore()
		{
			return before;
		}
	}

	private boolean whiteIcon;
	private Icon icon;
	private Label label;

	public IconLabel(Icon icon, Label label)
	{
		this(icon, label, true);
	}

	public IconLabel(Icon icon, Label label, boolean isWhiteIcon)
	{
		this.icon = icon;
		this.label = label;
		this.whiteIcon = isWhiteIcon;
	}

	@Override
	public String getText()
	{
		StringBuilder rv = new StringBuilder();

		if( label == null )
		{
			writeIcon(rv);
			return rv.toString();
		}

		if( icon != Icon.NONE && icon.isBefore() )
		{
			writeIcon(rv);
			rv.append(" ");
		}

		String t = label.getText();
		if( !label.isHtml() )
		{
			t = SectionUtils.ent(t);
		}
		rv.append(t);

		if( icon != Icon.NONE && !icon.isBefore() )
		{
			rv.append(" ");
			writeIcon(rv);
		}

		return rv.toString();
	}

	private void writeIcon(StringBuilder sb)
	{
		sb.append("<i class=\"");
		sb.append(icon.getCssClass());
		if( this.whiteIcon )
		{
			sb.append(" icon-white");

		}
		sb.append("\"></i>");

	}

	public boolean isWhiteIcon()
	{
		return whiteIcon;
	}

	public void setWhiteIcon(boolean whiteIcon)
	{
		this.whiteIcon = whiteIcon;
	}

	@Override
	public boolean isHtml()
	{
		return true;
	}

	@Override
	public Label getUnprocessedLabel()
	{
		return label;
	}
}
