var select2helper = {};

/**

The structure of the params object is:

{
  ajaxurl?: "",
  topOption?: {
    id: "",
    text: ""
  },
  placeholderText?: ""
}

It may also include arbitrary fields for any extensions.

*/

(function() {
  this.setup = function($elem, params, extension) {
    var p = {};
    var perpage = 10;

    var ajax = undefined;
    if (params.ajaxurl) {
      ajax = {
        delay: 200,
        url: function(ajaxParams) {
          return params.ajaxurl;
        },
        data: function(ajaxParams) {
          var resumptionToken =
            (ajaxParams.page || 1) === 1
              ? undefined
              : $elem.data("resumption-token");
          var queryParams = {
            q: ajaxParams.term,
            length: perpage,
            resumption: resumptionToken
          };
          if (extension && extension.processParameters) {
            queryParams = extension.processParameters(
              queryParams,
              params,
              ajaxParams
            );
          }
          return queryParams;
        },
        processResults: function(data) {
          var resumptionToken = data.resumptionToken;
          $elem.data("resumption-token", resumptionToken);
          var res = {
            results: data.results,
            pagination: {
              more: resumptionToken ? true : false
            }
          };
          if (extension && extension.processResultsExt) {
            res = extension.processResultsExt(params, res, data);
          }
          if (data.start === 0 && params.topOption) {
            res.results.unshift(params.topOption);
          }
          return res;
        },
        cache: true
      };
    }

    var placeholder = undefined;
    if (ajax) {
      placeholder = params.placeholderText;
    }
    var opts = {
      containerCssClass: "equella-dropdown",
      dropdownCssClass: "equella-dropdown-list",
      ajax: ajax,
      placeholder: placeholder,
      escapeMarkup: function(markup) {
        return markup;
      }
    };
    if (extension && extension.options) {
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
    $(elem).val("");
  };
}.apply(select2helper));
