package com.tle.web.api.collection.resource;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.springframework.transaction.annotation.Transactional;

import com.dytech.edge.common.LockedException;
import com.dytech.edge.exceptions.InUseException;
import com.dytech.edge.exceptions.InvalidDataException;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.tle.beans.entity.BaseEntity;
import com.tle.beans.entity.EntityLock;
import com.tle.beans.security.AccessEntry;
import com.tle.beans.security.AccessExpression;
import com.tle.common.Check;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.security.SecurityConstants;
import com.tle.core.dao.AccessExpressionDao;
import com.tle.core.dao.AclDao;
import com.tle.core.security.TLEAclManager;
import com.tle.core.security.impl.RequiresPrivilege;
import com.tle.core.services.LockingService;
import com.tle.core.services.entity.AbstractEntityService;
import com.tle.core.user.CurrentInstitution;
import com.tle.core.user.CurrentUser;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.api.baseentity.serializer.BaseEntitySerializer;
import com.tle.web.api.interfaces.BaseEntityResource;
import com.tle.web.api.interfaces.beans.BaseEntityBean;
import com.tle.web.api.interfaces.beans.EntityLockBean;
import com.tle.web.api.interfaces.beans.SearchBean;
import com.tle.web.api.interfaces.beans.UserBean;
import com.tle.web.api.interfaces.beans.security.BaseEntitySecurityBean;
import com.tle.web.api.interfaces.beans.security.TargetListEntryBean;
import com.tle.web.remoting.rest.service.UrlLinkService;

/**
 * FIXME: I'm not sure about having all these Transactionals in the REST resource...
 * 
 * @author Aaron
 *
 * @param <BE> Base Entity type
 * @param <SB> Security Bean type
 * @param <B> Serialized bean type
 */
public abstract class AbstractBaseEntityResource<BE extends BaseEntity, SB extends BaseEntitySecurityBean, B extends BaseEntityBean>
	implements
		BaseEntityResource<B, SB>
{
	// private static final Logger LOGGER =
	// Logger.getLogger(BaseEntityResource.class);

	protected abstract Node[] getAllNodes();

	protected abstract SB createAllSecurityBean();

	protected abstract AbstractEntityService<?, BE> getEntityService();

	protected abstract int getSecurityPriority();

	protected abstract BaseEntitySerializer<BE, B> getSerializer();

	protected abstract Class<?> getResourceClass();

	@Inject
	private AclDao aclDao;
	@Inject
	protected TLEAclManager aclManager;
	@Inject
	private AccessExpressionDao accessExpressionDao;
	@Inject
	private LockingService lockingService;
	@Inject
	private UrlLinkService urlLinkService;

	protected SB serializeAcls()
	{
		if( aclManager.filterNonGrantedPrivileges("VIEW_SECURITY_TREE", "EDIT_SECURITY_TREE").isEmpty() )
		{
			throw new AccessDeniedException("One of [VIEW_SECURITY_TREE, EDIT_SECURITY_TREE] is required");
		}

		Node[] allNodes = getAllNodes();
		ListMultimap<String, AccessEntry> entries = aclManager.getExistingEntriesForVirtualNodes(allNodes);
		SB securityBean = createAllSecurityBean();

		List<AccessEntry> list = entries.get(aclManager.getKeyForVirtualNode(allNodes[0], null));

		List<TargetListEntryBean> entryBeans = Lists.newLinkedList();
		for( AccessEntry accessEntry : list )
		{
			TargetListEntryBean entryBean = new TargetListEntryBean();
			entryBean.setGranted(accessEntry.isGrantRevoke() == SecurityConstants.GRANT);
			entryBean.setWho(accessEntry.getExpression().getExpression());
			entryBean.setPrivilege(accessEntry.getPrivilege());
			entryBean.setOverride(accessEntry.getAclPriority() > 0);
			entryBeans.add(entryBean);
		}

		securityBean.setRules(entryBeans);
		return securityBean;
	}

	@RequiresPrivilege(priv = "EDIT_SECURITY_TREE")
	@Transactional
	protected void updateAcls(SB securityBean, int priority)
	{
		List<AccessEntry> entries = Lists.newArrayList();
		List<TargetListEntryBean> rules = securityBean.getRules();
		for( TargetListEntryBean rule : rules )
		{
			AccessEntry newEntry = new AccessEntry();
			if( rule.isGranted() )
			{
				newEntry.setGrantRevoke(SecurityConstants.GRANT);
			}
			else
			{
				newEntry.setGrantRevoke(SecurityConstants.REVOKE);
			}
			newEntry.setPrivilege(rule.getPrivilege());
			newEntry.setTargetObject(SecurityConstants.TARGET_EVERYTHING);
			newEntry.setAclPriority(priority);
			newEntry.setAclOrder(0);
			newEntry.setExpression(null);
			newEntry.setInstitution(CurrentInstitution.get());

			String who = rule.getWho().trim();
			if( Check.isEmpty(who) )
			{
				throw new WebApplicationException(Status.BAD_REQUEST);
			}
			AccessExpression exprObj = accessExpressionDao.retrieveOrCreate(who);
			newEntry.setExpression(exprObj);

			entries.add(newEntry);
			aclDao.save(newEntry);
		}

		aclDao.flush();
		aclDao.clear();
	}

	@Override
	@Transactional
	public SearchBean<B> list(UriInfo uriInfo)
	{
		final List<BE> allEntities = getEntityService().enumerateListable();
		final List<B> retBeans = new ArrayList<B>(allEntities.size());
		for( BE entity : allEntities )
		{
			final B bean = serialize(entity, null, false);
			final Map<String, String> links = Collections.singletonMap("self", getGetUri(entity.getUuid()).toString());
			bean.set("links", links);
			retBeans.add(bean);
		}

		final SearchBean<B> results = new SearchBean<B>();
		results.setStart(0);
		results.setLength(retBeans.size());
		results.setResults(retBeans);
		results.setAvailable(retBeans.size());
		return results;
	}

	protected B serialize(BE entity, Object data, boolean heavy)
	{
		return getSerializer().serialize(entity, data, heavy);
	}

	@Override
	public SB getAcls(UriInfo uriInfo)
	{
		return serializeAcls();
	}

	@Override
	public Response editAcls(UriInfo uriInfo, SB security)
	{
		updateAcls(security, getSecurityPriority());
		return Response.ok().build();
	}

	/**
	 * Implementation for various entity endpoints.
	 * 
	 * @param uriInfo
	 * @param uuid
	 * @return
	 */
	@Override
	public Response getLock(UriInfo uriInfo, String uuid)
	{
		final BE entity = getEntityService().getByUuid(uuid);
		if( entity == null )
		{
			return Response.status(Status.NOT_FOUND).build();
		}
		final EntityLock lock = lockingService.getLockUnbound(entity);
		if( lock == null )
		{
			return Response.status(Status.NOT_FOUND).build();
		}
		return Response.ok().entity(convertLock(lock, entity)).build();
	}

	/**
	 * Implementation for various entity endpoints.
	 * 
	 * @param uriInfo
	 * @param uuid
	 * @return
	 */
	@Override
	public Response lock(UriInfo uriInfo, String uuid)
	{
		try
		{
			BE entity = getEntityService().getByUuid(uuid);
			if( entity == null )
			{
				return Response.status(Status.NOT_FOUND).build();
			}
			final EntityLock lock = lockingService.lockEntity(entity);
			return Response.status(Status.CREATED).entity(convertLock(lock, entity)).build();
		}
		catch( LockedException ex )
		{
			if( CurrentUser.getUserID().equals(ex.getUserID()) )
			{
				throw new WebApplicationException(
					"Entity is locked in a different session.  Call unlock with a force parameter value of true.",
					Status.CONFLICT);
			}
			else
			{
				throw new WebApplicationException("Entity is locked by another user: " + ex.getUserID(),
					Status.CONFLICT);
			}
		}
	}

	/**
	 * Implementation for various entity endpoints.
	 * 
	 * @param uuid
	 * @return
	 */
	@Override
	public Response unlock(UriInfo uriInfo, String uuid)
	{
		try
		{
			BE entity = getEntityService().getByUuid(uuid);
			if( entity == null )
			{
				return Response.status(Status.NOT_FOUND).build();
			}
			lockingService.unlockEntity(entity, true);
		}
		catch( LockedException ex )
		{
			if( CurrentUser.getUserID().equals(ex.getUserID()) )
			{
				throw new WebApplicationException(
					"Entity is locked in a different session.  Call unlock with a force parameter value of true.",
					Status.UNAUTHORIZED);
			}
			else
			{
				throw new WebApplicationException("You do not own the lock on this entity.  It is held by user ID "
					+ ex.getUserID(), Status.UNAUTHORIZED);
			}
		}
		// Should be 'no content'??
		return Response.noContent().build();
	}

	private EntityLockBean convertLock(EntityLock lock, BE entity)
	{
		final EntityLockBean lockBean = new EntityLockBean();
		lockBean.setOwner(new UserBean(lock.getUserID()));
		lockBean.setUuid(lock.getUserSession());
		final Map<String, String> links = new HashMap<>();
		final String lockUrl = urlLinkService.getMethodUriBuilder(getResourceClass(), "getLock")
			.build(entity.getUuid()).toString();
		links.put("self", lockUrl);
		lockBean.setLinks(links);
		return lockBean;
	}

	/**
	 * Implementation for various entity endpoints.
	 * 
	 * @param uriInfo
	 * @param uuid
	 * @return
	 */
	@Transactional
	@Override
	public B get(UriInfo uriInfo, String uuid)
	{
		final AbstractEntityService<?, BE> entityService = getEntityService();
		final BE entity = entityService.getByUuid(uuid);
		if( entity == null )
		{
			throw new WebApplicationException(Status.NOT_FOUND);
		}
		if( !entityService.canView(entity) )
		{
			throw new WebApplicationException(Status.FORBIDDEN);
		}
		return serialize(entity, null, true);
	}

	/**
	 * Implementation for various entity endpoints.
	 * 
	 * @param uuid
	 * @return
	 */
	@Override
	public Response delete(UriInfo uriInfo, String uuid)
	{
		final AbstractEntityService<?, BE> entityService = getEntityService();
		BE entity = entityService.getByUuid(uuid);
		if( entity == null )
		{
			return Response.status(Status.NOT_FOUND).build();
		}
		try
		{
			entityService.delete(entity, true);
			return Response.noContent().build();
		}
		catch( InUseException inUse )
		{
			return Response.status(Status.BAD_REQUEST).build();
		}
	}

	/**
	 * Implementation for various entity endpoints.
	 * 
	 * @param bean
	 * @param stagingUuid
	 * @return
	 */
	@Override
	public Response create(UriInfo uriInfo, B bean, String stagingUuid)
	{
		try
		{
			BE entity = getSerializer().deserializeNew(bean, stagingUuid);
			String uuid = entity.getUuid();
			return Response.created(getGetUri(uuid)).build();
		}
		catch( InvalidDataException ide )
		{
			return Response.status(Status.BAD_REQUEST).build();
		}
	}

	@Override
	public Response edit(UriInfo uriInfo, String uuid, B bean, String stagingUuid, String lockId, boolean keepLocked)
	{
		try
		{
			BE entity = getSerializer().deserializeEdit(bean, stagingUuid, lockId, keepLocked);
			if( entity == null )
			{
				return Response.status(Status.NOT_FOUND).build();
			}
			return Response.ok().location(getGetUri(uuid)).build();
		}
		catch( LockedException locked )
		{
			return Response.status(Status.CONFLICT).build();
		}
		catch( AccessDeniedException denied )
		{
			return Response.status(Status.FORBIDDEN).build();
		}
		catch( InvalidDataException ide )
		{
			return Response.status(Status.BAD_REQUEST).build();
		}
	}

	protected URI getGetUri(String uuid)
	{
		return urlLinkService.getMethodUriBuilder(getResourceClass(), "get").build(uuid);
	}

	protected boolean isLocked(BE entity)
	{
		return lockingService.isEntityLocked(entity, CurrentUser.getUserID(), null);
	}
}
