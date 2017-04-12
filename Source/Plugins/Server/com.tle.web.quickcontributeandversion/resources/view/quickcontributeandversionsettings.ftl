<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>
<#import "/com.tle.web.sections.standard@/list.ftl" as l />
<#include "/com.tle.web.sections.standard@/ajax.ftl"/>
<#include "/com.tle.web.sections.standard@/radio.ftl"/>

<@css "quickcontributeandversionsettings.css" />
<@css path="itemlist.css" plugin="com.tle.web.itemlist" />

<div class="area">
	<h2>${b.key('quickcontribute.title')}</h2>
	<@settingContainer mandatory=false>
		<@setting section=s.collectionSelector label=b.key('quickcontributeandversionsettings.collection.description')/>
	</@>

	<div class="spacer"></div>
	<div class="spacer"></div>

	<h2>${b.key('versionselection.title')}</h2>
	<@settingContainer mandatory=false>
		<@setting label=b.key('versionselection.checklist.label')>
			<#assign countt=0 />
			<ul>
			<@l.boollist section=s.versionViewOptions; opt, state>
				<#if countt == 2>
					<div class="defaultstext">${b.key('quickcontributeandversionsettings.allowuserchoice')}</div>
				</#if>
				<li><@radio state /></li>
				<#assign countt = countt +1 />
			</@>
			</ul>
		</@>
	</@>
	<div class="spacer"></div>
	<div class="spacer"></div>
	
	<h2>${b.key('selectionoption.title')}</h2>
	<@settingContainer mandatory=false>
		<@setting label='' help=b.key('selectionsettings.disablebutton.description')>
			<div class="input checkbox"><@render s.disable />
			</div>
		</@setting>
	
	</@settingContainer>
	
	
	<div class="spacer"></div>

	<div class="button-strip">
		<@button section=s.saveButton showAs="save" />
	</div>
</div>