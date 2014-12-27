package example


import org.apache.thrift.protocol._
import service.example.Example.{getState$args, getState$result}
import stats.ThriftMessageHandler


/**
 * Обработчик метода getState.
 */
class ExampleGetStateHandler extends ThriftMessageHandler {
  def apply(messageType: Byte, iprot: TProtocol, seqId: Int): (Int, String) = {
    messageType match {
      case TMessageType.CALL => val args = getState$args.decode(iprot)
        (seqId, "Get State method call. Args id=" + args.id)

      case TMessageType.REPLY => val result = getState$result.decode(iprot)
        (seqId, "Get State method cal result. Success=" + result.success)
    }
  }
}
