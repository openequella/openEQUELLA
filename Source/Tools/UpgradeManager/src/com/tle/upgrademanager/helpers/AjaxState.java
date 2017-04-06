package com.tle.upgrademanager.helpers;

import java.util.List;

public interface AjaxState
{
	public void addError(String id, String message);

	public void addErrorRaw(String id, String message);

	public void addBasic(String id, String message);

	public void addHeading(String id, String message);

	public void addConsole(String id, String message);

	public void start(String id, String message);

	public void start(String id);

	public void finish(String id, String message, String redirect);

	public List<AjaxMessage> getListOfAllMessages(String id);
}
