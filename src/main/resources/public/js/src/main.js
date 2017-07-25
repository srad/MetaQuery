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

    function AppViewModel() {
        var that = this;
        var editor = new JSONEditor(document.getElementById("response"), {
            mode: "view"
        });

        this.time = ko.observable("");
        this.isCached = ko.observable("");
        this.cacheFetchTime = ko.observable("");
        this.iterations = ko.observable("");

        this.query = ko.observable([
            "{",
            "  document(id: [102154,1039887,1021125]) {",
            "    id title token {",
            "      text begin end",
            "    }",
            "  }",
            "}"].join("\n"));

        this.executeQuery = function () {
            var $executeBtn = $(this).addClass("loading");
            $.post("/query/execute", that.query(), function (response) {
                if (response !== null) {
                    that.time(response.time + "ms");
                    that.isCached(response.isCached);
                    that.cacheFetchTime(response.cacheFetchTime + "ms");
                    that.iterations(response.iterations);
                    editor.set(response.resultSet);
                } else {
                    that.time("");
                    that.isCached("");
                    that.cacheFetchTime("");
                    editor.set("");
                }
                $executeBtn.removeClass("loading");
            });
        };

        this.startImport = function () {
            $.post("/service/import/start");
        };
    }

    $(function () {
        ko.applyBindings(new AppViewModel());
    });
});