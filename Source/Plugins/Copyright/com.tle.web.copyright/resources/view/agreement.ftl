<#include "/com.tle.web.freemarker@/macro/sections.ftl">

<@css "agreement.css"/>

<div id="copyright-agreement" class="area <#if m.standardAgreement>agreement-standard <#if m.inIntegration>integration</#if><#else>agreement-custom</#if>">	
	<div id="agreement-focus" class="focus" tabIndex="0">
		<@render m.agreement/>
	</div> 
	<#if m.buttons??>
		<div class="button-strip">
			<@render m.buttons />
		</div>
	</#if>
</div>