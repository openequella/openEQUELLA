function toggle(elem, elem2) {
    var element = document.getElementById(elem);
    var imgelem = document.getElementById(elem2);
    if (element.style.display == "none") {
        element.style.display = "block";
        imgelem.src = "images/folderopen.gif";
    } else {
        element.style.display = "none";
        imgelem.src = "images/folderclosed.gif";
    }
}
function openWebDav(id, webdavurlmsg, webdavurlie) 
{
    var browser = navigator.userAgent.toLowerCase();
    if (browser.indexOf("msie") >= 0) 
    {
        popup_percent("javascript:void(0)", "webdavdetails", 80);
        oChildWindow.document.write('<html><body><div id="webdav" style="behavior: url(#default#httpFolder);"></div></body></html>');
        oChildWindow.document.close();
        oChildWindow.document.getElementById("webdav").navigate(webdavurlie);
        oChildWindow.close();
    }
    else 
    {
        popup_percent_xy("javascript:void(0)", "webdavdetails", 50, 10);
        oChildWindow.document.write("<span style='font-family: arial; font-size: small;'>" + webdavurlmsg + "</span>");
        oChildWindow.document.close();
    }
}

