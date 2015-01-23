package livetex.io.stream


/**
 * Перечисление: тип пакета.
 */
object PacketType extends Enumeration {
  type PacketType = Value

  val BINARY_TYPE = Value(0)
  val JSON_TYPE = Value(1)
}
