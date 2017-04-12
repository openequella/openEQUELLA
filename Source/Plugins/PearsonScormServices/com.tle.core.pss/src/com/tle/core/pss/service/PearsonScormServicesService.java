package com.tle.core.pss.service;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.tle.beans.item.IItem;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.beans.system.PearsonScormServicesSettings;
import com.tle.web.viewurl.ResourceViewerConfig;

public interface PearsonScormServicesService
{
	boolean isEnabled();

	void addScormPackage(Item item, CustomAttachment scormAttachment);

	void deleteScormPackage(Item item, boolean log);

	Map<String, String> pseudoLaunchScormPackage(HttpServletRequest req, IItem<?> item, IAttachment attachment,
		String pssReturnUrl, ResourceViewerConfig config);

	PearsonScormServicesSettings getPearsonScormServicesSettings();

	String getLaunchURL();

	boolean pingConnection(PearsonScormServicesSettings settings);
}
