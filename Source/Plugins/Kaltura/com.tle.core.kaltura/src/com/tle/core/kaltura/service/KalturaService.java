package com.tle.core.kaltura.service;

import java.util.Collection;
import java.util.List;

import com.dytech.edge.exceptions.InvalidDataException;
import com.kaltura.client.KalturaApiException;
import com.kaltura.client.KalturaClient;
import com.kaltura.client.enums.KalturaSessionType;
import com.kaltura.client.types.KalturaMediaEntry;
import com.kaltura.client.types.KalturaMediaListResponse;
import com.kaltura.client.types.KalturaUiConf;
import com.tle.common.kaltura.entity.KalturaServer;
import com.tle.common.kaltura.service.RemoteKalturaService;
import com.tle.core.services.entity.AbstractEntityService;
import com.tle.core.services.entity.EntityEditingBean;

public interface KalturaService extends AbstractEntityService<EntityEditingBean, KalturaServer>, RemoteKalturaService
{
	KalturaMediaListResponse searchMedia(KalturaClient client, Collection<String> keywords, int page, int limit);

	KalturaMediaEntry getMediaEntry(KalturaClient client, String entryId);

	KalturaMediaListResponse getMediaEntries(KalturaClient client, List<String> entryIds);

	KalturaClient getKalturaClient(KalturaServer kalturaServer, KalturaSessionType type) throws KalturaApiException;

	KalturaUiConf getDefaultKcwUiConf(KalturaClient client);

	KalturaUiConf getDefaultKdpUiConf(KalturaServer ks);

	boolean testKalturaSetup(KalturaServer kalturaServer, KalturaSessionType type) throws KalturaApiException;

	String addKalturaServer(KalturaServer kalturaServer) throws InvalidDataException;

	void editKalturaServer(String ksUuid, KalturaServer kalturaServer) throws InvalidDataException;

	KalturaServer getForEdit(String kalturaServerUuid);

	List<KalturaUiConf> getPlayers(KalturaServer ks);

	void enable(KalturaServer ks, boolean enable);

	boolean hasConf(KalturaServer ks, String confId);

	boolean isUp(KalturaServer ks);
}
