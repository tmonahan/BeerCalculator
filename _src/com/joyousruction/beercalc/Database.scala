package com.joyousruction.beercalc

import scala.xml.{Elem, Node, NodeSeq, XML}
import android.content.res._
import android.content.Context


object Database {
  
  val MODE_WORLD_READABLE = android.content.Context.MODE_WORLD_READABLE
  
  val databaseFileName = "database.xml"
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
    val databaseStream = resources.getAssets().open(databaseFileName)
    val recipeStream = try {
      context.openFileInput(recipeFileName)
    } catch { 
      case e: Exception => resources.getAssets().open(recipeFileName)  
    }
    val optionsStream = resources.getAssets().open(optionsFileName)
    val mashStream = resources.getAssets().open(mashFileName)
    
    //Handle loading all database information
    all = XML .load(databaseStream)
    styles = all \\ "STYLE"
    fermentables = all \\ "FERMENTABLE"
    hops = all \\ "HOP"
    misc = all \\ "MISC"
    yeast = all \\ "YEAST"
    
    //Load all recipes
    recipes = XML .load(recipeStream)
    
    databaseStream.close()
    recipeStream.close()
    optionsStream.close()
    mashStream.close()
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
