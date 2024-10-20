<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#include "/com.tle.web.sections.standard@/textfield.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>

<@css path="logon.css" hasRtl=true />

<div class="area">
  <div id="login-page">
    <div id="native-login">
      <#if m.failed??>
        <p class="warning" role="alert">${b.gkey(m.failed)}</p>
      </#if>
      <#if m.error??>
        <p class="warning" role="alert">${b.key('logon.problems')}</p>
        <p class="warning">${m.error?html}</p>
      </#if>

      <noscript>
        <p class="warning" role="alert">${b.key("logon.enablejs")}</p>
      </noscript>
      <p id="cookieWarning" class="warning" style="display: none" role="alert">${b.key("logon.enablecookies")}</p>

      <p>
        <label for="username">${b.key("logon.username")}</label>
        <@textfield id="username" section=s.username autoSubmitButton=s.logonButton/>
      </p>
      <p>
        <label for="password">${b.key("logon.password")}</label>
        <@textfield id="password" section=s.password password=true autoSubmitButton=s.logonButton/>
        <@button section=s.logonButton class="loginbutton" size="medium" />
      </p>
      <#if m.childSections??>
        <@render m.childSections/>
      </#if>

      <#list m.loginLinks as link>
        <@render link />
      </#list>
    </div>
    <@render s.oidcLoginSection />
  </div>
</div>
<#if m.loginNotice??>
  <div class="area" style="overflow: hidden">
    <div id="loginNotice" class="loginnotice">
      ${m.loginNotice}
    </div>
  </div>
</#if>
