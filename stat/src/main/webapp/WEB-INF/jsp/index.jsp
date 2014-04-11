<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <title></title>
    <script type="text/javascript" src="//cdnjs.cloudflare.com/ajax/libs/jquery/2.1.0/jquery.min.js"></script>
    <script type="text/javascript" src="//cdnjs.cloudflare.com/ajax/libs/underscore.js/1.6.0/underscore-min.js"></script>
</head>
<body>

<style>

    body{color:#333333; background:#f0f0f0; font-family:"Trebuchet MS", Helvetica, sans-serif; }
    ul{margin:0; padding:0;}
    a{color:#333333; text-decoration: none;}

    #header{clear:both;}
    div, h2, ul{clear:both;}

    li{float:left; margin: 6px; padding:4px 12px; border-radius: 12px; -moz-border-radius: 12px; white-space:nowrap;background:#e0e0e0; border:1px solid #d0d0d0; list-style-type: none; }
    li.month-item{}
    li.date-item{}
    li.button-item{}

    span.cnt{font-size:70%;}

</style>

<div id="header">
</div>

<div id="content">
</div>

<script type="text/javascript">

    var SERVICE_URL = '/stat';

    var DateUtils = {
        monthDTF : new Intl.DateTimeFormat('ru', {year: "numeric", month: "long"}),
        dateDTF :  new Intl.DateTimeFormat('ru', {month: "long", day: "numeric"}),
        date2yyyymm : function(date){ return ""+date.getFullYear()+((date.getMonth()+1)<10?'0'+(date.getMonth()+1):(date.getMonth()+1)); },
        yyyymmdd2date : function(str){ return new Date(parseInt(str.substr(0,4)), parseInt(str.substr(4,2))-1, parseInt(str.substr(6,2))); },
        yyyymm2date : function(str){ return DateUtils.yyyymmdd2date(str+"01"); }
    };

    // resolve conflict with jsp
    _.templateSettings = {
        interpolate: /\<\?\=(.+?)\?\>/gim,
        evaluate: /\<\?([\s\S]+?)\?\>/gim,
        escape: /\<\?\-(.+?)\?\>/gim
     };

    function tmpl(id, data){
        return _.template($('#'+id).html(), data);
    }

    function generateLastYear(){
        var last12month = [new Date()];
        for(var i=0; i<11; ++i){
            var d = last12month[0];
            var m = d.getMonth();
            var y = d.getFullYear();
            if(0>--m){ m = 11; y--; }
            last12month.unshift(new Date(y, m, 1));
        }

        $('#header').html('<ul class="month-item">');
        last12month.forEach(function(e){
            $('#header').append('<li class="month-item"><a class="month-item" href="#/month/'+DateUtils.date2yyyymm(e)+'">'+DateUtils.monthDTF.format(e)+'</a></li>');
        });
        $('#header').append('</ul>');
    }

    // map buttonId : name
    var buttons = {};

    function loadButtonsCollection(){
        $.get(SERVICE_URL+'/buttons/').done(function(data){
            buttons = data;
        });
    }

    function showMonth(yyyymm){
        $('#content').html('<h1>'+DateUtils.monthDTF.format(DateUtils.yyyymm2date(yyyymm))+'</h1>');

        $.get(SERVICE_URL+'/count/'+yyyymm).done(function(data){
            // check data exist
            var noresult = true;
            for(var d in data){ noresult = false; break; }

            if(noresult){
                $('#content').append('No result for current period');
            }else{
                $('#content')
                        .append('<h2>Calls by date</h2>')
                        .append('<div>'+tmpl("month_table_tmpl", {date:yyyymm, rows:data})+'</div>')
                        .append('<h2>Calls by button</h2>')
                        .append('<div>'+tmpl("buttons_list_tmpl", {date:yyyymm, rows:buttons})+'</div>');
            }
        }).fail(function(){
            $('#content').append('Service error');
        });
    }


    function main(){
        loadButtonsCollection();
        generateLastYear();

        $('body').click(function(e){
            var $e = $(e.target);
            var href = $e.attr('href');
            if(href === undefined){ return; }
            if(0 !== href.indexOf('#')){ return; }
            if($e.hasClass('month-item')){ showMonth(href.match(/\d{6}/)[0]); }
            if($e.hasClass('date-item')){  showDate(href.match(/\d{8}/)[0]); }
        });

        if("" != document.location.hash){
            showMonth(document.location.hash.match(/\d{6}/)[0]);
        }
    }

    main();

</script>

<script type="text/html" id="month_table_tmpl">
    <ul class="date-item">
<? for(var d in rows){ ?>
 <li class="date-item"><span class="date"><a href="<?=SERVICE_URL?>/details/<?=d?>" class="date-item"><?=DateUtils.dateDTF.format(DateUtils.yyyymmdd2date(d))?></a></span>
        <span class="cnt">[<?=rows[d]?>]</span>
    </li>
<? } ?>
</ul>
</script>

<script type="text/html" id="buttons_list_tmpl">
    <ul class="button-item">
<? for(var id in rows){ ?>
 <li class="button-item"><a href="<?=SERVICE_URL?>/summary/<?=id?>/<?=date?>"><?=rows[id]?></a></li>
<? } ?>
</ul>
</script>

</body>
</html>
