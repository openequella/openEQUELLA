package com.tle.webtests.framework.setup;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.swing.table.AbstractTableModel;

import com.google.common.collect.Lists;
import com.google.common.io.Closeables;

@SuppressWarnings("serial")
public class InstitutionModel extends AbstractTableModel
{
	private static final int STATUS_COL = 5;
	private static final int UNLOCK_COL = 4;
	private static final int LOCK_COL = 3;
	private static final int EXPORT_COL = 2;
	private static final int SYNC_COL = 1;
	private static final int NAME_COL = 0;

	private final List<InstitutionData> institutionRows = new ArrayList<InstitutionData>();
	private ActionListener exportListener;

	public InstitutionModel(File rootFolder)
	{
		File[] insts = rootFolder.listFiles();
		List<File> instList = Lists.newArrayList(insts);
		Collections.sort(instList);
		for( File file : instList )
		{
			if( file.isDirectory() && !file.isHidden() )
			{
				try
				{
					InstitutionData inst = new InstitutionData(institutionRows.size());
					inst.setShortName(file.getName());
					inst.setFolder(file.getCanonicalFile());

					File propsFile = new File(file, "institution.properties");
					if( propsFile.exists() )
					{
						Properties props = new Properties();
						Reader rdr = new FileReader(propsFile);
						try
						{
							props.load(rdr);
							boolean https = Boolean.parseBoolean(props.getProperty("https", "false"));
							inst.setHttps(https);
						}
						finally
						{
							Closeables.closeQuietly(rdr);
						}
					}

					institutionRows.add(inst);
				}
				catch( IOException e )
				{
					e.printStackTrace();
					// skip it
				}
			}
		}
	}

	public void setActionListener(ActionListener listener)
	{
		this.exportListener = listener;
	}

	@Override
	public int getColumnCount()
	{
		return 6;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex)
	{
		switch( columnIndex )
		{
			case SYNC_COL:
			case EXPORT_COL:
			case LOCK_COL:
			case UNLOCK_COL:
				return true;
		}
		return false;
	}

	@Override
	public int getRowCount()
	{
		return institutionRows.size();
	}

	@Override
	public String getColumnName(int column)
	{
		switch( column )
		{
			case NAME_COL:
				return "Institution";
			case STATUS_COL:
				return "Status";
			case SYNC_COL:
			case EXPORT_COL:
				return "";
		}
		return "";
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		final InstitutionData instData = institutionRows.get(rowIndex);

		switch( columnIndex )
		{
			case NAME_COL:
				return instData.getShortName();
			case STATUS_COL:
				return instData.isLocked() ? "Locked by '" + instData.getUser() + "' on " + instData.getLockedDate()
					: "Not locked";
			case SYNC_COL:
				return "Import";
			case EXPORT_COL:
				return "Export";
			case UNLOCK_COL:
				return "Unlock";
			case LOCK_COL:
				return "Lock";
		}

		return null;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex)
	{
		InstitutionData instData = institutionRows.get(rowIndex);
		switch( columnIndex )
		{
			case SYNC_COL:
			case EXPORT_COL:
			case LOCK_COL:
			case UNLOCK_COL:
				exportListener.actionPerformed(new ActionEvent(instData, 0, (String) aValue));
				break;
		}
		fireTableRowsUpdated(rowIndex, rowIndex);
	}

	public static class InstitutionData
	{
		private static final String INSTITUTION_FILE = "institution.tar.gz";

		private int row;
		private String shortName;
		private File folder;
		private boolean selected;
		private boolean locked;
		private String user;
		private Date lockedDate;
		private boolean https;

		public InstitutionData(int row)
		{
			this.row = row;
		}

		public String getShortName()
		{
			return shortName;
		}

		public void setShortName(String name)
		{
			this.shortName = name;
		}

		public boolean isSelected()
		{
			return selected;
		}

		public void setSelected(boolean selected)
		{
			this.selected = selected;
		}

		public File getFolder()
		{
			return folder;
		}

		public void setFolder(File folder)
		{
			this.folder = folder;
		}

		public int getRow()
		{
			return row;
		}

		public void setRow(int row)
		{
			this.row = row;
		}

		public File getInstitutionFile()
		{
			return new File(folder, INSTITUTION_FILE);
		}

		public boolean isLocked()
		{
			return locked;
		}

		public void setLocked(boolean locked)
		{
			this.locked = locked;
		}

		public String getUser()
		{
			return user;
		}

		public void setUser(String user)
		{
			this.user = user;
		}

		public Date getLockedDate()
		{
			return lockedDate;
		}

		public void setLockedDate(Date lockedDate)
		{
			this.lockedDate = lockedDate;
		}

		public boolean isHttps()
		{
			return https;
		}

		public void setHttps(boolean https)
		{
			this.https = https;
		}
	}

	public List<InstitutionData> getInstitutionRows()
	{
		return institutionRows;
	}

	public void rowUpdated(InstitutionData inst)
	{
		fireTableRowsUpdated(inst.getRow(), inst.getRow());
	}

	public Runnable getRowChangeCallback(final InstitutionData inst)
	{
		return new Runnable()
		{
			@Override
			public void run()
			{
				rowUpdated(inst);
			}
		};
	}

}
