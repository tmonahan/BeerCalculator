package com.joyousruction.beercalc

import scala.xml.{Elem, Node, NodeSeq, XML}

object Calculation {

  //Constants
  val sucroseDensity_kgL = 1.587
  
  //User defined variables, default values are stored here.
  var efficiency_percent = 75.0
  var sugarLoss_percent = 100.0
  
 // source of bitterness calculations: http://www.realbeer.com/hops/FAQ.html#units
 def ragerIBU(alphaAcid_perUnit: Double, amount_grams: Double, water_liters: Double, gravity: Double, boil_minutes: Double): Double = {
    val gravityAdjustment = scala.math.max(0.0, ((gravity - 1.050) / 0.2))
    val utilizationPercent = 18.11 + 13.86 * scala.math.tanh((boil_minutes-31.32)/18.27)
    
    ((amount_grams * utilizationPercent / 100.0 * alphaAcid_perUnit * 1000) / ( water_liters * (1 + gravityAdjustment)))
  }
  
  //TODO add tinseth and garetz and a way to switch between them for fun
  
  def getIbuFromHops(hops: NodeSeq, gravity: Double, water_liters: Double): Double = {
    hops.foldLeft(0.0)((bitterness: Double, node: Node) => {
      var weight_gram: Double = 0.0
      var alphaAcid_perUnit: Double = 0.0
      var boil_minutes: Double = 0.0
      try {
        weight_gram = (node \ "AMOUNT").text.toString.toDouble
        alphaAcid_perUnit = ((node \ "ALPHA").text.toString.toDouble) / 100.0
        boil_minutes = (node \ "TIME").text.toString.toDouble
      } catch {
        case _ : Exception => {}
      }
      
      bitterness + ragerIBU(alphaAcid_perUnit, weight_gram, water_liters, gravity, boil_minutes)
    })
  }
 
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
