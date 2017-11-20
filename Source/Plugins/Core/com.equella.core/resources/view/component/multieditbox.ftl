<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#include "/com.tle.web.sections.standard@/textfield.ftl"/>
<#include "/com.tle.web.sections.standard@/textarea.ftl"/>
<#include "/com.tle.web.sections.standard@/dropdown.ftl"/>

<#if m.localeRows?size gt 1>
	<div class="universaltranslation">	
		<#if m.size lt 2>
			<@textfield section=m.universal />
		<#else>
			<@textarea section=m.universal rows=m.size />
		</#if>
		
		<@render section=m.localeSelector />
		
		<div style="clear: both;"></div>
	</div>
		
	<div class="alltranslations" style="display: none;">
		<#list m.localeRows as row>
			<div class="input text">
				<label for="${row.second.id}">${row.first}</label>
				<#if m.size lt 2>
					<@textfield section=row.second />
				<#else>
					<@textarea section=row.second rows=1 />
				</#if>
				<#if row_index == 0>
					<@render section=m.collapse />
				</#if>
			</div>
		</#list>
	</div>
<#else>
	<div class="singletranslation">
		<#list m.localeRows as row>
			<#if m.size lt 2>
				<@textfield section=row.second />
			<#else>
				<@textarea section=row.second rows=m.size />
			</#if>
		</#list>
	</div>
</#if>	
