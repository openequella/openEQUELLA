package com.dytech.edge.admin.wizard.editor;

import java.awt.Rectangle;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.dytech.edge.admin.wizard.model.Control;
import com.dytech.edge.admin.wizard.model.PageModel;
import com.dytech.gui.TableLayout;
import com.tle.admin.schema.SchemaModel;
import com.tle.common.i18n.CurrentLocale;

public class DefaultWizardPageEditor extends ContributionPageEditor
{
	private static final long serialVersionUID = 1L;
	private JTextField additionalCssClass;

	public DefaultWizardPageEditor(Control control, int wizardType, SchemaModel model)
	{
		super(control, wizardType, model);
		setup();
	}

	@Override
	protected void loadControl()
	{
		super.loadControl();
		PageModel control = (PageModel) getControl();
		additionalCssClass.setText(control.getAdditionalCssClass());
	}

	@Override
	protected void saveControl()
	{
		super.saveControl();
		PageModel control = (PageModel) getControl();
		control.setAdditionalCssClass(additionalCssClass.getText());
	}

	private void setup()
	{

		JLabel cssLabel = new JLabel(CurrentLocale.get("com.tle.admin.controls.standard.page.additionalcssclass.label"));
		additionalCssClass = new JTextField();
		JLabel cssHelp = new JLabel(CurrentLocale.get("com.tle.admin.controls.standard.page.additionalcssclass.help"));

		final int height1 = additionalCssClass.getPreferredSize().height;
		final int width1 = cssLabel.getPreferredSize().width;

		final int height2 = cssHelp.getPreferredSize().height;

		final int[] rows = {height1, height2};
		final int[] cols = {width1, TableLayout.FILL,};

		JPanel all = new JPanel(new TableLayout(rows, cols, 5, 5));
		cssHelp.setFont(all.getFont());

		all.add(cssLabel, new Rectangle(0, 0, 1, 1));
		all.add(additionalCssClass, new Rectangle(1, 0, 1, 1));
		all.add(cssHelp, new Rectangle(0, 1, 2, 1));
		addSection(all);
	}
}
