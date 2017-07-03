/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.cal.web.viewitem.summary;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.tle.beans.activation.ActivateRequest;
import com.tle.beans.cal.CALHolding;
import com.tle.beans.cal.CALPortion;
import com.tle.beans.cal.CALSection;
import com.tle.cal.CALConstants;
import com.tle.cal.service.CALService;
import com.tle.common.Check;
import com.tle.common.i18n.LangUtils;
import com.tle.core.activation.service.ActivationService;
import com.tle.core.activation.validation.PageCounter;
import com.tle.core.activation.validation.PageCounter.RangeCounter;
import com.tle.core.copyright.exception.CopyrightViolationException;
import com.tle.core.guice.Bind;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.web.copyright.CopyrightOverrideSection;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.AjaxGenerator.EffectType;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.viewitem.section.ParentViewItemSectionUtils;
import com.tle.web.viewitem.summary.content.AbstractContentSection;
import com.tle.web.viewitem.summary.section.ItemSummaryContentSection;

@Bind
public class CALPercentageOverrideSection extends AbstractContentSection<CALPercentageOverrideSection.Model>
	implements
		CopyrightOverrideSection
{
	@PlugKey("override.message")
	private static String KEY_OVERRIDE_MESSAGE;
	@PlugKey("override.timeout")
	private static String KEY_ERROR_TIMEOUT;

	@Component
	private Button continueButton;
	@Component(stateful = false)
	private TextField reasonTextField;
	@Component
	private Button cancelButton;

	@ViewFactory
	private FreemarkerFactory viewFactory;
	@Inject
	private ActivationService activationService;
	@AjaxFactory
	private AjaxGenerator ajax;
	@Inject
	private CALService calService;

	@TreeLookup
	private ItemSummaryContentSection summarySection;
	@TreeLookup
	private CALActivateSection calActivateSection;

	private final Cache<String, Map<Long, List<ActivateRequest>>> requestCache = CacheBuilder.newBuilder()
		.expireAfterWrite(30, TimeUnit.MINUTES).softValues().build();

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		Model model = getModel(context);
		if( model.getException() != null )
		{
			return calActivateSection.fatalError(this);
		}

		if( model.getActivePercent() == 0 )
		{
			model.setActivePercent(calculatePercentage(context));
		}
		model.setOverrideMessage(new KeyLabel(KEY_OVERRIDE_MESSAGE, model.getActivePercent()));
		continueButton.setDisabled(context, Check.isEmpty(reasonTextField.getValue(context)));
		return viewFactory.createResult("overridereason.ftl", this);
	}

	private double calculatePercentage(SectionInfo info)
	{
		CALHolding holding = calService.getHoldingForItem(ParentViewItemSectionUtils.getItemInfo(info).getItem());
		int totalPages = PageCounter.countTotalPages(holding.getLength());
		RangeCounter counter = new RangeCounter();
		for( CALPortion portion : holding.getCALPortions() )
		{
			for( CALSection section : portion.getCALSections() )
			{
				String copyrightStatus = section.getCopyrightStatus();
				if( (copyrightStatus == null || copyrightStatus.equals(CALConstants.CAL_COPYRIGHTSTATUS))
					&& isActiveOrCurrent(info, portion, section) )
				{
					PageCounter.processRange(section.getRange(), counter);
				}
			}
		}
		return (counter.getTotal() / (double) totalPages) * 100;
	}

	private boolean isActiveOrCurrent(SectionInfo info, CALPortion portion, CALSection section)
	{
		if( activationService.isActive(CALConstants.ACTIVATION_TYPE, portion.getItem(), section.getAttachment()) )
		{
			return true;
		}
		Map<Long, List<ActivateRequest>> requestMap = requestCache.getIfPresent(getCacheKey(info));
		return requestMap.containsKey(portion.getItem().getId());
	}

	@Override
	@SuppressWarnings("nls")
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		continueButton.setClickHandler(events.getNamedHandler("activate"));
		cancelButton.setClickHandler(events.getNamedHandler("cancel"));
		StatementHandler reasonTextFieldKeyupHandler = new StatementHandler(ajax.getAjaxUpdateDomFunction(tree, this,
			null, ajax.getEffectFunction(EffectType.REPLACE_IN_PLACE), "button-ajax"));
		reasonTextField.setEventHandler(JSHandler.EVENT_KEYUP, reasonTextFieldKeyupHandler);
	}

	@EventHandlerMethod
	public void activate(SectionInfo info)
	{
		Map<Long, List<ActivateRequest>> requestMap = requestCache.getIfPresent(getCacheKey(info));
		if( requestMap == null )
		{
			throw new CopyrightViolationException(LangUtils.createTempLangugageBundle(KEY_ERROR_TIMEOUT));
		}
		for( Map.Entry<Long, List<ActivateRequest>> entry : requestMap.entrySet() )
		{
			for( ActivateRequest ar : entry.getValue() )
			{
				ar.setOverrideReason(reasonTextField.getValue(info));
			}
		}
		try
		{
			activationService.activateAll(CALConstants.ACTIVATION_TYPE, requestMap, true);
			if( calActivateSection.updateSelectionSession(info, requestMap) )
			{
				return;
			}
		}
		catch( CopyrightViolationException we )
		{
			getModel(info).setException(we);
			info.preventGET();
			return;
		}
		summarySection.setSummaryId(info, null);
		ParentViewItemSectionUtils.getItemInfo(info).refreshItem(true);
	}

	@EventHandlerMethod
	public void cancel(SectionInfo info)
	{
		requestCache.invalidate(getCacheKey(info));
		calActivateSection.cancel(info);
	}

	@Override
	public void doOverride(SectionInfo info, Map<Long, List<ActivateRequest>> requestMap)
	{
		requestCache.put(getCacheKey(info), requestMap);
		summarySection.setSummaryId(info, this);
	}
	
	private String getCacheKey(SectionInfo info)
	{
		Model model = getModel(info);
		if( model.getCollisionAvoidance() == null )
		{
			// random uuid to avoid single user collisions on same session
			model.setCollisionAvoidance(UUID.randomUUID().toString());
		}
		return CurrentUser.getSessionID() + model.getCollisionAvoidance();
	}

	@Override
	public Class<Model> getModelClass()
	{
		return Model.class;
	}

	public Button getContinueButton()
	{
		return continueButton;
	}

	public TextField getReasonTextField()
	{
		return reasonTextField;
	}

	public Button getCancelButton()
	{
		return cancelButton;
	}

	public static class Model
	{
		private CopyrightViolationException exception;
		private Label overrideMessage;
		@Bookmarked(stateful = true, name = "cad")
		private String collisionAvoidance;
		@Bookmarked(stateful = false)
		private double activePercent;

		public Label getOverrideMessage()
		{
			return overrideMessage;
		}

		public void setOverrideMessage(Label overrideMessage)
		{
			this.overrideMessage = overrideMessage;
		}

		public CopyrightViolationException getException()
		{
			return exception;
		}

		public void setException(CopyrightViolationException exception)
		{
			this.exception = exception;
		}

		public double getActivePercent()
		{
			return activePercent;
		}

		public void setActivePercent(double activePercent)
		{
			this.activePercent = activePercent;
		}

		public String getCollisionAvoidance()
		{
			return collisionAvoidance;
		}

		public void setCollisionAvoidance(String collisionAvoidance)
		{
			this.collisionAvoidance = collisionAvoidance;
		}

	}
}
