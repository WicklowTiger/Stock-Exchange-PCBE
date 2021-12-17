# Stock-Exchange-PCBE
Simple stock exchange made in java + kafka framework.

## Requirements:
 - kafka_2.12-2.81
 - java 11 amazon correto
 - topics stockUpdates, tradeMessages, keepAlive, tradeReplies, userUpdates created

### Useful commands:
- zookeeper-server-start config\zookeeper.properties

- kafka-server-start config\server.properties

- create topic: kafka-topics -zookeeper localhost:2181 -topic <topic_name> -create --partitions <int> --replication-factor <int>

- list all topics: kafka-topics --list --zookeeper localhost:2181

### Client workflow:
- javaFx app started on separate thread
- wait for javaFx app to initialize
- instantiate ClientActionsManager singleton with userUid as parameter:
  - start heartbeat
  - instantiate TradeManager singleton
  - wait for homeWindowController to instantiate
  - start kafka consumer
- ClientActionsManager.run()

### Server workflow:
- instantiate ServerActionsManager singleton with userUid as parameter:
    - instantiate ExchangeManager singleton
    - start kafka consumer
- ServerActionsManager.run()
