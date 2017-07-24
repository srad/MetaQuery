requirejs.config({
    baseUrl: "/js",
    urlArgs: "bust=" + (new Date()).getTime()
});

require([
    "vendor/zepto/zepto.min",
    "vendor/knockout/dist/knockout",
    "vendor/jsoneditor/dist/jsoneditor.min"
], function ($, ko, JSONEditor) {
    "use strict";

    var $executeBtn = $("#execute");

    function ViewModelQueryPlan() {
        this.time = ko.observable("");
        this.isCached = ko.observable("");
        this.cacheFetchTime = ko.observable("");
        this.iterations = ko.observable("");
    }

    var queryPlan = new ViewModelQueryPlan();

    ko.applyBindings(queryPlan, document.getElementById("queryPlan"));

    $(function () {
        var editor = new JSONEditor(document.getElementById("response"), {
            mode: "view"
        });

        document.getElementById("query").value = [
            "{",
            "  document(id: [102154,1039887,1021125]) {",
            "    id title token {",
            "      text begin end",
            "    }",
            "  }",
            "}"].join("\n");

        $executeBtn.on("click", function () {
            $executeBtn.addClass("loading");

            $.getJSON("/doc/query/" + encodeURIComponent($("#query").val()), function (response) {
                if (response !== null) {
                    queryPlan.time(response.time + "ms");
                    queryPlan.isCached(response.isCached);
                    queryPlan.cacheFetchTime(response.cacheFetchTime + "ms");
                    queryPlan.iterations(response.iterations);
                    editor.set(response.resultSet);
                } else {
                    queryPlan.time("");
                    queryPlan.isCached("");
                    queryPlan.cacheFetchTime("");
                    editor.set("");
                }
                $executeBtn.removeClass("loading");
            });
        });
    });
});