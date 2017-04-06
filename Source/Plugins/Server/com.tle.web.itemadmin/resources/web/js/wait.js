$(document).ready(function () {
    $(document).ajaxStart(function () { $("html").addClass("wait"); });
    $(document).ajaxStop(function () { $("html").removeClass("wait"); });
});
