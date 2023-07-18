<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.standard@/dialog.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>

<@script "webdav.js"/>

<#if !m.hideDetails>
  <table>
    <tr>
      <td>
        URL:
      </td>
      <td>
        ${m.webdavUrl}&nbsp;&nbsp;
      </td>
      <td>
        <button class="btn btn-equella btn-mini" onclick="copyToClipboard('${m.webdavUrl}')">Copy</button>
      </td>
    </tr>
    <tr>
      <td>
        ${b.gkey('webdav.username')}:&nbsp;&nbsp;
      </td>
      <td>
        ${m.webdavUsername}
      </td>
      <td>
        <button class="btn btn-equella btn-mini" onclick="copyToClipboard('${m.webdavUsername}')">Copy</button>
      </td>
    </tr>
    <tr>
      <td>
        ${b.gkey('webdav.password')}:&nbsp;&nbsp;
      </td>
      <td>
        ${m.webdavPassword}
      </td>
      <td>
        <button class="btn btn-equella btn-mini" onclick="copyToClipboard('${m.webdavPassword}')">Copy</button>
      </td>
    </tr>
  </table>
  <br/>
  <@render section=s.refreshButton class="ctrlbuttonNW">${b.gkey('wizard.controls.file.refresh')}</@render>
</#if>
<@render s.filesTable />
