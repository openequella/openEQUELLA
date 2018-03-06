var FileUploadHandler = {
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
