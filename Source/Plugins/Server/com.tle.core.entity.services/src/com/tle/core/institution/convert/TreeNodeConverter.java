/*
 * Created on 4/05/2006
 */
package com.tle.core.institution.convert;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;

import com.dytech.common.collections.Iter8;
import com.thoughtworks.xstream.XStream;
import com.tle.beans.Institution;
import com.tle.beans.tree.TreeNodeInterface;
import com.tle.core.dao.AbstractTreeDao;
import com.tle.core.filesystem.BucketFile;
import com.tle.core.filesystem.SubTemporaryFile;
import com.tle.core.filesystem.TemporaryFileHandle;
import com.tle.core.initialiser.Property;
import com.tle.core.institution.importhandler.ImportHandler;
import com.tle.core.institution.importhandler.MultiFileImportHandler;
import com.tle.core.institution.importhandler.SingleTreeNodeFileImportHandler;
import com.tle.core.institution.importhandler.SingleTreeNodeFileImportHandler.NodeCreator;
import com.tle.core.services.entity.BaseEntityXmlConverter;
import com.tle.core.services.entity.EntityRegistry;
import com.tle.core.util.DefaultMessageCallback;

public abstract class TreeNodeConverter<T extends TreeNodeInterface<T>> extends AbstractConverter<Object>
	implements
		NodeCreator<T>
{
	private final String folder;
	private final String oldSingleFilename;

	@Inject
	private EntityRegistry registry;

	// Always Initialise lazily - XStream has a memory leak whenever you create
	// a new one
	private XStream xstream;

	public abstract AbstractTreeDao<T> getDao();

	public abstract Class<T> getNodeClass();

	public TreeNodeConverter(String folder, String oldSingleFilename)
	{
		this.folder = folder;
		this.oldSingleFilename = oldSingleFilename;
	}

	protected XStream createXStream()
	{
		XStream x = xmlHelper.createXStream();
		x.registerConverter(new BaseEntityXmlConverter(registry));
		return x;
	}

	protected final synchronized XStream getXStream()
	{
		if( xstream == null )
		{
			xstream = createXStream();
		}
		return xstream;
	}

	@Override
	public void doDelete(Institution institution, ConverterParams params)
	{
		DefaultMessageCallback message = new DefaultMessageCallback(null);
		params.setMessageCallback(message);

		List<Long> ids = getDao().enumerateIdsInOrder();
		message.setTotal(ids.size());
		message.setKey("institutions.converter.generic.genericdeletemsg"); //$NON-NLS-1$
		message.setType(getNodeClass().getSimpleName().toLowerCase());
		for( Long id : ids )
		{
			getDao().deleteInOrder(id);
			message.incrementCurrent();
			getDao().flush();
			getDao().clear();
		}
	}

	@Override
	public void exportIt(final TemporaryFileHandle staging, Institution institution, final ConverterParams params,
		String cid) throws IOException
	{
		// Listing nodes in order is important because we need to write out each
		// file with a sequential numbering system. This allows us to import
		// them in order and re-link children to their parents correctly.
		final List<Long> nodes = getDao().listIdsInOrder();

		final DefaultMessageCallback message = new DefaultMessageCallback("institutions.converter.generic.genericmsg"); //$NON-NLS-1$
		message.setType(getNodeClass().getSimpleName().toLowerCase());
		message.setTotal(nodes.size());

		params.setMessageCallback(message);

		final SubTemporaryFile exportFolder = new SubTemporaryFile(staging, folder);
		xmlHelper.writeExportFormatXmlFile(exportFolder, true);

		for( final Iter8<Long> iter = new Iter8<Long>(nodes); iter.hasNext(); )
		{
			// Handle one node per transaction to avoid running out of
			// connections in
			// SQL Server 2000 SP4. This was occurring with the large hierarchy
			// that
			// EQ has, but the problem is not evident in SP3 or lower!
			doInTransaction(new Runnable()
			{
				@SuppressWarnings("nls")
				@Override
				public void run()
				{
					T node = getDao().findById(iter.next());

					// Initialise to fix up references to schemata, collections,
					// advanced searches, etc...
					node = initialiserService.initialise(node, new TreeInitialiserCallback<T>());

					// Remove anything we don't want to keep
					node.setInstitution(null);
					node.setAllParents(null);

					// Allow for other custom work to be done...
					preExport(node, params);

					int count = iter.getCount();
					BucketFile bucketFolder = new BucketFile(exportFolder, Long.valueOf(count));
					xmlHelper.writeXmlFile(bucketFolder, count + ".xml", node, getXStream());

					message.incrementCurrent();
				}
			});
		}
	}

	@Override
	public void importIt(TemporaryFileHandle staging, Institution institution, final ConverterParams params, String cid)
		throws IOException
	{
		final SubTemporaryFile nodesFolder = new SubTemporaryFile(staging, folder);

		final ImportHandler<T> importHandler = fileSystemService.fileExists(nodesFolder, oldSingleFilename)
			? new SingleTreeNodeFileImportHandler<T>(nodesFolder, oldSingleFilename, xmlHelper, this, getXStream())
			: new MultiFileImportHandler<T>(nodesFolder, xmlHelper, getXStream());

		@SuppressWarnings("nls")
		final DefaultMessageCallback message = new DefaultMessageCallback("institutions.converter.generic.genericmsg");
		message.setType(getNodeClass().getSimpleName().toLowerCase());
		message.setTotal(importHandler.getNodeCount());

		params.setMessageCallback(message);

		final Map<Long, Long> oldIdToNewId = new ConcurrentHashMap<Long, Long>();

		for( final Iterator<T> iter = importHandler.iterateNodes(); iter.hasNext(); )
		{
			final T node = iter.next();

			final long oldId = node.getId();
			final Long parentId = getParentId(node);

			// We set the parent again afterwards, but we need to null it out
			// for when we're
			// importing old-style nodes. Because they are loaded up using
			// XStream, the parents
			// will point to real nodes, and initialiseClones will destroy their
			// new ID.
			node.setParent(null);

			initialiserService.initialiseClones(node);

			node.setId(0);
			node.setInstitution(institution);

			// Set our parent (if we have one) and save our original ID for
			// child nodes to lookup
			if( parentId != null )
			{
				T parent;
				try
				{
					parent = getNodeClass().newInstance();
				}
				catch( Exception e )
				{
					throw new RuntimeException(e);
				}
				Long newParentId = oldIdToNewId.get(parentId);
				if( newParentId != null )
				{
					parent.setId(newParentId);
					node.setParent(parent);
				}
			}

			doInTransaction(new Runnable()
			{
				@Override
				public void run()
				{
					// Allow for other custom work to be done...
					preInsert(node, params);

					// Save the node
					AbstractTreeDao<T> dao = getDao();
					node.setId(dao.save(node));
					dao.flush();
					dao.clear();
					// Populate the old to new ID mapping
					Map<Long, Long> idMap = getIdMap(params);
					if( idMap != null )
					{
						idMap.put(oldId, node.getId());
					}

					oldIdToNewId.put(oldId, node.getId());
				}
			});

			message.incrementCurrent();
		}
	}

	protected void preExport(T topic, ConverterParams params)
	{
		// nothing
	}

	protected void preInsert(T topic, ConverterParams params)
	{
		// nothing
	}

	protected Map<Long, Long> getIdMap(ConverterParams params)
	{
		return null;
	}

	protected Long getParentId(T node)
	{
		return node.getParent() == null ? null : node.getParent().getId();
	}

	protected static class TreeInitialiserCallback<T extends TreeNodeInterface<T>> extends EntityInitialiserCallback
	{
		@Override
		@SuppressWarnings("unchecked")
		public void set(Object obj, Property property, Object value)
		{
			if( value instanceof TreeNodeInterface )
			{
				T toset = (T) value;
				toset.setUuid(((T) property.get(obj)).getUuid());
			}
			super.set(obj, property, value);
		}

		@Override
		@SuppressWarnings("unchecked")
		public void entitySimplified(Object old, Object newObj)
		{
			if( old instanceof TreeNodeInterface )
			{
				T toset = (T) newObj;
				T oldObj = (T) old;
				toset.setUuid(oldObj.getUuid());
			}
			super.entitySimplified(old, newObj);
		}
	}

	@Override
	public T createNode()
	{
		try
		{
			return getNodeClass().newInstance();
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
	}
}
