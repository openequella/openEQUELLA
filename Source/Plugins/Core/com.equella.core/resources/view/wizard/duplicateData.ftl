<#include "/com.tle.web.freemarker@/macro/sections.ftl">

<@css path="dupedata.css" />

<div class="area">
	<h2>${b.key('duplicatedatasection.pagename')}</h2>

  <#if m.textfieldDuplicateData?size gt 0>
    <h4>${b.key("duplicatedatasection.preamble")}</h4>
    <#if m.canAcceptAny>
      <p>${b.key("duplicatedatasection.checktheboxes")}</p>
    </#if>
    <#if m.mustChangeAny>
      <p>${b.key("duplicatedatasection.mustchange")}</p>
    </#if>
    <#list m.textfieldDuplicateData as duplicateData>
      <#if duplicateData.visible>
        <div class="input checkbox">
          <label>
            <#if duplicateData.canAccept>
              <@render s.getCheckbox(_info, duplicateData.identifier)/>
            <#else>
              <span class="mandatory">${b.key("duplicatedatasection.mustchangesymbol")}</span>
            </#if>
            <span>${b.key("duplicatedatasection.usedby", [duplicateData.value])}</span>
          </label>
          <ul class="blue">
            <#list duplicateData.items as item>
              <li><@render item.link/></li>
            </#list>
          </ul>
        </div>
      </#if>
    </#list>
  </#if>

  <#if m.attachmentDuplicateData?size gt 0>
    <br/>
    <h4>${b.key("duplicatedatasection.preambleforattachment")}</h4>
    <#list m.attachmentDuplicateData as duplicateData>
        <label>${b.key("duplicatedatasection.usedby", [duplicateData.value])}</label>
        <ul class="blue">
          <#list duplicateData.items as item>
            <li><@render item.link/></li>
          </#list>
        </ul>
    </#list>
  </#if>

</div>
