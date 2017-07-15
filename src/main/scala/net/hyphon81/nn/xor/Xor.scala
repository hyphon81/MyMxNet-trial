/*
 MxNet XOR trial with Scala
 @hyphon81
 */

package net.hyphon81.nn.xor

import ml.dmlc.mxnet._
import ml.dmlc.mxnet.io._
import ml.dmlc.mxnet.optimizer.SGD

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

import org.kohsuke.args4j.{CmdLineParser, Option}
import scala.collection.JavaConverters._

object Xor {
  val logger = Logger(LoggerFactory.getLogger(classOf[Xor]))

  // print ArrayLayers
  private def printArrayLayers(length: Int, shape: Array[Int], ndarray: NDArray): Unit = {
    if(shape.length > 0 && shape.length != 1){
      print("[")
      printArrayLayers(length, shape.tail, ndarray)
      println("]")
    } else if(shape.length == 1){
      for(i <- 0 to length - 1){
        print("[")
        var flag = false
        ndarray.at(i).toArray.foreach { value =>
          if(flag){
            print(", ")
          }
          print(value)
          flag = true
        }
        println("]")
      }
    }
  }
  // print NDArray
  def printNDArray(ndarray: NDArray) = {
    val shape = ndarray.shape.toArray
    printArrayLayers(shape(0), shape, ndarray)
  }

  // input data definition
  def getData: NDArray = {
    val inputData = NDArray.array(Array(0, 0, 0, 1, 1, 0, 1, 1), shape = Shape(4, 2))
    inputData
  }

  // label data definition
  def getLabel: NDArray = {
    val labelData = NDArray.array(Array(0, 1, 1, 0), shape = Shape(4))
    labelData
  }

  def getNDArrayIter(): NDArrayIter = {
    val dataIter = new NDArrayIter(
      data = IndexedSeq(getData),
      label = IndexedSeq(getLabel),
      dataName = "input",
      labelName = "output_label",
      dataBatchSize = 4
    )
    dataIter
  }
  /** for CSVIter
  def getDataIter(shuffle: Boolean = false): DataIter = {
    val dataIter = IO.CSVIter(
      Map(
        "data_csv" -> "src/main/resources/train_data.csv",
        "label_csv" -> "src/main/resources/data.csv",
        "data_shape" -> "(2, )",
        "label_shape" -> "(1, )",
        "data_name" -> "input",
        "label_name" -> "output_label",
        "batch_size" -> "1",
        "shuffle" -> { if(shuffle) "True" else "False" }
      )
    )
    dataIter
  }
    **/

  // model definition
  def buildNetwork(): Symbol = {
    val input = Symbol.Variable("input")
    val fc1 = Symbol.FullyConnected(name = "fc1")()(Map("data" -> input, "num_hidden" -> 2))
    val sigmoid1 = Symbol.Activation(name = "sigmoid1")()(Map("data" -> fc1, "act_type" -> "sigmoid"))
    val fc2 = Symbol.FullyConnected(name = "fc2")()(Map("data" -> sigmoid1, "num_hidden" -> 2))
    val output = Symbol.SoftmaxOutput(name = "output")()(Map("data" -> fc2))

    output
  }

  def main(args: Array[String]): Unit = {
    val options = new Xor
    val parser: CmdLineParser = new CmdLineParser(options)
    try {
      parser.parseArgument(args.toList.asJava)

      val batchSize = 4
      val numEpoch = 200
      val ctx = if(options.gpu != -1) Context.gpu(options.gpu) else Context.cpu()
      val learningRate = 1.0f
      val momentum = 0.9f
      val network = buildNetwork()
      val initializer = new Xavier(factorType = "in")

      val optimizer = new SGD(learningRate = learningRate, momentum = momentum)

      val dataSet = getNDArrayIter()

      val model = new FeedForward(
        network,
        ctx = ctx,
        numEpoch = numEpoch,
        optimizer = optimizer,
        initializer = initializer,
        batchSize = batchSize
      )

      model.fit(trainData = dataSet, evalData = dataSet)

      println("Input Data: ")
      dataSet.getData.foreach { ndarray =>
        printNDArray(ndarray)
      }

      println("")
      println("Output Result: ")
      model.predict(data = dataSet).foreach { ndarray =>
        printNDArray(ndarray)
      }
      println("")

    } catch {
      case ex: Exception => {
        logger.error(ex.getMessage, ex)
        parser.printUsage(System.err)
        sys.exit(1)
      }
    }
  }
}

class Xor {
  @Option(name = "--gpu", usage = "which gpu card to use, default is -1, means using cpu")
  private val gpu: Int = -1
}
