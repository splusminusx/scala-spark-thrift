import java.io._
import org.apache.thrift.transport.TMemoryInputTransport
import org.apache.thrift.protocol._
import scala.collection.mutable.HashMap
import stats.ThriftMessageHandler
import example._


/**
 * Десериализация вызовов Thrift методов.
 */
object ThriftMethodCallDeserialization {
  protected val protocolFactory = new TBinaryProtocol.Factory(true, true)
  protected val handlerMap = new HashMap[String, ThriftMessageHandler]
  handlerMap.put("getState", new ExampleGetStateHandler())

  def decode(request: Array[Byte]): Unit = {
    val inputTransport = new TMemoryInputTransport(request)
    val iprot = protocolFactory.getProtocol(inputTransport)

    try {
      val msg = iprot.readMessageBegin()
      println("Message name: " + msg.name + " SeqId: " + msg.seqid.toString)
      val func = handlerMap.get(msg.name)
      func match {
        case Some(fn) =>
          fn(msg.`type`, iprot, msg.seqid)
        case _ =>
          println("Invalid method name: '" + msg.name + "'")
      }
    } catch {
      case e: Exception => println("Exception: " + e.getMessage)
    }
  }

  def main(args: Array[String]): Unit = {
    val filename = "/home/stx/work/projects/Offline-Service-Statistics-Example/thrift_method_call.bin"
    val bis = new BufferedInputStream(new FileInputStream(filename))
    val dis = new DataInputStream(bis)

    while (dis.available() != 0) {
      val packetLength = dis.readInt()
      if (dis.available() >= packetLength) {
        val buf = new Array[Byte](512)
        dis.read(buf, 0, packetLength)
        decode(buf)
      }
    }
  }
}
