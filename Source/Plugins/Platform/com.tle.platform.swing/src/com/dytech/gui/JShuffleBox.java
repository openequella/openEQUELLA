package com.dytech.gui;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;

import com.tle.common.gui.models.GenericListModel;

/**
 * A Swing component allowing the user to select one or more elements from a
 * list on the left-hand side, by moving them to the right-hand side with the
 * given controls. Note: There is an implementation of a JFlatShuffleBox which
 * has the same functionality, but using a Flat GUI. It would probably be wise
 * to one day combine the two, or implement JFlatShuffleBox using this, and
 * applying a Flat style GUI.
 * 
 * @author Nicholas Read
 * @created 2 May 2003
 */
public class JShuffleBox<T> extends JPanel
{
	protected JShuffleBox<T>.Group left;
	protected JShuffleBox<T>.Group right;
	protected JPanel buttons;
	protected boolean bAllowDuplicates = false;

	// // CONSTRUCTORS
	// //////////////////////////////////////////////////////////

	public JShuffleBox()
	{
		this(null, null);
	}

	public JShuffleBox(T[] items)
	{
		this();
		addToLeft(items);
	}

	public JShuffleBox(String leftTitle, String rightTitle)
	{
		setupGUI(leftTitle, rightTitle);
	}

	public JShuffleBox(T[] items, String leftTitle, String rightTitle)
	{
		this(leftTitle, rightTitle);
		addToLeft(items);
	}

	// // RENDERING
	// /////////////////////////////////////////////////////////////

	public void setLeftCellRenderer(ListCellRenderer renderer)
	{
		left.list.setCellRenderer(renderer);
	}

	public void setRightCellRenderer(ListCellRenderer renderer)
	{
		right.list.setCellRenderer(renderer);
	}

	// // RETRIEVING VALUES
	// /////////////////////////////////////////////////////

	public GenericListModel<T> getRightModel()
	{
		return right.items;
	}

	public GenericListModel<T> getLeftModel()
	{
		return left.items;
	}

	public int getLeftCount()
	{
		return getLeft().size();
	}

	public T getLeftAt(int index)
	{
		return getLeft().get(index);
	}

	public List<T> getLeft()
	{
		return getLeftModel();
	}

	public int getLeftIndexOf(Object element)
	{
		return getLeft().indexOf(element);
	}

	public int getRightCount()
	{
		return getRight().size();
	}

	public T getRightAt(int index)
	{
		return getRight().get(index);
	}

	public List<T> getRight()
	{
		return getRightModel();
	}

	public int getRightIndexOf(Object element)
	{
		return getRight().indexOf(element);
	}

	// // ADDING VALUES
	// /////////////////////////////////////////////////////////

	public void addToLeft(T item)
	{
		addToSide(left, item);
	}

	public void addToLeft(T[] items)
	{
		addToSide(left, items);
	}

	public void addToLeft(Collection<T> items)
	{
		for( T item : items )
		{
			addToLeft(item);
		}
	}

	public void addToRight(T item)
	{
		addToSide(right, item);
	}

	public void addToRight(T[] items)
	{
		addToSide(right, items);
	}

	public void addToRight(Collection<T> items)
	{
		for( T item : items )
		{
			addToRight(item);
		}
	}

	private void addToSide(JShuffleBox<T>.Group group, T... items)
	{
		for( T item : items )
		{
			// If not allowing duplicates then check for
			// components that equal the given component:
			if( !bAllowDuplicates )
			{
				List<T> locRight = getRight();
				boolean remove = locRight.remove(item);
				while( remove )
				{
					remove = locRight.remove(item);
				}

				List<T> locLeft = getLeft();
				remove = locLeft.remove(item);
				while( remove )
				{
					remove = locLeft.remove(item);
				}
			}

			group.items.add(item);
		}

		updateButtons();
	}

	// // REMOVING ITEMS ///////////////////////////////////////////////////////

	public void removeFromLeftAt(int index)
	{
		getLeft().remove(index);
	}

	public void removeFromLeft(T item)
	{
		getLeft().remove(item);
	}

	public void removeAllFromLeft()
	{
		getLeft().clear();
	}

	public void removeFromRightAt(int index)
	{
		getRight().remove(index);
	}

	public void removeFromRight(T item)
	{
		getRight().remove(item);
	}

	public void removeAllFromRight()
	{
		getRight().clear();
	}

	// // MISCELLANEOUS ////////////////////////////////////////////////////////

	/**
	 * Sets whether or not to allow duplicates in the lists. The duplicates are
	 * tested with the <code>equals</code> method.
	 */
	public void setAllowDuplicates(boolean allow)
	{
		bAllowDuplicates = allow;
	}

	public int getCenterWidth()
	{
		// There is a 5 pixel gap on each side.
		return left.all.getPreferredSize().width + 10;
	}

	public void setCellRenderer(ListCellRenderer renderer)
	{
		left.list.setCellRenderer(renderer);
		right.list.setCellRenderer(renderer);
	}

	public void setFixedCellHeight(int height)
	{
		left.list.setFixedCellHeight(height);
		right.list.setFixedCellHeight(height);
	}

	// // ENABLING AND DISABLING ///////////////////////////////////////////////

	@Override
	public void setEnabled(boolean b)
	{
		super.setEnabled(b);

		left.list.setEnabled(b);
		right.list.setEnabled(b);

		left.scroll.setEnabled(b);
		right.scroll.setEnabled(b);

		updateButtons();
	}

	public void updateButtons()
	{
		left.updateButtons();
		right.updateButtons();
	}

	// // GUI CREATION
	// //////////////////////////////////////////////////////////

	private void setupGUI(String leftTitle, String rightTitle)
	{
		boolean hasTitle = leftTitle != null && rightTitle != null;

		JLabel leftLabel = null;
		JLabel rightLabel = null;
		if( hasTitle )
		{
			leftLabel = new JLabel(leftTitle);
			rightLabel = new JLabel(rightTitle);
		}

		left = new Group(">>", ">");
		right = new Group("<<", "<");

		left.single.addActionListener(new MovementHandler(left, right, false));
		left.all.addActionListener(new MovementHandler(left, right, true));

		right.single.addActionListener(new MovementHandler(right, left, false));
		right.all.addActionListener(new MovementHandler(right, left, true));

		right.single.setEnabled(false);
		right.all.setEnabled(false);

		updateButtons();

		Dimension csize = left.all.getPreferredSize();
		Dimension lsize = leftLabel != null ? leftLabel.getPreferredSize() : null;
		Dimension rsize = rightLabel != null ? rightLabel.getPreferredSize() : null;

		int width1 = (lsize != null && rsize != null ? Math.max(lsize.width, rsize.width) : 30);
		int width2 = csize.width;
		int height1 = (lsize != null ? lsize.height : 0);
		int height2 = csize.height;

		setMinimumSize(new Dimension(width1 + width2 + width1 + 10, height1 + height2));

		int[] rows = new int[]{height1, TableLayout.FILL, height2, height2, height2, height2, TableLayout.FILL};
		int[] columns = new int[]{TableLayout.FILL, width2, TableLayout.FILL};

		TableLayout layout = new TableLayout(rows, columns, 5, 5);
		setLayout(layout);

		if( hasTitle )
		{
			add(leftLabel, new Rectangle(0, 0, 1, 1));
			add(rightLabel, new Rectangle(2, 0, 1, 1));
		}

		add(left.scroll, new Rectangle(0, 1, 1, 6));
		add(right.scroll, new Rectangle(2, 1, 1, 6));

		add(left.all, new Rectangle(1, 2, 1, 1));
		add(left.single, new Rectangle(1, 3, 1, 1));
		add(right.single, new Rectangle(1, 4, 1, 1));
		add(right.all, new Rectangle(1, 5, 1, 1));
	}

	protected class Group
	{
		public JScrollPane scroll;
		public JList list;
		public GenericListModel<T> items;
		public JButton all;
		public JButton single;

		public Group(String allText, String singleText)
		{
			items = new GenericListModel<T>();
			list = new JList(items);
			scroll = new JScrollPane(list);
			all = new JButton(allText);
			single = new JButton(singleText);
		}

		public void updateButtons()
		{
			boolean enable = JShuffleBox.this.isEnabled() && items.size() > 0;
			single.setEnabled(enable);
			all.setEnabled(enable);
		}
	}

	// // SELECTION MOVERS
	// //////////////////////////////////////////////////////

	public void shiftSome(JShuffleBox<T>.Group source, JShuffleBox<T>.Group destination)
	{
		int[] selections = source.list.getSelectedIndices();
		for( int i = selections.length - 1; i >= 0; i-- )
		{
			destination.items.add(source.items.remove(selections[i]));
		}

		source.updateButtons();
		destination.updateButtons();
	}

	public void shiftAll(JShuffleBox<T>.Group source, JShuffleBox<T>.Group destination)
	{
		destination.items.addAll(source.items);
		source.items.clear();

		source.updateButtons();
		destination.updateButtons();
	}

	// // EVENT HANDLERS
	// ////////////////////////////////////////////////////////

	protected class MovementHandler implements ActionListener
	{
		protected JShuffleBox<T>.Group source;
		protected JShuffleBox<T>.Group destination;
		protected boolean moveAll;

		public MovementHandler(JShuffleBox<T>.Group source, JShuffleBox<T>.Group destination, boolean moveAll)
		{
			this.source = source;
			this.destination = destination;
			this.moveAll = moveAll;
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			if( moveAll )
			{
				shiftAll(source, destination);
			}
			else
			{
				shiftSome(source, destination);
			}
		}
	}

	// // TESTER
	// ////////////////////////////////////////////////////////////////

	@SuppressWarnings("nls")
	public static void main(String args[])
	{
		JShuffleBox<String> sb = new JShuffleBox<String>("left", "right");
		sb.addToLeft("item 1");
		sb.addToLeft("item 2");
		sb.addToLeft("item 3");
		sb.addToLeft("item 4");
		sb.addToLeft("item 5");
		sb.addToLeft("item 6");
		sb.addToLeft("item 7");
		sb.addToLeft("item 8");
		sb.addToLeft("item 9");
		sb.addToLeft("item 10 -------------------");
		sb.addToLeft("item 11");
		sb.addToLeft("item 12");
		sb.addToLeft("item 13");
		sb.addToLeft("item 14");
		sb.addToLeft("item 15");
		sb.addToLeft("item 16");
		sb.addToLeft("item 17");
		sb.addToLeft("item 18");
		sb.addToLeft("item 19");
		sb.addToLeft("item 20");

		final JFrame f = new JFrame();
		f.setBounds(100, 100, 500, 250);
		f.getContentPane().add(sb);
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}