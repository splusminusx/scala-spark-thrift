package example


import org.apache.thrift.protocol._
import service.example.Example.{getState$args, getState$result}
import stats.{ThriftMessage, ThriftReplay, ThriftCall, ThriftMessageHandler}


case class ExampleGetStateArgs(id: String)
case class ExampleGetStateCall(seqId: Int, args: ExampleGetStateArgs)
  extends ThriftCall
case class ExampleGetStateReplay(seqId: Int, success: Option[Boolean], error: Option[Any])
  extends ThriftReplay
case class ExampleGetState(seqId: Int, args: ExampleGetStateArgs, success: Option[Boolean], error: Option[Any])
  extends ThriftMessage


/**
 * Обработчик метода getState.
 */
class ExampleGetStateHandler extends ThriftMessageHandler {
  def apply(message: TMessage, iprot: TProtocol): Any = {
    message.`type` match {
      case TMessageType.CALL => val args = getState$args.decode(iprot)
        ExampleGetStateCall(message.seqid, ExampleGetStateArgs(args.id))

      case TMessageType.REPLY => val result = getState$result.decode(iprot)
        ExampleGetStateReplay(message.seqid, result.success, None)
    }
  }
}
