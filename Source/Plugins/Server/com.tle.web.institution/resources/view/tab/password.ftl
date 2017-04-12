<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.standard@/textfield.ftl">
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>

<h2>${b.gkey("institutions.password.link.name")}</h2>
<@settingContainer false>
	<#if m.changeSuccessful>
		<@setting label="" error=b.gkey("institutions.password.changed")/>
	</#if>
	
	<#if m.requiresInitialPassword>
		<@setting label="" error=b.gkey("institutions.password.setpass")/>
	<#else>
		<@setting label=b.gkey("institutions.password.column.old") error=m.error labelFor=s.oldPassword>
			<@textfield password=true section=s.oldPassword class="detail" />
		</@setting>
	</#if>
	
	<@setting label=b.gkey("institutions.password.column.password") labelFor=s.adminPassword >
		<@textfield password=true section=s.adminPassword class="detail" />
	</@setting>
	<@setting label=b.gkey("institutions.password.column.confirm") labelFor=s.confirmPassword >
		<@textfield password=true section=s.confirmPassword class="detail" autoSubmitButton=s.changeButton/>
	</@setting>
</@settingContainer>

<div class="button-strip">
	<@button section=s.changeButton showAs="save" />
</div>


<#assign PART_FUNCTION_DEFINITIONS>
function confirmAction(p1, p2, p3)
{
	if( p1 != p2 )
	{
		alert("${b.gkey("institutions.password.match")?js_string}");
	}
	else if( p1.length == 0 )
	{
		alert("${b.gkey("institutions.password.empty")?js_string}");
	}
	else if( p3 != undefined && p3.value.length == 0 )
	{
		alert("${b.gkey("institutions.password.oldempty")?js_string}");
	}
	else if( confirm("${b.gkey("institutions.password.proceed")?js_string}") )
	{
		return true;
	}
	return false;
}
</#assign>