package com.tle.web.htmleditor.settings.section;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.Section;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.render.Label;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;

/**
 * @author Aaron
 */
@NonNullByDefault
public interface ModalHtmlEditorSettingsSection extends Section
{
	void addBreadcrumbsAndTitle(SectionInfo info, Decorations decorations, Breadcrumbs crumbs);

	void startSession(SectionInfo info);

	/**
	 * @param info
	 * @return Return null if it doesn't apply (e.g. no privs)
	 */
	@Nullable
	SettingInfo getSettingInfo(SectionInfo info);

	public class SettingInfo
	{
		private final String id;
		private final Label linkTitle;
		private final Label blurb;

		public SettingInfo(String id, Label linkTitle, Label blurb)
		{
			this.id = id;
			this.linkTitle = linkTitle;
			this.blurb = blurb;
		}

		public String getId()
		{
			return id;
		}

		public Label getLinkTitle()
		{
			return linkTitle;
		}

		public Label getBlurb()
		{
			return blurb;
		}
	}
}
