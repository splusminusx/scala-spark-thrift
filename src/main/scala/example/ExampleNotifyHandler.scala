package example


import org.apache.thrift.protocol._
import service.example.Example.notify$args
import stats.{VoidReplay, ThriftCall, ThriftMessageHandler, ThriftMessage}


case class ExampleNotifyArgs(id: String, state: Boolean)
case class ExampleNotifyCall(seqId: Int, args: ExampleNotifyArgs) extends ThriftCall
case class ExampleNotifyReplay(seqId: Int) extends VoidReplay


/**
 * Обработчик метода getState.
 */
class ExampleNotifyHandler extends ThriftMessageHandler{
  def apply(message: TMessage, iprot: TProtocol): ThriftMessage = {
    message.`type` match {
      case TMessageType.CALL => val args = notify$args.decode(iprot)
        ExampleNotifyCall(message.seqid, ExampleNotifyArgs(args.id, args.state))
        
      case TMessageType.REPLY => ExampleNotifyReplay(message.seqid)
    }
  }
}
