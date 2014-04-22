<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html>
<head>
    <title></title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <script src="//${hostname}/a3/libs.js"></script>
    <script src="//${hostname}/a3/a3.js"></script>
    <script src="//${hostname}/a3/main.js"></script>
    <script src="//${hostname}/a3/time.js"></script>
    <script src="//${hostname}/a3/time-check.js"></script>
</head>
<body>

<script>
    var communicator = new Click2CallCommunicator();
    communicator.query['id'] = '${buttonId}';
    var factory = new CompatibleFactory();
    factory.addEndpoint("rtmps://be1b.v2chat.com/webcall2/${buttonId}");
    factory.addEndpoint("rtmp://be1.v2chat.com/webcall2/${buttonId}");
    factory.addEndpoint("rtmp://be1b.v2chat.com:80/webcall2/${buttonId}");
    factory.addEndpoint("rtmpt://be1.v2chat.com/webcall2/${buttonId}");
    factory.addEndpoint(document.location.protocol+"//${hostname}");
    factory.setServiceName("constructor");
    communicator.setFactory(factory);
    var resources = new Resources('//${hostname}/tmpl/${buttonId}');
    resources.load( function(res){
        if(res === false){
            alert('Error on loading resource. See console for details. Mission impossible');
            throw new Error('Critical error');
        }
        $('body').append(res.tmpl);
        communicator.setLocale(res.locale);
        communicator.start();
    }, (typeof communicator.query['lang'] !== 'undefined' ? communicator.query.lang : null));
</script>
</body>
</html>
