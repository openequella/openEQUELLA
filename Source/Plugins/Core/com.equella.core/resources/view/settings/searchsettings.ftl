<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>
<#include "/com.tle.web.sections.standard@/checklist.ftl"/>
<#include "/com.tle.web.sections.standard@/textfield.ftl"/>

<@css "settings/searchsettings.css"/>

<div class="area">
	<h2>${b.key('searching.settings.title')}</h2>
	<@settingContainer mandatory=false wide=true>
		<@setting label=b.key('settings.label.defaultsort') section=s.defaultSortType />
		<@setting label=b.key('settings.label.shownonlive') help=b.key('settings.help.shownonlive') section=s.showNonLiveCheckbox />
		<@setting label=b.key('settings.label.authenticatefeeds') help=b.key('settings.help.authenticatefeeds') section=s.authenticateByDefault />
	</@>

	<div class="spacer"></div>
	
	<h2>${b.key('settings.boost.title')}</h2>
	<p>${b.key('settings.boost.term.help')}</p>
	
	<#if m.notSuccessful && m.errors['boostValueZero']?? >
		<div class ="ctrlinvalid">
	<#else>
		<div>
	</#if>
		<div class ="term-row">
			<@textfield section=s.titleBoost hidden=true/>
			<div class ="term-label">${b.key('settings.boost.term.title')}</div>
			<div class ="slider"><div id="title-slider"></div></div>
		</div>	
		
		<div class ="term-row">
			<@textfield section=s.descriptionBoost hidden=true/>
			<div class ="term-label">${b.key('settings.boost.term.description')}</div>
			<div class ="slider"><div id ="description-slider"></div></div>
		</div>
		
		<div class ="term-row">
			<@textfield section=s.attachmentBoost hidden=true/>
			<div class ="term-label">${b.key('settings.boost.term.attachment')}</div>
			<div class ="slider"><div id="attachment-slider"></div></div>
		</div>
	
	<#if m.notSuccessful && m.errors['boostValueZero']?? >
		<div class="error">
			<p>${m.errors['boostValueZero']}</p>
		</div>
	</#if>
	</div>	
		
	<div class="spacer"></div>

	<h2>${b.key('settings.freetext.title')}</h2>
	<@settingContainer mandatory=false wide=true>
		<@setting label=b.key('settings.options.label') labelFor=s.harvestOptions>
			<@checklist section=s.harvestOptions list=true/>
		</@setting>
	</@settingContainer>
	
	<div class="spacer"></div>
	
	<@renderList m.extensions>
		<div class="spacer"></div>
	</@renderList>
	
	<div class="spacer"></div>

	<h2>${b.key('settings.views.title')}</h2>
	<@settingContainer mandatory=false wide=true>
		<@setting label=b.key('settings.label.gallery') help=b.key('settings.help.gallery') section=s.disableGalleryView />
		<@setting label=b.key('settings.label.videos') help=b.key('settings.help.videos') section=s.disableVideosView />
		<@setting label=b.key('settings.label.disablefilecount') help=b.key('settings.help.disablefilecount') section=s.disableFileCountCheckbox />
		<div class="spacer"></div>
	</@>

	<div class="button-strip">
		<@button section=s.saveButton showAs="save"/>
	</div>

	<hr>

	<h2>${b.key('settings.filter.title')}</h2>
	<@render s.filtersTable />
</div>