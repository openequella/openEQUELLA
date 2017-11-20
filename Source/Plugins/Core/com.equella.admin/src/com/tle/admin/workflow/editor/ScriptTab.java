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

package com.tle.admin.workflow.editor;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import net.miginfocom.swing.MigLayout;

import org.fife.ui.rtextarea.RTextScrollPane;

import com.dytech.edge.common.Constants;
import com.dytech.gui.ChangeDetector;
import com.dytech.gui.JStatusBar;
import com.tle.admin.codeeditor.EquellaSyntaxTextArea;
import com.tle.admin.common.gui.EditorHelper;
import com.tle.admin.gui.i18n.I18nTextArea;
import com.tle.admin.gui.i18n.I18nTextField;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.LanguageString;
import com.tle.common.applet.gui.AppletGuiUtils;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.workflow.node.ScriptNode;
import com.tle.common.workflow.node.WorkflowItem.MoveLive;
import com.tle.i18n.BundleCache;

public class ScriptTab extends JPanel implements FocusListener, CaretListener
{
	private static final long serialVersionUID = 1L;
	private JLabel nameLabel;
	private I18nTextField name;

	private JLabel descriptionLabel;
	private I18nTextArea description;

	private JCheckBox moveToLive;
	private ButtonGroup moveToLiveGroup;
	private JRadioButton moveToLiveArrival;
	private JRadioButton moveToLiveAccept;

	private JCheckBox proceedNext;
	private JLabel proceedExplanation;

	private JLabel scriptLabel;
	private EquellaSyntaxTextArea script;
	private JStatusBar statusbar;

	public ScriptTab(ChangeDetector changeDetector)
	{
		setupGui();
		setupLayout();
		setupDefaults();
		setupChangeDetector(changeDetector);
	}

	private String s(String keyPart)
	{
		return CurrentLocale.get("com.tle.admin.workflow.editor.scripteditor." + keyPart);
	}

	private void setupGui()
	{
		nameLabel = new JLabel(s("name"));
		name = new I18nTextField(BundleCache.getLanguages());

		descriptionLabel = new JLabel(s("description"));
		description = new I18nTextArea(BundleCache.getLanguages());
		description.setTextRows(5);

		moveToLive = new JCheckBox(s("move"));
		moveToLive.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent e)
			{
				enableMoveToLive(moveToLive.isSelected());
			}
		});

		moveToLiveGroup = new ButtonGroup();
		moveToLiveArrival = new JRadioButton(s("move.arrival"));
		moveToLiveAccept = new JRadioButton(s("move.acceptance"));
		moveToLiveGroup.add(moveToLiveArrival);
		moveToLiveGroup.add(moveToLiveAccept);

		proceedNext = new JCheckBox(s("proceed.next"));
		proceedExplanation = new JLabel("<html>" + s("proceed.explanation"));

		scriptLabel = new JLabel(s("scriptlabel"));
		script = new EquellaSyntaxTextArea(100, 200);
		script.addFocusListener(this);
		script.addCaretListener(this);

		statusbar = new JStatusBar(EditorHelper.getStatusBarSpinner());
	}

	private void enableMoveToLive(boolean b)
	{
		moveToLiveAccept.setEnabled(b);
		moveToLiveArrival.setEnabled(b);
	}

	private void setupLayout()
	{
		setLayout(new MigLayout("wrap 3", "[][][fill,grow]"));
		setBorder(AppletGuiUtils.DEFAULT_BORDER);

		add(nameLabel);
		add(name, "span 2, grow");

		add(descriptionLabel, "aligny top");
		add(description, "span 2, grow");

		add(moveToLive, "span 3");
		add(moveToLiveArrival, "skip, span 3");
		add(moveToLiveAccept, "skip, span 3");

		add(proceedNext, "span 3");
		add(proceedExplanation, "skip, span3, gap i*2.3");

		add(scriptLabel, "span 3");
		add(script, "span 3, grow");

		RTextScrollPane scroll = new RTextScrollPane(script);
		add(scroll, "span 3, grow");
		
		add(statusbar, "span 3, grow");
	}

	private void setupDefaults()
	{
		enableMoveToLive(false);
		moveToLiveAccept.setSelected(true);
	}

	private void setupChangeDetector(ChangeDetector changeDetector)
	{
		changeDetector.watch(name);
		changeDetector.watch(description);
		changeDetector.watch(script);
	}

	public void save(ScriptNode item)
	{
		LanguageBundle nameBundle = name.save();
		if (nameBundle != null)
		{
			Map<String, LanguageString> strings = nameBundle.getStrings();
			for (Entry<String, LanguageString> entry : strings.entrySet())
			{
				LanguageString currentString = entry.getValue();
				if (currentString.getText().length() > 100)
				{
					currentString.setText(currentString.getText().substring(0, 100));
					strings.put(entry.getKey(), currentString);
				}
			}
		}
		item.setName(nameBundle);
		item.setDescription(description.save());

		item.setMovelive(moveToLive.isSelected() ? (moveToLiveArrival.isSelected() ? MoveLive.ARRIVAL
			: MoveLive.ACCEPTED) : MoveLive.NO);

		item.setProceedNext(proceedNext.isSelected());
		item.setScript(script.getText());

	}

	public void load(ScriptNode item)
	{
		name.load(item.getName());
		description.load(item.getDescription());
		MoveLive movelive = item.getMovelive();
		moveToLive.setSelected(movelive != MoveLive.NO);
		moveToLiveArrival.setSelected(movelive == MoveLive.ARRIVAL);
		moveToLiveAccept.setSelected(movelive == MoveLive.ACCEPTED || movelive == MoveLive.NO);
		proceedNext.setSelected(item.isProceedNext());
		script.setText(item.getScript());
	}

	@Override
	public void focusGained(FocusEvent event)
	{
		// nothing
	}

	@Override
	public void focusLost(FocusEvent event)
	{
		statusbar.setMessage(Constants.BLANK);
	}

	@Override
	public void caretUpdate(CaretEvent event)
	{
		statusbar.setMessage(
			CurrentLocale.get("com.tle.admin.scripting.editor.lineandcolumn", script.getCaretLineNumber() + 1,
				script.getCaretOffsetFromLineStart()));
	}
}
