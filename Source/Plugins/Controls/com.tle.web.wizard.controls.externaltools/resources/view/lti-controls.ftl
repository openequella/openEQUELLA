<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl" />
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a/>
<#include "/com.tle.web.sections.standard@/link.ftl"/>
<#include "/com.tle.web.sections.standard@/dropdown.ftl" />
<#include "/com.tle.web.sections.standard@/textarea.ftl"/>

<@css path="universalresource.css"  plugin="com.tle.web.wizard.controls.universal"/>

<#macro lticontrols>
	<@a.div id="ltidiv" class="settingContainer">
		<div class="input select" id="querytags">
			<@setting labelFor=s.ltiSelector section=s.ltiSelector label=b.key('handlers.lti.select.lti') error=m.errors["nolaunchurl"]/>
		</div>

		<@setting label=b.key("handlers.lti.launchurl") section=s.launchUrl error=m.errors["badlaunchurl"] />

		<#if m.hideMismatchWarning>
			<@render id="warningDiv" section=s.warningDiv style="display:none">
				<@render m.matchUrlWarning />
				<label>${b.key("handlers.lti.warning.unmatched")}</label>
			</@render>
		<#else>
			<@render id="warningDiv" section=s.warningDiv>
				<@render m.matchUrlWarning />
				<label>${b.key("handlers.lti.warning.unmatched")}</label>
			</@render>
		</#if>

		<h3>${b.key("handlers.lti.advanced")}</h3>

		<@setting label=b.key("handlers.lti.consumerkey") section=s.consumerKey />
		<@setting label=b.key("handlers.lti.sharedsecret") section=s.sharedSecret />
		<@setting label=b.key("handlers.lti.customparams") help=b.key("handlers.lti.help.customparams") >
			<@textarea section=s.customParams rows=3 />
		</@setting>
		<@setting label=b.key("handlers.lti.iconurl") section=s.iconUrl error=m.errors["badiconurl"] help=b.key("handlers.lti.help.iconurl") />

		<div id="sharetags">
			<@setting label=b.key("handlers.lti.privacy")  help=b.key("handlers.lti.help.default") >
				<div class="input checkbox"><@render section=s.useDefaultPrivacy /></div>
			</@setting>
			<div class="subsiduaryboxes">
				<@setting label='' help=b.key("handlers.lti.help.name")> 
					<div class="input checkbox"><@render section=s.shareName /></div>
				</@setting>
				<@setting label=''  help=b.key("handlers.lti.help.email") >
					<div class="input checkbox"><@render section=s.shareEmail /></div>
				</@setting>
			</div>
		</div>

	</@>
</#macro>
