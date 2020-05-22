<#ftl />
<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>

<@css "auth.css" />

<div id="auth_container">
  <#if m.showReceipt >
    <p>${b.key('export.authorization.newtab.receipt')}</p>
  <#else>
    <#if m.showNewTabLauncher >
      <p>${b.key('export.authorization.newtab.description')}</p>

      <button onclick="newTabAuth()" class="btn btn-equella">
          ${b.key('export.authorization.newtab.launch')}
      </button>

        <script>
            function newTabAuth() {
                window.open(
                  "${m.authUrl}", "_blank");
            }
        </script>
    <#else>
      <iframe frameBorder="0" src="${m.authUrl?html}"></iframe>
    </#if>
	</#if>
</div>
