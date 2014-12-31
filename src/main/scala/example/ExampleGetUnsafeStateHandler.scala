package example


import org.apache.thrift.protocol._
import org.apache.thrift.protocol.{TProtocol, TMessageType}
import service.example.Example.{getUnsafeState$args, getUnsafeState$result}
import stats.{ThriftReplay, ThriftCall, ThriftMessageHandler, ThriftMessage}

case class ExampleError(message: String)
case class ExampleGetUnsafeStateArgs(id: String)
case class ExampleGetUnsafeStateCall(seqId: Int, args: ExampleGetUnsafeStateArgs)
  extends ThriftCall
case class ExampleGetUnsafeStateResult(seqId: Int, success: Option[Boolean],
                                       error: Option[ExampleError]) extends ThriftReplay

/**
 * Обработчик метода getStateUnsafe.
 */
class ExampleGetUnsafeStateHandler extends ThriftMessageHandler {
  /**
   * @param message - Thrift сообщение.
   * @param iprot - Thrift протокол.
   */
  def apply(message: TMessage, iprot: TProtocol): ThriftMessage = {
    message.`type` match {
      case TMessageType.CALL => val args = getUnsafeState$args.decode(iprot)
        ExampleGetUnsafeStateCall(message.seqid, ExampleGetUnsafeStateArgs(args.id))

      case TMessageType.REPLY => val result = getUnsafeState$result.decode(iprot)
        val error = result.error match {
          case Some(e) => Some(ExampleError(e.message))
          case None => None
        }
        ExampleGetUnsafeStateResult(message.seqid, result.success, error)
    }
  }
}
