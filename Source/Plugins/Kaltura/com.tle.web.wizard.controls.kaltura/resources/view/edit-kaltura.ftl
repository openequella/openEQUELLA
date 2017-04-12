<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl" />
<#include "/com.tle.web.wizard.controls.universal@/common-edit-handler.ftl" />
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a />

<@css path="kaltura.css" hasRtl=true />

<@detailArea >
	<@a.div id="mediapreview">
		<#assign dataurl = m.specificDetail['dataurl'].second >
		<#if dataurl??>
			<div class="preview-container">
				<@render section=s.divKdp />
			</div>
		</#if>
	</@a.div>
	<@editArea>
		<#if m.showPlayers>
			<@setting label=b.key('edit.players.label') labelFor=s.players help=b.key('edit.players.help.label')>
					 <@render section=s.players />
				</@setting>
		</#if>		
	</@editArea>
</@detailArea>

<@detailList />

<br clear="both">