import java.io.{DataInputStream, ByteArrayInputStream, DataOutputStream, ByteArrayOutputStream}
import livetex.io.stream.PacketType._
import livetex.io.stream.{PacketInputStreamReader, PacketOutputStreamWriter}
import org.scalatest._


class TestPacketReaderWriter extends FunSuite with ShouldMatchers {
  test("packet should be correct serialized") {
    val packet = Seq(Array[Byte](1, 2, 3), Array[Byte](4, 5, 6), Array[Byte](7, 8, 9, 10))

    val codedByteStream = new ByteArrayOutputStream(packet.length * 10)
    val writer = new PacketOutputStreamWriter(new DataOutputStream(codedByteStream))
    writer.write(JSON_TYPE, packet)

    val rawByteStream = new ByteArrayOutputStream(packet.length * 10)
    val rawStream = new DataOutputStream(rawByteStream)
    rawStream.writeInt(JSON_TYPE.id)
    rawStream.writeInt(packet.length)
    for (p <- packet) {
      rawStream.writeInt(p.length)
    }
    for (p <- packet) {
      rawStream.write(p, 0, p.length)
    }

    rawByteStream.toByteArray should equal (codedByteStream.toByteArray)
  }

  test("packet should be correct decoded") {
    val packet = Seq(Array[Byte](1, 2, 3), Array[Byte](4, 5, 6), Array[Byte](7, 8, 9, 10))

    val codedByteStream = new ByteArrayOutputStream(packet.length * 10)
    val writer = new PacketOutputStreamWriter(new DataOutputStream(codedByteStream))
    writer.write(JSON_TYPE, packet)
    writer.write(JSON_TYPE, packet)

    val inputCodedStream = new ByteArrayInputStream(codedByteStream.toByteArray)
    val reader = new PacketInputStreamReader(new DataInputStream(inputCodedStream))

    def checkPacketCorrectness(x: Option[(PacketType, Seq[Array[Byte]])]): Unit = x match {
      case Some((JSON_TYPE, readPacket)) => readPacket.zip(packet).map {
        case (a, b) => a should equal (b)
      }
        readPacket should have size packet.length
      case _ => fail("message must be decoded")
    }

    checkPacketCorrectness(reader.readPacket())
    checkPacketCorrectness(reader.readPacket())
  }
}
