import org.apache.spark.SparkContext
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.*

object HelloWorld:
  @main def run(): Unit =
    val spark = SparkSession.builder
      .master("local[*]")
      .getOrCreate()

    val ssc = new StreamingContext(spark.sparkContext, Seconds(5))
    val lines = ssc.socketTextStream("localhost", 9000)
    val words_window = lines.window(Seconds(10))
    val words = lines.flatMap(_.split(" "))
    val wordCounts = words.map(x => (x, 1)).reduceByKey(_ + _)
    wordCounts.print()
    ssc.start()
    ssc.awaitTermination()
    ssc.stop()
