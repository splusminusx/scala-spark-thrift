package livetex.io.stream

import java.io._


/**
 * Статичные функции для работы с потоком сообщений.
 */
class MessageOutputStreamWriter(output: DataOutputStream) {
  /**
   * Записывает закодированное сообщение в выходной покток.
   *
   * @param data - закодированное сообщение.
   */
  def apply(data: Array[Byte]) {
    output.writeInt(data.length)
    output.write(data, 0, data.length)
  }
}
