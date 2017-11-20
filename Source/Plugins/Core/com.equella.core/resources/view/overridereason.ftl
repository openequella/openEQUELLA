<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#include "/com.tle.web.sections.standard@/textarea.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a/>

<@css path="override.css"/>
<h2>${b.key("override.title")}</h2>
<p class="override-message">${m.overrideMessage}</p>
<label for="reason"><span class="mandatory">*</span>${b.key("override.textfield")}</label><br/>
<@textarea id="reason" section=s.reasonTextField />
<@a.div id="button-ajax" class="button-strip">
	<@button section=s.continueButton showAs="save">${b.key("override.continue")}</@button>
	<@render section=s.cancelButton>${b.key("override.cancel")}</@render>
</@a.div>
