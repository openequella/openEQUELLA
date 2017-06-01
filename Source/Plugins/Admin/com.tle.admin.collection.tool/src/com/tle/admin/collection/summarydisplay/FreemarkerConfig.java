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

package com.tle.admin.collection.summarydisplay;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import com.dytech.devlib.PropBagEx;
import com.dytech.gui.ChangeDetector;
import com.tle.admin.baseentity.EntityStagingFileViewer;
import com.tle.admin.codeeditor.EquellaSyntaxTextArea;
import com.tle.admin.gui.i18n.I18nTextField;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.itemdef.SummarySectionsConfig;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.remoting.RemoteItemDefinitionService;
import com.tle.i18n.BundleCache;

@SuppressWarnings("nls")
public class FreemarkerConfig extends AbstractTemplatingConfig
{
	private static final long serialVersionUID = 1L;

	private EquellaSyntaxTextArea script;

	@Override
	public void setup()
	{
		setLayout(new MigLayout("fill, wrap 1", "[grow]", "[][fill,sg1][][fill,sg1][]"));
		final JLabel titleLabel = new JLabel(getTitleLabelKey());
		title = new I18nTextField(BundleCache.getLanguages());

		add(titleLabel, "split 2");
		add(title, "grow, push");

		add(new JLabel(CurrentLocale.get(getEditorLabelKey())));
		editor = new EquellaSyntaxTextArea(SyntaxConstants.SYNTAX_STYLE_HTML, 500, 2000);
		add(new RTextScrollPane(editor), "grow, push");

		add(new JLabel(CurrentLocale.get("com.tle.admin.collection.tool.summarysections.freemarker.label.script")));
		script = new EquellaSyntaxTextArea(500, 2000);
		add(new RTextScrollPane(script), "grow, push");

		final JButton showFiles = new JButton(
			CurrentLocale.get("com.tle.admin.collection.tool.summarydisplay.abstracttemplating.showfiles"));
		showFiles.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				EntityStagingFileViewer file = new EntityStagingFileViewer(state, clientService
					.getService(RemoteItemDefinitionService.class), "displaytemplate/");
				changeDetector.watch(file.getFileTreeModel());

				add(file, "grow, push");
				remove(showFiles);
				revalidate();

				showFiles.removeActionListener(this);
			}
		});
		add(showFiles);

		changeDetector = new ChangeDetector();
		changeDetector.watch(editor);
		changeDetector.watch(script);
		changeDetector.watch(title);
	}

	@Override
	public void save(SummarySectionsConfig element)
	{
		PropBagEx xml = new PropBagEx();
		xml.setNode("markup", editor.getText());
		xml.setNode("script", script.getText());
		element.setConfiguration(xml.toString());
		LanguageBundle titleBundle = title.save();
		element.setBundleTitle(titleBundle);
	}

	@Override
	public void load(SummarySectionsConfig element)
	{
		String config = element.getConfiguration();
		title.load(element.getBundleTitle());
		if( !Check.isEmpty(config) )
		{
			try
			{
				PropBagEx xml = new PropBagEx(config);
				editor.setText(xml.getNode("markup"));
				script.setText(xml.getNode("script"));
			}
			catch( Exception e )
			{
				editor.setText(config);
			}
		}
	}

	@Override
	public String getEditorLabelKey()
	{
		return "com.tle.admin.collection.tool.summarysections.freemarker.label.markup";
	}
}