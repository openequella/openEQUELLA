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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

import com.tle.admin.baseentity.EditorState;
import com.tle.admin.baseentity.EntityStagingFileViewer;
import com.tle.admin.schema.SchemaModel;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.entity.itemdef.SummarySectionsConfig;
import com.tle.common.applet.client.ClientService;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.remoting.RemoteItemDefinitionService;

@SuppressWarnings("nls")
public abstract class AbstractTemplatingConfig extends AbstractOnlyTitleConfig
{
	private static final long serialVersionUID = 1L;

	protected RSyntaxTextArea editor;
	protected EditorState<ItemDefinition> state;
	protected ClientService clientService;

	protected abstract String getEditorLabelKey();

	@Override
	public void setup()
	{
		// Layout
		setLayout(new MigLayout("fill, wrap 1"));
		super.setup();

		// Label
		add(new JLabel(CurrentLocale.get(getEditorLabelKey())));

		// Editor (Note size is irrelevant and annoying)
		// Aaron: No, not irrelevant. Well, maybe to us, but not the highlighter
		editor = new RSyntaxTextArea(500, 2000);
		add(new RTextScrollPane(editor), "grow, push");

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

		changeDetector.watch(editor);
	}

	@Override
	public void save(SummarySectionsConfig element)
	{
		element.setConfiguration(editor.getText());
		super.save(element);
	}

	@Override
	public void load(SummarySectionsConfig element)
	{
		editor.setText(element.getConfiguration());
		super.load(element);
	}

	@Override
	public Component getComponent()
	{
		return this;
	}

	@Override
	public void setSchemaModel(SchemaModel model)
	{
		// Don't need this
	}

	@Override
	public void setState(EditorState<ItemDefinition> state)
	{
		this.state = state;
	}

	@Override
	public void setClientService(ClientService service)
	{
		this.clientService = service;
	}
}