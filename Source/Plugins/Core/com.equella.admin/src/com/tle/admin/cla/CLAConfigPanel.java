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

package com.tle.admin.cla;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dytech.edge.exceptions.BannedFileException;
import com.dytech.gui.TableLayout;
import com.dytech.gui.workers.GlassSwingWorker;
import com.tle.admin.Driver;
import com.tle.admin.gui.i18n.I18nTextArea;
import com.tle.admin.itemdefinition.AbstractExtensionConfigPanel;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.common.adminconsole.FileUploader;
import com.tle.common.adminconsole.RemoteAdminService;
import com.tle.common.applet.client.ClientService;
import com.tle.common.applet.gui.JGroup;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.LangUtils;
import com.tle.i18n.BundleCache;

public class CLAConfigPanel extends AbstractExtensionConfigPanel
{
	private static final long serialVersionUID = 1L;

	private static final Log LOGGER = LogFactory.getLog(CLAConfigPanel.class);

	private static final String FOLDER = "cla/"; //$NON-NLS-1$

	private static final String KEY_ACTIVATION_ERROR = "com.tle.cla-ActivationError"; //$NON-NLS-1$
	private static final String KEY_INACTIVE_ERROR = "com.tle.cla-InActiveError"; //$NON-NLS-1$
	private static final String KEY_HAS_AGREEMENT = "com.tle.cla-HasAgreement"; //$NON-NLS-1$
	private static final String KEY_AGREEMENT_FILENAME = "com.tle.cla-AgreementFile"; //$NON-NLS-1$

	private String stagingId;

	private final I18nTextArea activationError;
	private final I18nTextArea inactiveError;
	private JGroup agreementGroup;
	private JTextField agreementField;

	private RemoteAdminService adminService;

	public CLAConfigPanel()
	{
		JLabel actErrLabel = new JLabel(getString("activationerror")); //$NON-NLS-1$
		JLabel inactErrLabel = new JLabel(getString("inactiveerror")); //$NON-NLS-1$

		activationError = new I18nTextArea(BundleCache.getLanguages());
		inactiveError = new I18nTextArea(BundleCache.getLanguages());

		initUploadPanel();

		final int height1 = actErrLabel.getPreferredSize().height;
		final int height3 = agreementGroup.getPreferredSize().height;

		final int[] rows = {height1, TableLayout.FILL, 10, height1, TableLayout.FILL, 10, height3,};
		final int[] cols = {TableLayout.FILL,};

		setLayout(new TableLayout(rows, cols, 5, 5));
		add(actErrLabel, new Rectangle(0, 0, 1, 1));
		add(activationError, new Rectangle(0, 1, 1, 1));
		add(inactErrLabel, new Rectangle(0, 3, 1, 1));
		add(inactiveError, new Rectangle(0, 4, 1, 1));
		add(agreementGroup, new Rectangle(0, 6, 1, 1));
	}

	private void initUploadPanel()
	{
		JLabel titleLabel = new JLabel(getString("agreement.instructions")); //$NON-NLS-1$

		final JButton upload = new JButton(getString("agreement.upload")); //$NON-NLS-1$
		upload.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				uploadFile();
			}
		});

		final JButton remove = new JButton(getString("agreement.remove")); //$NON-NLS-1$
		remove.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				removeFile();
			}
		});

		agreementField = new JTextField();
		agreementField.setEditable(false);

		int rows[] = new int[]{TableLayout.PREFERRED, TableLayout.PREFERRED,};
		int cols[] = new int[]{TableLayout.FILL, upload.getPreferredSize().width, remove.getPreferredSize().width};

		JPanel uploadPanel = new JPanel(new TableLayout(rows, cols));

		uploadPanel.add(titleLabel, new Rectangle(0, 0, 3, 1));
		uploadPanel.add(agreementField, new Rectangle(0, 1, 1, 1));
		uploadPanel.add(upload, new Rectangle(1, 1, 1, 1));
		uploadPanel.add(remove, new Rectangle(2, 1, 1, 1));

		agreementGroup = new JGroup(getString("cla.agreement.requires")); //$NON-NLS-1$
		agreementGroup.setInnerLayout(new BorderLayout());
		agreementGroup.addInner(uploadPanel);
		agreementGroup.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				boolean isSelected = agreementGroup.isSelected();
				agreementField.setEnabled(isSelected);
				upload.setEnabled(isSelected);
				remove.setEnabled(isSelected);
				if( !isSelected )
				{
					removeFile();
				}
			}
		});
	}

	@Override
	public void setClientService(ClientService clientService)
	{
		super.setClientService(clientService);

		adminService = clientService.getService(RemoteAdminService.class);
	}

	@Override
	public void load(String stagingId, ItemDefinition itemdef)
	{
		this.stagingId = stagingId;

		Map<String, String> attributes = itemdef.getAttributes();

		activationError.load(LangUtils.getBundleFromXmlString(attributes.get(KEY_ACTIVATION_ERROR)));
		inactiveError.load(LangUtils.getBundleFromXmlString(attributes.get(KEY_INACTIVE_ERROR)));

		agreementGroup.setSelected(Boolean.valueOf(attributes.get(KEY_HAS_AGREEMENT)));
		agreementField.setText(attributes.get(KEY_AGREEMENT_FILENAME));
	}

	@Override
	public void save(ItemDefinition itemdef)
	{
		itemdef.setAttribute(KEY_ACTIVATION_ERROR, LangUtils.getBundleAsXmlString(activationError.save()));
		itemdef.setAttribute(KEY_INACTIVE_ERROR, LangUtils.getBundleAsXmlString(inactiveError.save()));
		itemdef.setAttribute(KEY_HAS_AGREEMENT, Boolean.toString(agreementGroup.isSelected()));
		itemdef.setAttribute(KEY_AGREEMENT_FILENAME, agreementField.getText());
	}

	void removeFile()
	{
		GlassSwingWorker<?> worker = new GlassSwingWorker<Object>()
		{
			@Override
			public Object construct() throws Exception
			{
				adminService.removeFile(stagingId, FOLDER);
				return null;
			}

			@Override
			public void finished()
			{
				agreementField.setText(""); //$NON-NLS-1$
			}

			@Override
			public void exception()
			{
				Exception ex = getException();
				Driver.displayError(getComponent(), "displayTemplate/readingXSLT", ex); //$NON-NLS-1$
				LOGGER.error("Error opening XSLT or clearing staging", ex);
			}
		};
		worker.setComponent(this);
		worker.start();
	}

	void uploadFile()
	{
		JFileChooser chooser = new JFileChooser();
		chooser.setFileFilter(new FileFilter()
		{
			@Override
			public String getDescription()
			{
				return getString("agreement.filefilter"); //$NON-NLS-1$
			}

			@Override
			public boolean accept(File f)
			{
				if( f.isDirectory() )
				{
					return true;
				}
				String name = f.toString().toLowerCase();
				return name.endsWith(".zip") || name.endsWith(".htm") || name.endsWith(".html") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					|| name.endsWith(".xsl") || name.endsWith(".xslt"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});

		final int returnVal = chooser.showOpenDialog(this);
		if( returnVal == JFileChooser.APPROVE_OPTION )
		{
			File selectedFile = chooser.getSelectedFile();
			if( selectedFile.getName().toLowerCase().endsWith(".zip") ) //$NON-NLS-1$
			{
				doZipUpload(selectedFile);
			}
			else
			{
				doSimpleUpload(selectedFile);
			}
		}
	}

	private void doZipUpload(final File file)
	{
		UploadWorker worker = new UploadWorker(file)
		{
			@Override
			protected String construct(InputStream in) throws Exception
			{
				String agreementName = null;

				ZipEntry entry;
				ZipInputStream zin = new ZipInputStream(in);
				while( (entry = zin.getNextEntry()) != null )
				{
					if( !entry.isDirectory() )
					{
						String name = FOLDER + entry.getName();
						if( name.endsWith(".htm") || name.endsWith(".html") //$NON-NLS-1$ //$NON-NLS-2$
							|| name.endsWith(".xsl") || name.endsWith(".xslt") ) //$NON-NLS-1$ //$NON-NLS-2$
						{
							agreementName = name;
						}

						FileUploader.upload(adminService, stagingId, name, zin);
					}
					zin.closeEntry();
				}

				return agreementName;
			}
		};
		worker.start();
	}

	private void doSimpleUpload(final File file)
	{
		UploadWorker worker = new UploadWorker(file)
		{
			@Override
			public String construct(InputStream in) throws Exception
			{
				String name = FOLDER + file.getName();
				FileUploader.upload(adminService, stagingId, name, in);
				return name;
			}
		};
		worker.start();
	}

	private abstract class UploadWorker extends GlassSwingWorker<String>
	{
		private final File file;

		public UploadWorker(File file)
		{
			this.file = file;

			setComponent(CLAConfigPanel.this);
		}

		protected abstract String construct(InputStream in) throws Exception;

		@Override
		public final String construct() throws Exception
		{
			try( BufferedInputStream in = new BufferedInputStream(new FileInputStream(file)) )
			{
				adminService.removeFile(stagingId, FOLDER);

				return construct(in);
			}
		}

		@Override
		public final void finished()
		{
			agreementField.setText(get());
		}

		@Override
		public final void exception()
		{
			Exception ex = getException();
			if( ex instanceof BannedFileException )
			{
				Driver.displayInformation(getComponent(), "File upload cancelled.  File extension has been banned.");
			}
			else if( ex instanceof FileNotFoundException )
			{
				Driver.displayError(getComponent(), "displayTemplate/readingXSLT", ex); //$NON-NLS-1$
				LOGGER.error("Error opening XSLT or clearing staging", ex);
			}
			else if( ex instanceof ZipException )
			{
				Driver.displayError(getComponent(), "displayTemplate/unzippingXSLT", ex); //$NON-NLS-1$
				LOGGER.error("Error unzippig XSLT", ex);
			}
			else
			{
				Driver.displayError(getComponent(), "displayTemplate/uploadingXSLT", ex); //$NON-NLS-1$
				LOGGER.error("Error uploading XSLT", ex);
			}
		}
	}
}
