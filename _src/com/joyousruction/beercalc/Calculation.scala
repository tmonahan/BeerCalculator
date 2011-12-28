package com.joyousruction.beercalc

import scala.xml.{Elem, Node, NodeSeq, XML}

object Calculation {

  //Constants
  val sucroseDensity_kgL = 1.587
  
  //User defined variables, default values are stored here.
  var efficiency_percent = 75.0
  var sugarLoss_percent = 100.0
  
 
  def getSugarFromFermentables(fermentables: NodeSeq): Double = {
    fermentables.foldLeft(0.0)((sugar:Double, node: Node) => {
      var weight_kg: Double = 0.0
      var yield_unit: Double = 0.0
      var efficiency: Double = 1.0
      try {
        weight_kg = (node \ "AMOUNT").text.toString.toDouble
        yield_unit = (node \ "YIELD").text.toString.toDouble / 100.0
        val sugarType = (node \ "TYPE").text.toString.toLowerCase.trim()
        if (sugarType.contains("extract") || sugarType.contains("sugar")) {
          efficiency = sugarLoss_percent / 100.0
        } else {
          efficiency = efficiency_percent / 100.0
        }
      } catch {
        case _ : Exception => {}
      }
      sugar + weight_kg * yield_unit * efficiency
    })
  }
  
  def getDegreesPlato(sugar_kg: Double, batch_l: Double): Double = {
    val water_l = batch_l - sugar_kg/sucroseDensity_kgL 
    val water_kg = water_l //Leaving option to correct for water density later
    
    sugar_kg/(sugar_kg + water_kg)*100.0
  }

  def getSGfromPlato(plato: Double): Double = {
    plato/(258.6-(plato/258.2*227.1)) + 1.0
  }
  
  def getLitersFromGallons(gallons: Double): Double = {
    gallons * 3.78541178
  }
  
  def setEfficiency(newEfficiency_percent: Double) = {
    efficiency_percent = newEfficiency_percent
  }
  
  def setSugarLoss(newSugarLoss_percent: Double) {
    sugarLoss_percent = newSugarLoss_percent
  }
}
