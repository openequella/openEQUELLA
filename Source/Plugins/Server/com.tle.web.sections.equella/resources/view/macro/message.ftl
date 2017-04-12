<#ftl strip_whitespace=true />

<#macro warning label="" block=false>
	<div class="alert<#if block> alert-block</#if>" tabindex="0">
		<#if label != "">
			<#if block>
				<h4>${label}</h4>
			<#else>
				<strong>${label}</strong>
			</#if>
		</#if>
		<#nested/>
	</div>
</#macro>

<#macro error label="" block=false>
	<div class="alert alert-error<#if block> alert-block</#if>" tabindex="0">
		<#if label != "">
			<#if block>
				<h4>${label}</h4>
			<#else>
				<strong>${label}</strong>
			</#if>
		</#if>
		<#nested/>
	</div>
</#macro>

<#macro success label="" block=false>
	<div class="alert alert-success<#if block> alert-block</#if>" tabindex="0">
		<#if label != "">
			<#if block>
				<h4>${label}</h4>
			<#else>
				<strong>${label}</strong>
			</#if>
		</#if>
		<#nested/>
	</div>
</#macro>

<#macro info label="" block=false>
	<div class="alert alert-info<#if block> alert-block</#if>" tabindex="0">
		<#if label != "">
			<#if block>
				<h4>${label}</h4>
			<#else>
				<strong>${label}</strong>
			</#if>
		</#if>
		<#nested/>
	</div>
</#macro>