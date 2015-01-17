package livetex.io.thrift

import com.twitter.scrooge.{ThriftStruct, ThriftStructCodec3}
import org.apache.thrift.TException
import org.apache.thrift.protocol._
import org.apache.thrift.transport.{TMemoryBuffer, TTransport, TMemoryInputTransport}
import scala.collection.mutable


/**
 * Кодек для работы с сообщениями Thrift протокола.
 */
object MessageCodec {
  type MethodDecoder = (TMessage, TProtocol) => ThriftStruct

  private val protocolFactory = new TBinaryProtocol.Factory(true, true)
  private val methodDecoders = new mutable.HashMap[String, MethodDecoder]

  /**
   * Устанавливает декодер для метода.
   *
   * @param name - имя метода.
   * @param methodDecoder - декодер метода.
   */
  def setMethodDecoder(name: String, methodDecoder: MethodDecoder) = {
    methodDecoders.put(name, methodDecoder)
  }

  /**
   * Фабрчный метод для построения декодера метода.
   *
   * @param name - имя метода.
   * @param args - кодек аргументов метода.
   * @param result - кодек результата метода.
   * @tparam A - тип аргументов метода.
   * @tparam R - тип результата метода.
   */
  def buildMethodDecoder[A <: ThriftStruct, R <: ThriftStruct](name: String, args: ThriftStructCodec3[A], result: ThriftStructCodec3[R]) {
    val decoder: (TMessage, TProtocol) => ThriftStruct = (m, p) => {
      m.`type` match {
        case TMessageType.CALL => args.decode(p)
        case TMessageType.REPLY => result.decode(p)
      }
    }
    methodDecoders.put(name, decoder)
  }

  /**
   * Кодирует сообщение Thrift протокола.
   *
   * @param message - заголовки сообщения.
   * @param args - структурированное тело сообщения.
   * @return - массив байт закодированного сообщения.
   */
  def encodeMessage(message: TMessage, args: ThriftStruct): Array[Byte] = {
    val buf = new TMemoryBuffer(512)
    val oprot = protocolFactory.getProtocol(buf)

    oprot.writeMessageBegin(message)
    args.write(oprot)
    oprot.writeMessageEnd()
    buf.getArray
  }

  /**
   * Кодирует вызов метода.
   *
   * @param name - имя методя.
   * @param seqId - идентификатор последовательности сообщений.
   * @param args - структурированное тело сообщения.
   * @return - массив байт закодированного сообщения.
   */
  def encodeCall(name: String, seqId: Int, args: ThriftStruct): Array[Byte] = {
    encodeMessage(new TMessage(name, TMessageType.CALL, seqId), args)
  }

  /**
   * Кодирует результат вызова метода.
   *
   * @param name - имя методя.
   * @param seqId - идентификатор последовательности сообщений.
   * @param args - структурированное тело сообщения.
   * @return - массив байт закодированного сообщения.
   */
  def encodeReplay(name: String, seqId: Int, args: ThriftStruct):  Array[Byte] = {
    encodeMessage(new TMessage(name, TMessageType.REPLY, seqId), args)
  }

  /**
   * Декодирует сообщение Thrift протокола.
   *
   * @param request - массив байт закодированного сообщения.
   * @return - пара: заголовки сообщения и структурированное тело сообщения.
   */
  def decode(request: Array[Byte]): (TMessage, ThriftStruct) = {
    val inputTransport = new TMemoryInputTransport(request)
    val iprot = protocolFactory.getProtocol(inputTransport)

    try {
      val msg = iprot.readMessageBegin()
      val func = methodDecoders.get(msg.name)
      func match {
        case Some(fn) => (msg, fn(msg, iprot))
        case _ => (null, null)
      }
    } catch {
      case e: TException => (null, null)
    }
  }
}
