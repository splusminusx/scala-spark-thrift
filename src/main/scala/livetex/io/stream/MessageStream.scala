package livetex.io.stream

import java.io._


/**
 * Статичные функции для работы с потоком сообщений.
 */
object MessageStream {
  /**
   * Записывает закодированное сообщение в выходной покток.
   *
   * @param stream - выходной поток.
   * @param data - закодированное сообщение.
   */
  def write(stream: DataOutputStream, data: Array[Byte]) {
    stream.writeInt(data.length)
    stream.write(data, 0, data.length)
  }

  /**
   * Читает закодированное сообщение из входного потока.
   *
   * @param stream - входной поток.
   * @return - закодированное сообщение.
   */
  def read(stream: DataInputStream): Array[Byte] = {
    if (stream.available() != 0) {
      try {
        val length = stream.readInt()
        val data = new Array[Byte](length)
        stream.read(data, 0, length)
        data
      } catch {
        case e: IOException => new Array[Byte](0)
      }
    } else {
      new Array[Byte](0)
    }
  }
}
