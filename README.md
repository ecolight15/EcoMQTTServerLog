# EcoMQTTServerLog

EcoMQTTプラグインを利用したサーバー関連ログのpublishプラグインです。Minecraftサーバーのプレイヤーのログイン/ログアウトイベントやサーバーの起動/停止イベントをMQTTトピックに送信し、外部システムとの連携を可能にします。

## 特徴

- **プレイヤーイベント監視**: ログイン、ログアウト、初回ログインイベントの検出とMQTT送信
- **サーバーイベント監視**: プラグインの有効化/無効化イベントの検出とMQTT送信
- **JSON形式メッセージ**: AWS IoT Lambda等との連携を考慮したJSON形式でのメッセージ送信
- **UUID管理**: プレイヤーのUUIDと名前の対応をデータベースで管理
- **オンラインプレイヤー追跡**: オンラインプレイヤー情報のデータベース記録
- **柔軟な設定**: MQTTトピック形式、データベース設定等の詳細な設定が可能

## 依存関係

- **Spigot/Bukkit API** 1.18.2以上
- **EcoFramework** 0.30以上 (ecolight15製フレームワーク)
- **EcoMQTT** 0.10以上 (ecolight15製MQTTプラグイン)

## インストール

1. 依存プラグインをサーバーのpluginsフォルダに配置
   - EcoFramework
   - EcoMQTT
2. EcoMQTTServerLog.jarをpluginsフォルダに配置
3. サーバーを起動してconfig.ymlを生成
4. 設定ファイルを編集して再起動

## 設定

`config.yml`でプラグインの動作を設定できます：

### 基本設定
```yaml
DateFormat: "yyyy/MM/dd HH:mm:ss.SSS"  # 日時フォーマット
```

### MQTTトピック設定
各イベントごとにMQTT送信を制御できます：
```yaml
Topic:
  OnEnable:
    Enable: true                          # 有効化イベント送信の有無
    Format: "{server}/p/{plugin}/onenable" # トピック形式
    URL: ""                              # 付帯URL（Webhook等で利用）
  OnDisable:
    Enable: true
    Format: "{server}/p/{plugin}/ondisable"
    URL: ""
  UserLogin:
    Enable: true
    Format: "{server}/p/{plugin}/login"
    URL: ""
  UserLogout:
    Enable: true
    Format: "{server}/p/{plugin}/logout"
    URL: ""
  UserFirstLogin:
    Enable: true
    Format: "{server}/p/{plugin}/firstlogin"
    URL: ""
```

### データベース設定
```yaml
UUIDLogger:
  Enabled: false                    # UUID管理機能の有効/無効
  Database:
    type: "sqlite"                  # データベースタイプ
    name: "uuid.db"                 # データベースファイル名
    server: "localhost:port"        # サーバー（SQLite以外）
    user: "user"                    # ユーザー名
    pass: "pass"                    # パスワード

OnlinePlayerLogger:
  Enabled: false                    # オンラインプレイヤー記録の有効/無効
  Database:
    type: "sqlite"
    name: "online.db"
    server: "localhost:port"
    user: "user"
    pass: "pass"
```

## コマンド

### /ecms
メインコマンド。以下のサブコマンドが利用可能：

- `/ecms reload` - 設定ファイルの再読み込み
  - 権限: `ecomqttserverlog.reload`
- `/ecms player` - プラグインでバッファしているプレイヤーデータの表示
  - 権限: `ecomqttserverlog.player`

### /uuid \<プレイヤー名\>
指定したプレイヤー名の最後のUUIDと、同じUUIDのプレイヤーをサーバーの記録から一覧作成
- 権限: `ecouser.uuid`

### /uname \<UUID\>
指定したプレイヤー名でサーバーにログインしていたユーザーの一覧を取得
- 権限: `ecouser.uname`

## MQTTメッセージ形式

### ログイン/ログアウトイベント
```json
{
  "type": "login",
  "date": "2019/12/27 07:41:32.274",
  "url": "",
  "count": "1",
  "player": {
    "name": "PlayerName",
    "uuid": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
    "world": "world",
    "address": "127.0.0.1"
  }
}
```

### サーバー起動/停止イベント
```json
{
  "type": "onenable",
  "date": "2019/12/27 07:41:32.274",
  "url": ""
}
```

## データベース機能

### UUID管理
プレイヤーのUUIDと名前の対応を記録し、名前変更の履歴を追跡します。

### オンラインプレイヤー管理
現在オンラインのプレイヤー情報をデータベースに記録し、外部からの参照を可能にします。

## 例: MQTTイベント出力

以下は実際のMQTT publish動作の例です：

### ログインイベントのpublish例
```
[2019/12/27 07:41:32.278]type=Publish, topic=mcserver/p/EcoMQTTServerLog/login, payload=2019/12/27 07:41:32.274,xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx,username,CraftWorld{name=world},127.0.0.1
[2019/12/27 07:41:32.724]type=DeliveryToken, topic=[mcserver/p/EcoMQTTServerLog/login], id=0, SessionPresent=false, isComplete=true, Message=2019/12/27 07:41:32.274,xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx,username,CraftWorld{name=world},127.0.0.1
```

### ログアウトイベントのpublish例
```
[2019/12/27 07:41:37.973]type=Publish, topic=mcserver/p/EcoMQTTServerLog/logout, payload=2019/12/27 07:41:37.972,xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx,username,CraftWorld{name=world},127.0.0.1
[2019/12/27 07:41:38.276]type=DeliveryToken, topic=[mcserver/p/EcoMQTTServerLog/logout], id=0, SessionPresent=false, isComplete=true, Message=2019/12/27 07:41:37.972,xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx,username,CraftWorld{name=world},127.0.0.1
```

**注記**: 現在のMessage内のフォーマットは検討の余地があります。AWS IoT => Lambda への送信を考慮すると、より構造化されたJSON形式での送信が推奨されます。

## AWS Lambda でのMQTTトピックの制御例

```python
import requests
import hashlib
import hmac

def hash_ip(ip_address, salt, iterations=10000):
    result = hmac.new(salt.encode(), ip_address.encode(), hashlib.sha256).digest()
    for _ in range(iterations):
        result = hmac.new(salt.encode(), result, hashlib.sha256).digest()
    hex_result = result.hex()[-10:]
    return hex_result

def lambda_handler(event, context):
    topic = event["topicName"]
    topics = topic.split('/')
    server = topics[0]
    if (topic.startswith("server")):
        if (topic.endswith("/p/EcoMQTTServerLog/login") or
            topic.endswith("/p/EcoMQTTServerLog/firstlogin") or
            topic.endswith("/p/EcoMQTTServerLog/logout") or
            topic.endswith("/p/EcoMQTTServerLog/onenable") or
            topic.endswith("/p/EcoMQTTServerLog/ondisable")):
            url = event["payload"]["url"]
            date = event["payload"]["date"]
            if topic.endswith('/onenable'):
                msg="["+date+"] サーバー("+server+") プラグイン起動の通知を受信"
            elif topic.endswith('/ondisable'):
                msg="["+date+"] サーバー("+server+") プラグイン停止の通知を受信"
            elif topic.endswith('/login') or topic.endswith('/logout') or topic.endswith('/firstlogin'):
                count = event["payload"]["count"]
                name = event["payload"]["player"]["name"]
                host = hash_ip(event["payload"]["player"]["host"], "saltsaltsalt", 1234)
                if topic.endswith('/login'):
                    msg="["+date+"] サーバー("+server+") ["+name+" (host:" + host + ")]がログインしました(現在 "+count+"人)"
                if topic.endswith('/firstlogin'):
                    msg="["+date+"] サーバー("+server+") **["+name+" (host:" + host + ")]**が初めてログインしました(現在 "+count+"人)"
                if topic.endswith('/logout'):
                    msg="["+date+"] サーバー("+server+") ["+name+" (host:" + host + ")]がログアウトしました(現在 "+count+"人)"
            requests.post(url, {'content': msg})
```

このコードは AWS IoT へ接続し Lambda へ接続している例を示しています。

## ライセンス

このプロジェクトはGNU Lesser General Public License v3.0の下でライセンスされています。

## 作者

ecolight
