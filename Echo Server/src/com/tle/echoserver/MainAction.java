package com.tle.echoserver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

public class MainAction extends DispatchAction
{
	@Override
	public ActionForward unspecified(ActionMapping mapping, ActionForm formData,
		HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		MainForm form = (MainForm) formData;
		form.setMethod("submit");
		return mapping.findForward("main");
	}

	public ActionForward submit(ActionMapping mapping, ActionForm formData,
		HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		MainForm form = (MainForm) formData;

		return mapping.findForward("main");
	}
}
