package com.tle.web.payment.section.region;

import java.util.Locale;
import java.util.Set;

import javax.inject.Inject;

import com.google.common.collect.Sets;
import com.tle.common.payment.entity.Region;
import com.tle.core.guice.Bind;
import com.tle.core.payment.service.RegionService;
import com.tle.core.payment.service.session.RegionEditingBean;
import com.tle.core.services.entity.AbstractEntityService;
import com.tle.core.services.entity.EntityEditingSession;
import com.tle.web.entities.section.AbstractEntityEditor;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.payment.model.CountryListModel;
import com.tle.web.payment.section.region.RegionEditorSection.RegionEditorModel;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.utils.KeyOption;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.MultiSelectionList;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.SimpleHtmlListModel;

@Bind
public class RegionEditorSection extends AbstractEntityEditor<RegionEditingBean, Region, RegionEditorModel>
{
	private static final PluginResourceHelper resources = ResourcesService.getResourceHelper(RegionEditorSection.class);

	@PlugKey("region.preset.select")
	private static String KEY_SELECT_PRESET;

	@Inject
	private RegionService regionService;
	@ViewFactory
	private FreemarkerFactory view;

	@EventFactory
	private EventGenerator events;
	@AjaxFactory
	private AjaxGenerator ajax;

	@Component(stateful = false, name = "cl")
	private MultiSelectionList<Locale> countryList;
	@Component(stateful = false, name = "pl")
	private SingleSelectionList<Set<String>> prepopulateList;

	@Override
	protected SectionRenderable renderFields(RenderEventContext context,
		EntityEditingSession<RegionEditingBean, Region> session)
	{
		return view.createResult("editregion.ftl", this); //$NON-NLS-1$
	}

	@SuppressWarnings("nls")
	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		countryList.setListModel(new CountryListModel());

		SimpleHtmlListModel<Set<String>> prepopulateModel = new SimpleHtmlListModel<Set<String>>();

		prepopulateModel.add(new KeyOption<Set<String>>(KEY_SELECT_PRESET, null, null));
		for( PresetRegions presetRegion : PresetRegions.values() )
		{
			KeyOption<Set<String>> option = new KeyOption<Set<String>>(resources.key(presetRegion.nameKey()),
				presetRegion.nameKey(), presetRegion.countryCodes());
			prepopulateModel.add(option);
		}

		prepopulateList.setListModel(prepopulateModel);

		prepopulateList.addChangeEventHandler(new OverrideHandler(ajax.getAjaxUpdateDomFunction(tree, null,
			events.getEventHandler("preopoulate"), "countries")));
	}

	@EventHandlerMethod
	public void preopoulate(SectionInfo info)
	{

	}

	@Override
	public void register(SectionTree tree, String parentId)
	{
		tree.registerInnerSection(this, parentId);
	}

	@Override
	protected AbstractEntityService<RegionEditingBean, Region> getEntityService()
	{
		return regionService;
	}

	@Override
	protected Region createNewEntity(SectionInfo info)
	{
		return new Region();
	}

	@Override
	protected void loadFromSession(SectionInfo info, EntityEditingSession<RegionEditingBean, Region> session)
	{
		final RegionEditingBean region = session.getBean();
		countryList.setSelectedStringValues(info, region.getCountries());
	}

	@Override
	protected void saveToSession(SectionInfo info, EntityEditingSession<RegionEditingBean, Region> session,
		boolean validate)
	{
		final RegionEditingBean region = session.getBean();

		final Set<String> countries = prepopulateList.getSelectedValue(info);
		final Set<String> beanCountries = region.getCountries();
		beanCountries.clear();

		if( countries != null && countries.size() > 0 )
		{
			beanCountries.addAll(countries);
			prepopulateList.setSelectedStringValue(info, null);
		}
		else
		{
			beanCountries.addAll(countryList.getSelectedValuesAsStrings(info));
		}
	}

	public MultiSelectionList<Locale> getCountryList()
	{
		return countryList;
	}

	public SingleSelectionList<Set<String>> getPrepopulateList()
	{
		return prepopulateList;
	}

	public class RegionEditorModel
		extends
			AbstractEntityEditor<RegionEditingBean, Region, RegionEditorModel>.AbstractEntityEditorModel
	{

	}

	@SuppressWarnings("nls")
	public enum PresetRegions
	{
		AFRICA("region.preset.africa", "AO", "BF", "BI", "BJ", "BV", "BW", "CD", "CF", "CG", "CI", "CM", "CV", "DJ",
			"DZ", "EG", "EH", "ER", "ET", "GA", "GH", "GM", "GN", "GQ", "GW", "HM", "KE", "KM", "LR", "LS", "LY", "MA",
			"MG", "ML", "MR", "MU", "MW", "MZ", "NA", "NE", "NG", "RE", "RW", "SC", "SD", "SH", "SL", "SN", "SO", "ST",
			"SZ", "TD", "TF", "TG", "TN", "TZ", "UG", "YT", "ZA", "ZM", "ZR", "ZW"),

		ASIA("region.preset.asia", "AP", "AE", "AF", "AM", "AZ", "BD", "BH", "BN", "BT", "CC", "CN", "CX", "CY", "GE",
			"HK", "ID", "IL", "IN", "IO", "IQ", "IR", "JO", "JP", "KG", "KH", "KP", "KR", "KW", "KZ", "LA", "LB", "LK",
			"MM", "MN", "MO", "MV", "MY", "NP", "OM", "PH", "PK", "PS", "QA", "RU", "SA", "SG", "SY", "TH", "TJ", "TM",
			"TP", "TR", "TW", "UZ", "VN", "YE"),

		EUROPE("region.preset.europe", "EU", "AD", "AL", "AT", "BA", "BE", "BG", "BY", "CH", "CZ", "DE", "DK", "EE",
			"ES", "FI", "FO", "FR", "FX", "GB", "GI", "GR", "HR", "HU", "IE", "IS", "IT", "LI", "LT", "LU", "LV", "MC",
			"MD", "MK", "MT", "NL", "NO", "PL", "PT", "RO", "SE", "SI", "SJ", "SK", "SM", "UA", "VA", "YU"),

		OCEANIA("region.preset.oceania", "AS", "AU", "CK", "FJ", "FM", "GU", "KI", "MH", "MP", "NC", "NF", "NR", "NU",
			"NZ", "PF", "PG", "PN", "PW", "SB", "TK", "TO", "TV", "UM", "VU", "WF", "WS"),

		NORTH_AMERICA("region.preset.north_america", "CA", "MX", "US"),

		SOUTH_AMERICA("region.preset.south_america", "AG", "AI", "AN", "AR", "AW", "BB", "BM", "BO", "BR", "BS", "BZ",
			"CL", "CO", "CR", "CU", "DM", "DO", "EC", "FK", "GD", "GF", "GL", "GP", "GS", "GT", "GY", "HN", "HT", "JM",
			"KN", "KY", "LC", "MQ", "MS", "NI", "PA", "PE", "PM", "PR", "PY", "SA", "SR", "SV", "TC", "TT", "UY", "VC",
			"VE", "VG", "VI");

		private final String nameKey;
		private final Set<String> countryCodes;

		PresetRegions(String nameKey, String... countryCodes)
		{
			this.nameKey = nameKey;
			this.countryCodes = Sets.newHashSet(countryCodes);
		}

		String nameKey()
		{
			return nameKey;
		}

		Set<String> countryCodes()
		{
			return countryCodes;
		}
	}

}
