package com.joyousruction.beercalc

import scala.xml.{ Elem, Node, NodeSeq }

import android.content.Context
import android.widget.ArrayAdapter

object NodeSeqAdapter {
  def getNodeSeqAdapter(context:Context, nodes: NodeSeq, nameStr: String): ArrayAdapter[NamedNode] = {
    var nameArray: Array[NamedNode] = new Array (nodes.length)
    nodes.map { node: Node =>
      new NamedNode(node, nameStr)
    }.copyToArray(nameArray)

    new ArrayAdapter[NamedNode](context, android.R.layout.simple_spinner_item, nameArray)
  }

  def getPositionFromNodeName(adapter: ArrayAdapter[NamedNode], name: String): Option[Int] = {
    var position = -1
    for(i <- 0 to adapter.getCount() - 1){
      if(adapter.getItem(i).toString.equals(name))
        position = i
    }
    if(position >= 0)
      new Some(position)
    else
      None
  }
}

class NamedNode(val node: Node, val nameStr: String) {
  override def toString(): String = {
    ((node \ nameStr).text).toString()
  }
}
