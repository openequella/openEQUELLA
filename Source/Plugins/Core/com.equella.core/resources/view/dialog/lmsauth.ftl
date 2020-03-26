<#ftl />
<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>

<@css "auth.css" />

<div id="auth_container">
  <p>This auth launch needs to happen in a new tab! </p>

    <button onclick="newTabAuth()">
      Open new tab for auth
  </button>

    <script>
        function newTabAuth() {
            window.open(
              "${m.authUrl}", "_blank");
        }
    </script>

    <button onclick="newTabAuth()">
      Open new tab for auth
  </button>

    <script>
        function newTabAuth() {
            window.open(
              "${m.authUrl}", "_blank");
        }
    </script>
	<iframe frameBorder="0" src="${m.authUrl?html}"></iframe>
</div>
