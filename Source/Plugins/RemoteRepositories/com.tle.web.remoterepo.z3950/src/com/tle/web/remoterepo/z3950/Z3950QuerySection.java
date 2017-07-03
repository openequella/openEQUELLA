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

package com.tle.web.remoterepo.z3950;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.google.common.base.Strings;
import com.tle.beans.entity.FederatedSearch;
import com.tle.beans.search.Z3950Settings;
import com.tle.beans.search.Z3950Settings.AttributeProfile;
import com.tle.common.Check;
import com.tle.common.NameValue;
import com.tle.common.Pair;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.i18n.BundleCache;
import com.tle.core.i18n.BundleNameValue;
import com.tle.core.remoterepo.z3950.Z3950Constants.Operator;
import com.tle.core.remoterepo.z3950.service.Z3950Service;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.remoterepo.section.RemoteRepoQuerySection;
import com.tle.web.remoterepo.service.RemoteRepoWebService;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.NameValueOption;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.sections.standard.model.SimpleHtmlListModel;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
public class Z3950QuerySection extends RemoteRepoQuerySection<Z3950SearchEvent>
{
	private static final PluginResourceHelper resources = ResourcesService.getResourceHelper(Z3950QuerySection.class);

	@Inject
	private BundleCache bundleCache;
	@Inject
	private RemoteRepoWebService repoWebService;
	@Inject
	private Z3950Service z3950Service;

	@Component
	private TextField term2;
	@Component
	private TextField term3;
	@Component
	private SingleSelectionList<NameValue> use1;
	@Component
	private SingleSelectionList<NameValue> use2;
	@Component
	private SingleSelectionList<NameValue> use3;
	@Component
	private SingleSelectionList<Pair<String, Operator>> op2;
	@Component
	private SingleSelectionList<Pair<String, Operator>> op3;

	@ViewFactory
	private FreemarkerFactory view;

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		final FederatedSearch search = repoWebService.getRemoteRepository(context);
		final Z3950Settings settings = new Z3950Settings();
		settings.load(search);
		if( settings.isAdvanced() )
		{
			getModel(context).setTitle(CurrentLocale.get(search.getName()));
			List<NameValue> fields;
			if( !Check.isEmpty(settings.getAdvancedSearchFields()) )
			{
				fields = z3950Service.convertAdvancedFieldsXml(settings.getAdvancedSearchFields(), bundleCache);

			}
			else
			{
				fields = z3950Service.listDefaultFields(AttributeProfile.EQUELLA);
			}
			SimpleHtmlListModel<NameValue> fieldModel = new SimpleHtmlListModel<NameValue>(fields);
			use1.setListModel(fieldModel);
			use2.setListModel(fieldModel);
			use3.setListModel(fieldModel);

			return view.createResult("extraquery.ftl", this);
		}
		return super.renderHtml(context);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		List<Pair<String, Operator>> ops = new ArrayList<Pair<String, Operator>>();
		ops.add(op("operator.and", Operator.AND));
		ops.add(op("operator.or", Operator.OR));
		ops.add(op("operator.andnot", Operator.ANDNOT));
		SimpleHtmlListModel<Pair<String, Operator>> opModel = new SimpleHtmlListModel<Pair<String, Operator>>(ops)
		{
			@Override
			protected Option<Pair<String, Operator>> convertToOption(Pair<String, Operator> obj)
			{
				return new NameValueOption<Pair<String, Operator>>(new BundleNameValue(obj.getFirst(), obj.getSecond()
					.toString(), bundleCache), obj);
			}
		};
		op2.setListModel(opModel);
		op3.setListModel(opModel);
	}

	private Pair<String, Operator> op(String key, Operator op)
	{
		return new Pair<String, Operator>(resources.key("search." + key), op);
	}

	@Override
	public void prepareSearch(SectionInfo info, Z3950SearchEvent event) throws Exception
	{
		final FederatedSearch search = repoWebService.getRemoteRepository(info);
		final Z3950Settings settings = new Z3950Settings();
		settings.load(search);
		if( settings.isAdvanced() )
		{
			String t = Strings.nullToEmpty(getQueryField().getValue(info)).trim();

			String u1 = use1.getSelectedValue(info).getValue();
			event.addExtra(u1, t, null);

			String t2 = Strings.nullToEmpty(term2.getValue(info)).trim();
			if( !Check.isEmpty(t2) )
			{
				String u2 = use2.getSelectedValue(info).getValue();
				Operator o2 = op2.getSelectedValue(info).getSecond();
				event.addExtra(u2, t2, o2);

				String t3 = Strings.nullToEmpty(term3.getValue(info)).trim();
				if( !Check.isEmpty(t3) )
				{
					String u3 = use3.getSelectedValue(info).getValue();
					Operator o3 = op3.getSelectedValue(info).getSecond();
					event.addExtra(u3, t3, o3);
				}
			}
		}
		else
		{
			super.prepareSearch(info, event);
		}
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new Z3950QueryModel();
	}

	public TextField getTerm2()
	{
		return term2;
	}

	public TextField getTerm3()
	{
		return term3;
	}

	public SingleSelectionList<NameValue> getUse1()
	{
		return use1;
	}

	public SingleSelectionList<NameValue> getUse2()
	{
		return use2;
	}

	public SingleSelectionList<NameValue> getUse3()
	{
		return use3;
	}

	public SingleSelectionList<Pair<String, Operator>> getOp2()
	{
		return op2;
	}

	public SingleSelectionList<Pair<String, Operator>> getOp3()
	{
		return op3;
	}

	public static class Z3950QueryModel extends RemoteRepoQuerySection.RemoteRepoQueryModel
	{
		@Bookmarked(name = "adv")
		private boolean advanced;

		public boolean isAdvanced()
		{
			return advanced;
		}

		public void setAdvanced(boolean advanced)
		{
			this.advanced = advanced;
		}
	}
}
