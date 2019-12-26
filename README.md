# EcoMQTTServerLog

Message内のフォーマットは検討の余地あり…
AWS IoT => Lambda に投げることを考えるとjsonにすべきでは？
- ログインイベントのpublish例
```
[2019/12/27 07:41:32.278]type=Publish, topic=mcserver/p/EcoMQTTServerLog/login, payload=2019/12/27 07:41:32.274,xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx,username,CraftWorld{name=world},127.0.0.1
[2019/12/27 07:41:32.724]type=DeliveryToken, topic=[mcserver/p/EcoMQTTServerLog/login], id=0, SessionPresent=false, isComplete=true, Message=2019/12/27 07:41:32.274,xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx,username,CraftWorld{name=world},127.0.0.1
```

- ログアウトイベントのpublish例
```
[2019/12/27 07:41:37.973]type=Publish, topic=mcserver/p/EcoMQTTServerLog/logout, payload=2019/12/27 07:41:37.972,xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx,username,CraftWorld{name=world},127.0.0.1
[2019/12/27 07:41:38.276]type=DeliveryToken, topic=[mcserver/p/EcoMQTTServerLog/logout], id=0, SessionPresent=false, isComplete=true, Message=2019/12/27 07:41:37.972,xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx,username,CraftWorld{name=world},127.0.0.1
```
