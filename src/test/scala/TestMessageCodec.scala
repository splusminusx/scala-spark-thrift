import org.apache.thrift.protocol.TMessageType
import org.scalatest.FunSuite
import livetex.io.thrift.MessageCodec
import service.example.Example.{getState$args, getState$result}


class TestMessageCodec extends FunSuite{
  test("Message decoder should decode Thrift structure") {
    val codec = new MessageCodec
    codec.buildMethodDecoder("getState", getState$args, getState$result)

    val name = "getState"
    val id = "SOME UUID"
    val seqId = 1337

    val call = codec.encodeCall(name, seqId, getState$args(id))
    val result = codec.encodeReplay(name, seqId, getState$result(Some(true)))

    val decodedCall = codec.decode(call)
    val decodedResult = codec.decode(result)

    assert(decodedCall._1.name == name, "Decoded method name error")
    assert(decodedCall._1.seqid == seqId, "Decoded message seqId error")
    assert(decodedCall._1.`type` == TMessageType.CALL, "Decoded Thrift message type error")

    decodedCall._2 match {
      case getState$args(decodedId) => assert(decodedId == id, "Wrong \"getState\" id argument")
      case _ => fail("\"getState\" arguments not decoded")
    }

    assert(decodedResult._1.name == name, "Decoded method name error")
    assert(decodedResult._1.seqid == seqId, "Decoded message seqId error")
    assert(decodedResult._1.`type` == TMessageType.REPLY, "Decoded Thrift message type error")

    decodedResult._2 match {
      case getState$result(success) => assert(success == Some(true), "Wrong \"getState\" result")
      case _ => fail("\"getState\" results not decoded")
    }

  }
}
