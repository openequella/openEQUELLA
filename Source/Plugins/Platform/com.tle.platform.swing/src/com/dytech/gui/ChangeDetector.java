package com.dytech.gui;

import java.awt.ItemSelectable;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ListModel;
import javax.swing.SpinnerModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.table.TableModel;
import javax.swing.text.JTextComponent;
import javax.swing.tree.TreeModel;

/**
 * The ChangeDetector provides a simple interface for registering graphical
 * components and detecting whether any of them have changed. This is great for
 * cases where you want to display a <i>"Do you want to save you work"</i>
 * message when someone exits, but only if a change has taken place.
 * <hr>
 * <h2>Developers Notes</h2> If you want to detect other changes, then add a
 * pair of <code>watch</code> and <code>ignore</code> methods for the given
 * type. For example, if you wish to add a new type listener to a
 * JSomeComponent, then you add <code>watch</code> and <code>ignore</code>
 * methods such as the following:
 * 
 * <pre>
 * public void watch(JSomeComponent c)
 * {
 * 	c.addSomeListener(listener);
 * }
 * 
 * public void ignore(JSomeComponent c)
 * {
 * 	c.removeSomeListener(listener);
 * }
 * </pre>
 * 
 * The middle section of this class file is devoted to <code>watch</code> and
 * <code>ignore</code> methods, so put yours there too. You now need to make the
 * EventHandler inner class implement the new listener type and implement any
 * relavent methods.
 * 
 * @author Nicholas Read
 */
public class ChangeDetector implements Changeable
{
	/**
	 * Our class that listens to all the components cries.
	 */
	private final EventHandler listener;

	/**
	 * Constructs a new ChangeDetector
	 */
	public ChangeDetector()
	{
		super();
		listener = new EventHandler();
	}

	/**
	 * Indicates whether a change has been detected since construction of the
	 * <code>ChangeDetector</code>, or the invocation of
	 * <code>clearChanges</code>.
	 * 
	 * @return true if a change has occured, else false.
	 */
	@Override
	public boolean hasDetectedChanges()
	{
		return listener.hasDetectedChanges();
	}

	/**
	 * Wipes any recorded changes.
	 */
	@Override
	public void clearChanges()
	{
		listener.clearChanges();
	}

	/**
	 * Indicates whether events are currently being ignored.
	 * 
	 * @return true if events are currently being ignored.
	 */
	public boolean isIgnoreChanges()
	{
		return listener.isIgnoreChanges();
	}

	/**
	 * Will activate/suspend the event handlers recording events.
	 * 
	 * @param ignore true if events should be ignored.
	 */
	public void setIgnoreChanges(boolean ignore)
	{
		listener.setIgnoreChanges(ignore);
	}

	/**
	 * Iterates the components that have registered changes.
	 * 
	 * @return an iteration of components.
	 */
	public Iterator<?> iterateChangers()
	{
		return listener.iterateChangers();
	}

	/**
	 * Forces the change detector to register the given object as something that
	 * has changed. This is useful in some situations where something is changed
	 * programmatically, but the component does not generate an event.
	 * 
	 * @param obj The object that has changed.
	 */
	public void forceChange(Object obj)
	{
		listener.forceChange(obj);
	}

	//
	// Watch/Ignore methods
	//

	public void watch(JTextComponent c)
	{
		c.getDocument().addDocumentListener(listener);
	}

	public void ignore(JTextComponent c)
	{
		c.getDocument().removeDocumentListener(listener);
	}

	public void watch(ItemSelectable c)
	{
		c.addItemListener(listener);
	}

	public void ignore(ItemSelectable c)
	{
		c.removeItemListener(listener);
	}

	public void watch(ListModel m)
	{
		m.addListDataListener(listener);
	}

	public void ignore(ListModel m)
	{
		m.removeListDataListener(listener);
	}

	public void watch(TableModel m)
	{
		m.addTableModelListener(listener);
	}

	public void ignore(TableModel m)
	{
		m.removeTableModelListener(listener);
	}

	public void watch(SpinnerModel m)
	{
		m.addChangeListener(listener);
	}

	public void ignore(SpinnerModel m)
	{
		m.removeChangeListener(listener);
	}

	public void watch(ButtonGroup g)
	{
		Enumeration<AbstractButton> e = g.getElements();
		while( e.hasMoreElements() )
		{
			watch(e.nextElement());
		}
	}

	public void ignore(ButtonGroup g)
	{
		Enumeration<AbstractButton> e = g.getElements();
		while( e.hasMoreElements() )
		{
			ignore(e.nextElement());
		}
	}

	public void watch(TreeModel m)
	{
		m.addTreeModelListener(listener);
	}

	public void ignore(TreeModel m)
	{
		m.removeTreeModelListener(listener);
	}

	public void watch(Changeable c)
	{
		listener.watch(c);
	}

	public void ignore(Changeable c)
	{
		listener.ignore(c);
	}

	private static class EventHandler
		implements
			ItemListener,
			DocumentListener,
			ListDataListener,
			TableModelListener,
			ChangeListener,
			TreeModelListener
	{
		/**
		 * Keeps a set of components that have possibly been changed.
		 */
		private final Set<Object> changers;

		/**
		 * Keeps a set of Changeable components to watch.
		 */
		private final Set<Changeable> changeables;

		/**
		 * Indicates whether events should be recorded or not.
		 */
		private boolean ignoreChanges;

		/**
		 * Constructs an event handler
		 */
		public EventHandler()
		{
			ignoreChanges = false;
			changers = new HashSet<Object>();
			changeables = new HashSet<Changeable>();
		}

		/**
		 * Generically handles all incoming events
		 * 
		 * @param e
		 */
		private void dealWithEvent(EventObject e)
		{
			if( !isIgnoreChanges() )
			{
				changers.add(e.getSource());
			}
		}

		/**
		 * Generically handles all incoming events
		 * 
		 * @param e
		 */
		private void dealWithEvent(DocumentEvent e)
		{
			if( !isIgnoreChanges() )
			{
				changers.add(e.getDocument());
			}
		}

		/**
		 * Forces the change detector to register the given object as something
		 * that has changed. This is useful in some situations where something
		 * is changed programmatically, but the component does not generate an
		 * event.
		 * 
		 * @param obj The object that has changed.
		 */
		public void forceChange(Object obj)
		{
			changers.add(obj);
		}

		/**
		 * Indicates whether a change has been detected since construction of
		 * the <code>ChangeDetector</code>, or the invocation of
		 * <code>clearChanges</code>.
		 * 
		 * @return true if a change has occured, else false.
		 */
		public boolean hasDetectedChanges()
		{
			if( !changers.isEmpty() )
			{
				return true;
			}
			else if( !isIgnoreChanges() )
			{
				for( Changeable c : changeables )
				{
					if( c.hasDetectedChanges() )
					{
						return true;
					}
				}
			}
			return false;
		}

		/**
		 * Wipes any recorded changes.
		 */
		public void clearChanges()
		{
			changers.clear();
			for( Changeable c : changeables )
			{
				c.clearChanges();
			}
		}

		/**
		 * Indicates whether events are currently being ignored.
		 * 
		 * @return true if events are currently being ignored.
		 */
		public boolean isIgnoreChanges()
		{
			return ignoreChanges;
		}

		/**
		 * Will activate/suspend the event handlers recording events.
		 * 
		 * @param ignore true if events should be ignored.
		 */
		public void setIgnoreChanges(boolean ignore)
		{
			ignoreChanges = ignore;
		}

		/**
		 * Iterates the components that have registered changes.
		 * 
		 * @return an iteration of components.
		 */
		public Iterator<?> iterateChangers()
		{
			return changers.iterator();
		}

		/**
		 * Watch a Changeable component for changes.
		 * 
		 * @param c a Changeable component.
		 */
		public void watch(Changeable c)
		{
			changeables.add(c);
		}

		/**
		 * Ignores any changes of the given Changeable component.
		 * 
		 * @param c a Changeable component.
		 */
		public void ignore(Changeable c)
		{
			changeables.remove(c);
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent
		 * )
		 */
		@Override
		public void itemStateChanged(ItemEvent e)
		{
			dealWithEvent(e);
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * javax.swing.event.ListDataListener#contentsChanged(javax.swing.event
		 * .ListDataEvent)
		 */
		@Override
		public void contentsChanged(ListDataEvent e)
		{
			dealWithEvent(e);
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * javax.swing.event.ListDataListener#intervalAdded(javax.swing.event
		 * .ListDataEvent)
		 */
		@Override
		public void intervalAdded(ListDataEvent e)
		{
			dealWithEvent(e);
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * javax.swing.event.ListDataListener#intervalRemoved(javax.swing.event
		 * .ListDataEvent)
		 */
		@Override
		public void intervalRemoved(ListDataEvent e)
		{
			dealWithEvent(e);
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * javax.swing.event.TableModelListener#tableChanged(javax.swing.event
		 * .TableModelEvent)
		 */
		@Override
		public void tableChanged(TableModelEvent e)
		{
			dealWithEvent(e);
		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.
		 * ChangeEvent)
		 */
		@Override
		public void stateChanged(ChangeEvent e)
		{
			dealWithEvent(e);
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * javax.swing.event.TreeModelListener#treeNodesChanged(javax.swing.
		 * event.TreeModelEvent)
		 */
		@Override
		public void treeNodesChanged(TreeModelEvent e)
		{
			dealWithEvent(e);
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * javax.swing.event.TreeModelListener#treeNodesInserted(javax.swing
		 * .event.TreeModelEvent)
		 */
		@Override
		public void treeNodesInserted(TreeModelEvent e)
		{
			dealWithEvent(e);
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * javax.swing.event.TreeModelListener#treeNodesRemoved(javax.swing.
		 * event.TreeModelEvent)
		 */
		@Override
		public void treeNodesRemoved(TreeModelEvent e)
		{
			dealWithEvent(e);
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * javax.swing.event.TreeModelListener#treeStructureChanged(javax.swing
		 * .event.TreeModelEvent)
		 */
		@Override
		public void treeStructureChanged(TreeModelEvent e)
		{
			dealWithEvent(e);
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * javax.swing.event.DocumentListener#changedUpdate(javax.swing.event
		 * .DocumentEvent)
		 */
		@Override
		public void changedUpdate(DocumentEvent e)
		{
			dealWithEvent(e);
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * javax.swing.event.DocumentListener#insertUpdate(javax.swing.event
		 * .DocumentEvent)
		 */
		@Override
		public void insertUpdate(DocumentEvent e)
		{
			dealWithEvent(e);
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * javax.swing.event.DocumentListener#removeUpdate(javax.swing.event
		 * .DocumentEvent)
		 */
		@Override
		public void removeUpdate(DocumentEvent e)
		{
			dealWithEvent(e);
		}
	}
}