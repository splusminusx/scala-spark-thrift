package stats

import com.twitter.scrooge.{ThriftStructCodec3, ThriftStruct}
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
class ThriftStructDecoder[A <: ThriftStruct, R <: ThriftStruct] {
  /**
   * @param message - Thrift сообщение.
   * @param iprot - Thrift протокол.
   */
  def apply(message: TMessage, iprot: TProtocol)(implicit args: ThriftStructCodec3[A], result: ThriftStructCodec3[R]): ThriftStruct = {
    message.`type` match {
      case TMessageType.CALL => args.decode(iprot)
      case TMessageType.REPLY => result.decode(iprot)
    }
  }
}


/**
 * Пустая Thrift Структура.
 */
class NullThriftStruct extends ThriftStruct {
  def write(oprot : org.apache.thrift.protocol.TProtocol) : scala.Unit = {}
}