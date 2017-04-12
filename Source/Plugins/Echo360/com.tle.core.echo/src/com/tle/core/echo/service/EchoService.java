package com.tle.core.echo.service;

import com.dytech.edge.exceptions.InvalidDataException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.core.echo.entity.EchoServer;
import com.tle.core.services.entity.AbstractEntityService;
import com.tle.core.services.entity.EntityEditingBean;

public interface EchoService extends AbstractEntityService<EntityEditingBean, EchoServer>
{
	@Override
	boolean canEdit(BaseEntityLabel echoServer);

	@Override
	boolean canEdit(EchoServer echoServer);

	@Override
	boolean canDelete(BaseEntityLabel echoServer);

	@Override
	boolean canDelete(EchoServer echoServer);

	EchoServer getForEdit(String uuid);

	String addEchoServer(EchoServer es) throws InvalidDataException;

	void editEchoServer(String uuid, EchoServer es) throws InvalidDataException;

	String getAuthenticatedUrl(String esn, String redirectUrl);

	ObjectMapper getMapper();
}
