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
  var recipes: NodeSeq = null

  def getAll: NodeSeq = all
  def getCurrentRecipe: NodeSeq = currentRecipe
  def getFermentables: NodeSeq = fermentables
  def getHops: NodeSeq = hops
  def getMisc: NodeSeq = misc
  def getYeast: NodeSeq = yeast
  def getStyles: NodeSeq = styles
  def getRecipes: NodeSeq = recipes
  
  def init(resources: Resources) = {
    val databaseStream = resources.getAssets().open("database.xml")
    val recipeStream = resources.getAssets().open("recipes.xml")
    val optionsStream = resources.getAssets().open("options.xml")
    val mashStream = resources.getAssets().open("mashs.xml")
    
    //Handle loading all database information
    all = XML .load(databaseStream)
    styles = all \\ "STYLE"
    fermentables = all \\ "FERMENTABLE"
    hops = all \\ "HOP"
    misc = all \\ "MISC"
    yeast = all \\ "YEAST"
    
    //Load all recipes
    recipes = XML .load(recipeStream)
  }
  
  def setCurrentRecipe(recipe: NodeSeq) = {
    currentRecipe = recipe
  }
}
