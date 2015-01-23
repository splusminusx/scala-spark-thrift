package livetex.io.stream

import java.io.DataOutputStream

import livetex.io.stream.PacketType.PacketType


/**
 * @param output - выходнйо поток.
 */
class PacketOutputStreamWriter(output: DataOutputStream) {
  /**
   * Запись пакета состоящего из одного запроса.
   *
   * @param message - сериализованное сообщение.
   */
  def write(packetType: PacketType, message: Array[Byte]): Unit = {
    output.writeInt(packetType.id)

    output.writeInt(1)
    output.writeInt(message.length)
    output.write(message)
  }

  /**
   * Запись пакета состоящего из одного ответа.
   *
   * @param messages - сериализованные сообщения.
   */
  def write(packetType: PacketType, messages: Seq[Array[Byte]]): Unit = {
    output.writeInt(packetType.id)

    output.writeInt(messages.length)
    messages.map(m => output.writeInt(m.length))
    messages.map(m => output.write(m))
  }

  /**
   * @param request - запрос.
   * @param response - ответ.
   * @param packetType - тип пакета.
   */
  def write(packetType: PacketType, request: Array[Byte], response: Array[Byte]): Unit = {
    write(packetType, List(request, response))
  }
}
