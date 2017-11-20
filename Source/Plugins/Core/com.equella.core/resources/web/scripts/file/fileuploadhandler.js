var FileUploadHandler = {
    addAttachmentEntry: function(cid, canceller, uid, filename, xhr) {
        var attachTable = $("#"+cid+"_a")
        var newEntry = $('<tr class="rowShown"><td class="name"><span class="nametext"></span><div id="u'+uid+
                            '" class="progress-bar"></div></td><td class="actions"><a class="unselect"></a></td></tr>');
        $(".nametext", newEntry).text(filename);
        $(".unselect", newEntry).bind('click', function(e) {
            canceller(uid);
            xhr.abort();
            return false;
        });
        var tbody = $("tbody", attachTable);
        if ($("td", tbody).length == 1)
        {
            $("tr", tbody).remove();
        }
        var trCount = $("tr", tbody).length;
        newEntry.addClass(trCount & 1 == 0 ? "odd" : "even")
        newEntry.appendTo(tbody);
    },
    setupZipProgress: function(checkCall, done) {
        var intId;
        intId = window.setInterval(function() {
            checkCall(function(r){
                if (r.finished)
                {
                    window.clearInterval(intId);
                    done();
                }
                else {
                    var percent = 100 * (r.upto / r.total);
                    $('#zipProgress').progression({Current:percent, AnimateTimeOut : 100});
                }
            });
        }, 500);
    },
};
