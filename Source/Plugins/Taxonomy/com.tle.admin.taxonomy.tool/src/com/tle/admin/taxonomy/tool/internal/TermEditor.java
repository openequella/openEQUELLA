package com.tle.admin.taxonomy.tool.internal;

import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.KeyListener;
import java.awt.font.TextAttribute;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.miginfocom.swing.MigLayout;

import com.dytech.gui.JValidatingTextField;
import com.tle.admin.common.gui.tree.AbstractTreeNodeEditor;
import com.tle.admin.gui.EditorException;
import com.tle.admin.gui.common.ListWithView;
import com.tle.admin.gui.common.ListWithViewInterface;
import com.tle.common.Check;
import com.tle.common.LazyTreeNode;
import com.tle.common.Pair;
import com.tle.common.taxonomy.Taxonomy;
import com.tle.common.taxonomy.TaxonomyConstants;
import com.tle.common.taxonomy.terms.RemoteTermService;
import com.tle.common.taxonomy.terms.Term;

@SuppressWarnings("nls")
public class TermEditor extends AbstractTreeNodeEditor
{
	private final RemoteTermService termService;
	private final Map<String, Pair<String, String>> predefinedTermDataKeys;
	private final Taxonomy taxonomy;
	private final TermTreeNode ttn;

	private final JValidatingTextField termName;
	private final ListWithView<DataEntry, DataEditor> listWithView;

	public TermEditor(RemoteTermService termService, Map<String, Pair<String, String>> predefinedTermDataKeys,
		Taxonomy taxonomy, TermTreeNode ttn)
	{
		this.termService = termService;
		this.predefinedTermDataKeys = predefinedTermDataKeys;
		this.taxonomy = taxonomy;
		this.ttn = ttn;

		// Disallow backslashes, which are term separators
		termName = new JValidatingTextField(new JValidatingTextField.MaxLength(Term.MAX_TERM_VALUE_LENGTH),
			new JValidatingTextField.DisallowStr(TaxonomyConstants.TERM_SEPARATOR));
		termName.setText(ttn.getName());

		listWithView = new ListWithView<DataEntry, DataEditor>()
		{
			@Override
			protected ListWithViewInterface<DataEntry> getEditor(DataEntry currentSelection)
			{
				return currentSelection == null ? null : new DataEditor();
			}

			@Override
			protected DataEntry createElement()
			{
				Set<String> existingKeys = new HashSet<String>();
				for( DataEntry de : model )
				{
					existingKeys.add(de.getKey());
				}

				NewDataEntryDialog nded = new NewDataEntryDialog(TermEditor.this.predefinedTermDataKeys, existingKeys);
				return nded.showDialog(listWithView);
			}
		};
		listWithView.setListCellRenderer(new DefaultListCellRenderer()
		{
			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus)
			{
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				setText(((DataEntry) value).getDisplayName());
				return this;
			}
		});

		setLayout(new MigLayout("wrap 1, fill", "[grow]"));
		add(new JLabel(InternalDataSourceTab.s("termeditor.name")), "split 2");
		add(termName, "grow");
		add(new JLabel(InternalDataSourceTab.s("termeditor.datatitle")), "gaptop unrelated");
		add(listWithView, "push");
		add(new JButton(createSaveAction()), "alignx right");

		// Load data for term
		final Collection<DataEntry> entries = new ArrayList<DataEntry>();
		for( Map.Entry<String, String> data : termService.getAllData(taxonomy, ttn.getFullPath()).entrySet() )
		{
			final String key = data.getKey();
			final String displayName = predefinedTermDataKeys.containsKey(key) ? predefinedTermDataKeys.get(key)
				.getFirst() : key;

			DataEntry de = new DataEntry(key, displayName);
			de.setValue(data.getValue());
			entries.add(de);
		}
		listWithView.load(entries);

		// Watch for changes
		changeDetector.watch(termName);
		changeDetector.watch(listWithView);
	}

	@Override
	protected LazyTreeNode getUpdatedNode()
	{
		ttn.setName(termName.getText().trim());
		ttn.updateFullPath((TermTreeNode) ttn.getParent());
		return ttn;
	}

	@Override
	protected void save()
	{
		// Save the data
		Map<String, String> data = new HashMap<String, String>();
		for( DataEntry entry : listWithView.save() )
		{
			data.put(entry.getKey(), entry.getValue());
		}
		termService.setAllData(taxonomy, ttn.getFullPath(), data);

		// Possibly rename it
		String newName = termName.getText().trim();
		if( !newName.equals(ttn.getName()) )
		{
			termService.renameTermValue(taxonomy, ttn.getFullPath(), newName);
		}
	}

	private static class DataEditor extends JPanel implements ListWithViewInterface<DataEntry>
	{
		private JTextArea data;
		private String originalData;

		@Override
		public void addNameListener(KeyListener listener)
		{
			// Nothing to do here
		}

		@Override
		public Component getComponent()
		{
			return this;
		}

		@Override
		public void load(DataEntry entry)
		{
			data.setText(entry.getValue());
		}

		@Override
		public void save(DataEntry entry)
		{
			entry.setValue(data.getText());
		}

		@Override
		@SuppressWarnings("unchecked")
		public void setup()
		{
			data = new JTextArea();
			data.setLineWrap(true);
			data.setWrapStyleWord(true);
			Map<TextAttribute, Object> fas = (Map<TextAttribute, Object>) data.getFont().getAttributes();
			fas.put(TextAttribute.FAMILY, Font.MONOSPACED);
			data.setFont(new Font(fas));

			setLayout(new GridLayout(1, 1));
			add(new JScrollPane(data));
		}

		@Override
		public void clearChanges()
		{
			originalData = data.getText();
		}

		@Override
		public boolean hasDetectedChanges()
		{
			return !Check.bothNullOrDeepEqual(originalData, data.getText());
		}
	}

	@Override
	protected void validation() throws EditorException
	{
		// nothing to validate
	}
}
