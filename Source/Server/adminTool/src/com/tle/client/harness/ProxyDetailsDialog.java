package com.tle.client.harness;

import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import com.dytech.gui.ComponentHelper;
import com.dytech.gui.TableLayout;
import com.tle.common.applet.gui.JGroup;

public class ProxyDetailsDialog extends JDialog implements ActionListener, KeyListener
{
	private static final long serialVersionUID = 1L;
	public static final int RESULT_CANCEL = 0;
	public static final int RESULT_OK = 1;

	private static final int WINDOW_WIDTH = 300;

	private static final int PROXY_MIN = 1;
	private static final int PROXY_DEFAULT = 8080;
	private static final int PROXY_MAX = Short.MAX_VALUE - 1;
	private static final int PROXY_STEP = 1;

	private int result = RESULT_CANCEL;

	private JGroup overall;
	private JTextField hostField;
	private SpinnerNumberModel portModel;

	private JGroup credentials;
	private JTextField usernameField;
	private JPasswordField passwordField;

	private JButton ok;
	private JButton cancel;

	public ProxyDetailsDialog(Frame frame)
	{
		super(frame);
		setup();
	}

	private void setup()
	{
		setupOverall();

		ok = new JButton("OK");
		cancel = new JButton("Cancel");

		ok.addActionListener(this);
		cancel.addActionListener(this);

		final int width1 = cancel.getPreferredSize().width;
		final int height1 = overall.getPreferredSize().height;
		final int height2 = cancel.getPreferredSize().height;
		final int[] rows = {height1, height2,};
		final int[] cols = {WINDOW_WIDTH - (width1 * 2), width1, width1,};

		JPanel all = new JPanel(new TableLayout(rows, cols));
		all.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		all.add(overall, new Rectangle(0, 0, 3, 1));
		all.add(ok, new Rectangle(1, 1, 1, 1));
		all.add(cancel, new Rectangle(2, 1, 1, 1));

		updateButtons();

		setTitle("Proxy Settings");
		setModal(true);
		setResizable(false);
		getContentPane().add(all);
		getRootPane().setDefaultButton(ok);

		pack();
		ComponentHelper.centreOnScreen(this);
	}

	private void setupOverall()
	{
		setupCredentials();

		JLabel hostLabel = new JLabel("Proxy Host:");
		JLabel portLabel = new JLabel("Proxy Port:");

		hostField = new JTextField();
		hostField.addKeyListener(this);

		portModel = new SpinnerNumberModel(PROXY_DEFAULT, PROXY_MIN, PROXY_MAX, PROXY_STEP);
		JSpinner portSpinner = new JSpinner(portModel);

		final int width1 = hostLabel.getPreferredSize().width;
		final int width2 = portSpinner.getPreferredSize().width;
		final int height1 = hostField.getPreferredSize().height;
		final int height2 = credentials.getPreferredSize().height;
		final int[] rows = {height1, height1, height2,};
		final int[] cols = {width1, width2, TableLayout.FILL,};

		overall = new JGroup("Enable Proxy", false);
		overall.addActionListener(this);
		overall.setInnerLayout(new TableLayout(rows, cols));

		overall.addInner(hostLabel, new Rectangle(0, 0, 1, 1));
		overall.addInner(hostField, new Rectangle(1, 0, 2, 1));
		overall.addInner(portLabel, new Rectangle(0, 1, 1, 1));
		overall.addInner(portSpinner, new Rectangle(1, 1, 1, 1));
		overall.addInner(credentials, new Rectangle(0, 2, 3, 1));

		overall.setSelected(false);
	}

	private void setupCredentials()
	{
		JLabel usernameLabel = new JLabel("Username:");
		JLabel passwordLabel = new JLabel("Password:");

		usernameField = new JTextField();
		passwordField = new JPasswordField();

		usernameField.addKeyListener(this);
		passwordField.addKeyListener(this);

		final int width1 = usernameLabel.getPreferredSize().width;
		final int width2 = passwordLabel.getPreferredSize().width;
		final int width3 = Math.max(width1, width2);
		final int height1 = usernameField.getPreferredSize().height;

		final int[] rows = {height1, height1};
		final int[] cols = {width3, TableLayout.FILL};

		credentials = new JGroup("Server requires authentication", false);
		credentials.addActionListener(this);
		credentials.setInnerLayout(new TableLayout(rows, cols));

		credentials.addInner(usernameLabel, new Rectangle(0, 0, 1, 1));
		credentials.addInner(passwordLabel, new Rectangle(0, 1, 1, 1));
		credentials.addInner(usernameField, new Rectangle(1, 0, 1, 1));
		credentials.addInner(passwordField, new Rectangle(1, 1, 1, 1));

		credentials.setSelected(false);
	}

	public void loadConfiguration(ProxySettings settings)
	{
		if( settings != null )
		{
			overall.setSelected(true);
			hostField.setText(settings.getHost());
			portModel.setValue(settings.getPort());

			String username = settings.getUsername();
			if( username != null && username.length() > 0 )
			{
				credentials.setSelected(true);
				usernameField.setText(username);
				passwordField.setText(settings.getPassword());
			}
		}
		updateButtons();
	}

	public ProxySettings saveConfiguration()
	{
		ProxySettings settings = null;
		if( overall.isSelected() )
		{
			settings = new ProxySettings();

			settings.setHost(hostField.getText());
			settings.setPort(portModel.getNumber().intValue());

			if( credentials.isSelected() )
			{
				settings.setUsername(usernameField.getText());
				settings.setPassword(new String(passwordField.getPassword()));
			}
		}
		return settings;
	}

	public int getResult()
	{
		return result;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if( e.getSource() == ok )
		{
			result = RESULT_OK;
			dispose();
		}
		else if( e.getSource() == cancel )
		{
			dispose();
		}
		else
		{
			updateButtons();
		}
	}

	private void updateButtons()
	{
		boolean enabled = overall.isSelected();
		boolean hostEmpty = hostField.getText().length() == 0;
		boolean credsRequired = credentials.isSelected();
		boolean usernameEmpty = usernameField.getText().length() == 0;
		boolean passwordEmpty = passwordField.getPassword().length == 0;
		boolean credsEmpty = usernameEmpty || passwordEmpty;

		ok.setEnabled(!(enabled && (hostEmpty || (credsRequired && credsEmpty))));
	}

	@Override
	public void keyReleased(KeyEvent e)
	{
		updateButtons();
	}

	@Override
	public void keyPressed(KeyEvent e)
	{
		// We do not want to listen to this event
	}

	@Override
	public void keyTyped(KeyEvent e)
	{
		// We do not want to listen to this event
	}
}
