## Messages

### START_SESSION Начать пользовательскую сессию
```
{
    "type": "START_SESSION",
    "username": "string", 
    "password": "string",
    "clientId": "string",
    "sender": "string" 
}
```

### SESSION_STARTED Сессия стартовала ок
```
{
    "type": "SESSION_STARTED",
    "sessionId": "string",
    "clientId": "string"    
}
```

### SESSION_FAILED Неудачный старт/завершение сессии
```
{
    "type": "SESSION_FAILED",
    "sessionId": "string", 
    "clientId": "string"     
    "reason": "string"      /* null, FORBIDDEN|TIMEOUT|CAPTCHA_FAILED */
}
```

### SESSION_CLOSED Сессия закрыта ок
```
{
    "type": "SESSION_CLOSED",
    "sessionId": "string",
    "clientId": "string"   
}
```

### CLOSE_SESSION Завершить пользовательскую сессию
```
{
    "type": "CLOSE_SESSION",
    "sessionId": "string",
    "sender": "string"    
}
```

### CONNECTED Клиент присоединился
```
{
    "type": "CONNECTED",
    "clientId": "string"   
}
```

### DISCONNECTED Клиент отсоединился
```
{
    "type": "DISCONNECTED",
    "clientId": "string"   
}
```

### START_CALL Начать звонок
```
{
    "type": "START_CALL",
    "sessionId": "string",   
    "bUri": "string",
    "cc": "object",
    "vv": [true, true] // voice/video
}
```

### INCOMING_CALL Входящий звонок
```
{
    "type": "INCOMING_CALL",
    "sessionId": "string",  
    "aUri": "string",    
    "sdp": "string"
}
```

### SEND_DTMF_SIGNAL Послать DTMF пиииии
```
{
    "type": "SEND_DTMF_SIGNAL",
    "sessionId": "string",
    "callId": "string",
    "dtmfString": "string"   /* [0-9A-D*#]+ */
}
```

### REJECT_CALL Отклонить звонок
```
{
    "type": "REJECT_CALL",
    "sessionId": "string",
    "callId": "string",
    "reason": "string"       /* UNAVAILABE|DECLINE|BUSY */
}
```

### ACCEPT_CALL Принять звонок
```
{
    "type": "ACCEPT_CALL",
    "sessionId": "string",
    "callId": "string",
    "cc": "object"
}
```

### HANGUP_CALL Завершить звонок
```
{
    "type": "HANGUP_CALL",
    "sessionId": "string",
    "callId": "string"
}
```

### CALL_STARTING Звонок в процессе начинания...
```
{
    "type": "CALL_STARTING",
    "sessionId": "string",
    "callId": "string",
    "stage": "string",        /* SESSION_PROGRESS|TRYING|RINGING */
}
```

### CALL_STARTED Звонок начался ок
```
{
    "type": "CALL_STARTED",
    "sessionId": "string",
    "callId": "string",
}
```

### CALL_FAILED Звонок сломался
```
{
    "type": "CALL_FAILED",
    "sessionId": "string",
    "callId": "string",
    "reason": "string"             /* UNAVAILABE|DECLINE|BUSY|MEDIA_FAILED.+ */
}
```

### CALL_FINISHED Звонок закончился ок
```
{
    "type": "CALL_FINISHED",
    "sessionId": "string",
    "callId": "string"
}
```

### SDP_OFFER Обмен SDP для установления медиа
```
{
    "type": "SDP_OFFER",
    "sessionId": "string",
    "callId": "string",
    "sdp": "string"
}
```

### SDP_ANSWER Обмен SDP для установления медиа
```
{
    "type": "SDP_ANSWER",
    "sessionId": "string",
    "callId": "string",
    "sdp": "string"
}
```

### CREATE_MEDIA_POINT
```
{
    "type": "CREATE_MEDIA_POINT",
    "pointId": "string",
    "cc": "object"
}
```
### REMOVE_MEDIA_POINT
```
{
    "type": "REMOVE_MEDIA_POINT",
    "pointId": "string",
}
```

### JOIN_ROOM
```
{
    "type": "JOIN_ROOM",
    "sessionId": "string",
    "roomId": "string", /* exacty the same as callId */
}
```

### cc - объект, описывающий codecs/capabilities 
Передается в сообщениях `START_CALL`, `ACCEPT_CALL`     
В процессе передачи от компонента к компоненту содержимое полей может быть изменено
```
{
    "userAgent" : "string",    /* Flash Player 11.3, Google Chrome 23.12 WebRTC 0.7 */
    "audioCodecs" : "array",   /* ['PCMU/8000','PCMA/8000','Speex/8000','Speex/16000'] */
    "videoCodecs" : "array",   /* such as audioCodecs */
    "mediaTransport" : "array" /* ['RTP', 'SRTP', 'RTMP', 'RTMPT']
    /* May be another parameters such as up/down bandwith or so on */
}
``` 

### Адаптированый формат SDP сообщения для swf клиента. 
see http://tools.ietf.org/html/rfc4566 for details.
```
v=0                                     // must for SDP. not used.
o=- 0 0 IN IP4 127.0.0.1                // must for SDP. not used.
s=CallId                                // session name. must for SDP. 
i=550e8400-e29b-41d4-a716-446655440000  // session ID
c=IN IP4 rtmps://1.cdn.com/live         // several endpoints to connected to
c=IN IP4 rtmp://1.cdn.com:80/live
c=IN IP4 rtmp://1.cdn.com/live
c=IN IP4 rtmpt://1.cdn.com/live
t=0 0                                   // must for SDP. not used.
m=audio 0 FLASH/RTMP Speex              // several streams defined by m= and following i- *b- *a-
i=550e8400-e29b-41d4-a716-446655440000  // stream ID
m=video 0 FLASH/RTMP H264               // <media> <port> <proto> <fmt> by RFC 4566 
i=550e8400-e29b-41d4-a716-446655440000  // stream ID
```