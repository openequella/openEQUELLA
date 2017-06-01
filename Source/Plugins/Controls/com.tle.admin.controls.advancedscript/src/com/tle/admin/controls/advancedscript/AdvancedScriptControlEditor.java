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

package com.tle.admin.controls.advancedscript;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import org.fife.ui.rtextarea.RTextScrollPane;

import com.dytech.common.io.FileExtensionFilter;
import com.dytech.devlib.PropBagEx;
import com.dytech.edge.admin.wizard.editor.AbstractControlEditor;
import com.dytech.edge.admin.wizard.model.Control;
import com.dytech.edge.common.Constants;
import com.dytech.gui.JShuffleBox;
import com.dytech.gui.TableLayout;
import com.dytech.gui.file.FileFilterAdapter;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.tle.admin.Driver;
import com.tle.admin.codeeditor.EquellaSyntaxTextArea;
import com.tle.admin.common.gui.AbstractFileWorker;
import com.tle.admin.schema.SchemaModel;
import com.tle.common.Check;
import com.tle.common.NameValue;
import com.tle.common.applet.client.DialogUtils;
import com.tle.common.applet.client.FileWorker;
import com.tle.common.applet.gui.AppletGuiUtils;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.javascript.RemoteJavascriptService;
import com.tle.common.wizard.controls.advancedscript.AdvancedScriptControl;

@SuppressWarnings("nls")
public class AdvancedScriptControlEditor extends AbstractControlEditor<AdvancedScriptControl>
	implements
		ActionListener,
		CaretListener,
		FocusListener
{
	private static final long serialVersionUID = 1L;

	private static final int CODE_HEIGHT = 400;
	private static final int CODE_HEIGHT_LINES = 500;
	private static final int CODE_WIDTH_COLUMNS = 300;

	private static final String EXPORT_KEY_NOTES = "notes"; //$NON-NLS-1$
	private static final String EXPORT_KEY_ONLOAD = "onload"; //$NON-NLS-1$
	private static final String EXPORT_KEY_ONSUBMIT = "onsubmit"; //$NON-NLS-1$
	private static final String EXPORT_KEY_LOAD = "load"; //$NON-NLS-1$
	private static final String EXPORT_KEY_STORE = "store"; //$NON-NLS-1$
	private static final String EXPORT_KEY_MARKUP = "markup"; //$NON-NLS-1$
	private static final String EXPORT_KEY_LIBS = "libs"; //$NON-NLS-1$
	private static final String EXPORT_KEY_LIB = "lib"; //$NON-NLS-1$
	private static final String EXPORT_FILE_EXTENSION = "asc"; //$NON-NLS-1$

	protected JTextArea notes;

	private JButton exportButton;
	private JButton importButton;

	protected JShuffleBox<NameValue> jquery;

	protected EquellaSyntaxTextArea freemarker;
	protected EquellaSyntaxTextArea onloadJs;
	protected EquellaSyntaxTextArea onsubmitJs;

	protected EquellaSyntaxTextArea readJs;
	protected EquellaSyntaxTextArea storeJs;

	// used to determine changes in syntax highlighters because style changes
	// report a change
	private final Map<EquellaSyntaxTextArea, String> changedStringsReferenceMap;

	private EquellaSyntaxTextArea focus;

	private static String DEFAULT_TEMPLATE;
	protected static String[] HIGHLIGHTER_KEYWORDS;
	static
	{
		try
		{
			DEFAULT_TEMPLATE = Resources.toString(
				AdvancedScriptControlEditor.class.getResource("defaultDisplayTemplate.txt"), Charsets.UTF_8);
			HIGHLIGHTER_KEYWORDS = Resources.toString(
				AdvancedScriptControlEditor.class.getResource("highlighterKeywords.txt"), Charsets.UTF_8).split("\\s");
		}
		catch( IOException io )
		{
			DEFAULT_TEMPLATE = Constants.BLANK;
			HIGHLIGHTER_KEYWORDS = new String[0];
		}
	}

	public AdvancedScriptControlEditor(final Control control, final int wizardType, final SchemaModel schema)
	{
		super(control, wizardType, schema);
		setShowScripting(true);

		addSection(createExportSection());
		addSection(createDetailsSection());
		addSection(createClientEditorsSection());
		addSection(createServerEditorsSection());

		changedStringsReferenceMap = new HashMap<EquellaSyntaxTextArea, String>();
	}

	@Override
	protected void loadControl()
	{
		final AdvancedScriptControl control = getWizardControl();

		notes.setText(control.getNotes());

		loadSyntaxHighlightedPane(onloadJs, control.getOnLoadJs(), null);
		loadSyntaxHighlightedPane(onsubmitJs, control.getOnSubmitJs(), ""); //$NON-NLS-1$
		loadSyntaxHighlightedPane(readJs, control.getReadJs(), null);
		loadSyntaxHighlightedPane(storeJs, control.getStoreJs(), null);
		loadSyntaxHighlightedPane(freemarker, control.getMarkUp(), DEFAULT_TEMPLATE);

		jquery.removeAllFromLeft();
		jquery.removeAllFromRight();
		selectLibs(control.getJsLibs());
	}

	protected void selectLibs(List<String> selectedLibs)
	{
		NameValue[] libs = getJavascriptModules();
		jquery.addToLeft(libs);
		for( NameValue lib : libs )
		{
			if( selectedLibs.contains(lib.getValue()) )
			{
				jquery.addToRight(lib);
			}
		}
	}

	@Override
	protected void saveControl()
	{
		final AdvancedScriptControl control = getWizardControl();

		control.setNotes(notes.getText());
		control.setOnLoadJs(saveSyntaxHighlightedPane(onloadJs));
		control.setOnSubmitJs(saveSyntaxHighlightedPane(onsubmitJs));
		control.setReadJs(saveSyntaxHighlightedPane(readJs));
		control.setStoreJs(saveSyntaxHighlightedPane(storeJs));
		control.setMarkUp(saveSyntaxHighlightedPane(freemarker));

		List<String> selectedLibs = new ArrayList<String>();
		for( NameValue lib : jquery.getRight() )
		{
			selectedLibs.add(lib.getValue());
		}
		control.setJsLibs(selectedLibs);
	}

	private JComponent createDetailsSection()
	{
		final JLabel notesLabel = new JLabel(getString("label.notes")); //$NON-NLS-1$

		notes = new JTextArea();
		notes.setWrapStyleWord(true);
		notes.setLineWrap(true);
		notes.setRows(3);
		notes.setBorder(new EmptyBorder(0, 0, 10, 0));

		final int height1 = notesLabel.getPreferredSize().height;
		final int height2 = notes.getPreferredSize().height;

		final int[] rows = {height1, height2};
		final int[] cols = {TableLayout.FILL};
		final JPanel all = new JPanel(new TableLayout(rows, cols));

		all.add(notesLabel, new Rectangle(0, 0, 1, 1));
		all.add(new JScrollPane(notes), new Rectangle(0, 1, 1, 1));

		return all;
	}

	protected JComponent createExportSection()
	{
		JLabel importExport = new JLabel(getString("label.importexport")); //$NON-NLS-1$
		exportButton = new JButton(getString("button.export")); //$NON-NLS-1$
		importButton = new JButton(getString("button.import")); //$NON-NLS-1$

		final int height1 = importExport.getPreferredSize().height;
		final int height2 = exportButton.getPreferredSize().height;
		final int width1 = AppletGuiUtils.getGreatestPreferredSize(importButton, exportButton).width;

		final int[] rows = {height1, height2};
		final int[] cols = {width1, width1, TableLayout.FILL};

		final JPanel all = new JPanel(new TableLayout(rows, cols));

		all.add(importExport, new Rectangle(0, 0, 3, 1));
		all.add(importButton, new Rectangle(0, 1, 1, 1));
		all.add(exportButton, new Rectangle(1, 1, 1, 1));

		importButton.addActionListener(this);
		exportButton.addActionListener(this);

		return all;
	}

	protected JComponent createClientEditorsSection()
	{
		freemarker = createHtmlHighlightingPane();
		jquery = new JShuffleBox<NameValue>();
		onloadJs = createJavascriptHighlightingPane();
		onsubmitJs = createJavascriptHighlightingPane();

		JTabbedPane clientEditors = new JTabbedPane();
		addCodeTab(clientEditors, "tab.template", new RTextScrollPane(freemarker)); //$NON-NLS-1$
		addCodeTab(clientEditors, "tab.onload", new RTextScrollPane(onloadJs)); //$NON-NLS-1$
		addCodeTab(clientEditors, "tab.onsubmit", new RTextScrollPane(onsubmitJs)); //$NON-NLS-1$
		clientEditors.addTab(getString("tab.libraries"), jquery); //$NON-NLS-1$
		clientEditors.setPreferredSize(new Dimension(clientEditors.getWidth(), CODE_HEIGHT));

		return clientEditors;
	}

	protected JComponent createServerEditorsSection()
	{
		readJs = createJavascriptHighlightingPane();
		storeJs = createJavascriptHighlightingPane();

		JTabbedPane serverEditors = new JTabbedPane();
		addCodeTab(serverEditors, "tab.read", new RTextScrollPane(readJs)); //$NON-NLS-1$
		addCodeTab(serverEditors, "tab.store", new RTextScrollPane(storeJs)); //$NON-NLS-1$
		serverEditors.setPreferredSize(new Dimension(serverEditors.getWidth(), CODE_HEIGHT));

		return serverEditors;
	}

	private EquellaSyntaxTextArea createJavascriptHighlightingPane()
	{
		EquellaSyntaxTextArea pane = new EquellaSyntaxTextArea(CODE_HEIGHT_LINES, CODE_WIDTH_COLUMNS, "attributes",
			"request");
		pane.addCaretListener(this);
		pane.addFocusListener(this);
		return pane;
	}

	private EquellaSyntaxTextArea createHtmlHighlightingPane()
	{
		EquellaSyntaxTextArea pane = new EquellaSyntaxTextArea(CODE_HEIGHT_LINES, CODE_WIDTH_COLUMNS, "attributes");
		pane.addCaretListener(this);
		pane.addFocusListener(this);
		return pane;
	}

	private void addCodeTab(JTabbedPane tabs, String titleKey, RTextScrollPane codePane)
	{
		JLabel helpLabel = new JLabel(getString(titleKey + ".help")); //$NON-NLS-1$

		final int[] rows = {helpLabel.getPreferredSize().height, TableLayout.FILL};
		final int[] cols = {TableLayout.FILL};

		JPanel tabArea = new JPanel(new TableLayout(rows, cols));
		tabArea.add(helpLabel, new Rectangle(0, 0, 1, 1));
		tabArea.add(codePane, new Rectangle(0, 1, 1, 1));

		tabs.add(getString(titleKey), tabArea);
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean hasDetectedChanges()
	{
		boolean superChanges = super.hasDetectedChanges();
		if( superChanges )
		{
			return superChanges;
		}
		// check our syntax higlighted editors
		return syntaxHighlightingPaneDetectedChanges(onloadJs) || syntaxHighlightingPaneDetectedChanges(onsubmitJs)
			|| syntaxHighlightingPaneDetectedChanges(readJs) || syntaxHighlightingPaneDetectedChanges(storeJs)
			|| syntaxHighlightingPaneDetectedChanges(freemarker);
	}

	private void loadSyntaxHighlightedPane(final EquellaSyntaxTextArea pane, final String value,
		final String defaultValue)
	{
		final String refValue = (value != null ? value : defaultValue);
		pane.setText(refValue);
		changedStringsReferenceMap.put(pane, refValue);
	}

	private String saveSyntaxHighlightedPane(final EquellaSyntaxTextArea pane)
	{
		String reference = pane.getText();
		return reference; // NOSONAR (kept variable for readability)
	}

	private boolean syntaxHighlightingPaneDetectedChanges(final EquellaSyntaxTextArea pane)
	{
		String reference = changedStringsReferenceMap.get(pane);
		if( reference != null )
		{
			return !reference.equals(pane.getText());
		}
		else
		{
			return !Check.isEmpty(pane.getText());
		}
	}

	protected static String getString(String partKey, Object... params)
	{
		return CurrentLocale.get("com.tle.admin.controls.advancedscript." + partKey, params); //$NON-NLS-1$
	}

	private static NameValue[] getJavascriptModules()
	{
		List<NameValue> result = new ArrayList<NameValue>();
		RemoteJavascriptService jsService = Driver.instance().getClientService()
			.getService(RemoteJavascriptService.class);

		List<NameValue> libs = jsService.getAllJavascriptLibraryNames();
		for( NameValue lib : libs )
		{
			for( NameValue module : jsService.getAllJavascriptModuleNames(lib.getValue()) )
			{
				result.add(new NameValue(lib.getName() + " - " + module.getName(), lib.getValue() //$NON-NLS-1$
					+ "." + module.getValue())); //$NON-NLS-1$
			}
		}
		return result.toArray(new NameValue[result.size()]);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if( e.getSource() == exportButton )
		{
			String controlName = getWizardControl().getCustomName();
			if( Check.isEmpty(controlName) )
			{
				// it may have a title, they used to have one in RC1
				controlName = CurrentLocale.get(getWizardControl().getTitle(), Constants.BLANK);
				if( Check.isEmpty(controlName) )
				{
					controlName = getString("untitledexport"); //$NON-NLS-1$
				}
			}
			DialogUtils.doSaveDialog(this,
				getString("export"), //$NON-NLS-1$
				new AdvancedScriptConfigFileFilter(),
				DialogUtils.getSuggestedFileName(controlName, EXPORT_FILE_EXTENSION), createExporter());
		}
		else if( e.getSource() == importButton )
		{
			DialogUtils.doOpenDialog(this, getString("import"), //$NON-NLS-1$
				new AdvancedScriptConfigFileFilter(), createImporter());
		}
	}

	private FileWorker createImporter()
	{
		return new AbstractFileWorker<Object>(getString("success.import"), //$NON-NLS-1$
			getString("error.import")) //$NON-NLS-1$
		{
			@Override
			public Object construct() throws Exception
			{
				final PropBagEx imp = new PropBagEx(file);
				freemarker.setText(imp.getNode(EXPORT_KEY_MARKUP));
				onloadJs.setText(imp.getNode(EXPORT_KEY_ONLOAD));
				onsubmitJs.setText(imp.getNode(EXPORT_KEY_ONSUBMIT));
				readJs.setText(imp.getNode(EXPORT_KEY_LOAD));
				storeJs.setText(imp.getNode(EXPORT_KEY_STORE));
				notes.setText(imp.getNode(EXPORT_KEY_NOTES));
				selectLibs(imp.getNodeList(EXPORT_KEY_LIBS + '/' + EXPORT_KEY_LIB));
				return null;
			}
		};
	}

	private FileWorker createExporter()
	{
		return new AbstractFileWorker<Object>(getString("success.export"), //$NON-NLS-1$
			getString("error.export")) //$NON-NLS-1$
		{
			@Override
			public Object construct() throws Exception
			{
				final PropBagEx exp = new PropBagEx();
				exp.setNode(EXPORT_KEY_MARKUP, freemarker.getText());
				exp.setNode(EXPORT_KEY_ONLOAD, onloadJs.getText());
				exp.setNode(EXPORT_KEY_ONSUBMIT, onsubmitJs.getText());
				exp.setNode(EXPORT_KEY_LOAD, readJs.getText());
				exp.setNode(EXPORT_KEY_STORE, storeJs.getText());
				exp.setNode(EXPORT_KEY_NOTES, notes.getText());
				final List<NameValue> selectedLibs = jquery.getRight();
				if( selectedLibs.size() > 0 )
				{
					final PropBagEx libs = exp.newSubtree(EXPORT_KEY_LIBS);
					for( NameValue lib : selectedLibs )
					{
						libs.createNode(EXPORT_KEY_LIB, lib.getValue());
					}
				}

				try (PrintWriter w = new PrintWriter(file))
				{
					w.write(exp.toString());
				}
				return null;
			}
		};
	}

	public static class AdvancedScriptConfigFileFilter extends FileFilterAdapter
	{
		public AdvancedScriptConfigFileFilter()
		{
			super(new FileExtensionFilter(EXPORT_FILE_EXTENSION), getString("file.description", EXPORT_FILE_EXTENSION));
		}
	}

	@Override
	public void focusGained(FocusEvent event)
	{
		focus = (EquellaSyntaxTextArea) event.getComponent();
	}

	@Override
	public void focusLost(FocusEvent event)
	{
		focus = null;
		getEntityEditor().getStatusBar().setMessage(Constants.BLANK);
	}

	@Override
	public void caretUpdate(CaretEvent event)
	{
		if( focus != null )
		{
			getEntityEditor().getStatusBar().setMessage(
				getString("lineandcolumn", focus.getCaretLineNumber() + 1, focus.getCaretOffsetFromLineStart())); //$NON-NLS-1$
		}
	}
}
