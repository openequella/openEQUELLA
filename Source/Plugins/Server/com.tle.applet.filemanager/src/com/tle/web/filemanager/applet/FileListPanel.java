package com.tle.web.filemanager.applet;

import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionListener;

import net.miginfocom.swing.MigLayout;

import com.tle.admin.gui.common.actions.TLEAction;
import com.tle.client.gui.popup.ListDoubleClickListener;
import com.tle.client.gui.popup.ListPopupListener;
import com.tle.common.Check;
import com.tle.common.FileSizeUtils;
import com.tle.common.gui.models.GenericListModel;
import com.tle.common.i18n.CurrentLocale;
import com.tle.web.appletcommon.dnd.DnDUtils;
import com.tle.web.appletcommon.dnd.DropHandler;
import com.tle.web.appletcommon.dnd.HoverHandler;
import com.tle.web.appletcommon.gui.GlassProgressWorker;
import com.tle.web.filemanager.applet.actions.MarkAsResourceAction;
import com.tle.web.filemanager.applet.backend.Backend;
import com.tle.web.filemanager.applet.backend.BackendEvent;
import com.tle.web.filemanager.applet.backend.BackendListener;
import com.tle.web.filemanager.applet.dragactions.FileInfoTransferable;
import com.tle.web.filemanager.applet.gui.AddressBarButton;
import com.tle.web.filemanager.applet.gui.SystemIconCache;
import com.tle.web.filemanager.common.FileInfo;

@SuppressWarnings("nls")
public class FileListPanel extends JPanel
{
	private static final long serialVersionUID = 1L;

	private static final int MAX_CELL_WIDTH = 200;

	private final Backend backend;
	private final JList list;
	private final JScrollPane listScroller;
	private final GenericListModel<FileInfo> model;
	private final Renderer renderer;
	private final SyncChangesBar syncBar;

	private final AddressBarButton upFolderButton;
	private final AddressBarButton rootFolderButton;
	private final JPanel addressBar;

	private FileInfo currentDirectory;

	private final List<DropHandler> dropHandlers = new ArrayList<DropHandler>();

	public FileListPanel(Backend backend)
	{
		this.backend = backend;
		this.backend.addListener(new BackendListener()
		{
			@Override
			public void fileAdded(BackendEvent event)
			{
				setCwdForFilePane(currentDirectory);
				if( event.getInfo().isDirectory() )
				{
					setCwdForBreadcrumb(currentDirectory);
				}
			}

			@Override
			public void fileDeleted(BackendEvent event)
			{
				model.remove(event.getInfo());
				setCwdForBreadcrumb(currentDirectory);
			}

			@Override
			public void fileMoved(BackendEvent event)
			{
				setCwdForFilePane(currentDirectory);
			}

			@Override
			public void fileCopied(BackendEvent event)
			{
				setCwdForFilePane(currentDirectory);
			}

			@Override
			public void fileMarkedAsResource(BackendEvent event)
			{
				setCwdForFilePane(currentDirectory);
			}

			@Override
			public void localFilesEdited(BackendEvent event)
			{
				syncBar.refresh();
				setCwdForFilePane(currentDirectory);
			}

			@Override
			public void extractArchive(BackendEvent event)
			{
				setCwdForFilePane(currentDirectory);
			}
		});

		addressBar = new JPanel(new MigLayout());
		addressBar.setBorder(null);

		// Strictly speaking, we aren't really reading an uninitialised field in
		// the this.constructor, it just looks that way ...
		upFolderButton = new AddressBarButton(CurrentLocale.get("updir"), "back.gif", currentDirectory) // NOSONAR
		{
			@Override
			public void onClick()
			{
				FileInfo parent = currentDirectory != null ? currentDirectory.getParentFileInfo() : null;
				if( parent == null )
				{
					parent = new FileInfo();
					parent.setDirectory(true);
				}
				setCwd(parent);
			}
		};
		upFolderButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("BACK_SPACE"),
			"clicked");
		upFolderButton.getActionMap().put("clicked", new AbstractAction()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				upFolderButton.onClick();
			}
		});

		rootFolderButton = new AddressBarButton(CurrentLocale.get("rootpath"), "home.gif", currentDirectory)
		{
			@Override
			public void onClick()
			{
				FileInfo root = new FileInfo();
				root.setDirectory(true);
				setCwd(root);
			}
		};

		syncBar = new SyncChangesBar(this, backend);

		model = new GenericListModel<FileInfo>();

		renderer = new Renderer();

		list = new JList(model);

		list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		list.setCellRenderer(renderer);
		list.setDropTarget(null);
		list.setBackground(Color.WHITE);

		listScroller = new JScrollPane(list);
		listScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		listScroller.addComponentListener(new ComponentAdapter()
		{
			@Override
			public void componentResized(ComponentEvent e)
			{
				updateListColumns();
			}
		});

		setLayout(new MigLayout());

		addressBar.add(upFolderButton);
		addressBar.add(rootFolderButton);
		add(addressBar, "width 100%, growx,  wrap");
		add(syncBar, "spanx 3, hidemode 1, wrap");
		add(listScroller, "spanx 3, height 100%, width 100%");

		if( CurrentLocale.isRightToLeft() )
		{
			applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		}

		registerDndHandlers();
	}

	public void registerDropHandler(DropHandler handler)
	{
		dropHandlers.add(handler);
		Collections.sort(dropHandlers, new Comparator<DropHandler>()
		{
			@Override
			public int compare(DropHandler o1, DropHandler o2)
			{
				return o2.getDropHandlerPriority() - o1.getDropHandlerPriority();
			}
		});
	}

	public FileInfo getCurrentDirectory()
	{
		return currentDirectory;
	}

	private int getListColumnCount()
	{
		Dimension listDims = listScroller.getSize();
		Dimension cellDims = renderer.getMaximumDimensions();
		// surely never 0 ? But to be safe ...
		return cellDims.width != 0 ? (listDims.width / cellDims.width) : 0;
	}

	private int getListRowCount()
	{
		int cols = getListColumnCount();
		return cols == 0 ? model.size() : (int) Math.ceil(((double) model.size() / cols));
	}

	private void updateListColumns()
	{
		list.setVisibleRowCount(getListRowCount());
	}

	public void addListSelectionListener(ListSelectionListener l)
	{
		list.addListSelectionListener(l);
	}

	public void addMouseSelectionListener(MouseListener l)
	{
		list.addMouseListener(l);
	}

	public int getSelectionCount()
	{
		return list.getSelectedIndices().length;
	}

	public FileInfo getSelectedFile()
	{
		int i = list.getSelectedIndex();
		return i >= 0 ? model.get(list.getSelectedIndex()) : null;
	}

	private int getListIndexUnderPoint(Point point)
	{
		if( point != null )
		{
			// NOTE: list.locationToIndex is useless. It does not take into
			// account dragging onto the list but past the last index, and
			// assumes you mean the last index.
			int index = list.locationToIndex(point);
			Rectangle cellBounds = list.getCellBounds(index, index);

			if( index >= 0 && cellBounds.contains(point) )
			{
				return index;
			}
		}
		return -1;
	}

	private int getListIndexUnderMouseCursor()
	{
		return getListIndexUnderPoint(list.getMousePosition());
	}

	public FileInfo getFileUnderMouseCursor()
	{
		int index = getListIndexUnderMouseCursor();
		return index >= 0 ? model.get(index) : null;
	}

	public void showPopup(JPopupMenu menu, FileInfo overFile)
	{
		Point p = list.indexToLocation(model.indexOf(overFile));
		menu.show(list, p.x + 10, p.y + 10);
	}

	public List<FileInfo> getSelectedFiles()
	{
		List<FileInfo> results = new ArrayList<FileInfo>();
		for( int index : list.getSelectedIndices() )
		{
			results.add(model.get(index));
		}
		return results;
	}

	public List<FileInfo> getCurrentDirectoryFiles()
	{
		return Collections.unmodifiableList(model);
	}

	public void addPopupActions(List<TLEAction> actions)
	{
		list.addMouseListener(new ListPopupListener(list, actions));
	}

	public void addDoubleClickAction(TLEAction action)
	{
		ListDoubleClickListener ldcl = new ListDoubleClickListener(list, action);
		ldcl.setCheckActionEnabled(false);
		list.addMouseListener(ldcl);
	}

	public void setCwd(final FileInfo directory)
	{
		if( directory.isDirectory() )
		{
			setCwdForBreadcrumb(directory);
			setCwdForFilePane(directory);
		}
		else
		{
			throw new RuntimeException("Cannot change current directory to a file!");
		}
	}

	private void setCwdForBreadcrumb(FileInfo directory)
	{
		if( directory == null || directory.isRoot() )
		{
			upFolderButton.setEnabled(false);
		}
		else
		{
			upFolderButton.setEnabled(true);
		}

		List<AddressBarButton> abl = new ArrayList<AddressBarButton>();
		while( directory != null )
		{
			String name = directory.getName();
			String folderName = "folder.ltr.gif";
			if( !Check.isEmpty(name) )
			{
				if( CurrentLocale.isRightToLeft() )
				{
					folderName = "folder.rtl.gif";
				}

				AddressBarButton ab = new AddressBarButton(name, folderName, directory)
				{

					@Override
					public void onClick()
					{
						setCwd(getButtonFileInfo());

					}
				};
				abl.add(ab);

			}

			directory = directory.getParentFileInfo();

		}

		addressBar.removeAll();

		addressBar.add(upFolderButton);
		addressBar.add(rootFolderButton);
		for( int x = abl.size() - 1; x >= 0; x-- )
		{
			addressBar.add(abl.get(x));
		}
		if( CurrentLocale.isRightToLeft() )
		{
			addressBar.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		}

	}

	private void setCwdForFilePane(final FileInfo directory)
	{
		GlassProgressWorker<?> worker = new GlassProgressWorker<List<FileInfo>>("Loading...", -1, false)
		{
			@Override
			public List<FileInfo> construct() throws Exception
			{
				return backend.listFiles(directory.getFullPath());
			}

			@Override
			public void finished()
			{
				model.clear();
				model.addAll(get());
				updateListColumns();
			}

			@Override
			public void exception()
			{
				// TODO: Pretty this up
				getException().printStackTrace();
			}
		};
		worker.setComponent(this);
		worker.start();

		currentDirectory = directory;
	}

	private static class Renderer implements ListCellRenderer
	{
		private final Color LIGHTER_GRAY = new Color(120, 120, 120);

		private final DateFormat dateFormat;

		private final JPanel base;
		private final IconLabel icon;
		private final JLabel filename;
		private final JLabel size;
		private final JLabel modified;
		private final JLabel sizeLabel;
		private final JLabel modifiedLabel;

		public Renderer()
		{
			dateFormat = new SimpleDateFormat();

			icon = new IconLabel();

			filename = new JLabel(" ");
			// filename.setVerticalAlignment(SwingConstants.CENTER);

			Font f = filename.getFont();
			filename.setFont(f.deriveFont((float) (f.getSize() + 2)));

			sizeLabel = new JLabel(CurrentLocale.get("cell.size"));
			size = new JLabel();
			modifiedLabel = new JLabel(CurrentLocale.get("cell.modified"));
			modified = new JLabel();

			Dimension modPrefSize = modifiedLabel.getPreferredSize();
			final int width1 = 40;
			final int width2 = Math.max(sizeLabel.getPreferredSize().width, modPrefSize.width) + 5;
			final int height1 = filename.getPreferredSize().height;
			final int height2 = modPrefSize.height;

			MigLayout layout = new MigLayout("", "[" + width1 + "][" + width2 + "][200 - " + (width2 + width1) + "]",
				"[" + height1 + "]2[" + height2 + "]2[" + height2 + "]");

			base = new JPanel(layout);
			base.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			base.setBackground(Color.WHITE);

			base.add(icon, "cell 0 0, spany 3");
			base.add(filename, "cell 1 0, spanx 2");
			base.add(sizeLabel, "cell 1 1");
			base.add(size, "cell 2 1");
			base.add(modifiedLabel, "cell 1 2");
			base.add(modified, "cell 2 2");
			int prefHeight = height1 + (height2 * 2) + 24; // size of the 3 rows
															// plus a little
															// padding

			base.setPreferredSize(new Dimension(MAX_CELL_WIDTH, prefHeight));
			base.setMaximumSize(new Dimension(MAX_CELL_WIDTH, prefHeight));
			base.setMinimumSize(new Dimension(MAX_CELL_WIDTH, prefHeight));

			if( CurrentLocale.isRightToLeft() )
			{
				base.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

			}
		}

		public Dimension getMaximumDimensions()
		{
			return base.getMaximumSize();
		}

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
			boolean cellHasFocus)
		{
			FileInfo info = (FileInfo) value;

			icon.setIcon(SystemIconCache.getIcon(info, true));
			icon.setMarkAsResource(info.isMarkAsAttachment());

			base.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());

			filename.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());

			Color fg = isSelected ? list.getSelectionForeground() : LIGHTER_GRAY;
			size.setForeground(fg);
			modified.setForeground(fg);
			sizeLabel.setForeground(fg);
			modifiedLabel.setForeground(fg);

			boolean isFile = !info.isDirectory();
			size.setVisible(isFile);
			modified.setVisible(isFile);
			sizeLabel.setVisible(isFile);
			modifiedLabel.setVisible(isFile);

			base.remove(filename);

			int height = isFile ? 1 : 3;
			base.add(filename, "cell 1 0, spanx 2, spany " + height);
			filename.setText(info.getName());
			size.setText(FileSizeUtils.humanReadableFileSize(info.getSize()));
			modified.setText(dateFormat.format(new Date(info.getLastModified())));
			if( CurrentLocale.isRightToLeft() )
			{
				base.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
			}

			return base;
		}
	}

	private static class IconLabel extends JLabel
	{
		private static final long serialVersionUID = 1L;

		private final ImageIcon icon;

		private boolean markAsResource;

		public IconLabel()
		{
			icon = new ImageIcon(MarkAsResourceAction.class.getResource(MarkAsResourceAction.ICON_PATH));
		}

		public void setMarkAsResource(boolean markAsResource)
		{
			this.markAsResource = markAsResource;
		}

		@Override
		public void paint(Graphics g)
		{
			super.paint(g);
			if( markAsResource )
			{
				icon.paintIcon(this, g, 0, 0);
			}
		}
	}

	private void registerDndHandlers()
	{
		new DragSource().createDefaultDragGestureRecognizer(list, DnDConstants.ACTION_COPY, new DragGestureListener()
		{
			@Override
			public void dragGestureRecognized(DragGestureEvent dge)
			{
				Transferable t = new FileInfoTransferable(getSelectedFile());
				dge.startDrag(DragSource.DefaultMoveDrop, t);
			}
		});

		DnDUtils.registerDropHandler(this, dropHandlers, new HoverHandler()
		{
			@Override
			public void hovering(DropTargetDragEvent e)
			{
				FileInfo fi = getFileUnderMouseCursor();
				if( fi == null )
				{
					int i = getListIndexUnderPoint(e.getLocation());
					if( i >= 0 )
					{
						fi = model.get(i);
					}
				}

				if( fi != null && fi.isDirectory() )
				{
					list.setSelectedValue(fi, true);
				}
				else
				{
					list.getSelectionModel().clearSelection();
				}
			}
		});
	}
}