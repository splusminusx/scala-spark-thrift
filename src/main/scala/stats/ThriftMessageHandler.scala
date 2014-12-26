package stats


import org.apache.thrift.protocol._


/**
 * Обработчик Thrift сообщения.
 */
trait ThriftMessageHandler {
  /**
   * @param messageType - тип Thrift сообщения.
   * @param iprot - Thrift протокол.
   * @param seqId - идентификатор последовательности.
   */
  def apply(messageType: Byte, iprot: TProtocol, seqId: Int): String
}
