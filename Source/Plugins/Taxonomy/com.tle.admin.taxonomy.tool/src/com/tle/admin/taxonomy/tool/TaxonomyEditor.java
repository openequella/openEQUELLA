/*
 * Created on May 10, 2005
 */
package com.tle.admin.taxonomy.tool;

import java.util.ArrayList;
import java.util.List;

import com.tle.admin.baseentity.AccessControlTab;
import com.tle.admin.baseentity.BaseEntityEditor;
import com.tle.admin.baseentity.BaseEntityTab;
import com.tle.admin.tools.common.BaseEntityTool;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.taxonomy.Taxonomy;

/**
 * @author Nicholas Read
 */
public class TaxonomyEditor extends BaseEntityEditor<Taxonomy>
{
	public TaxonomyEditor(BaseEntityTool<Taxonomy> tool, boolean readonly)
	{
		super(tool, readonly);
	}

	@Override
	protected AbstractDetailsTab<Taxonomy> constructDetailsTab()
	{
		return new DetailsTab();
	}

	@Override
	protected List<BaseEntityTab<Taxonomy>> getTabs()
	{
		List<BaseEntityTab<Taxonomy>> tabs = new ArrayList<BaseEntityTab<Taxonomy>>();
		tabs.add((DetailsTab) detailsTab);
		tabs.add(new AccessControlTab<Taxonomy>(Node.TAXONOMY));
		return tabs;
	}

	@Override
	protected String getEntityName()
	{
		return CurrentLocale.get("com.tle.admin.taxonomy.tool.entityname"); //$NON-NLS-1$
	}

	@Override
	protected String getWindowTitle()
	{
		return CurrentLocale.get("com.tle.admin.taxonomy.tool.windowtitle"); //$NON-NLS-1$
	}

	@Override
	public String getDocumentName()
	{
		return CurrentLocale.get("com.tle.admin.taxonomy.tool.entityname"); //$NON-NLS-1$
	}
}
