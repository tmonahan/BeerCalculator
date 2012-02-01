package com.joyousruction.beercalc

import scala.xml.{Elem, Node, NodeSeq, PrettyPrinter, XML}
import android.content.res._
import android.content.Context
import android.widget.Toast


object Database {
  
  val MODE_WORLD_READABLE = android.content.Context.MODE_WORLD_READABLE

  val fermentableFileName = "fermentable.xml"
  val hopFileName = "hop.xml"
  val miscFileName = "misc.xml"
  val yeastFileName = "yeast.xml"
  val styleFileName = "style.xml"
    
  val recipeFileName = "recipes.xml"
  val optionsFileName = "options.xml"
  val mashFileName = "mashs.xml"
  
  var all: NodeSeq = null
  var styles: NodeSeq = null
  var fermentables: NodeSeq = null
  var hops: NodeSeq = null
  var misc: NodeSeq = null
  var yeast: NodeSeq = null
  var currentRecipe: NodeSeq = null
  var recipes: NodeSeq = null

  def getAll: NodeSeq = all
  def getCurrentRecipe: NodeSeq = currentRecipe
  def getFermentables: NodeSeq = fermentables
  def getHops: NodeSeq = hops
  def getMisc: NodeSeq = misc
  def getYeast: NodeSeq = yeast
  def getStyles: NodeSeq = styles
  def getRecipes: NodeSeq = recipes
  
  def init(resources: Resources, context: Context) = {
    val fermentableStream = resources.getAssets().open(fermentableFileName)
    val hopStream = resources.getAssets().open(hopFileName)
    val miscStream = resources.getAssets().open(miscFileName)
    val yeastStream = resources.getAssets().open(yeastFileName)
    val styleStream = resources.getAssets().open(styleFileName)
    val recipeStream = try {
      context.openFileInput(recipeFileName)
    } catch { 
      case e: Exception => resources.getAssets().open(recipeFileName)  
    }
    val optionsStream = resources.getAssets().open(optionsFileName)
    val mashStream = resources.getAssets().open(mashFileName)
    
    //Handle loading all database information
    fermentables = XML .load(fermentableStream) \ "_"
    hops = XML .load(hopStream) \ "_"
    misc = XML .load(miscStream) \ "_"
    yeast = XML .load(yeastStream) \ "_"
    styles = XML .load(styleStream) \ "_"

    //Load all recipes
    recipes = XML .load(recipeStream)
    
    fermentableStream.close()
    hopStream.close()
    miscStream.close()
    yeastStream.close()
    styleStream.close()
    recipeStream.close()
    optionsStream.close()
    mashStream.close()
  }

  def exportRecipe(is: java.io.OutputStream, recipeToExport:NodeSeq, context: Context) {
    val recipeWriter = new java.io.BufferedWriter(new java.io.OutputStreamWriter(is))
    val exportNode = <RECIPES>{recipeToExport}</RECIPES>
    val outputString: String = new PrettyPrinter(80,2).format(exportNode)
    val headerString: String = "<?xml version='1.0' encoding='UTF-8'?>\n"
    //write the file
    try{
      recipeWriter.write(headerString, 0, headerString.length())
      recipeWriter.write(outputString, 0, outputString.length())
    } finally {
      recipeWriter.flush()
      recipeWriter.close()
      Toast.makeText(context, "File exported successfully", Toast.LENGTH_SHORT).show()
    }
  }

  def importRecipe(is: java.io.InputStream, context: Context) {
    val data = XML .load(is)
    currentRecipe = data \\ "RECIPE"
    saveCurrentRecipe(context)
  }
  
  def setCurrentRecipe(recipe: NodeSeq) = {
    currentRecipe = recipe
  }
  
  def saveCurrentRecipe(context: Context) = {
    val CurrentRecipeName = (currentRecipe \ "NAME").text.toString
    var allRecipes = (recipes \\ "RECIPE").foldLeft(NodeSeq.Empty)((list: NodeSeq, node:Node) =>{
      (node \ "NAME").text.toString match {
        case CurrentRecipeName => list
        case _ => list ++ node
      }
    })
    allRecipes = currentRecipe ++ allRecipes
    val recipeNode: Node = <RECIPES>{ allRecipes }</RECIPES>
    val recipeOutput = context.getApplicationContext.openFileOutput(recipeFileName, MODE_WORLD_READABLE)
    val recipeWriter = new java.io.BufferedWriter(new java.io.OutputStreamWriter(recipeOutput))
    
    //update the recipes NodeSeq
    recipes = recipeNode
    //write the file
    try{
      XML .write(recipeWriter, recipeNode, "UTF-8", true, null)
    } finally {
      recipeWriter.flush()
      recipeWriter.close()
    }
  }
}
