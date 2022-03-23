package myPackage

import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.util.sketch.CountMinSketch

import java.io.{File, PrintWriter}

object Main {
  def main(args: Array[String]): Unit = {
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
    // Make private variable table accessible
    countMinSketchClassTable.setAccessible(true)
    // Initialize current time in seconds
    var startTime: Long = 0
    // Number of processed updates since the start of the program (need to print this in the output)
    var numProcessedUpdates: Integer = 0;

    val spark = SparkSession.builder.master("local[*]").getOrCreate()
    val ssc = new StreamingContext(spark.sparkContext, Seconds(2))
    // disable spark errors
    ssc.sparkContext.setLogLevel("ERROR")
    val linesSocket = ssc.socketTextStream("stream-host", 9000)
    val linesWindow = linesSocket.window(w)
    val linesWindow2 = linesSocket.window(Seconds(2))
    val lines = linesWindow.map(x => (x.split(",")(0).toInt, x.split(",")(1).toInt))

    // create csv file to append
    val writer = new PrintWriter(new File("output.csv"))
    writer.write("num_processed_updates, num_pairs, time\n")

    linesWindow2.foreachRDD(rdd => {
      val collectedRdd = rdd.collect()
      numProcessedUpdates += rdd.count().toInt
    })

    lines.foreachRDD(rdd => {
      if (startTime == 0) {
        startTime = System.currentTimeMillis() / 1000
      }
      val sketches = List.fill(100)(CountMinSketch.create(epsilon, delta, 100))
      val tables = List.range(0, 100).map(i => countMinSketchClassTable.get(sketches(i)).asInstanceOf[Array[Array[Long]]])
      val collectedRdd = rdd.collect()
      collectedRdd.foreach(x => {
        val (server, ip) = x
        sketches(server).add(ip)
      })
      var numPairs = 0
      // Get difference between current time and start time
      val currentTime = System.currentTimeMillis() / 1000
      val timeDiff = currentTime - startTime
      println("++++++++++++++++++++++++++++++++++++++++")
      for (i <- 0 until 99) {
        for (j <- i + 1 until 99) {
          val similarity = calculateSimilarity(tables(i), tables(j))
          if (similarity > threshold) {
            numPairs += 1
            println(s"Number processed updates: ${numProcessedUpdates}, " +
              s"Similarity between ${i} and ${j}: ${similarity}")
          }
        }
      }
      println(s"Number processed updates: ${numProcessedUpdates}, " +
        s"Number of pairs: ${numPairs}, " +
        s"Time difference: ${timeDiff}")
      // write line to csv
      writer.write(s"${numProcessedUpdates}, ${numPairs}, ${timeDiff}\n")
      if (timeDiff >= 120) {
        writer.close()
        ssc.stop()
      }
      println("++++++++++++++++++++++++++++++++++++++++")
    })

    ssc.start()
    ssc.awaitTermination()
    ssc.stop()
  }

  def calculateSimilarity(table1: Array[Array[Long]], table2: Array[Array[Long]]) = {
    var similarity: Double = 0;
    for (i <- 0 until table1.length) {
      for (j <- 0 until table1(i).length) {
        similarity += table1(i)(j) * table2(i)(j)
      }
    }
    similarity
  }
}