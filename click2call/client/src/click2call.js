
var Click2Call = (function () {

	function Click2Call() {
		this.widgets = [];
		this.url = this.explicitUrl();
		this.styleready = false;
		document.cookie="A3Stat="+(""+Math.random()).replace(/^0\./, "")+"; path=/";
	};

	Click2Call.prototype.explicitUrl = function() {
		var scripts = document.getElementsByTagName('script');
		for(var i=0; i<scripts.length; ++i){
			if(-1 != scripts[i].src.indexOf('click2call.js')){
				return scripts[i].src.replace(/click2call\.js$/, "");
			}
		}
	};

	Click2Call.prototype.init = function (id, opts) {
		if(typeof this.widgets[id] !== 'undefined') throw new Error("You must create a widget with id="+id+" only once");
		if(!this.styleready){ this.initcss(); }
		var self = this;
		var opts = opts || {};
		var explicitSize = [0,0];
		if(typeof opts['w'] === 'undefined'){ opts.w = 370; /* default width */} else {  explicitSize[0] = opts['w']; }
		if(typeof opts['h'] === 'undefined'){ opts.h = 256; /* default height */} else { explicitSize[1] = opts['h']; }
		if(typeof opts['top'] === 'undefined'){ opts.top = '32px';}
		if(typeof opts['left'] === 'undefined' && typeof opts['right'] === 'undefined'){ opts.right = '0';}
		if(typeof opts['tab'] === 'undefined'){ opts.tab = this.url+'tab.png';}
		var q = ['id='+id];
		if(typeof opts['query'] !== 'undefined'){ for(var p in opts['query']){ q.push(p+'='+opts.query[p]); } }
		if(explicitSize[0] != 0){q.push('w='+explicitSize[0]);}
		if(explicitSize[1] != 0){q.push('h='+explicitSize[1]);}
		opts.query = q;
		opts.horz = (typeof opts['left'] !== 'undefined' ? 'L' : 'R');
		opts.vert = (typeof opts['bottom'] !== 'undefined' ? 'B' : 'T');
		opts.csspos = ('L' == opts.horz ? 'left:0' : 'right:0') + ';' + ('T' == opts.vert ? 'top:'+opts['top'] : 'bottom:'+opts['bottom']);

		var src = this.url+'click2call.html?'+opts.query.join('&');
		document.write('<img src="'+opts.tab+'" id="click2call-tab-'+id+'" class="click2call-tab click2call-tab-'+opts.horz+'" style="'+opts.csspos.replace(/(left|right):0/, "$1:-8px")+';" onclick="click2call.toggle(\''+id+'\');">');
		document.write('<iframe src="'+src+'" id="click2call-frm-'+id+'" frameborder="0" scrolling="no" class="click2call-frm" style="width:0; height:'+opts.h+'px;'+opts.csspos+';"></iframe>');
		this.widgets[id] = {'opts' : opts};
		var sc=''; var m = document.cookie.match(/A3Stat=(\d+)/); if(null != m){ sc = m[1]; }

		var postMessageListener = function(event){
			if(0 != self.url.indexOf(event.origin)) return;
			var obj = JSON.parse(event.data);
			if(obj['cmd'] === undefined) return;
			switch (obj.cmd){
				case 'close' : self.toggle(obj.id); break;
				case 'resize': self.resize(obj.id, obj.w, obj.h); break;
			}
		};

		if(window.addEventListener){
			window.addEventListener("message", postMessageListener, false);
			window.addEventListener("onmessage", postMessageListener, false);
		}else{
			window.attachEvent("onmessage", postMessageListener);
		}
	};

	Click2Call.prototype.toggle = function (id) {
		var tab = document.getElementById('click2call-tab-'+id);
		var frm = document.getElementById('click2call-frm-'+id);
		var opts = this.widgets[id].opts;
		if(0 == parseInt(frm.style.width)){ //hidden
			//frm.src = this.url+'click2call.html?'+opts.query.join('&');
			frm.style.width = opts.w+'px';
			tab.style[('L' == opts.horz ? 'left' : 'right')] = (opts.w-8)+'px';
		}else if(opts.w == parseInt(frm.style.width)){ // visible
			frm.style.width = 0;
			//frm.src = "about:blank";
			tab.style[('L' == opts.horz ? 'left' : 'right')] = '-8px';
		}
	};

	Click2Call.prototype.resize = function (id, w, h) {
		// resize width not implemented
		var frm = document.getElementById('click2call-frm-'+id);
		frm.style.height = h+'px';
	};

	Click2Call.prototype.initcss = function() {
		this.styleready = true;
		var s  = document.createElement('link');
		s.type = 'text/css';
		s.rel  = 'stylesheet';
		s.href = this.url + 'click2call.css';
		document.getElementsByTagName("head")[0].appendChild(s);
	};
	return Click2Call;
})();

var click2call = new Click2Call();
