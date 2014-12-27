package example


import org.apache.thrift.protocol.{TProtocol, TMessageType}
import service.example.Example.{getUnsafeState$args, getUnsafeState$result}
import stats.ThriftMessageHandler


/**
 * Обработчик метода getStateUnsafe.
 */
class ExampleGetUnsafeStateHandler extends ThriftMessageHandler{
  /**
   * @param messageType - тип Thrift сообщения.
   * @param iprot - Thrift протокол.
   * @param seqId - идентификатор последовательности.
   */
  def apply(messageType: Byte, iprot: TProtocol, seqId: Int): (Int, String) = {
    messageType match {
      case TMessageType.CALL => val args = getUnsafeState$args.decode(iprot)
        (seqId, "Get State method call. Args id=" + args.id)

      case TMessageType.REPLY => val result = getUnsafeState$result.decode(iprot)
        (result.success, result.error) match  {
          case (Some(success), _) => (seqId, "getUnsafeState method call result. Success=" + success)
          case (_, Some(error)) => (seqId, "getUnsafeState method call error. Message=" + error.message)
          case _ => (seqId, "OOps")
        }
    }
  }
}
