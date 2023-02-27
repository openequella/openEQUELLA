<#assign PART_HEAD>
  <script async src="https://www.googletagmanager.com/gtag/js?id=${m.googleAccountId?js_string}"></script>
  <script>
    window.dataLayer = window.dataLayer || [];
    function gtag(){dataLayer.push(arguments);}
    gtag('js', new Date());
    gtag('config', '${m.googleAccountId?js_string}');
  </script>
</#assign>
