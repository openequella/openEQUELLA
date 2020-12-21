<#include "/com.tle.web.freemarker@/macro/sections.ftl">

<@css path="contribute.css" hasRtl=true />

<div class="area" id="contribution_panel">

  <#if !m.hideResumable && m.resumables?size gt 0>
    <h2 id="in_progress_help">${b.key("resumablewizards")}</h2>
    <p id="resumeable_help">${b.key("resumablewizardsinfo")}</p>
    <div class="resumeables">
      <ul>
        <#list m.resumables as resumeable >
          <li>
            <span
                class="resumable"><@render resumeable.resumeLink>${resumeable.collectionName}</@render>  - ${resumeable.startedDate}</span>
            <span class="removelink">[<@render resumeable.removeLink />]</span>
          </li>
        </#list>
      </ul>
    </div>
	</#if>

  <#if m.categories?size == 0>
    <h2 id="nowizard_help">${b.key("nowizards")}</h2>
  <#else>
    <h2 id="contribution_help">${b.key("help")}</h2>

    <#list m.categories as category>
      <@render section=category class="large" />
    </#list>
  </#if>
</div>
