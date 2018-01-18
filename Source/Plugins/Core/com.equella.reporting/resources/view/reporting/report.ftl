<#include "/com.tle.web.freemarker@/macro/sections.ftl" >
<#include "/com.tle.web.sections.equella@/component/button.ftl" >
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a>

<@css "reporting.css"/>

<@a.div id="report-button-bar">
	<@render s.navBar />
</@a.div>

<@a.div id="reportContent">
	<#if m.showWizard>
		<div class="area">
			<@a.div id="report-params" class="wizard-controls">
				<#list m.wizard as ctrl>
					<@render ctrl.result/>
				</#list>
			</@a.div>
			<div id="reportWizardButtons" class="button-strip">
				<@button section=s.submitButton size="large" />
			</div>
		</div>
	<#else>
		<#if m.showReport>
			<iframe id="reportFrame" src="${m.reportUrl}" style="width: 100%; height:95%;"></iframe>
		</#if>
	</#if>
</@a.div>
	