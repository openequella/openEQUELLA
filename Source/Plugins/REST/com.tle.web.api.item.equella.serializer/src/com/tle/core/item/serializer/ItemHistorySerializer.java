package com.tle.core.item.serializer;

import javax.inject.Singleton;

import com.tle.beans.item.HistoryEvent;
import com.tle.core.guice.Bind;
import com.tle.web.api.interfaces.beans.UserBean;
import com.tle.web.api.item.interfaces.beans.HistoryEventBean;

/**
 * @author Aaron
 */
@Bind
@Singleton
public class ItemHistorySerializer
{
	public HistoryEventBean serialize(HistoryEvent event)
	{
		final HistoryEventBean bean = new HistoryEventBean();
		UserBean user = new UserBean();
		user.setId(event.getUser());
		bean.setUser(user);
		bean.setDate(event.getDate());
		// bean.setApplies(event.isApplies());
		bean.setComment(event.getComment());
		bean.setState(event.getState().toString());
		bean.setStep(event.getStep());
		bean.setStepName(event.getStepName());
		bean.setToStep(event.getToStep());
		bean.setToStepName(event.getToStepName());
		bean.setType(event.getType().toString());
		return bean;
	}
}
