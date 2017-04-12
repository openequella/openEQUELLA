package com.tle.core.kaltura.service;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.inject.Inject;

import org.springframework.transaction.annotation.Transactional;

import com.dytech.edge.common.valuebean.ValidationError;
import com.dytech.edge.exceptions.InvalidDataException;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Throwables;
import com.google.common.io.Resources;
import com.google.inject.Singleton;
import com.kaltura.client.KalturaApiException;
import com.kaltura.client.KalturaClient;
import com.kaltura.client.KalturaConfiguration;
import com.kaltura.client.enums.KalturaEntryStatus;
import com.kaltura.client.enums.KalturaMediaType;
import com.kaltura.client.enums.KalturaSessionType;
import com.kaltura.client.enums.KalturaUiConfCreationMode;
import com.kaltura.client.enums.KalturaUiConfObjType;
import com.kaltura.client.services.KalturaMediaService;
import com.kaltura.client.services.KalturaSessionService;
import com.kaltura.client.services.KalturaUiConfService;
import com.kaltura.client.types.KalturaFilterPager;
import com.kaltura.client.types.KalturaMediaEntry;
import com.kaltura.client.types.KalturaMediaEntryFilter;
import com.kaltura.client.types.KalturaMediaListResponse;
import com.kaltura.client.types.KalturaUiConf;
import com.kaltura.client.types.KalturaUiConfFilter;
import com.kaltura.client.types.KalturaUiConfListResponse;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.common.Check;
import com.tle.common.EntityPack;
import com.tle.common.i18n.LangUtils;
import com.tle.common.kaltura.entity.KalturaServer;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.core.guice.Bind;
import com.tle.core.kaltura.KalturaConstants;
import com.tle.core.kaltura.dao.KalturaDao;
import com.tle.core.security.impl.SecureEntity;
import com.tle.core.security.impl.SecureOnReturn;
import com.tle.core.services.entity.EntityEditingBean;
import com.tle.core.services.entity.EntityEditingSession;
import com.tle.core.services.entity.impl.AbstractEntityServiceImpl;

@Bind(KalturaService.class)
@Singleton
@SuppressWarnings("nls")
@SecureEntity("KALTURA")
public class KalturaServiceImpl extends AbstractEntityServiceImpl<EntityEditingBean, KalturaServer, KalturaService>
	implements
		KalturaService
{
	protected final KalturaDao kalturaDao;

	@Inject
	public KalturaServiceImpl(KalturaDao dao)
	{
		super(Node.KALTURA, dao);
		kalturaDao = dao;
	}

	// Increase number on the end to replace
	private static final String EQUELLA_KCW_UICONF = "EQUELLA-KCW-UICONF_5.2-134";
	private static final String EQUELLA_KDP_UICONF = "EQUELLA-KDP-UICONF_5.2-114";

	@Override
	public KalturaMediaListResponse searchMedia(KalturaClient client, Collection<String> keywords, int page, int limit)
	{
		KalturaMediaService ms = client.getMediaService();
		KalturaMediaEntryFilter ef = new KalturaMediaEntryFilter();
		String joined = "";

		if( !Check.isEmpty(keywords) && keywords != null )
		{
			joined = Joiner.on(',').join(keywords).trim();
			joined = joined.replaceAll("[\\*\\?\\~]", "");
			if( !Objects.equals(joined, "") )
			{
				ef.tagsNameMultiLikeOr = joined.toLowerCase();
				ef.mediaTypeIn = Joiner.on(",").join(KalturaMediaType.VIDEO.hashCode, KalturaMediaType.AUDIO.hashCode);
				ef.orderBy = "+name"; // KalturaMediaEntryOrderBy.NAME_ASC.name();
				KalturaFilterPager pager = new KalturaFilterPager();
				pager.pageSize = limit;
				pager.pageIndex = page;

				try
				{
					return ms.list(ef, pager);
				}
				catch( KalturaApiException e )
				{
					throw Throwables.propagate(e);
				}
			}
		}

		// No results (or blank search)
		return null;
	}

	@Override
	public KalturaClient getKalturaClient(KalturaServer kalturaServer, KalturaSessionType type)
		throws KalturaApiException
	{
		int pid = kalturaServer.getPartnerId();
		String secret = type.equals(KalturaSessionType.ADMIN) ? kalturaServer.getAdminSecret() : kalturaServer
			.getUserSecret();

		KalturaConfiguration kConfig = new KalturaConfiguration();
		kConfig.setPartnerId(pid);
		kConfig.setEndpoint(kalturaServer.getEndPoint());

		KalturaClient kClient = new KalturaClient(kConfig);

		try
		{
			String ks = kClient.getSessionService().start(secret, "", type, pid);
			kClient.setSessionId(ks);

			return kClient;
		}
		catch( Exception ex )
		{
			throw new KalturaApiException();
		}
	}

	@Override
	public boolean isUp(KalturaServer ks)
	{
		if( ks == null )
		{
			return false;
		}

		KalturaConfiguration kConfig = new KalturaConfiguration();
		kConfig.setEndpoint(ks.getEndPoint());

		try
		{
			new KalturaClient(kConfig).getSystemService().ping();
		}
		catch( KalturaApiException e )
		{
			return false;
		}
		return true;
	}

	@Override
	public KalturaMediaEntry getMediaEntry(KalturaClient client, String entryId)
	{
		try
		{
			return client.getMediaService().get(entryId);
		}
		catch( KalturaApiException e )
		{
			throw Throwables.propagate(e);
		}
	}

	@Override
	public KalturaUiConf getDefaultKcwUiConf(KalturaClient client)
	{
		try
		{
			KalturaUiConf conf = null;

			KalturaUiConfFilter kcf = new KalturaUiConfFilter();
			kcf.nameLike = "EQUELLA-KCW-UICONF_5.2";
			kcf.objTypeEqual = KalturaUiConfObjType.CONTRIBUTION_WIZARD;
			KalturaUiConfService uiConfService = client.getUiConfService();
			KalturaUiConfListResponse uiList = uiConfService.list(kcf);

			if( uiList.totalCount == 0 )
			{
				// No Configs add default
				conf = createDefaultKCWUiConf(uiConfService);
			}
			else if( uiList.totalCount > 1 )
			{
				// More than 1 delete all and add default
				for( KalturaUiConf uiConf : uiList.objects )
				{
					uiConfService.delete(uiConf.id);
				}

				conf = createDefaultKCWUiConf(uiConfService);
			}
			else
			{
				// If there is one is it the latest
				conf = uiList.objects.get(0);
				if( !conf.name.equals(EQUELLA_KCW_UICONF) )
				{
					uiConfService.delete(conf.id);
					conf = createDefaultKCWUiConf(uiConfService);
				}
			}

			return conf;
		}
		catch( Exception e )
		{
			Throwables.propagate(e);
		}
		return null;
	}

	private KalturaUiConf createDefaultKCWUiConf(KalturaUiConfService uiConfService) throws IOException,
		KalturaApiException
	{
		KalturaUiConf equellaKcwUiConf = new KalturaUiConf();
		equellaKcwUiConf.objType = KalturaUiConfObjType.CONTRIBUTION_WIZARD;
		equellaKcwUiConf.creationMode = KalturaUiConfCreationMode.ADVANCED;
		equellaKcwUiConf.swfUrl = "/flash/kcw/v2.1.6.7/ContributionWizard.swf";
		equellaKcwUiConf.confFile = readUiConfXml("default_kcw_ui_conf.xml");
		equellaKcwUiConf.name = EQUELLA_KCW_UICONF;

		return uiConfService.add(equellaKcwUiConf);
	}

	@Override
	public KalturaUiConf getDefaultKdpUiConf(KalturaServer ks)
	{

		try
		{
			KalturaClient kc = getKalturaClient(ks, KalturaSessionType.ADMIN);
			KalturaUiConf conf = null;

			KalturaUiConfFilter kcf = new KalturaUiConfFilter();
			kcf.nameLike = "EQUELLA-KDP-UICONF_5.2";
			kcf.objTypeEqual = KalturaUiConfObjType.PLAYER_V3;
			KalturaUiConfService uiConfService = kc.getUiConfService();
			KalturaUiConfListResponse uiList = uiConfService.list(kcf);

			if( uiList.totalCount == 0 )
			{
				// No Configs add default
				conf = createDefaultKDPUiConf(uiConfService);
			}
			else if( uiList.totalCount > 1 )
			{
				// More than 1 delete all and add default
				for( KalturaUiConf uiConf : uiList.objects )
				{
					uiConfService.delete(uiConf.id);
				}

				conf = createDefaultKDPUiConf(uiConfService);
			}
			else
			{
				// If there is one is it the latest
				conf = uiList.objects.get(0);
				if( !conf.name.equals(EQUELLA_KDP_UICONF) )
				{
					uiConfService.delete(conf.id);
					conf = createDefaultKDPUiConf(uiConfService);
				}
			}

			return conf;
		}
		catch( Exception e )
		{
			Throwables.propagate(e);
		}
		return null;
	}

	private KalturaUiConf createDefaultKDPUiConf(KalturaUiConfService uiConfService) throws IOException,
		KalturaApiException
	{
		KalturaUiConf equellaKdpUiConf = new KalturaUiConf();
		equellaKdpUiConf.objType = KalturaUiConfObjType.PLAYER_V3;
		equellaKdpUiConf.creationMode = KalturaUiConfCreationMode.ADVANCED;
		equellaKdpUiConf.swfUrl = "/flash/kdp3/v3.5.35/kdp3.swf";
		equellaKdpUiConf.confFile = readUiConfXml("default_kdp_ui_conf.xml");
		equellaKdpUiConf.name = EQUELLA_KDP_UICONF;
		equellaKdpUiConf.tags = "kdp3,player";
		equellaKdpUiConf.useCdn = true;

		return uiConfService.add(equellaKdpUiConf);
	}

	private String readUiConfXml(String filename) throws IOException
	{
		return Resources.toString(KalturaServiceImpl.class.getResource(filename), Charsets.UTF_8);
	}

	@Override
	public boolean testKalturaSetup(KalturaServer kalturaServer, KalturaSessionType type) throws KalturaApiException
	{
		KalturaClient kclient = null;
		try
		{
			kclient = getKalturaClient(kalturaServer, type);
			return !Check.isEmpty(kclient.getSessionId());
		}
		finally
		{
			if( kclient != null )
			{
				KalturaSessionService ss = kclient.getSessionService();
				ss.end();
			}
		}
	}

	@Override
	public KalturaMediaListResponse getMediaEntries(KalturaClient client, List<String> entryIds)
	{
		try
		{
			KalturaMediaEntryFilter filter = new KalturaMediaEntryFilter();
			filter.idIn = Joiner.on(',').join(entryIds);
			filter.statusIn = Joiner.on(',').join(KalturaEntryStatus.READY.hashCode,
				KalturaEntryStatus.PENDING.hashCode, KalturaEntryStatus.PRECONVERT.hashCode);

			KalturaMediaListResponse list = client.getMediaService().list(filter);

			if( list == null || list.totalCount != entryIds.size() )
			{
				// Get each individually
				list = new KalturaMediaListResponse();
				for( String id : entryIds )
				{
					list.objects.add(client.getMediaService().get(id));
				}
			}

			return list;
		}
		catch( KalturaApiException e )
		{
			throw Throwables.propagate(e);
		}
	}

	@Override
	protected void doValidation(EntityEditingSession<EntityEditingBean, KalturaServer> session, KalturaServer ks,
		List<ValidationError> errors)
	{
		addIfEmpty(errors, LangUtils.isEmpty(ks.getName()), "name");
		addIfEmpty(errors, Check.isEmpty(ks.getEndPoint()), "endpoint");
		addIfEmpty(errors, ks.getPartnerId() == 0, "partnerid");
		addIfInvalid(errors, ks.getPartnerId() == -1, "partnerid");
		addIfInvalid(errors, ks.getSubPartnerId() == -1, "subpartnerid");
		addIfEmpty(errors, Check.isEmpty(ks.getAdminSecret()), "adminsecret");
		addIfEmpty(errors, Check.isEmpty(ks.getUserSecret()), "usersecret");
	}

	private void addIfEmpty(List<ValidationError> errors, boolean empty, String field)
	{
		if( empty )
		{
			errors.add(new ValidationError(field, "mandatory"));
		}
	}

	private void addIfInvalid(List<ValidationError> errors, boolean invalid, String field)
	{
		if( invalid )
		{
			errors.add(new ValidationError(field, "invalid"));
		}
	}

	@Override
	public boolean canDelete(BaseEntityLabel kalturaServer)
	{
		return canDelete((Object) kalturaServer);
	}

	private boolean canDelete(Object server)
	{
		Set<String> privs = new HashSet<String>();
		privs.add(KalturaConstants.PRIV_DELETE_KALTURA);
		return !aclManager.filterNonGrantedPrivileges(server, privs).isEmpty();
	}

	@Override
	public boolean canEdit(BaseEntityLabel kalturaServer)
	{
		return canEdit((Object) kalturaServer);
	}

	@Override
	public boolean canEdit(KalturaServer kalturaServer)
	{
		return canEdit((Object) kalturaServer);
	}

	private boolean canEdit(Object kalturaServer)
	{
		Set<String> privs = new HashSet<String>();
		privs.add(KalturaConstants.PRIV_EDIT_KALTURA);
		return !aclManager.filterNonGrantedPrivileges(kalturaServer, privs).isEmpty();
	}

	@Override
	@SecureOnReturn(priv = KalturaConstants.PRIV_EDIT_KALTURA)
	public KalturaServer getForEdit(String kalturaServerUuid)
	{
		return getByUuid(kalturaServerUuid);
	}

	@Override
	public EntityPack<KalturaServer> startEdit(KalturaServer entity)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public KalturaServer stopEdit(EntityPack<KalturaServer> pack, boolean unlock)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String addKalturaServer(KalturaServer kalturaServer)
	{
		EntityPack<KalturaServer> pack = new EntityPack<KalturaServer>();
		pack.setEntity(kalturaServer);
		return add(pack, false).getUuid();
	}

	@Override
	@Transactional
	public void editKalturaServer(String uuid, KalturaServer newServer) throws InvalidDataException
	{
		KalturaServer oldServer = getForEdit(uuid);

		// Common details
		editCommonFields(oldServer, newServer);

		// Other details
		oldServer.setEndPoint(newServer.getEndPoint());
		oldServer.setPartnerId(newServer.getPartnerId());
		oldServer.setSubPartnerId(newServer.getSubPartnerId());
		oldServer.setAdminSecret(newServer.getAdminSecret());
		oldServer.setUserSecret(newServer.getUserSecret());
		oldServer.setKdpUiConfId(newServer.getKdpUiConfId());

		// Validate
		validate(null, oldServer);

		kalturaDao.update(oldServer);
	}

	// The method 'throws RuntimeException', but seeing as that throws
	// declaration is technically superfluous, we can omit it to keep Sonar
	// happy
	@Override
	public List<KalturaUiConf> getPlayers(KalturaServer ks)
	{
		KalturaClient kc;
		try
		{
			kc = getKalturaClient(ks, KalturaSessionType.ADMIN);
			KalturaUiConfFilter uiConfFilter = new KalturaUiConfFilter();
			uiConfFilter.objTypeEqual = KalturaUiConfObjType.PLAYER;
			return kc.getUiConfService().list(uiConfFilter).objects;
		}
		catch( KalturaApiException e )
		{
			Throwables.propagate(e);
		}
		return null;
	}

	@Override
	@Transactional
	public void enable(KalturaServer ks, boolean enable)
	{
		ks.setEnabled(enable);
		kalturaDao.update(ks);
	}

	@Override
	public boolean hasConf(KalturaServer ks, String confId)
	{
		KalturaClient client = null;

		try
		{
			client = getKalturaClient(ks, KalturaSessionType.ADMIN);
			KalturaUiConf conf = client.getUiConfService().get(Integer.parseInt(confId));
			return conf != null;
		}
		catch( KalturaApiException e )
		{
			return false;
		}
	}
}
