package com.joyousruction.beercalc

import scala.xml.{ Elem, Node, NodeSeq, XML }

object Calculation {

  //Constants
  val sucroseDensity_kgL = 1.587
  val kg_l_to_lb_g = 8.34538

  //User defined variables, default values are stored here.
  var efficiency_percent = 75.0
  var sugarLoss_percent = 100.0

  // source of bitterness calculations: http://www.realbeer.com/hops/FAQ.html#units
  def ragerIBU(alphaAcid_perUnit: Double, amount_grams: Double, water_liters: Double, gravity: Double, boil_minutes: Double): Double = {
    val gravityAdjustment = scala.math.max(0.0, ((gravity - 1.050) / 0.2))
    val utilizationPercent = 18.11 + 13.86 * scala.math.tanh((boil_minutes - 31.32) / 18.27)

    ((amount_grams * utilizationPercent / 100.0 * alphaAcid_perUnit * 1000) / (water_liters * (1 + gravityAdjustment)))
  }

  def tinsethIBU(alphaAcid_perUnit: Double, amount_grams: Double, water_liters: Double, gravity: Double, boil_minutes: Double): Double = {
    val bignessFactor = 1.65 * scala.math.pow(0.000125, (gravity - 1))
    val boilTimeFactor = (1 - scala.math.exp(-0.04 * boil_minutes)) / 4.15
    ((bignessFactor * boilTimeFactor) * (amount_grams * alphaAcid_perUnit * 1000) / water_liters)
  }

  //TODO add garetz and a way to switch between them for fun

  //TODO add source of SRM calculations
  def mcuToSRM_morey(mcu: Double): Double = {
    1.4922 * scala.math.pow(mcu, 0.6859)
  }

  def mcuToSRM_daniel(mcu: Double): Double = {
    0.2 * mcu + 8.4
  }

  def mcuToSRM_mosher(mcu: Double): Double = {
    0.3 * mcu + 4.7
  }

  def convertLbsKg(lbs: Double): Double = { 0.45359237 * lbs }
  def convertKgLbs(kg: Double): Double = { 2.20462262 * kg }

  def convertOzG(oz: Double): Double = { 28.3495231 * oz }
  def convertGOz(g: Double): Double = { 0.035273961 * g }

  def convertLitersGallons(liters: Double): Double = { 0.264172052 * liters }
  def convertGallonsLiters(gallons: Double): Double = { 3.78541178 * gallons }

  def getAttenuationFromYeast(yeast: NodeSeq): Double = {
    var maxAttenuation = yeast.foldLeft(0.0)((attenuation: Double, node: Node) => {
      var myAttenuation = 0.0
      try {
        myAttenuation = (node \ "ATTENUATION").text.toString.toDouble
      }
      scala.math.max(myAttenuation, attenuation)
    })

    if (!yeast.isEmpty && maxAttenuation == 0.0) {
      maxAttenuation = 75
    }

    maxAttenuation
  }

  def getIbuFromHops(hops: NodeSeq, boil_start_gravity: Double, boil_end_gravity: Double, water_start_liters: Double, water_end_liters: Double, total_boil_minutes: Double): Double = {
    hops.foldLeft(0.0)((bitterness: Double, node: Node) => {
      var weight_gram: Double = 0.0
      var alphaAcid_perUnit: Double = 0.0
      var boil_minutes: Double = 0.0
      try {
        weight_gram = (node \ "AMOUNT").text.toString.toDouble * 1000.0
        alphaAcid_perUnit = ((node \ "ALPHA").text.toString.toDouble) / 100.0
        boil_minutes = (node \ "TIME").text.toString.toDouble
      } catch {
        case _: Exception => {}
      }

      //Calculate the average gravity and water volume during the boil time of each hop for additional accuracy (and additional confusion!)
      var gravity = boil_end_gravity
      var water_liters = water_end_liters
      if (total_boil_minutes > 0.0) {
        gravity = boil_end_gravity + ((boil_start_gravity - boil_end_gravity) * (boil_minutes / total_boil_minutes) / 2)
        //water_liters = water_end_liters + ((water_start_liters - water_end_liters) * (boil_minutes / total_boil_minutes) / 2)
      }

      bitterness + tinsethIBU(alphaAcid_perUnit, weight_gram, water_liters, gravity, boil_minutes)
    })
  }

  def getSugarFromFermentables(fermentables: NodeSeq): Double = {
    fermentables.foldLeft(0.0)((sugar: Double, node: Node) => {
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
        case _: Exception => {}
      }
      sugar + weight_kg * yield_unit * efficiency
    })
  }

  def getSRMFromFermentables(fermentables: NodeSeq, water_liters: Double): Double = {
    val mcu = fermentables.foldLeft(0.0)((color: Double, node: Node) => {

      var weight_kg: Double = 0.0
      var srm: Double = 0.0
      try {
        weight_kg = (node \ "AMOUNT").text.toString.toDouble
        srm = (node \ "COLOR").text.toString.toDouble
      } catch {
        case _: Exception => {}
      }
      color + (srm * kg_l_to_lb_g * weight_kg / water_liters)
    })
    mcuToSRM_morey(mcu)
  }

  def getDegreesPlato(sugar_kg: Double, batch_l: Double): Double = {
    val water_l = batch_l - sugar_kg / sucroseDensity_kgL
    val water_kg = water_l //Leaving option to correct for water density later

    sugar_kg / (sugar_kg + water_kg) * 100.0
  }

  def getSGfromPlato(plato: Double): Double = {
    plato / (258.6 - (plato / 258.2 * 227.1)) + 1.0
  }

  def setEfficiency(newEfficiency_percent: Double) = {
    efficiency_percent = newEfficiency_percent
  }

  def setSugarLoss(newSugarLoss_percent: Double) {
    sugarLoss_percent = newSugarLoss_percent
  }
}
