
function setupUpload(elemId, options) {
    var $div = $(elemId)
    var $file = $('input[type="file"]', $div)
    var $browse = $("button", $div);
    var $feedback = $(".customfile-feedback", $div);
    var doFileClick = function() {
        $file.click();
    }
    $browse.bind('click', doFileClick);
    $feedback.bind('click', doFileClick);
    $file.bind('change', function(e) {
        if (options.onchange)
        {
            options.onchange();
        }
        var files = e.target.files || e.dataTransfer.files;
        for (var i = 0, f; f = files[i]; i++) {
            var xhr = null;
            if (options.ajaxUploadUrl)
            {
                xhr = new XMLHttpRequest();
                xhr.open("POST", options.ajaxUploadUrl, true);
            }
            if (!options.validateFile || options.validateFile(f, xhr))
            {
                $feedback.text(f.name);

                if (xhr)
                {
                    xhr.setRequestHeader("X_FILENAME", window.btoa(unescape(encodeURIComponent(f.name))));
                    xhr.send(f);
                }
            }
        }
    });
}