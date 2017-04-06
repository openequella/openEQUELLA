/*
 * Created on Oct 26, 2004
 */
package com.dytech.edge.licence.client;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SpinnerNumberModel;

import com.dytech.edge.common.valuebean.License;
import com.dytech.edge.exceptions.LicenseException;
import com.dytech.gui.TableLayout;
import com.dytech.gui.calendar.CalendarDialog;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import com.tle.common.Check;

/**
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
public class OptionsWindow extends JPanel implements ActionListener
{
	private static final long serialVersionUID = 1L;

	public final DateFormat DISPLAY_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

	private static final int MAX_USERS_SPINNER = 0;
	private static final int WARNING_SPINNER = 1;
	private static final int MAX_INSTITUTION_SPINNER = 2;

	private static final int SPINNER_MIN = 0;
	private static final int SPINNER_DEFAULT = 1;
	private static final int SPINNER_MAX = 2;
	private static final int SPINNER_STEP = 3;
	private static final int[][] SPINNER_VALUES = {{1, 100, Integer.MAX_VALUE, 10}, {0, 14, 365, 1}, {1, 3, 100, 1},};

	private SpinnerNumberModel maxUsersModel;
	private SpinnerNumberModel institutionModel;
	private SpinnerNumberModel warningModel;
	private Date expiryDate;

	private JSpinner maxUsersSpinner;
	private JSpinner institutionSpinner;
	private JSpinner warningSpinner;
	private JCheckBox restrictToEquellaVersion;
	private JCheckBox restrictToHost;
	private JTextField hostField;
	private JTextField expiryField;
	private JTextField supportKey;
	private JCheckBox contentExchange;
	private JCheckBox pss;
	private JTextPane licenseText;
	private JButton expirySelect;
	private JButton expiryClear;
	private JComboBox equellaVersionField;
	private JButton generate;

	/**
	 * Constructs a new <code>OptionsWindow</code>
	 */
	public OptionsWindow()
	{
		createGUI();
	}

	private void createGUI()
	{
		// Presence or absence of ALLOW_FULL_SETTINGS is via the .launch file
		final boolean allowFullSettings = Boolean.getBoolean("ALLOW_FULL_SETTINGS");

		final JLabel maxUsersLabel = new JLabel("Maximum number of users");
		final JLabel institutionLabel = new JLabel("Maximum number of Institutions");
		final JLabel expiryLabel = new JLabel("Expire on the following");
		final JLabel warningLabel = new JLabel("Days warning before expiry");
		final JLabel hostLabel = new JLabel("Restrict to hostname(s)");
		final JLabel equellaVersionLabel = new JLabel("Restrict to EQUELLA version");
		final JLabel supportKeyLabel = new JLabel("Remote support key");
		final JLabel contentExchangeLabel = new JLabel("Enable content exchange");
		final JLabel pssLabel = new JLabel("Enable Pearson SCORM Services");

		maxUsersModel = createSpinnerModel(MAX_USERS_SPINNER);
		warningModel = createSpinnerModel(WARNING_SPINNER);
		institutionModel = createSpinnerModel(MAX_INSTITUTION_SPINNER);

		maxUsersSpinner = new JSpinner(maxUsersModel);
		warningSpinner = new JSpinner(warningModel);
		institutionSpinner = new JSpinner(institutionModel);

		restrictToHost = new JCheckBox();
		restrictToHost.setSelected(true);
		restrictToHost.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				boolean b = restrictToHost.isSelected();
				hostField.setEnabled(b);
				hostField.setEditable(b);
				if( !b )
				{
					hostField.setText(""); //$NON-NLS-1$
				}
			}
		});

		hostField = new JTextField();

		supportKey = new JTextField(UUID.randomUUID().toString().substring(0, 8));

		contentExchange = new JCheckBox();
		pss = new JCheckBox();

		expiryField = new JTextField();
		expiryField.setEditable(false);

		restrictToEquellaVersion = new JCheckBox();
		restrictToEquellaVersion.setSelected(true);
		restrictToEquellaVersion.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				equellaVersionField.setEnabled(restrictToEquellaVersion.isSelected());
				equellaVersionField.setEditable(restrictToEquellaVersion.isSelected());
			}
		});

		equellaVersionField = new JComboBox();
		// Old EQUELLA version numbers not even a developer could love
		// equellaVersionField.addItem("2.x Series");
		// equellaVersionField.addItem("3.0");
		// equellaVersionField.addItem("3.1");
		// equellaVersionField.addItem("3.2");
		// equellaVersionField.addItem("4.0");
		equellaVersionField.addItem("4.1");
		equellaVersionField.addItem("5.0");
		equellaVersionField.addItem("5.1");
		equellaVersionField.addItem("5.2");
		equellaVersionField.addItem("5.3");
		equellaVersionField.addItem("5.4");
		equellaVersionField.addItem("6.0");
		equellaVersionField.addItem("6.1");
		equellaVersionField.addItem("6.2");
		equellaVersionField.addItem("6.3");
		equellaVersionField.addItem("6.4");
		equellaVersionField.addItem("7.0");
		equellaVersionField.addItem("7.1");
		equellaVersionField.addItem("7.2");
		equellaVersionField.addItem("7.3");
		equellaVersionField.addItem("8.0");
		equellaVersionField.addItem("8.1");
		if( allowFullSettings )
		{
			equellaVersionField.addItem(License.DEVELOPMENT_BUILD);
		}

		equellaVersionField.setSelectedItem("6.0"); // flavour of the month

		expirySelect = new JButton("Select");
		expiryClear = new JButton("Clear");
		generate = new JButton("Generate");

		expirySelect.addActionListener(this);
		expiryClear.addActionListener(this);
		generate.addActionListener(this);

		licenseText = new JTextPane();

		final int height1 = hostField.getPreferredSize().height;
		final int width1 = pssLabel.getPreferredSize().width;
		final int width2 = restrictToHost.getPreferredSize().width;
		final int width3 = expirySelect.getPreferredSize().width;

		final int[] cols = {width1, width2, TableLayout.FILL, width3, width3,};
		final int[] rows = {height1, height1, height1, height1, height1, height1, height1, height1, height1};

		JPanel all = new JPanel(new TableLayout(rows, cols));
		all.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		all.add(expiryLabel, new Rectangle(0, 0, 1, 1));
		all.add(expiryField, new Rectangle(1, 0, 2, 1));
		all.add(expirySelect, new Rectangle(3, 0, 1, 1));
		all.add(expiryClear, new Rectangle(4, 0, 1, 1));
		all.add(warningLabel, new Rectangle(0, 1, 1, 1));
		all.add(warningSpinner, new Rectangle(1, 1, 2, 1));
		all.add(supportKeyLabel, new Rectangle(0, 2, 1, 1));
		all.add(supportKey, new Rectangle(1, 2, 2, 1));
		all.add(contentExchangeLabel, new Rectangle(0, 3, 1, 1));
		all.add(contentExchange, new Rectangle(1, 3, 2, 1));
		all.add(pssLabel, new Rectangle(0, 4, 1, 1));
		all.add(pss, new Rectangle(1, 4, 2, 1));
		all.add(maxUsersLabel, new Rectangle(0, 5, 1, 1));
		all.add(maxUsersSpinner, new Rectangle(1, 5, 2, 1));
		all.add(institutionLabel, new Rectangle(0, 6, 1, 1));
		all.add(institutionSpinner, new Rectangle(1, 6, 2, 1));
		all.add(equellaVersionLabel, new Rectangle(0, 7, 1, 1));
		all.add(restrictToEquellaVersion, new Rectangle(1, 7, 1, 1));
		all.add(equellaVersionField, new Rectangle(2, 7, 3, 1));
		all.add(hostLabel, new Rectangle(0, 8, 1, 1));
		all.add(restrictToHost, new Rectangle(1, 8, 1, 1));
		all.add(hostField, new Rectangle(2, 8, 3, 1));

		add(all);
		add(generate);
		add(new JScrollPane(licenseText));
		generate.setAlignmentX(Component.CENTER_ALIGNMENT);
		all.setAlignmentX(Component.CENTER_ALIGNMENT);
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		Calendar now = Calendar.getInstance();
		now.add(Calendar.DAY_OF_YEAR, allowFullSettings ? 30 : 5);
		expiryDate = now.getTime();
		update();

		if( !allowFullSettings )
		{
			expiryField.setEnabled(false);
			expirySelect.setEnabled(false);
			expiryClear.setEnabled(false);
			warningSpinner.setEnabled(false);
			supportKey.setEnabled(false);
			restrictToEquellaVersion.setEnabled(false);
			restrictToHost.setEnabled(false);
		}
	}

	/**
	 * Creates a new spinner model for the given type.
	 */
	private SpinnerNumberModel createSpinnerModel(int type)
	{
		int value = SPINNER_VALUES[type][SPINNER_DEFAULT];
		int min = SPINNER_VALUES[type][SPINNER_MIN];
		int max = SPINNER_VALUES[type][SPINNER_MAX];
		int step = SPINNER_VALUES[type][SPINNER_STEP];

		return new SpinnerNumberModel(value, min, max, step);
	}

	/**
	 * Makes sure the GUI is in a consistent state for the current values.
	 */
	private void update()
	{
		if( expiryDate == null )
		{
			expiryField.setText(""); //$NON-NLS-1$
			warningSpinner.setEnabled(false);
		}
		else
		{
			expiryField.setText(DISPLAY_FORMAT.format(expiryDate));
			warningSpinner.setEnabled(true);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if( e.getSource() == expirySelect )
		{
			expiryDate = CalendarDialog.showCalendarDialog(this, "Select an expiry date", expiryDate);
		}
		else if( e.getSource() == expiryClear )
		{
			expiryDate = null;
		}
		else if( e.getSource() == generate )
		{
			if( restrictToHost.isSelected() )
			{
				String hosts = hostField.getText();
				if( hosts.trim().length() == 0 )
				{
					JOptionPane.showMessageDialog(this,
						"Enter a hostname, or a comma separated list of hostnames for 4.0 onwards.");
					return;
				}
				else if( hosts.contains("_") )
				{
					JOptionPane.showMessageDialog(this, "Hostnames cannot contain underscores.");
					return;
				}
			}
			else
			{
				if( JOptionPane.showConfirmDialog(this,
					"This license will *not* be restricted to a hostname.  Are you sure?") != JOptionPane.YES_OPTION )
				{
					return;
				}
			}

			if( restrictToEquellaVersion.isSelected() )
			{
				Object selectedObj = equellaVersionField.getSelectedItem();
				if( selectedObj == null || selectedObj.toString().trim().length() == 0 )
				{
					if( JOptionPane.showConfirmDialog(this,
						"This license will be ineffective for every EQUELLA 5.2 and above.  Are you sure?") != JOptionPane.YES_OPTION )
					{
						return;
					}
				}
				else if( selectedObj.toString().trim().startsWith(License.DEVELOPMENT_BUILD) )
				{
					if( JOptionPane.showConfirmDialog(this,
						"This license will override any EQUELLA version restriction.  Are you sure?") != JOptionPane.YES_OPTION )
					{
						return;
					}
				}
				else
				{
					boolean edited = true;
					for( int i = 0; i < equellaVersionField.getItemCount(); ++i )
					{
						if( selectedObj.equals(equellaVersionField.getItemAt(i)) )
						{
							edited = false;
							break;
						}
					}
					if( edited
						&& JOptionPane.showConfirmDialog(this,
							"You have entered a manually edited EQUELLA version.  Are you sure?") != JOptionPane.YES_OPTION )
					{
						return;
					}
				}
			}
			else
			{
				if( JOptionPane.showConfirmDialog(this,
					"This license will override any EQUELLA version restriction.  Are you sure?") != JOptionPane.YES_OPTION )
				{
					return;
				}
			}
			generateKey();
		}
		update();
	}

	private void generateKey()
	{
		License license = new License();
		license.setExpiry(expiryDate);
		Calendar cal = Calendar.getInstance();
		cal.setTime(expiryDate);
		int daysbefore = ((Integer) warningSpinner.getValue()).intValue();
		cal.add(Calendar.DAY_OF_YEAR, -daysbefore);
		license.setWarning(cal.getTime());
		license.setFreeHostname(!restrictToHost.isSelected());
		license.setHostnames(Sets.newHashSet(Splitter.on(',').split(hostField.getText().replaceAll("\\s", ""))));
		license.setUsers(((Integer) maxUsersSpinner.getValue()).intValue());
		license.setInstitutions(((Integer) institutionSpinner.getValue()).intValue());
		// Explicit deselection of restrict-to-version means we have the
		// opens-all-doors
		// development licence. If the user wants (eg, for test purposes) to
		// specify an
		// empty licence version, they need to select restrictToEquellaVersion,
		// and save
		// with a (manually edited) empty string in the combo box)
		license.setVersion(restrictToEquellaVersion.isSelected() ? equellaVersionField.getSelectedItem().toString()
			: License.DEVELOPMENT_BUILD);
		license.setSupportKey(supportKey.getText());

		Set<String> features = new HashSet<String>();
		if( contentExchange.isSelected() )
		{
			features.add("contentExchange");
		}

		if( pss.isSelected() )
		{
			features.add("PearsonSCORMServices");
		}
		license.setFeatures(features);

		try
		{
			String licensebase64 = license.encrypt();

			StringBuilder text = new StringBuilder();
			text.append("\nEQUELLA version: ");
			if( !Check.isEmpty(license.getVersion()) )
			{
				text.append(license.getVersion());
			}
			else
			{
				text.append("<no restrictions>");
			}
			text.append("\nHostname(s): ");
			if( license.isFreeHostname() )
			{
				text.append("<no restrictions>");
			}
			else
			{
				text.append(license.getJoinedHostnames());
			}
			text.append("\nMax Users: ");
			text.append(license.getUsers());
			text.append("\nMax Institutions: ");
			text.append(license.getInstitutions());
			text.append("\nExpires: ");
			text.append(license.getExpiry());
			text.append("\nWarning: ");
			text.append(license.getWarning());
			text.append("\nSupport Key: ");
			text.append(license.getSupportKey());
			text.append("\nFeatures: ");
			text.append(license.getJoinedFeatures());
			text.append("\n\n");
			text.append(licensebase64);

			licenseText.setText(text.toString());
		}
		catch( LicenseException e )
		{
			e.printStackTrace();
		}
	}
}
