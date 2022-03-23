//import java.io.DataOutputStream
//import java.net.{ServerSocket, Socket}
//import java.text.SimpleDateFormat
//import java.util.{Calendar, Random}
//
//object SocketNetworkTrafficSimulator {
//  @throws[Exception] def main(): Unit = {
//    val rn = new Random
//    val welcomeSocket = new ServerSocket(9999)
//    val possiblePortTypes = Array[Int](21, 22, 80, 8080, 463)
//    val numberOfRandomIps = 100
//    val randomIps = new Array[String](numberOfRandomIps)
//    for (i <- 0 until numberOfRandomIps) {
//      randomIps(i) = (rn.nextInt(250) + 1) + "." + (rn.nextInt(250) + 1) + "." + (rn.nextInt(250) + 1) + "." + (rn.nextInt(250) + 1)
//    }
//    System.err.println("Server started")
//    while ( {
//      true
//    }) try {
//      val connectionSocket = welcomeSocket.accept
//      System.err.println("Server accepted connection")
//      val outToClient = new DataOutputStream(connectionSocket.getOutputStream)
//      while ( {
//        true
//      }) {
//        val str = "" + possiblePortTypes(rn.nextInt(possiblePortTypes.length)) + "," + randomIps(rn.nextInt(numberOfRandomIps)) + "," + randomIps(rn.nextInt(numberOfRandomIps)) + "\n"
//        outToClient.writeBytes(str)
//        Thread.sleep(10)
//      }
//    } catch {
//      case e: Exception => e.printStackTrace()
//    }
//  }
//}