<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a/>

<@css path="wizard.css" hasRtl=true />

<#assign TEMP_body>
	<@renderList list=m.sections/>
	<@a.div id="wizard-navigation">
		<@button section=s.previousButton showAs="prev" size="medium"/>
		<@button section=s.nextButton showAs="next" size="medium" />
	</@a.div>
</#assign>

<#assign TEMP_right>
<script>
	$(document).ready(function(){
		var offset = ($("#affix-div").offset().top) - 55;
		$("#affix-div").attr("data-offset-top",offset);
	}); 
</script>

	<div id="affix-div" data-spy="affix">
		<@a.div id="wizard-major-actions">
		<#list m.majorActions as action>
			<@render action/>
		</#list>
	</@a.div>
	
	<@a.div id="wizard-actions">
		<#list m.minorActions as action>
			<#if action_index gt 0><span>|</span></#if>
			<@render action/>
		</#list>
	</@a.div>
	
	<#if s.additionalActions?has_content>
		<#list m.additionalActions as action>			
			<@render action/>
		</#list>
	</#if>

	<#if m.tabNavigation>
		<div id="wizard-pagelist">
			<ul>
				<#list m.tabs as tab>
					<@a.div id="wizard-pagelist-page" writediv=false collection=true>
						<#if tab.active>
							<li id="${tab.id?html}" class="active"><@render tab.content/></li>
						<#else>
							<li id="${tab.id?html}"><@render tab.content/></li>
						</#if>
					</@a.div>	
				</#list>
			</ul>
		</div>
	</#if>
	
	<div id="more-actions">
		<#list m.moreActions as action>
			<ul>
				<li><div class ="action-link"> <@render action/></div></li>
			</ul>
		</#list>
	</div >
		
	</div>
</#assign>