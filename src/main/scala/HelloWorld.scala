import org.apache.spark.SparkContext
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.*
import org.apache.spark.util.sketch
import org.apache.spark.util.sketch.CountMinSketch

import java.io.{File, PrintWriter}
import scala.collection.mutable.ListBuffer

// representing a threshold
val threshold = 3000
// representing the duration in seconds of a  sliding window
val w = Seconds(60)
// epsilon denotes  the  acceptable error  on  the  similarity  function
val epsilon = 0.001
// delta denotes the acceptable probability that the error exceeds epsilon
val delta = 0.01
// The stream S(teamId), which is generated using the stream generator
val streamId = 7
// Use reflection to get table field of CountMinSketch (so we can calculate similarity later)
val countMinSketchClass = Class.forName("org.apache.spark.util.sketch.CountMinSketchImpl")
val countMinSketchClassTable = countMinSketchClass.getDeclaredField("table")

def calculateSimilarity(table1: Array[Array[Long]], table2: Array[Array[Long]]) = {
  var similarity: Double = 0;
  for (i <- 0 until table1.length) {
    for (j <- 0 until table1(i).length) {
      similarity += table1(i)(j) * table2(i)(j)
    }
  }
  similarity
}

object HelloWorld:
  @main def run(): Unit = {
    // Make private variable table accessible
    countMinSketchClassTable.setAccessible(true)
    // Capture current time in seconds
    val startTime = System.currentTimeMillis() / 1000
    // Number of processed updates since the start of the program (need to print this in the output)
    var numProcessedUpdates: Integer = 0;

    val spark = SparkSession.builder.master("local[*]").getOrCreate()
    val ssc = new StreamingContext(spark.sparkContext, Seconds(2))
    val linesSocket = ssc.socketTextStream("localhost", 9000)
    val linesWindow = linesSocket.window(w)
    val lines = linesWindow.map(x => (x.split(",")(0).toInt, x.split(",")(1).toInt))
    lines.print()

    val sketches = List.fill(100)(CountMinSketch.create(epsilon, delta, 100))
    val tables = List.range(0,100).map(i => countMinSketchClassTable.get(sketches(i)).asInstanceOf[Array[Array[Long]]])

    // create csv file to append
    val writer = new PrintWriter(new File("output.csv"))
    writer.write("num_processed_updates, num_pairs, time\n")

    lines.foreachRDD(rdd => {
      val collectedRdd = rdd.collect()
      collectedRdd.foreach(x => {
        val (server, ip) = x
        sketches(server).add(ip)

        numProcessedUpdates += 1
        //        println("Sketch 41:")
//        collectedRdd.foreach(x => println(x._2 + " " + sketch41.estimateCount(x._2)))
//        println("Sketch 42:")
//        collectedRdd.foreach(x => println(x._2 + " " + sketch42.estimateCount(x._2)))
//        println("Similarity: " + calculateSimilarity(table1, table2))

//        for(i <- 0 until 99){
//          for(j <- i+1 until 99){
//              println("Similarity: " + calculateSimilarity(tables(i), tables(j)))
//          }
//        }
      })

      var numPairs = 0
      // Get difference between current time and start time
      val currentTime = System.currentTimeMillis() / 1000
      val timeDiff = currentTime - startTime
      println("++++++++++++++++++++++++++++++++++++++++")
      for(i <- 0 until 99) {
        for (j <- i + 1 until 99) {
          val similarity = calculateSimilarity(tables(i), tables(j))
          if (similarity > threshold){
            numPairs += 1
//            println(s"Number processed updates: ${numProcessedUpdates}, " +
//                    s"Similarity between ${i} and ${j}: ${similarity}")
          }
        }
      }
      println(s"Number processed updates: ${numProcessedUpdates}, " +
              s"Number of pairs: ${numPairs}, " +
              s"Time difference: ${timeDiff}")
      // write line to csv
      writer.write(s"${numProcessedUpdates}, ${numPairs}, ${timeDiff}\n")
      if (timeDiff > 120) {
        writer.close()
        ssc.stop()
      }
      println("++++++++++++++++++++++++++++++++++++++++")
    })

    writer.close()
    ssc.start()
    ssc.awaitTermination()
    ssc.stop()
  }