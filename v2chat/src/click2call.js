
var Click2Call = (function () {

	function Click2Call() {
		this.widgets = [];
		this.url = Array.prototype.slice.call(document.getElementsByTagName('script'), 0).filter(function(e){return (e.src && -1 != e.src.indexOf('click2call.js'));})[0].src.replace(/click2call\.js$/, "");
		this.styleready = false;
	};

	Click2Call.prototype.init = function (id, opts) {
		if(typeof this.widgets[id] !== 'undefined') throw new Error("You must create a widget with id="+id+" only once");
		if(!this.styleready){ this.initcss(); }
		var opts = opts || {};
		if(typeof opts['w'] === 'undefined'){ opts.w = 410; /* default width */}
		if(typeof opts['h'] === 'undefined'){ opts.h = 480; /* default height */}
		if(typeof opts['top'] === 'undefined'){ opts.top = '32px';}
		if(typeof opts['left'] === 'undefined' && typeof opts['right'] === 'undefined'){ opts.right = '0';}
		if(typeof opts['tab'] === 'undefined'){ opts.tab = this.url+'tab.png';}
		var q = ['id='+id];
		if(typeof opts['query'] !== 'undefined'){ for(var p in opts['query']){ q.push(p+'='+opts.query[p]); } }
		opts.query = q;
		opts.horz = (typeof opts['left'] !== 'undefined' ? 'L' : 'R');
		opts.vert = (typeof opts['bottom'] !== 'undefined' ? 'B' : 'T');
		opts.csspos = ('L' == opts.horz ? 'left:0' : 'right:0') + ';' + ('T' == opts.vert ? 'top:'+opts['top'] : 'bottom:'+opts['bottom']);

		document.write('<div id="click2call-tab-'+id+'" class="click2call-tab click2call-tab-'+opts.horz+'" style="'+opts.csspos.replace(/(left|right):0/, "$1:-8px")+'; background-image:url('+opts.tab+');" onclick="click2call.toggle(\''+id+'\');"></div>');
		document.write('<iframe id="click2call-frm-'+id+'" frameborder="0" scrolling="no" class="click2call-frm" style="width:0; height:'+opts.h+'px;'+opts.csspos+';"></iframe>');
		this.widgets[id] = {'opts' : opts};
	};

	Click2Call.prototype.toggle = function (id) {
		var tab = document.getElementById('click2call-tab-'+id);
		var frm = document.getElementById('click2call-frm-'+id);
		var opts = this.widgets[id].opts;
		if(0 == parseInt(frm.style.width)){ //hidden
			frm.src = this.url+'click2call.html?'+opts.query.join('&');
			frm.style.width = opts.w+'px';
			tab.style[('L' == opts.horz ? 'left' : 'right')] = (opts.w-8)+'px';
		}else if(opts.w == parseInt(frm.style.width)){ // visible
			frm.style.width = 0;
			frm.src = "about:blank";
			tab.style[('L' == opts.horz ? 'left' : 'right')] = '-8px';
		}
	};

	Click2Call.prototype.initcss = function() {
		this.styleready = true;
		var style = document.createElement('style');
		var transition = function(p){ var t = ['transition: '+p+' 500ms;']; t[1] = '-webkit-'+t[0]; t[2] = '-ms-'+t[0]; t[3] = '-o-'+t[0]; t[1] = '-moz-'+t[0]; return t.join(' '); };
		var h = '.click2call-frm{position:fixed; background:#f0f0f0; '+transition('width')+'}';
		h+= '.click2call-tab{width:48px; height:256px; position:fixed; cursor:pointer;}';
		h+= '.click2call-tab-L{'+transition('left')+'}';
		h+= '.click2call-tab-R{'+transition('right')+'}';
		style.innerHTML = h;
		document.body.appendChild(style);
	};
	return Click2Call;
})();

var click2call = new Click2Call();