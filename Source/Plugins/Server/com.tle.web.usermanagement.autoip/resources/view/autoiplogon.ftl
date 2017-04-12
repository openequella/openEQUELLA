<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>

<p><@render section=s.autoLogonLink class="autologinlink">${b.key("logon.loginas", [m.autoUsername])}</@render></p>
