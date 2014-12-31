package stats


import org.apache.thrift.protocol._


/**
 * Последовательное сообщение.
 */
trait ThriftMessage {
  def seqId: Int
}

/**
 * Вызов Thrift метода.
 */
trait ThriftCall extends ThriftMessage{
  def args: Any
}

/**
 * Результат вызова Thrift метода.
 */
trait ThriftReplay extends ThriftMessage {
  def success: Option[Any]
  def error: Option[Any]
}

/**
 * Пустой Thrift ответ.
 */
trait VoidReplay extends ThriftReplay {
  final def success = None
  final def error = None
}

/**
 * Нулевое сообщение.
 */
object NullMessage extends ThriftMessage {
  val seqId: Int = 0
}


/**
 * Обработчик Thrift сообщения.
 */
trait ThriftMessageHandler {
  /**
   * @param message - Thrift сообщение.
   * @param iprot - Thrift протокол.
   */
  def apply(message: TMessage, iprot: TProtocol): ThriftMessage
}
