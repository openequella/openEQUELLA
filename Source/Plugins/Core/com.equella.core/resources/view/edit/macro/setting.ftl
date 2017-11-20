<#macro setting section key mandatory=false errorKey="__undef"><#t/>
<#local isErr = (errorKey != "__undef" && m.errors[errorKey]??) /><#t/>
<div>
	<fieldset <#if isErr>class="ctrlinvalid"</#if>>
		<#if isErr>
			<p class="ctrlinvalidmessage">${m.errors[errorKey]?html}</p>
		</#if>
		<h3><@bundlekey key/><#if mandatory><span class="ctrlmandatory">*</span></#if></h3>
		<@render section />
	</fieldset>
</div><#t/>
</#macro>