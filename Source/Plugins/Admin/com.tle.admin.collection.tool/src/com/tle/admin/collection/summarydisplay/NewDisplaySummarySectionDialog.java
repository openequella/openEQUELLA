package com.tle.admin.collection.summarydisplay;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.java.plugin.registry.Extension;

import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.itemdef.SummarySectionsConfig;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.LangUtils;
import com.tle.core.plugins.PluginService;

@SuppressWarnings("nls")
public class NewDisplaySummarySectionDialog extends AbstractChoiceDialog<SummarySectionsConfig>
{
	private final Map<String, String> defaultNames = new HashMap<String, String>();

	public NewDisplaySummarySectionDialog(final PluginService pluginService)
	{
		super(CurrentLocale.get("com.tle.admin.collection.tool.summarysections.adddialog.instructions"), CurrentLocale
			.get("com.tle.admin.collection.tool.summarysections.adddialog.title"));

		for( Extension ext : pluginService.getConnectedExtensions("com.tle.admin.collection.tool", "summaryDisplay") )
		{
			final String id = ext.getParameter("id").valueAsString();
			addChoice(id, CurrentLocale.get(ext.getParameter("nameKey").valueAsString()));
			defaultNames.put(id, ext.getParameter("defaultNameKey").valueAsString());
		}
	}

	@Override
	protected void addClicked(String key)
	{
		selection = new SummarySectionsConfig(key);
		String title = CurrentLocale.get(defaultNames.get(key));
		selection.setTitle(title);
		LanguageBundle bundleTitle = new LanguageBundle();
		LangUtils.setString(bundleTitle, CurrentLocale.getLocale(), title);
		selection.setBundleTitle(bundleTitle);
		selection.setUuid(UUID.randomUUID().toString());
		dialog.dispose();
	}
}
