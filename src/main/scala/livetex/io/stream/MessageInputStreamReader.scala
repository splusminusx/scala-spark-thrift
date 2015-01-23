package livetex.io.stream

import java.io._


/**
 * Работа с потоком сообщений.
 * @param input - входной поток.
 */
class MessageInputStreamReader(input: DataInputStream) {
  protected[stream] val INT_BYTES = 4
  private var messageSize: Option[Int] = None

  /**
   * Читает закодированное сообщение из входного потока.
   *
   * @return - закодированное сообщение.
   */
  def readMessage(): Option[Array[Byte]] = {
    readSize() match {
      case None => None
      case Some(s) => readBuffer(s) match {
        case None => None
        case message =>
          messageEnd()
          message
      }
    }
  }

  /**
   * @return - целое число.
   */
  protected[stream] def readSize(): Option[Int] = {
    messageSize = readInt(messageSize)
    messageSize
  }

  /**
   * @return - размер сообщения.
   */
  protected[stream] def readInt(option: Option[Int]): Option[Int] = {
    var result = option
    if (option.isEmpty & (input.available() >= INT_BYTES))
      result = Some(input.readInt())
    result
  }

  /**
   * @param size - размер ожидаемого сообщения.
   * @return - сообщение.
   */
  protected[stream] def readBuffer(size: Int): Option[Array[Byte]] = {
    if (input.available() >= size) {
      val data = new Array[Byte](size)
      input.read(data, 0, size)
      Some(data)
    } else None
  }

  /**
   * Обработка заверщшения сообщения
   */
  protected[stream] def messageEnd(): Unit = {
    messageSize = None
  }
}
