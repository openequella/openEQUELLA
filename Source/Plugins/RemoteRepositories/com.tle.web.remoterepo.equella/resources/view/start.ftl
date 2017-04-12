<#ftl />
<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#include "/com.tle.web.sections.standard@/checklist.ftl"/>
<#include "/com.tle.web.sections.standard@/dialog.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>

<@css "start.css"/>

<@render s.selectionsMadeFunction />

<div class="area">
	<h2><@bundlekey "start.title"/></h2>

	<@settingContainer mandatory=false>
		<@setting label=b.key('start.selection.title')>
			<@dialog section=s.selectionDialog />
			<@button section=s.selectionDialog.opener class="selectitem" size="medium"><@bundlekey "start.select"/></@button>
			
			<span class="currentselection">
			<#if m.selection??>
				<@bundlekey value="start.selection.current" />
				<a href="${m.selection.url}" target="_blank">${m.selection.name}</a>
			<#else>
				<@bundlekey value="start.selection.none" />
			</#if>
			</span>
			
		</@setting>
	
		<@setting label=b.key('start.downloadoption.title')>
			<div class="input radio">
				<@checklist section=s.downloadOptions list=true />
			</div>	
		</@setting>
	</@settingContainer>
	
	<#if m.selection??>
		<div class="button-strip">
			<@render s.startDownloadButton><@bundlekey "start.download"/></@render>
		</div> 
	</#if>
</div>