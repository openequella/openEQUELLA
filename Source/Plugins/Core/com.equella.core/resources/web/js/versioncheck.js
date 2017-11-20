function checkVersion(mmr, commit, display, versionUrl, aurl, cb)
{
    $.ajax(versionUrl + "?mmr=" + encodeURIComponent(mmr) + "&commit=" + encodeURIComponent(commit)
    + "&display=" + encodeURIComponent(display) +"&aurl=" + encodeURIComponent(aurl)).done(function (data){
        cb(data.newer, data.url);
    });

}