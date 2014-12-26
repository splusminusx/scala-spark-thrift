package example


import org.apache.thrift.protocol._
import service.example.Example.notify$args
import stats.ThriftMessageHandler


/**
 * Обработчик метода getState.
 */
class ExampleNotifyHandler extends ThriftMessageHandler {
  def apply(messageType: Byte, iprot: TProtocol, seqId: Int): String = {
    messageType match {
      case TMessageType.CALL => val args = notify$args.decode(iprot)
        "Notify method call. Args id=" + args.id + ", state=" + args.state.toString

      case TMessageType.REPLY => "Get State method cal result"
    }
  }
}
