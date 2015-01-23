package livetex.io.stream

import java.io.DataInputStream

import livetex.io.stream.PacketType.PacketType


/**
 * @param input - выходнйо поток.
 */
class PacketInputStreamReader(input: DataInputStream) {
  private val messageReader = new MessageInputStreamReader(input)
  private var packetType: Option[Int] = None
  private var count: Option[Int] = None
  private var sizes: Option[Seq[Int]] = None

  /**
   * @return - пакет сообщений.
   */
  def readPacket(): Option[(PacketType, Seq[Array[Byte]])] = {
    readType() match {
      case None => None
      case Some(_) => readCount() match {
        case None => None
        case Some(c) => readSizes(c) match {
          case None => None
          case Some(s) => if (s.sum >= input.available()) {
            val messages: Seq[Array[Byte]] = (for (size <- s) yield {
              messageReader.readBuffer(size)
            }).map(o => o.getOrElse(new Array[Byte](0)))
            Some(PacketType(packetType.getOrElse(0)), messages)
          } else None
        }
      }
    }
  }

  /**
   * @return - тип пакета.
   */
  protected[stream] def readType(): Option[Int] = {
    packetType = messageReader.readInt(packetType)
    packetType
  }

  /**
   * @return - количество сообщений в пакете.
   */
  protected[stream] def readCount(): Option[Int] = {
    count = messageReader.readInt(count)
    count
  }

  /**
   * @param count - количество
   * @return
   */
  protected[stream] def readSizes(count: Int): Option[Seq[Int]] = {
    if (sizes.isEmpty && (input.available() >= count * messageReader.INT_BYTES)) {
      Some(for (_ <- 1 to count) yield {
        input.readInt()
      })
    } else sizes
  }

  /**
   * Обработка завершения пакета.
   */
  protected[stream] def packetEnd(): Unit = {
    packetType = None
    count = None
    sizes = None
  }
}
