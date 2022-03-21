import org.apache.spark.SparkContext
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.*
import org.apache.spark.util.sketch
import org.apache.spark.util.sketch.CountMinSketch

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

object HelloWorld:
  @main def run(): Unit = {
    val spark = SparkSession.builder.master("local[*]").getOrCreate()
    val ssc = new StreamingContext(spark.sparkContext, Seconds(5))
    val lines = ssc.socketTextStream("localhost", 9000)
    val words_window = lines.window(Seconds(10))
    val wordsMap = words_window.map(x => (x.split(",")(0), x.split(",")(1)))
    wordsMap.print()
    val sketch41 = CountMinSketch.create(epsilon, delta, 100)
    val sketch42 = CountMinSketch.create(epsilon, delta, 100)

    val field = sketch41.getClass().getDeclaredField("table")
    field.setAccessible(true)
    val table = field.get(sketch41).asInstanceOf[Array[Array[Long]]]

    val onlyFortyOneFortyTwo = wordsMap.filter(x => x._1 == "41" || x._1 == "42").map(x => (x._1.toInt, x._2.toInt))

    onlyFortyOneFortyTwo.foreachRDD(rdd => {
      val collectedRdd = rdd.collect()
      collectedRdd.foreach(x => {
        if (x._1 == 41) {
          sketch41.add(x._2)
        } else {
          sketch42.add(x._2)
        }

        // TODO: Loop through table and do similarity check
        println("Sketch 41:")
        collectedRdd.foreach(x => println(x._2 + " " + sketch41.estimateCount(x._2)))
        println("Sketch 42:")
        collectedRdd.foreach(x => println(x._2 + " " + sketch42.estimateCount(x._2)))
      })
    })

    ssc.start()
    ssc.awaitTermination()
    ssc.stop()
  }