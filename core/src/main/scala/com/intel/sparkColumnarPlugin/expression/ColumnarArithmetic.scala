package com.intel.sparkColumnarPlugin.expression

import com.google.common.collect.Lists

import org.apache.arrow.gandiva.evaluator._
import org.apache.arrow.gandiva.exceptions.GandivaException
import org.apache.arrow.gandiva.expression._
import org.apache.arrow.vector.types.pojo.ArrowType
import org.apache.arrow.vector.types.pojo.Field

import org.apache.spark.internal.Logging
import org.apache.spark.sql.catalyst.expressions._
import org.apache.spark.sql.types._

import scala.collection.mutable.ListBuffer
/**
 * A version of add that supports columnar processing for longs.
 */
class ColumnarAdd(left: Expression, right: Expression, original: Expression)
  extends Add(left: Expression, right: Expression) with ColumnarExpression with Logging {
  override def doColumnarCodeGen(fieldTypes: ListBuffer[Field]): (TreeNode, ArrowType) = {
    val (left_node, left_type): (TreeNode, ArrowType) = left.asInstanceOf[ColumnarExpression].doColumnarCodeGen(fieldTypes)
    val (right_node, right_type): (TreeNode, ArrowType) = right.asInstanceOf[ColumnarExpression].doColumnarCodeGen(fieldTypes)

    val resultType = CodeGeneration.getResultType(left_type, right_type)
    //logInfo(s"(TreeBuilder.makeFunction(add, Lists.newArrayList($left_node, $right_node), $resultType), $resultType)")
    (TreeBuilder.makeFunction(
      "add", Lists.newArrayList(left_node, right_node), resultType), resultType)
  }
}

class ColumnarMultiply(left: Expression, right: Expression, original: Expression)
  extends Multiply(left: Expression, right: Expression) with ColumnarExpression with Logging {
  override def doColumnarCodeGen(fieldTypes: ListBuffer[Field]): (TreeNode, ArrowType) = {
    val (left_node, left_type): (TreeNode, ArrowType) = left.asInstanceOf[ColumnarExpression].doColumnarCodeGen(fieldTypes)
    val (right_node, right_type): (TreeNode, ArrowType) = right.asInstanceOf[ColumnarExpression].doColumnarCodeGen(fieldTypes)

    val resultType = CodeGeneration.getResultType(left_type, right_type)
    //logInfo(s"(TreeBuilder.makeFunction(multiply, Lists.newArrayList($left_node, $right_node), $resultType), $resultType)")
    (TreeBuilder.makeFunction(
      "multiply", Lists.newArrayList(left_node, right_node), resultType), resultType)
  }
}

object ColumnarBinaryArithmetic {

  def create(left: Expression, right: Expression, original: Expression): Expression = original match {
    case a: Add =>
      new ColumnarAdd(left, right, a)
    case m: Multiply =>
      new ColumnarMultiply(left, right, m)
    case other =>
      throw new UnsupportedOperationException(s"not currently supported: $other.")
  }
}
