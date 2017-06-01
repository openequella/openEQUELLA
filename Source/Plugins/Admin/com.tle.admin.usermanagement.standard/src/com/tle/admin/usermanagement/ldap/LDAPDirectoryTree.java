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

package com.tle.admin.usermanagement.ldap;

import java.awt.Component;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import com.dytech.common.text.NumberStringComparator;
import com.dytech.gui.workers.GlassSwingWorker;
import com.tle.admin.Driver;
import com.tle.beans.usermanagement.standard.LDAPSettings;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.remoting.RemoteLDAPService;

@SuppressWarnings("nls")
public class LDAPDirectoryTree extends JTree implements TreeExpansionListener
{
	private static final long serialVersionUID = 1L;
	protected DefaultTreeModel treeModel;
	protected TreeNode2 topNode;
	private RemoteLDAPService ldapService;
	protected LDAPSettings settings;

	public LDAPDirectoryTree()
	{
		super();

		topNode = new TreeNode2(null);
		treeModel = new DefaultTreeModel(topNode);

		setModel(treeModel);
		setCellRenderer(new TreeRenderer());
		addTreeExpansionListener(this);
	}

	public void reload()
	{
		try
		{
			topNode.removeAllChildren();
			clearSelection();

			for( Name element : ldapService.getBases(settings) )
			{
				TreeNode2 node = new TreeNode2(element);
				topNode.add(node);
				browse(node, "" + element);
			}
			treeModel.reload();
		}
		catch( Exception e )
		{
			Driver.displayError(this, "com.tle.admin.usermanagement.standard.ldap.general.error",
				"com.tle.admin.usermanagement.standard.ldap.general.check", e);
		}

	}

	@Override
	public void treeExpanded(TreeExpansionEvent event)
	{
		final TreeNode2 tmp = (TreeNode2) event.getPath().getLastPathComponent();
		TreeNode2 t = (TreeNode2) tmp.getFirstChild();

		final String dn = tmp.getDN();

		if( !t.isUpdated() )
		{
			GlassSwingWorker<?> worker = new GlassSwingWorker<Object>()
			{
				@Override
				protected void beforeConstruct()
				{
					tmp.remove(0);
					tmp.setLoading(true);
					treeModel.reload(tmp);
					repaint();
				}

				@Override
				public Object construct() throws Exception
				{
					tmp.removeAllChildren();
					browse(tmp, dn);
					return null;
				}

				@Override
				public void finished()
				{
					tmp.setLoading(false);
					expandPath(new TreePath(tmp.getPath()));
					treeModel.reload(tmp);
					repaint();
				}

				@Override
				public void exception()
				{
					Driver.displayError(getComponent(), "com.tle.admin.usermanagement.standard.ldap.general.error",
						"com.tle.admin.usermanagement.standard.ldap.general.check", getException());
				}
			};

			worker.setComponent(this);
			worker.start();
		}
	}

	protected void addToNode(TreeNode2 node, Collection<TreeNode2> values)
	{
		for( TreeNode2 tn2 : values )
		{
			node.add(tn2);
		}
	}

	@Override
	public void treeCollapsed(TreeExpansionEvent e)
	{
		// We don't care about this event.
	}

	public static final class TreeNode2 extends DefaultMutableTreeNode
	{
		private static final long serialVersionUID = 1L;

		private final String DN;
		private String toolTip = "";
		private boolean loading;

		public TreeNode2(Object o, String dn)
		{
			super(o);
			DN = dn;
			loading = false;
		}

		public TreeNode2(Object o)
		{
			this(o, "");
		}

		// Property type of Vector determined by external jar
		public Vector<?> getChildren() // NOSONAR
		{
			return children;
		}

		public TreeNode2()
		{
			this(null);
		}

		public String getDN()
		{
			return DN;
		}

		public void setLoading(boolean load)
		{
			loading = load;
		}

		public boolean isUpdated()
		{
			return !((String) getUserObject()).equals("nothing here");
		}

		public void setToolTipText(String tip)
		{
			toolTip = tip;
		}

		public String getToolTipText()
		{
			return toolTip;
		}

		@Override
		public String toString()
		{
			String string = super.toString();
			if( loading )
			{
				string = CurrentLocale.get("com.tle.admin.usermanagement.ldap.ldapdirectorytree.loading", string);
			}
			return string;
		}
	}

	public void browse(DefaultMutableTreeNode parent, String base)
	{
		Comparator<TreeNode2> comp = new NodeComparator();

		Set<TreeNode2> foldermap = new TreeSet<TreeNode2>(comp);
		Set<TreeNode2> map = new TreeSet<TreeNode2>(comp);

		try
		{
			SearchControls ctls = new SearchControls();
			ctls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
			ctls.setCountLimit(settings.getSearchLimit());
			ctls.setReturningAttributes(new String[]{"1.1"});
			// Search for objects using the filter
			List<SearchResult> ne = ldapService.search(settings, base, "(objectClass=*)", ctls);
			for( SearchResult sr : ne )
			{
				String name = sr.getName();
				TreeNode2 node = new TreeNode2(name, name + ", " + base);
				if( !sr.isRelative() )
				{
					String url = sr.getName();
					int index = url.indexOf("://");
					if( index >= 0 )
					{
						index = index + 3;
						int index2 = url.indexOf('/', index);
						String dn = url.substring(index2 + 1);
						node = new TreeNode2(url, dn);
					}
				}
				Attributes attributes = sr.getAttributes();

				BasicAttribute n = (BasicAttribute) attributes.get("numsubordinates");
				if( n != null )
				{
					Integer j = Integer.valueOf((String) n.get());

					if( j.intValue() > 0 )
					{
						node.add(new TreeNode2("something here"));
					}
				}
				else
				{
					node.add(new TreeNode2("nothing here"));
				}
				foldermap.add(node);
			}

			try
			{
				List<? extends Attribute> allAttributes = ldapService.getAttributes(settings, base, null);
				for( Attribute a : allAttributes )
				{
					NamingEnumeration<?> attributeList = a.getAll();
					while( attributeList != null && attributeList.hasMore() )
					{
						TreeNode2 tn = new TreeNode2(a.getID());
						tn.setToolTipText("" + attributeList.nextElement());
						map.add(tn);
					}
				}
			}
			catch( Exception err )
			{
				err.printStackTrace();
			}
		}
		// In the interests of diagnostics, we'll allow an explicit catch of
		// generic exception
		catch( Exception eek ) // NOSONAR
		{
			eek.printStackTrace();
		}

		for( TreeNode2 tn : foldermap )
		{
			parent.add(tn);
		}

		for( TreeNode2 tn : map )
		{
			parent.add(tn);
		}
	}

	protected static final class TreeRenderer extends DefaultTreeCellRenderer
	{
		private static final long serialVersionUID = 1L;

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
			boolean leaf, int row, boolean hasFocus2)
		{

			super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus2);

			if( value instanceof TreeNode2 )
			{
				TreeNode2 tn = (TreeNode2) value;

				if( tn.getDN().length() == 0 )
				{
					setToolTipText(tn.getToolTipText());
				}
				else
				{
					setToolTipText(null);
				}
			}

			return this;
		}
	}

	protected static class NodeComparator extends NumberStringComparator<TreeNode2>
	{
		private static final long serialVersionUID = 1L;

		@Override
		public int compare(TreeNode2 node1, TreeNode2 node2)
		{
			int comp = 0;
			boolean isAttribute = node1.getDN().length() == 0;
			if( isAttribute ^ node2.getDN().length() == 0 )
			{
				comp = isAttribute ? 1 : -1;
			}
			else
			{
				comp = super.compare(node1, node2);
			}

			return comp;
		}
	}

	public void setLdapService(RemoteLDAPService ldapService)
	{
		this.ldapService = ldapService;
	}

	public void setLDAPSettings(LDAPSettings settings)
	{
		this.settings = settings;
	}
}
