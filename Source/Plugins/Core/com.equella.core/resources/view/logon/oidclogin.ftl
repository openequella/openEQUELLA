<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.standard@/button.ftl">

<@css "institutions.css" />

<div id="divider"></div>
<div id="oidc-login">
  <p>
    ${b.key("login.oidc.description")}
  </p>
  <p>
    <@button section=s.loginButton/>
  </p>
</div>
