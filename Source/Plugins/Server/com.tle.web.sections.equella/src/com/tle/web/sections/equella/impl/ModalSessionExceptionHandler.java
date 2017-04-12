package com.tle.web.sections.equella.impl;

import javax.inject.Inject;

import com.tle.web.sections.equella.AbstractModalSessionExceptionHandler;
import com.tle.web.sections.equella.AbstractModalSessionServiceImpl;
import com.tle.web.sections.equella.ModalSession;
import com.tle.web.sections.equella.ModalSessionService;

/**
 * @author aholland
 */
public class ModalSessionExceptionHandler extends AbstractModalSessionExceptionHandler<ModalSession>
{
	@Inject
	private ModalSessionService modalService;

	@Override
	protected AbstractModalSessionServiceImpl<ModalSession> getModalService()
	{
		return (ModalSessionServiceImpl) modalService;
	}
}
