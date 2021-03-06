
(function () {
    function v(a, b) {
        function c() {
            e("html")[0].style.overflowY = "hidden";
            b.apply(d, arguments)
        }
        var d = document.createElement("iframe");
        with(d) {
            id = "markup-ui";
            className = "added-by-markup";
            frameBorder = "0";
            allowTransparency = "true";
            with(style) {
                position = "absolute";
                left = top = "0px";
                width = height = "100%";
                borderStyle = "none";
                zIndex = "5000";
                visibility = "hidden";
                overflow = "auto"
            }
        }
        d.addEventListener ? d.addEventListener("load", c, false) : d.attachEvent("onreadystatechange", c);
        d.src = H + "/ui" + a.uri;
        x.appendChild(d)
    }
    function A() {
        var a = [].concat(e("script") || []).concat(e("style") || []).concat(e("link") || []).concat(function (b) {
            for (var c = [], d = 0, h = b.length; d < h; d++) c.push(b.item(d));
            return c
        }(document.body.childNodes) || []);
        r(a, function (b) {
            /added-by-markup/.test(b.className) || w(b)
        })
    }
    function y() {
        function a(d) {
            c(d)
        }
        function b() {
            function d(f) {
                var g = new XMLHttpRequest;
                if ("withCredentials" in g) g.open(f.type, f.url, true);
                else {
                    g = flensed.flXHR({
                        instancePooling: true,
                        xmlResponseText: false,
                        autoUpdatePlayer: true
                    });
                    if (f.type == "get" && !f.headers) g.open(f.type,
                    f.url, true);
                    else {
                        g.open("post", f.url);
                        g.setRequestHeader("X-HTTP-Method-Override", f.type);
                        f.data = f.data || "{}"
                    }
                }
                return g
            }
            function h(f, g) {
                var q = document.createElement("script"),
                    s = 0,
                    E;
                document.getElementsByTagName("head")[0].appendChild(q);
                q.src = f;
                q.type = "text/javascript";
                E = setInterval(function () {
                    if (g()) {
                        if (s++ > 50) {
                            clearInterval(E);
                            throw "Could not load: " + f;
                        }
                    } else clearInterval(E)
                }, 50)
            }
            c = function (f) {
                var g = d(f);
                if (f.headers) for (var q in f.headers) g.setRequestHeader(q, f.headers[q]);
                g.onreadystatechange = function () {
                    try {
                        if (g.readyState == 4) if (g.status >= 200 && g.status < 300) f.success && f.success(JSON.parse(g.responseText));
                        else f.error && f.error(g, g.status)
                    } catch (s) {
                        console.log(s.toString())
                    }
                };
                g.onerror = function (s) {
                    console.log("Error:", s.status);
                    s.status = s.status || s.number;
                    f.error && f.error(s, s.status)
                };
                if (f.data) {
                    g.setRequestHeader("Content-Type", "application/json");
                    g.send(JSON.stringify(f.data))
                } else g.send()
            };
            c.setup = function (f, g) {
                var q = new XMLHttpRequest;
                if (typeof f == "function") {
                    g = f;
                    f = F
                }
                if ("withCredentials" in q) g();
                else {
                    f = f || "/flensed";
                    if (typeof flensed == "undefined") window.flensed = {
                        base_path: f + "/"
                    };
                    flensed.flXHR ? g() : h(f + "/flXHR.js?cache=false-" + (new Date).getTime(), function () {
                        if (!flensed.flXHR) return true;
                        flensed.flXHR.module_ready();
                        g()
                    })
                }
            }
        }
        var c;
        return {
            start_draft: function (d) {
                a({
                    type: "post",
                    url: MarkUp.api_uri + "/draft",
                    data: {
                        html: u.html,
                        width: u.width,
                        height: u.height,
                        useragent: u.useragent,
                        location: u.location,
                        client: MarkUp.client_id,
                        version: MarkUp.api_version
                    },
                    success: d,
                    error: function (h) {
                        console.log(h)
                    }
                })
            },
            setup: function () {
                b();
                c.setup.apply(c, arguments);
                D.xdom = c
            }
        }
    }
    function C() {
        e("frameset").length && function () {
            var a = e("html")[0],
                b = document.createElement("body"),
                c = e("frameset")[0];
            a.appendChild(b);
            b.appendChild(c)
        }()
    }
    function B() {
        var a = ["lib", "pagefix"];
        if (!(document.getElementsByTagName("base").length > 0)) {
            var b = document.getElementsByTagName("head")[0],
                c = document.createElement("base");
            c.href = document.location.href;
            if (b && b.firstChild) b.insertBefore(c, b.firstChild);
            else b && b.appendChild(c)
        }
        m(e("script"),
        w);
        p() && a.push("svg");
        add_js(I + "/dynamic/pagefix??" + a.join(","));
        a = document.getElementsByTagName("*");
        b = ["onload", "onunload", "onmouseover", "onmouseout", "onclick", "onmousedown", "onmouseup"];
        c = 0;
        for (var d = a.length; c < d; c++) for (var h = 0, f = b.length; h < f; h++) try {
            if (b[h] in a[c] && a[c][b[h]] !== null) {
                a[c][b[h]] = null;
                a[c].removeAttribute(b[h])
            }
        } catch (g) {}
    }
    function p() {
        var a = false;
        r(e("svg"), function (b) {
            var c = l("script", {
                type: "image/svg+xml"
            });
            b.parentNode.insertBefore(c, b);
            c.innerHTML = t(b);
            b.parentNode.removeChild(b);
            a = true
        });
        return a
    }
    function z() {
        var a = k(),
            b = a.body;
        a = a.window;
        var c = t(document),
            d = [document.getElementsByTagName("html")[0]];
        return {
            html: c,
            nodes: Array.prototype.slice.call(d),
            width: a.width - b.width < 17 ? b.width : a.width > b.width ? a.width : b.width,
            height: a.height > b.height ? a.height : b.height,
            useragent: window.navigator.userAgent,
            location: window.location.href
        }
    }
    function w(a) {
        a && a.parentNode && a.parentNode.removeChild(a)
    }
    function t(a) {
        try {
            return (new XMLSerializer).serializeToString(a)
        } catch (b) {
            try {
                for (var c = [], d = 0, h = document.childNodes.length; d < h; d++) {
                    a = document.childNodes[d];
                    /!/.test(a.tagName) && a.data ? c.push("<!" + a.data + ">") : c.push(a.outerHTML || "")
                }
                return c.join("\n")
            } catch (f) {
                console.error("Cannot serialize DOM.")
            }
        }
        return false
    }
    function j() {
        var a = /MSIE ([^;]+);/.exec(window.navigator.userAgent),
            b = a ? parseInt(a[1]) : F;
        if (!a || b > 6) return false;
        a = "mu-" + (new Date).getTime();
        var c = "#" + a,
            d = k();
        b = d.window.width > d.body.width ? d.window.width : d.body.width;
        d = d.window.height > d.body.height ? d.window.height : d.body.height;
        var h = l("div", {
            id: a
        });
        x.appendChild(l("div", {
            id: a + "-backdrop"
        }));
        x.appendChild(h);
        h.innerHTML = '<a href="#" id="' + a + '-close">Close</a><h1>Oops&hellip;</h1><p class="first">Markup\'s drawing tools don\'t work very well in Internet Explorer just yet.</p><p>Stay tuned to <a target="_blank" href="http://twitter.com/markupio">@markupio</a> on Twitter to be the first to know when we get it ready.</p><p>Until then, please try another browser like <a target="_blank" href="http://www.google.com/chrome">Google Chrome</a>, <a target="_blank" href="http://www.apple.com/safari/">Safari</a>, <a target="_blank" href="http://getfirefox.com">Firefox</a>, or <a  target="_blank" href="http://opera.com/">Opera</a>. (All of which are free to download and use).</p><style type="text/css">' + c + "{position:absolute; top:50px; left:50%;z-index:32000;width:380px; margin-left:-190px; padding:20px;text-align:left;background:#111;}" + c + " h1 {color:#edeae1; font-family:helvetica,arial,sans-serif; font-size:28px; line-height:28px; font-weight:bold; margin:0px 0px 20px; padding:0px;}" + c + " p {color:#ccc; font-family:helvetica,arial,sans-serif; font-size:12px; line-height:18px; font-weight:normal; margin:0px 0px 10px; padding:0px;}" + c + " a {color:#fff; text-decoration:underline;}" + c + " p.first {color:#fff;font-size:18px; line-height:22px;border-bottom:1px #fff solid;padding-bottom:10px; margin-bottom:20px;}#" + a + "-close {display: block;position: absolute; top: -5px; left: -5px;width: 21px; height: 21px;text-indent:-9999px; border: none; cursor: pointer;background:url(" + G + "/client/assets/images/close.gif) no-repeat;}#" + a + "-backdrop {position:absolute; top:0px; left:0px;background:#fff; z-index:4999;opacity: 0.4; filter:alpha(opacity=40);margin-left: -20px; width:" + b + "px; height:" + d + "px;}</style>";
        window.close = e(c + "-close");
        window.K = e;
        e(c + "-close").onclick = function () {
            x.removeChild(e(c + "-backdrop"));
            x.removeChild(e(c));
            return false
        };
        return true
    }
    function o() {
        var a = J,
            b = a.window.width > a.body.width ? a.window.width : a.body.width;
        a = a.window.height > a.body.height ? a.window.height : a.body.height;
        x.appendChild(l("div", {
            id: "markup_loading",
            style: "position:fixed; top:-5px; right:10px;                   height:40px;                   color:#ccc; font-family:Tahoma;                   font-size:12px; line-height:12px;                   font-family:helvetica, arial, sans-serif;                   -moz-border-radius:3px; -webkit-border-radius:4px;                   cursor:default;                   background: #222;                   background: -moz-linear-gradient(top, rgba(70,70,70,0.95), rgba(6,6,6,0.95));                   background: -webkit-gradient(linear, left top, left bottom, from(rgba(70,70,70,0.95)), to(rgba(6,6,6,0.95)));                   z-index:5000;"
        })).appendChild(l("div", {
            style: "padding:15px 20px 7px 20px;"
        }, "MarkUp &mdash; Loading&hellip;"));
        x.appendChild(l("div", {
            id: "markup_loading_backdrop",
            style: "position:fixed; top:0px; left:0px;                  background:#eee; z-index:4999;                  opacity: 0.5; filter:alpha(opacity=50);                  margin-left: 0px;                  width:" + b + "px; height:" + a + "px;"
        }))
    }
    function i(a) {
        if (!a) return [];
        if ("toArray" in Object(a)) return a.toArray();
        for (var b = a.length || 0, c = [], d = 0; d < b; d++) c.push(a[d]);
        return c
    }
    function k() {
        return {
            window: window.innerHeight !== F ? {
                width: window.innerWidth,
                height: window.innerHeight
            } : document.documentElement.clientWidth ? {
                width: document.documentElement.clientWidth,
                height: document.documentElement.clientHeight
            } : {
                width: document.body.clientWidth,
                height: document.body.clientHeight
            },
            body: {
                width: K.scrollWidth,
                height: K.scrollHeight
            }
        }
    }
    function m(a, b) {
        for (var c = i(a), d = 0, h = c.length; d < h; d++) c[d] = b.call(c, c[d], d, h);
        return c
    }
    function r(a, b) {
        for (var c = i(a), d = 0, h = c.length; d < h; d++) b.call(c, c[d], d, h);
        return c
    }
    function l(a, b, c) {
        var d = document.createElementNS ? document.createElementNS("http://www.w3.org/1999/xhtml", a) : document.createElement(a);
        b = b || {};
        b.className = "added-by-markup" + (b.className ? " " + b.className : "");
        for (q in b) if (q != "style") d[q] = b[q];
        if (b.style) {
            b = b.style.split(";");
            for (var h = 0, f = b.length - 1; h < f; h++) {
                var g = b[h].split(":"),
                    q = g[0].replace(/^\s+|\s+$/, "").toLowerCase().replace(/-(a-z)/gm, "$1".toUpperCase());
                g = g[1].replace(/^\s+|\s+$/, "");
                try {
                    d.style[q] = g
                } catch (s) {}
            }
        }
        if (!/html|head|body|style|link|meta|img/i.test(a)) d.innerHTML = c || "";
        return d
    }

    function n() {
        for (var a = this.all || this.getElementsByTagName("*"), b = [], c = 0, d = a.length; c < d; c++) re.test(a[c].className) && b.push(a[c]);
        return b
    }
    function e(a, b) {
        var c = b || document;
        c.getElementsByClassName = c.getElementsByClassName || n;
        if (/,/.test(a)) m(a.split(/\s*,\s*/), function () {});
        else return /^#/.test(a) ? c.getElementById(a.substr(1)) : /^\./.test(a) ? i(c.getElementsByClassName(a)) : i(c.getElementsByTagName(a))
    }
    var x = document.body,
        K = document.documentElement ? document.documentElement : document.body;
    if (window.MarkUp) if (window.MarkUp.demo) {
        w(document.getElementById("markup-bar"));
        _MarkUp = MarkUp;
        _Draw = Draw;
        _Ui = Ui
    } else {
        if (window.MarkUp.muid) return false
    } else {
        window.MarkUp = {};
        window.add_js = window.add_js || function (a) {
            var b = (document.getElementsByTagName("head")[0] || document.body).appendChild(document.createElement("script"));
            b.src = a;
            b.type = "text/javascript"
        }
    }
    var F, I = window.MarkUp.base_uri || "http://markup.io",
        G = window.MarkUp.static_uri || "http://markup.io/media",
        H = window.MarkUp.api_uri || "http://api.markup.io",
        M = parseInt("1"),
        L = window.MarkUp && window.MarkUp.id;
    r(e("script"), function (a) {
        if (a.markup) L = a.markup
    });
    window.MarkUp = {
        base_uri: I,
        api_uri: H,
        static_uri: G,
        muid: "muid_" + (new Date).getTime(),
        client_id: L,
        api_version: M
    };
    var u, J, D;
    MarkUp.check_in = function () {};
    if (typeof window.console === "undefined") console = {
        error: alert,
        log: alert
    };
    (function () {
        if (!j()) {
            B();
            J = k();
            u = z();
            D = y();
            C();
            o();
            D.setup(G + "/flensed", function () {
                D.start_draft(function (a) {
                    v(a, function () {
                        w(e("#markup_loading"));
                        w(e("#markup_loading_backdrop"));
                        var b = u.width + "px",
                            c = u.height + "px";
                        with(e("html")[0].style) {
                            width = b;
                            height = c;
                            overflow = "hidden"
                        }
                        with(e("body")[0].style) {
                            width = b;
                            height = c;
                            overflow = "hidden"
                        }
                        with(e("#markup-ui").style) {
                            width = b;
                            height = c;
                            visibility = "visible"
                        }
                        A();
                        with(e("html")[0].style) {
                            margin = "0px";
                            height = width = "100%"
                        }
                        with(e("body")[0].style) {
                            margin = "0px";
                            height = width = "100%"
                        }
                        with(e("#markup-ui").style) height = width = "100%"
                    })
                })
            })
        }
    })()
})();
