var tpl = '<!DOCTYPE html>\n<html>\n<head>\n<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />\n<link href="main.css" media="screen" rel="stylesheet" type="text/css" />\n</head>\n<body>\n${body}\n<hr>You should run <code>node ./docgen.js</code> to generate this document.</body>\n</html>';
var fs = require('fs');
var marked = require('marked');
// Set default options
marked.setOptions({
  gfm: true, tables: true, breaks: false, pedantic: false, sanitize: true,
  highlight: function(code, lang) {
    if (lang === 'js') { return highlighter.javascript(code);}
    return code;
  }
});

function md2doc(srcfile, dstfile){
    var src = fs.readFileSync(srcfile, 'utf8');
    var dst = marked(src);
    var out = tpl.replace('${body}', dst);
    fs.writeFileSync(dstfile, out);
    console.log(srcfile, '->', dstfile);
}

md2doc('./messages.md', 'messages.html');
md2doc('../client-handler/src/README.md', 'client-handler.html');