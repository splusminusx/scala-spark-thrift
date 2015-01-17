# scala-spark-thrift
Пример десериализации сообщений закодированных TBinaryProtocol с использованием Spark.

## Установка
Качаем и устанавливаем Spark 1.1.1 [отсюда](http://spark.apache.org/downloads.html).

## Сборка
```bash
$ ./sbt scrooge-gen assembly
```

## Запуск
```bash
$ ./spark-submit \
  --class "SparkDeserialization" \
  --master local[4] \
  $PATH_TO_BUILD/Offline-Service-Statistics-Example-assembly-1.0.jar
```

## Тесты
```bash
$ ./sbt test
```
