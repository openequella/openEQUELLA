<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/component/button.ftl" />
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a/>

<@css path="bulkscript.css" hasRtl=true />

<h3>${b.key("scriptdialog.title")}</h3>

<@a.div id="editor">
	<div class="btn-group load-action">
		<@render section=s.scripts />
	</div>
	<@render section=s.scriptEditor />
</@a.div>

<@button section=s.validateScriptButton showAs="verify"/><br/>
<@a.div id="errordisplay" showEffect="slide"> 
	<#if m.validationErrors>
		<div id="validationstatus" class="fail">
			<span></span>
		</div>
		<div id="errormessage">
			<pre>${m.errorMessage}</pre>
		</div>
	<#elseif m.validationRan && !m.validationErrors>
		<div id="validationstatus" class="ok">
			<span></span>
		</div>
		<div id="okmessage">
			<pre>${b.key("script.okmessage")}</pre>
		</div>
	<#else>
		<div id="validationstatus" class="waiting"></div>
	</#if>
</@a.div>
