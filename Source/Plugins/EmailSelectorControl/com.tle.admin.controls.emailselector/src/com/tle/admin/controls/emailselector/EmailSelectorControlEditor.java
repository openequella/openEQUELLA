package com.tle.admin.controls.emailselector;

import java.awt.Rectangle;
import java.util.Locale;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.dytech.edge.admin.wizard.WizardHelper;
import com.dytech.edge.admin.wizard.editor.AbstractControlEditor;
import com.dytech.edge.admin.wizard.model.Control;
import com.dytech.gui.TableLayout;
import com.tle.admin.gui.i18n.I18nTextField;
import com.tle.admin.schema.MultiTargetChooser;
import com.tle.admin.schema.SchemaModel;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.wizard.controls.emailselector.EmailSelectorControl;
import com.tle.i18n.BundleCache;

public class EmailSelectorControlEditor extends AbstractControlEditor<EmailSelectorControl>
{
	private static final long serialVersionUID = 1L;

	private final MultiTargetChooser picker;
	private I18nTextField title;
	private I18nTextField description;
	private JCheckBox mandatory;
	private JCheckBox selectMultiple;

	public EmailSelectorControlEditor(final Control control, final int wizardType, final SchemaModel schema)
	{
		super(control, wizardType, schema);
		setShowScripting(true);

		addSection(createDetailsSection());

		picker = WizardHelper.createMultiTargetChooser(this);
		addSection(WizardHelper.createMetaData(picker));
	}

	@Override
	protected void loadControl()
	{
		final EmailSelectorControl control = getWizardControl();

		title.load(control.getTitle());
		description.load(control.getDescription());
		mandatory.setSelected(control.isMandatory());
		selectMultiple.setSelected(control.isSelectMultiple());

		WizardHelper.loadSchemaChooser(picker, control);
	}

	@Override
	protected void saveControl()
	{
		final EmailSelectorControl control = getWizardControl();

		control.setTitle(title.save());
		control.setDescription(description.save());
		control.setMandatory(mandatory.isSelected());
		control.setSelectMultiple(selectMultiple.isSelected());

		WizardHelper.saveSchemaChooser(picker, control);
	}

	private JComponent createDetailsSection()
	{
		final JLabel titleLabel = new JLabel(CurrentLocale.get("wizard.controls.title")); //$NON-NLS-1$
		final JLabel descriptionLabel = new JLabel(CurrentLocale.get("wizard.controls.description")); //$NON-NLS-1$

		final Set<Locale> langs = BundleCache.getLanguages();
		title = new I18nTextField(langs);
		description = new I18nTextField(langs);
		mandatory = new JCheckBox(CurrentLocale.get("wizard.controls.mandatory")); //$NON-NLS-1$
		selectMultiple = new JCheckBox(getString("selectmultiple")); //$NON-NLS-1$

		final int height1 = title.getPreferredSize().height;
		final int width1 = descriptionLabel.getPreferredSize().width;

		final int[] rows = {height1, height1, height1, height1};
		final int[] cols = {width1, TableLayout.FILL, TableLayout.DOUBLE_FILL};
		final JPanel all = new JPanel(new TableLayout(rows, cols));

		int row = 0;
		all.add(titleLabel, new Rectangle(0, row, 1, 1));
		all.add(title, new Rectangle(1, row++, 2, 1));

		all.add(descriptionLabel, new Rectangle(0, row, 1, 1));
		all.add(description, new Rectangle(1, row++, 2, 1));

		all.add(mandatory, new Rectangle(0, row++, 3, 1));

		all.add(selectMultiple, new Rectangle(0, row++, 3, 1));

		return all;
	}

	protected String getString(String partKey)
	{
		return CurrentLocale.get("com.tle.admin.controls.emailselector." + partKey); //$NON-NLS-1$
	}
}
