package com.joyousruction.beercalc

import scala.xml.{Elem, Node, NodeSeq, XML}
import android.content.res._


object Database {
  
  var all: NodeSeq = null
  var styles: NodeSeq = null
  var fermentables: NodeSeq = null
  var hops: NodeSeq = null
  var misc: NodeSeq = null
  var yeast: NodeSeq = null
  var currentRecipe: NodeSeq = null

  def getAll: NodeSeq = all
  def getCurrentRecipe: NodeSeq = currentRecipe
  def getFermentables: NodeSeq = fermentables
  def getHops: NodeSeq = hops
  def getMisc: NodeSeq = misc
  def getYeast: NodeSeq = yeast
  def getStyles: NodeSeq = styles
  
  def init(resources: Resources) = {
    val databaseStream = resources.getAssets().open("database.xml")
    all = XML .load(databaseStream)
    styles = all \\ "STYLE"
    fermentables = all \\ "FERMENTABLE"
    hops = all \\ "HOP"
    misc = all \\ "MISC"
    yeast = all \\ "YEAST"
  }
  
  def setCurrentRecipe(recipe: NodeSeq) = {
    currentRecipe = recipe
  }
}
