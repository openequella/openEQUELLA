function externalLinks()
{
  if (!document.getElementsByTagName) return;
  var anchors = document.getElementsByTagName("a");
  for (var i=0; i<anchors.length; i++)
  {
    var anchorElement = anchors[i];
    if (anchorElement.getAttribute("href") && anchorElement.getAttribute('rel') == "external")
    {
      function newAlert()
        {
          window.alert('This will open a new window.');
        }
      try{anchorElement.addEventListener('click', newAlert, false)}
      catch(e){}
      try{anchorElement.attachEvent('onclick', newAlert)}
      catch(e){}
      anchorElement.setAttribute('target', "_blank");
    }
  }
}
window.onload = externalLinks;