var select2helper = {};

(function() {
    this.setup = function($elem, params, extension) {
        var p = {};
        var perpage = 10;

        var ajax = undefined;
        if (params.ajaxurl) {
            ajax = {
                delay: 200,
                url: function (ajaxParams) {
                    return params.ajaxurl;
                },
                data: function (ajaxParams) {
                    var start = (ajaxParams.page ? (ajaxParams.page - 1) * perpage : undefined);
                    var queryParams = {
                        q: ajaxParams.term,
                        length: perpage,
                        resumption: (start ? start + ':' + (start + perpage) : undefined)
                    };
                    if (extension && extension.processParameters) {
                        queryParams = extension.processParameters(queryParams, params, ajaxParams);
                    }
                    return queryParams;
                },
                processResults: function (data) {
                    var res = {
                        results: data.results,
                        pagination: {
                            more: (data.resumptionToken ? true : false)
                        }
                    };
                    if (extension && extension.processResultsExt) {
                        res = extension.processResultsExt(res, data);
                    }
                    return res;
                },
                cache: true
            };
        }

        var placeholder = undefined;
        if (ajax){
            placeholder = params.placeholderText;
        }
        var opts = {
            containerCssClass: 'equella-dropdown',
            dropdownCssClass: 'equella-dropdown-list',
            ajax: ajax,
            placeholder: placeholder,
            escapeMarkup: function (markup) { return markup; }
        };
        if (extension && extension.options){
            opts = $.extend({}, opts, extension.options(params));
        }
        $elem.select2(opts);
    };

    this.setValue = function(elem, value) {
        $(elem).val(value);
    };

    this.getValue = function(elem) {
        return $(elem).val();
    };

    this.reset = function(elem) {
        $(elem).val('');
    };

}).apply(select2helper);
