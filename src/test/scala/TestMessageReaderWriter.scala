import java.io.{ByteArrayInputStream, DataInputStream, DataOutputStream, ByteArrayOutputStream}

import livetex.io.stream.{MessageInputStreamReader, MessageOutputStreamWriter}
import org.scalatest.{ShouldMatchers, FunSuite}


class TestMessageReaderWriter extends FunSuite with ShouldMatchers {
  test("message should be correct serialized") {
    val messages = "Hello Scala".map(_.toByte).toArray

    val codedByteStream = new ByteArrayOutputStream(messages.length * 2)
    val writer = new MessageOutputStreamWriter(new DataOutputStream(codedByteStream))
    writer(messages)

    val rawByteStream = new ByteArrayOutputStream(messages.length * 2)
    val rawStream = new DataOutputStream(rawByteStream)
    rawStream.writeInt(messages.length)
    rawStream.write(messages, 0, messages.length)

    rawByteStream.toByteArray should equal(codedByteStream.toByteArray)
  }

  test("message should be correct decoded") {
    val messages = "Hello Scala".map(_.toByte).toArray

    val codedByteStream = new ByteArrayOutputStream(messages.length * 2)
    val writer = new MessageOutputStreamWriter(new DataOutputStream(codedByteStream))

    writer(messages)
    writer(messages)

    val inputCodedStream = new ByteArrayInputStream(codedByteStream.toByteArray)
    val reader = new MessageInputStreamReader(new DataInputStream(inputCodedStream))

    def checkMessageCorrectness(x: Option[Array[Byte]]): Unit = x match {
      case Some(readMessage) => readMessage should equal (messages)
      case None => fail("message must be decoded")
    }

    checkMessageCorrectness(reader.readMessage())
    checkMessageCorrectness(reader.readMessage())
  }
}
