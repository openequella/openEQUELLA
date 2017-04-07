package com.tle.web.api.interfaces;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.tle.web.api.interfaces.beans.BaseEntityBean;
import com.tle.web.api.interfaces.beans.SearchBean;
import com.tle.web.api.interfaces.beans.security.BaseEntitySecurityBean;

public interface BaseEntityResource<BEB extends BaseEntityBean, SB extends BaseEntitySecurityBean>
{
	@GET
	SearchBean<BEB> list(UriInfo uriInfo);

	@GET
	SB getAcls(UriInfo uriInfo);

	@PUT
	Response editAcls(UriInfo uriInfo, SB security);

	@GET
	BEB get(UriInfo uriInfo, String uuid);

	@DELETE
	Response delete(UriInfo uriInfo, String uuid);

	@POST
	Response create(UriInfo uriInfo, BEB bean, String stagingUuid);

	@PUT
	Response edit(UriInfo uriInfo, String uuid, BEB bean, String stagingUuid, String lockId, boolean keepLocked);

	@GET
	Response getLock(UriInfo uriInfo, String uuid);

	@POST
	Response lock(UriInfo uriInfo, String uuid);

	@DELETE
	Response unlock(UriInfo uriInfo, String uuid);
}
