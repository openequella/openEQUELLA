package com.tle.reporting.oda.webservice;

import java.util.Properties;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.tle.reporting.oda.Connection;
import com.tle.reporting.oda.ui.TLEOdaPlugin;

public class WebserviceSelectionPageHelper
{
	private static final String EMPTY_URL = TLEOdaPlugin.getResourceString("error.emptyDatabaseUrl");
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$

	private WizardPage m_wizardPage;
	private PreferencePage m_propertyPage;

	private String DEFAULT_MESSAGE;

	private Text webserviceUrl, userName, password;
	private Button testButton;

	WebserviceSelectionPageHelper(WizardPage page)
	{
		DEFAULT_MESSAGE = TLEOdaPlugin.getResourceString("wizard.message.createDataSource");
		m_wizardPage = page;
	}

	WebserviceSelectionPageHelper(PreferencePage page)
	{
		DEFAULT_MESSAGE = TLEOdaPlugin.getResourceString("wizard.message.editDataSource");
		m_propertyPage = page;
	}

	void createCustomControl(Composite parent)
	{
		// create the composite to hold the widgets
		Composite content = new Composite(parent, SWT.NONE);

		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.verticalSpacing = 10;
		content.setLayout(layout);

		GridData gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = SWT.FILL;

		// initialize Database URL editor
		new Label(content, SWT.RIGHT).setText(TLEOdaPlugin.getResourceString("wizard.label.url"));

		webserviceUrl = new Text(content, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		webserviceUrl.setLayoutData(gridData);

		// User Name
		new Label(content, SWT.RIGHT).setText(TLEOdaPlugin.getResourceString("wizard.label.username"));
		userName = new Text(content, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = SWT.FILL;
		userName.setLayoutData(gridData);

		// Password
		new Label(content, SWT.RIGHT).setText(TLEOdaPlugin.getResourceString("wizard.label.password"));
		password = new Text(content, SWT.BORDER | SWT.PASSWORD);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = SWT.FILL;
		password.setLayoutData(gridData);

		// Test connection
		new Label(content, SWT.NONE);

		testButton = new Button(content, SWT.PUSH);
		testButton.setText(TLEOdaPlugin.getResourceString("wizard.label.testConnection"));
		testButton.setLayoutData(new GridData(GridData.CENTER));

		addControlListeners();
		updateTestButton();
		verifyJDBCProperties();
	}

	void initCustomControl(Properties profileProps)
	{
		if( profileProps == null || profileProps.isEmpty() )
		{
			return; // nothing to initialize
		}

		String odaUrl = profileProps.getProperty(Constants.WEBSERVICE_URL);
		if( odaUrl == null )
		{
			odaUrl = EMPTY_STRING;
		}
		webserviceUrl.setText(odaUrl);

		String odaUser = profileProps.getProperty(Constants.WEBSERVICE_USER);
		if( odaUser == null )
		{
			odaUser = EMPTY_STRING;
		}
		userName.setText(odaUser);

		String odaPassword = profileProps.getProperty(Constants.WEBSERVICE_PASSWORD);
		if( odaPassword == null )
		{
			odaPassword = EMPTY_STRING;
		}
		password.setText(odaPassword);

		updateTestButton();
		verifyJDBCProperties();
	}

	Properties collectCustomProperties(Properties props)
	{
		if( props == null )
		{
			props = new Properties();
		}

		// set custom driver specific properties
		props.setProperty(Constants.WEBSERVICE_URL, getWebserviceURL());
		props.setProperty(Constants.WEBSERVICE_USER, getWebserviceUser());
		props.setProperty(Constants.WEBSERVICE_PASSWORD, getWebservicePassword());
		return props;
	}

	private String getWebserviceUser()
	{
		if( userName == null )
		{
			return EMPTY_STRING;
		}
		return getTrimedString(userName.getText());
	}

	private String getWebservicePassword()
	{
		if( password == null )
		{
			return EMPTY_STRING;
		}
		return getTrimedString(password.getText());
	}

	private String getWebserviceURL()
	{
		if( webserviceUrl == null )
		{
			return EMPTY_STRING;
		}
		return getTrimedString(webserviceUrl.getText());
	}

	private String getTrimedString(String tobeTrimed)
	{
		if( tobeTrimed != null )
		{
			tobeTrimed = tobeTrimed.trim();
		}
		return tobeTrimed;
	}

	/**
	 * Adds event listeners
	 */
	private void addControlListeners()
	{
		webserviceUrl.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				if( !webserviceUrl.isFocusControl() && isEmpty(webserviceUrl.getText()) )
				{
					return;
				}
				verifyJDBCProperties();
				updateTestButton();
			}
		});

		testButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				testButton.setEnabled(false);
				if( testConnection() )
				{
					MessageDialog.openInformation(getShell(), TLEOdaPlugin.getResourceString("connection.test"),
						TLEOdaPlugin.getResourceString("connection.success"));
				}
				else
				{
					MessageDialog.openError(getShell(), TLEOdaPlugin.getResourceString("connection.test"),
						TLEOdaPlugin.getResourceString("connection.failed"));
				}
				testButton.setEnabled(true);
			}
		});
	}

	/**
	 * Attempts to connect to the Jdbc Data Source using the properties (
	 * username, password, driver class ) specified.
	 * 
	 * @param: showErrorMessage is set to true , and error dialog box will be
	 *         displayed if the connection fails.
	 * @return Returns true if the connection is OK,and false otherwise
	 */
	private boolean testConnection()
	{
		if( isValidDataSource() )
		{
			try
			{
				Properties props = collectCustomProperties(null);
				Connection conn = new Connection();
				conn.open(props);
				return true;
			}
			catch( Exception ex )
			{
				// IGNORE
			}
		}
		return false;

	}

	/**
	 * Validates the data source and updates the window message accordingly
	 * 
	 * @return
	 */
	private boolean isValidDataSource()
	{
		return !isURLBlank();
	}

	/**
	 * Test if the input URL is blank
	 * 
	 * @return true url is blank
	 */
	private boolean isURLBlank()
	{
		return isEmpty(webserviceUrl.getText());
	}

	/**
	 * This method should be called in the following occations: 1. The value of
	 * selected driver is changed 2. The value of inputed URL is changed 3. When
	 * the control is created 4.
	 */
	private void updateTestButton()
	{
		if( isURLBlank() )
		{
			// Jdbc Url cannot be blank
			setMessage(EMPTY_URL, IMessageProvider.ERROR);
			testButton.setEnabled(false);
		}
		else
		{
			setMessage(DEFAULT_MESSAGE);
			if( !testButton.isEnabled() )
			{
				testButton.setEnabled(true);
			}
		}
	}

	private void verifyJDBCProperties()
	{
		if( webserviceUrl != null )
		{
			setPageComplete(!isEmpty(webserviceUrl.getText()));
		}
	}

	private Shell getShell()
	{
		if( m_wizardPage != null )
		{
			return m_wizardPage.getShell();
		}
		else if( m_propertyPage != null )
		{
			return m_propertyPage.getShell();
		}
		else
		{
			return null;
		}
	}

	private void setPageComplete(boolean complete)
	{
		if( m_wizardPage != null )
		{
			m_wizardPage.setPageComplete(complete);
		}
		else if( m_propertyPage != null )
		{
			m_propertyPage.setValid(complete);
		}
	}

	private void setMessage(String message)
	{
		if( m_wizardPage != null )
		{
			m_wizardPage.setMessage(message);
		}
		else if( m_propertyPage != null )
		{
			m_propertyPage.setMessage(message);
		}
	}

	private void setMessage(String message, int type)
	{
		if( m_wizardPage != null )
		{
			m_wizardPage.setMessage(message, type);
		}
		else if( m_propertyPage != null )
		{
			m_propertyPage.setMessage(message, type);
		}
	}

	public void setDefaultMessage(String message)
	{
		this.DEFAULT_MESSAGE = message;
	}

	public static boolean isNull(Object o)
	{
		return o == null;
	}

	public static boolean isEmpty(String s)
	{
		return isNull(s) || s.trim().length() == 0;
	}
}
