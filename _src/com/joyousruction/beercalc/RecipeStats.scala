package com.joyousruction.beercalc

import scala.xml.{ Elem, Node, NodeSeq, XML }

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TableRow.LayoutParams
import android.widget.TableRow.LayoutParams._
import android.widget.TextView
import R._

class RecipeStats extends Activity {
  // defined static parameters for convenience here
  val MATCH_PARENT = android.view.ViewGroup.LayoutParams.MATCH_PARENT
  val WRAP_CONTENT = android.view.ViewGroup.LayoutParams.WRAP_CONTENT

  // defined the dialog numbers here
  val FERMENTABLE_DIALOG = 0
  val HOPS_DIALOG = 1
  val MISC_DIALOG = 2
  val YEAST_DIALOG = 3

  //constants (TODO - make these user defined)
  val gallons = 6.0
  val liters = Calculation.getLitersFromGallons(gallons)

  //recipe variables:
  var sugar_kg = 0.0
  var degrees_plato = 0.0
  var sg = 1.000
  var fg = 1.000
  var bitterness_ibu = 0.0
  var color_srm = 0.0
  var estimatedAttenuation = 0.0

  lazy val fermentablesAddButton = findViewById(R.id.fermentablesAddButton).asInstanceOf[Button]
  lazy val fermentableTable = findViewById(R.id.fermentableTable).asInstanceOf[TableLayout]

  lazy val hopsAddButton = findViewById(R.id.hopsAddButton).asInstanceOf[Button]
  lazy val hopsTable = findViewById(R.id.hopsTable).asInstanceOf[TableLayout]
  
  lazy val miscAddButton = findViewById(R.id.miscAddButton).asInstanceOf[Button]
  lazy val miscTable = findViewById(R.id.miscTable).asInstanceOf[TableLayout]
  
  lazy val yeastAddButton = findViewById(R.id.yeastAddButton).asInstanceOf[Button]
  lazy val yeastTable = findViewById(R.id.yeastTable).asInstanceOf[TableLayout]

  lazy val originalGravityTPB = findViewById(R.id.ogTargetedProgressBar).asInstanceOf[TargetedProgressBar]
  lazy val finalGravityTPB = findViewById(R.id.fgTargetedProgressBar).asInstanceOf[TargetedProgressBar]
  lazy val bitternessUnitsTPB = findViewById(R.id.buTargetedProgressBar).asInstanceOf[TargetedProgressBar]
  lazy val bitternessGravityTPB = findViewById(R.id.buguTargetedProgressBar).asInstanceOf[TargetedProgressBar]
  lazy val colorTPB = findViewById(R.id.colorTargetedProgressBar).asInstanceOf[TargetedProgressBar]

  lazy val originalGravityValue = findViewById(R.id.ogValue).asInstanceOf[TextView]
  lazy val finalGravityValue = findViewById(R.id.fgValue).asInstanceOf[TextView]
  lazy val bitternessUnitsValue = findViewById(R.id.buValue).asInstanceOf[TextView]
  lazy val bitternessGravityValue = findViewById(R.id.buguValue).asInstanceOf[TextView]
  lazy val colorValue = findViewById(R.id.colorValue).asInstanceOf[TextView]

  var currentRecipe: NodeSeq = null
  var currentStyle: NodeSeq = null

  var currentFermentables: NodeSeq = NodeSeq.Empty
  var currentHops: NodeSeq = NodeSeq.Empty
  var currentMisc: NodeSeq = NodeSeq.Empty
  var currentYeast: NodeSeq = NodeSeq.Empty


  var originalGravity = 1.000
  var finalGravity = 1.000
  var ibu = 0.0
  var color = 0.0
  var abv = 0.0
  var carbonation = 0.0

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.recipestats)

    currentRecipe = Database.getCurrentRecipe

    currentStyle = currentRecipe \ "STYLE"

    fermentablesAddButton.setOnClickListener((v: View) => {
      showDialog(FERMENTABLE_DIALOG)
    })

    hopsAddButton.setOnClickListener((v: View) => {
      showDialog(HOPS_DIALOG)
    })

    miscAddButton.setOnClickListener((v: View) => {
      showDialog(MISC_DIALOG)
    })

    yeastAddButton.setOnClickListener((v: View) => {
      showDialog(YEAST_DIALOG)
    })

    //TODO- make this MIN/MAX come from the MIN/MAX in the recipes
    var minOG = getGravity("OG_MIN", 0)
    var maxOG = getGravity("OG_MAX", 200)
    originalGravityTPB.setMax(200)
    originalGravityTPB.setMaxTargetProgress(maxOG)
    originalGravityTPB.setMinTargetProgress(minOG)

    var minFG = getGravity("FG_MIN", 0)
    var maxFG = getGravity("FG_MAX", 50)
    finalGravityTPB.setMax(50)
    finalGravityTPB.setMaxTargetProgress(maxFG)
    finalGravityTPB.setMinTargetProgress(minFG)

    var minIBU = getValue("IBU_MIN", 1.0f, 0, 0)
    var maxIBU = getValue("IBU_MAX", 1.0f, 0, 150)
    bitternessUnitsTPB.setMax(150)
    bitternessUnitsTPB.setMaxTargetProgress(maxIBU)
    bitternessUnitsTPB.setMinTargetProgress(minIBU)

    var minColor = getValue("COLOR_MIN", 10.0f, 0, 0)
    var maxColor = getValue("COLOR_MAX", 10.0f, 0, 500)
    colorTPB.setMax(500)
    colorTPB.setMaxTargetProgress(maxColor)
    colorTPB.setMinTargetProgress(minColor)

    var minABV = getValue("ABV_MIN", 10.0f, 0, 0)
    var maxABV = getValue("ABV_MAX", 10.0f, 0, 30)

    var minCarbonation = getValue("CARB_MIN", 10.0f, 0, 0)
    var maxCarbonation = getValue("CARB_MAX", 10.0f, 0, 50)

  }
  private def getValue(name: String, scale: Float, offset: Int, defaultValue: Int): Int = {
    try {
      ((currentStyle \ name).text.toFloat * scale).round - offset
    } catch {
      case e: Exception => defaultValue
    }
  }
  private def getGravity(name: String, defaultValue: Int): Int = {
    getValue(name, 1000.0f, 1000, defaultValue)
  }

  private def deleteButton(text: String, func: () => Unit): Button = {
    val button: Button = new Button(this)
    button.setText(text)
    button.setOnClickListener((v: View) => {
      val buttonParent = button.getParent()
      val buttonGrandParent: ViewGroup = buttonParent.getParent().asInstanceOf[ViewGroup]

      func()

      buttonGrandParent.removeView(buttonParent.asInstanceOf[View])

    })

    button
  }

  private def removeNode(list: NodeSeq, itemToRemove: Node): NodeSeq = {
    list.foldLeft(NodeSeq.Empty)((allNodes: NodeSeq, currentNode: Node) => {
      currentNode match {
        case `itemToRemove` => allNodes
        case _ => allNodes ++ currentNode
      }
    })
  }

  private def updateAll() = {
    updateGravity()
    updateBitterness()
    updateColor()
  }

  private def updateGravity() = {
    sugar_kg = Calculation.getSugarFromFermentables(currentFermentables)
    degrees_plato = Calculation.getDegreesPlato(sugar_kg, liters)
    sg = Calculation.getSGfromPlato(degrees_plato)
    estimatedAttenuation = Calculation.getAttenuationFromYeast(currentYeast)
    fg = (sg - 1.0) * (1.0 - (estimatedAttenuation/100.0)) + 1.0

    originalGravityTPB.setProgress(((sg - 1.0) * 1000).toInt)
    originalGravityValue.setText("%.3f".format(sg))
    finalGravityTPB.setProgress(((fg-1.0) * 1000).toInt)
    finalGravityValue.setText("%.3f".format(fg))
  }

  private def updateBitterness() = {
    bitterness_ibu = Calculation.getIbuFromHops(currentHops, sg, liters)

    bitternessUnitsTPB.setProgress(bitterness_ibu.toInt)
    bitternessUnitsValue.setText("%.1f".format(bitterness_ibu))
  }
  
  private def updateColor() = {
    color_srm = Calculation.getSRMFromFermentables(currentFermentables, liters)
    
    colorTPB.setProgress((color_srm * 10.0).toInt)
    colorValue.setText("%.1f".format(color_srm))
  }

  private def addFermentableToTable(v: View, node: FermentableSpinnerNode) = {
    val tr: TableRow = new TableRow(this)
    val text: TextView = new TextView(this)
    val amount = (node.node \ "AMOUNT").text.toDouble
    val amountText: TextView = new TextView(this)

    amountText.setText("%.3f".format(Calculation.convertKgLbs(amount)))
    amountText.setGravity(android.view.Gravity.CENTER_HORIZONTAL)

    text.setText((node.node \ "NAME").text.toString())

    tr.addView(text)
    tr.addView(amountText)
    tr.addView(deleteButton("Delete", () => { currentFermentables = removeNode(currentFermentables, node.node); updateAll() }))

    fermentableTable.addView(tr, new TableLayout.LayoutParams(
      MATCH_PARENT,
      WRAP_CONTENT))

    v.requestLayout()
    v.invalidate()
  }

  private def addHopsToTable(v: View, node: HopsSpinnerNode) = {
    val tr: TableRow = new TableRow(this)
    val text: TextView = new TextView(this)
    val amount = (node.node \ "AMOUNT").text.toDouble
    val amountText: TextView = new TextView(this)
    val time = (node.node \ "TIME").text.toDouble
    val timeText: TextView = new TextView(this)

    amountText.setText("%.2f".format(Calculation.convertGOz(amount)))
    timeText.setText("%.0f".format(time))
    
    text.setText((node.node \ "NAME").text.toString())

    amountText.setGravity(android.view.Gravity.CENTER_HORIZONTAL)
    timeText.setGravity(android.view.Gravity.CENTER_HORIZONTAL)
    
    tr.addView(text)
    tr.addView(timeText)
    tr.addView(amountText)
    tr.addView(deleteButton("Delete", () => { currentHops = removeNode(currentHops, node.node); updateBitterness() }))

    hopsTable.addView(tr, new TableLayout.LayoutParams(
      MATCH_PARENT,
      WRAP_CONTENT))

    v.requestLayout()
    v.invalidate()
  }

  private def addMiscToTable(v: View, node: MiscSpinnerNode) = {
    val tr: TableRow = new TableRow(this)
    val text: TextView = new TextView(this)
    val amount = (node.node \ "AMOUNT").text.toDouble
    val amountText: TextView = new TextView(this)
    val time = (node.node \ "TIME").text.toDouble
    val timeText: TextView = new TextView(this)

    amountText.setText("%.2f".format(amount))
    timeText.setText("%.0f".format(time))

    text.setText((node.node \ "NAME").text.toString())
    
    amountText.setGravity(android.view.Gravity.CENTER_HORIZONTAL)
    timeText.setGravity(android.view.Gravity.CENTER_HORIZONTAL)

    tr.addView(text)
    tr.addView(timeText)
    tr.addView(amountText)
    tr.addView(deleteButton("Delete", () => { currentMisc = removeNode(currentMisc, node.node)}))

    miscTable.addView(tr, new TableLayout.LayoutParams(
      MATCH_PARENT,
      WRAP_CONTENT))

    v.requestLayout()
    v.invalidate()
  }

  private def addYeastToTable(v: View, node: YeastSpinnerNode) = {
    val tr: TableRow = new TableRow(this)
    val text: TextView = new TextView(this)
    val amount = (node.node \ "AMOUNT").text.toDouble
    val amountText: TextView = new TextView(this)
    amountText.setText("%.2f".format(amount))

    text.setText((node.node \ "NAME").text.toString())
    
    amountText.setGravity(android.view.Gravity.CENTER_HORIZONTAL)

    tr.addView(text)
    tr.addView(amountText)
    tr.addView(deleteButton("Delete", () => { currentYeast = removeNode(currentYeast, node.node)}))

    yeastTable.addView(tr, new TableLayout.LayoutParams(
      MATCH_PARENT,
      WRAP_CONTENT))

    v.requestLayout()
    v.invalidate()
  }

  override def onCreateDialog(id: Int): Dialog = {
    lazy val mContext = getApplicationContext()
    lazy val dialog: Dialog = new Dialog(this)

    id match {
      case FERMENTABLE_DIALOG => {
        dialog.setContentView(R.layout.fermentable_dialog)
        dialog.setTitle("Add Fermentable:")

        //TODO handle the spinners here
        val nameSpinner = dialog.findViewById(R.id.fermentableNameSpinner).asInstanceOf[Spinner]
        //TODO change the EditText boxes to NumberPickers when upgrading to API 11
        val yieldNumberPicker = dialog.findViewById(R.id.fermentableYeildNumberPicker).asInstanceOf[EditText]
        val colorNumberPicker = dialog.findViewById(R.id.fermentableColorNumberPicker).asInstanceOf[EditText]
        val notesTextView = dialog.findViewById(R.id.fermentableNotesTextView).asInstanceOf[TextView]

        val fermentables = Database.getFermentables

        var fermentableNameArray: Array[FermentableSpinnerNode] = new Array(fermentables.length)
        fermentables.map { node: Node =>
          new FermentableSpinnerNode(node)
        }.copyToArray(fermentableNameArray)

        var fermentableStyleArray: ArrayAdapter[FermentableSpinnerNode] = new ArrayAdapter[FermentableSpinnerNode](this, android.R.layout.simple_spinner_item, fermentableNameArray)

        nameSpinner.setAdapter(fermentableStyleArray)

        val nameOnSelectListener: AdapterView.OnItemSelectedListener = createOnItemSelectedListener((av: AdapterView[_], v: View, i: Int, l: Long) => {
          val selectedNode: Node = nameSpinner.getItemAtPosition(i).asInstanceOf[FermentableSpinnerNode].node

          yieldNumberPicker.setText((selectedNode \ "YIELD").text.toDouble.toString())
          colorNumberPicker.setText((selectedNode \ "COLOR").text.toDouble.toString())

          notesTextView.setText("Fermentable Notes:\n" + (selectedNode \ "NOTES").text.toString())

          v.requestLayout()
          v.invalidate()

        }, (av: AdapterView[_]) => {})

        nameSpinner.setOnItemSelectedListener(nameOnSelectListener)

        //Handle buttons
        val addButton = dialog.findViewById(R.id.fermentableAddItemButton).asInstanceOf[Button]
        val cancelButton = dialog.findViewById(R.id.fermentableCancelButton).asInstanceOf[Button]

        addButton.setOnClickListener((v: View) => {

          val amountTextView = dialog.findViewById(R.id.fermentableAmountNumberPicker).asInstanceOf[EditText]
          val colorTextView = dialog.findViewById(R.id.fermentableColorNumberPicker).asInstanceOf[EditText]
          val yieldTextView = dialog.findViewById(R.id.fermentableYeildNumberPicker).asInstanceOf[EditText]
          if (amountTextView.getText().toString != "") {
            val fermentableContents: NodeSeq = (nameSpinner.getSelectedItem().asInstanceOf[FermentableSpinnerNode].node)
            val node: NodeSeq = <FERMENTABLE>{
              (fermentableContents \ "_").foldLeft(NodeSeq.Empty)((B: NodeSeq, myNode: Node) => {
                myNode match {
                  case <AMOUNT>{ ns @ _* }</AMOUNT> => B ++ <AMOUNT>{ "%.5e".format(Calculation.convertLbsKg(amountTextView.getText().toString.toDouble)) }</AMOUNT>
                  case <COLOR>{ ns @ _* }</COLOR> => B ++ <COLOR>{ "%.5e".format(colorTextView.getText().toString.toDouble) }</COLOR>
                  case <YIELD>{ ns @ _* }</YIELD> => B ++ <YIELD>{"%.5e".format(yieldTextView.getText().toString.toDouble) }</YIELD>
                  case _ => B ++ myNode
                }
              })
            }</FERMENTABLE>
            val spinnerNode = new FermentableSpinnerNode(node.last)
            currentFermentables = currentFermentables ++ spinnerNode.node
            addFermentableToTable(v, spinnerNode)
            updateAll()
          }
          dialog.dismiss()
        })

        cancelButton.setOnClickListener((v: View) => {
          dialog.dismiss()
        })

      }

      case HOPS_DIALOG => {
        dialog.setContentView(R.layout.hops_dialog)
        dialog.setTitle("Add Hop:")

        val nameSpinner = dialog.findViewById(R.id.hopsNameSpinner).asInstanceOf[Spinner]
        val notesTextView = dialog.findViewById(R.id.hopsNotesTextView).asInstanceOf[TextView]
        val substitutesTextView = dialog.findViewById(R.id.hopsSubstitutesTextView).asInstanceOf[TextView]
        lazy val alphaNumberPicker = dialog.findViewById(R.id.hopsAlphaNumberPicker).asInstanceOf[EditText]
        lazy val minutesNumberPicker = dialog.findViewById(R.id.hopsMinutesNumberPicker).asInstanceOf[EditText]
        lazy val amountNumberPicker = dialog.findViewById(R.id.hopsAmountNumberPicker).asInstanceOf[EditText]

        val hops = Database.getHops

        var hopsNameArray: Array[HopsSpinnerNode] = new Array(hops.length)
        hops.map { node: Node =>
          new HopsSpinnerNode(node)
        }.copyToArray(hopsNameArray)

        var hopsStyleArray: ArrayAdapter[HopsSpinnerNode] = new ArrayAdapter[HopsSpinnerNode](this, android.R.layout.simple_spinner_item, hopsNameArray)

        nameSpinner.setAdapter(hopsStyleArray)

        val nameOnSelectListener: AdapterView.OnItemSelectedListener = createOnItemSelectedListener((av: AdapterView[_], v: View, i: Int, l: Long) => {
          val selectedNode: Node = nameSpinner.getItemAtPosition(i).asInstanceOf[HopsSpinnerNode].node

          alphaNumberPicker.setText((selectedNode \ "ALPHA").text.toDouble.toString())
          minutesNumberPicker.setText((selectedNode \ "TIME").text.toDouble.toString())

          notesTextView.setText("Hop Notes:\n" + (selectedNode \ "NOTES").text.toString())
          substitutesTextView.setText("Hop substitutes:\n" + (selectedNode \ "SUBSTITUTES").text.toString())

          v.requestLayout()
          v.invalidate()

        }, (av: AdapterView[_]) => {})

        nameSpinner.setOnItemSelectedListener(nameOnSelectListener)

        //Handle buttons
        val addButton = dialog.findViewById(R.id.hopsAddItemButton).asInstanceOf[Button]
        val cancelButton = dialog.findViewById(R.id.hopsCancelButton).asInstanceOf[Button]

        addButton.setOnClickListener((v: View) => {

          if (amountNumberPicker.getText().toString != "" && minutesNumberPicker.getText().toString != "") {
            val hopContents: NodeSeq = (nameSpinner.getSelectedItem().asInstanceOf[HopsSpinnerNode].node)
            val node: NodeSeq = <HOP>{
              (hopContents \ "_").foldLeft(NodeSeq.Empty)((B: NodeSeq, myNode: Node) => {
                myNode match {
                  case <AMOUNT>{ ns @ _* }</AMOUNT> => B ++ <AMOUNT>{ "%.5e".format(Calculation.convertOzG(amountNumberPicker.getText().toString.toDouble)) }</AMOUNT>
                  case <TIME>{ ns @ _* }</TIME> => B ++ <TIME>{ "%.5e".format(minutesNumberPicker.getText().toString.toDouble) }</TIME>
                  case <ALPHA>{ ns @ _* }</ALPHA> => B ++ <ALPHA>{ "%.5e".format(alphaNumberPicker.getText().toString.toDouble) }</ALPHA>
                  case _ => B ++ myNode
                }
              })
            }</HOP>
            val spinnerNode = new HopsSpinnerNode(node.last)
            currentHops = currentHops ++ spinnerNode.node
            addHopsToTable(v, spinnerNode)
            updateBitterness()
          }
          dialog.dismiss()
        })

        cancelButton.setOnClickListener((v: View) => {
          dialog.dismiss()
        })
      }

      case MISC_DIALOG => {
        dialog.setContentView(R.layout.misc_dialog)
        dialog.setTitle("Add Misc:")
        
        val nameSpinner = dialog.findViewById(R.id.miscNameSpinner).asInstanceOf[Spinner]
        lazy val amountNumberPicker = dialog.findViewById(R.id.miscAmountNumberPicker).asInstanceOf[EditText]
        lazy val timeNumberPicker = dialog.findViewById(R.id.miscTimeNumberPicker).asInstanceOf[EditText]
        lazy val typeEditText = dialog.findViewById(R.id.miscTypeEditText).asInstanceOf[EditText]
        val notesTextView = dialog.findViewById(R.id.miscNotesTextView).asInstanceOf[TextView]
        val useTextView = dialog.findViewById(R.id.miscUseTextView).asInstanceOf[TextView]
        
        val misc = Database.getMisc
        var nameArray: Array[MiscSpinnerNode] = new Array(misc.length)
        misc.map { node: Node =>
          new MiscSpinnerNode(node)
        }.copyToArray(nameArray)

        var styleArray: ArrayAdapter[MiscSpinnerNode] = new ArrayAdapter[MiscSpinnerNode](this, android.R.layout.simple_spinner_item, nameArray)

        nameSpinner.setAdapter(styleArray)
                
        val nameOnSelectListener: AdapterView.OnItemSelectedListener = createOnItemSelectedListener((av: AdapterView[_], v: View, i: Int, l: Long) => {
          val selectedNode: Node = nameSpinner.getItemAtPosition(i).asInstanceOf[MiscSpinnerNode].node

          timeNumberPicker.setText((selectedNode \ "TIME").text.toDouble.toString())
          typeEditText.setText((selectedNode \ "TYPE").text.toString())

          notesTextView.setText("Notes:\n" + (selectedNode \ "NOTES").text.toString())
          useTextView.setText("Use for:\n" + (selectedNode \ "USE_FOR").text.toString())

          v.requestLayout()
          v.invalidate()

        }, (av: AdapterView[_]) => {})

        nameSpinner.setOnItemSelectedListener(nameOnSelectListener)

        //Handle buttons
        val addButton = dialog.findViewById(R.id.miscAddItemButton).asInstanceOf[Button]
        val cancelButton = dialog.findViewById(R.id.miscCancelButton).asInstanceOf[Button]
        
        addButton.setOnClickListener((v: View) => {
          if (amountNumberPicker.getText().toString != "" && timeNumberPicker.getText().toString != "") {
            val miscContents: NodeSeq = (nameSpinner.getSelectedItem().asInstanceOf[MiscSpinnerNode].node)
            val node: NodeSeq = <MISC>{
              (miscContents \ "_").foldLeft(NodeSeq.Empty)((B: NodeSeq, myNode: Node) => {
                myNode match {
                  case <AMOUNT>{ ns @ _* }</AMOUNT> => B ++ <AMOUNT>{ "%.5e".format(amountNumberPicker.getText().toString.toDouble) }</AMOUNT>
                  case <TIME>{ ns @ _* }</TIME> => B ++ <TIME>{ "%.5e".format(timeNumberPicker.getText().toString.toDouble) }</TIME>
                  case <TYPE>{ ns @ _* }</TYPE> => B ++ <TYPE>{ typeEditText.getText().toString }</TYPE>
                  case _ => B ++ myNode
                }
              })
            }</MISC>
            val spinnerNode = new MiscSpinnerNode(node.last)
            currentMisc = currentMisc ++ spinnerNode.node
            addMiscToTable(v, spinnerNode)
          }
          dialog.dismiss()
        })        
        cancelButton.setOnClickListener((v: View) => {
          dialog.dismiss()
        })
        
        }

      case YEAST_DIALOG => {
        dialog.setContentView(R.layout.yeast_dialog)
        dialog.setTitle("Add Yeast:")
        
        val nameSpinner = dialog.findViewById(R.id.yeastNameSpinner).asInstanceOf[Spinner]
        lazy val amountNumberPicker = dialog.findViewById(R.id.yeastAmountNumberPicker).asInstanceOf[EditText]
        lazy val attenuationNumberPicker = dialog.findViewById(R.id.yeastAttenuationNumberPicker).asInstanceOf[EditText]
        val formTextView = dialog.findViewById(R.id.yeastFormTextView).asInstanceOf[TextView]
        val laboratoryTextView = dialog.findViewById(R.id.yeastLaboratoryTextView).asInstanceOf[TextView]
        val notesTextView = dialog.findViewById(R.id.yeastNotesTextView).asInstanceOf[TextView]
        val flocculationTextView = dialog.findViewById(R.id.yeastFlocculationTextView).asInstanceOf[TextView]
               
        val yeast = Database.getYeast
        var nameArray: Array[YeastSpinnerNode] = new Array(yeast.length)
        yeast.map { node: Node =>
          new YeastSpinnerNode(node)
        }.copyToArray(nameArray)

        var styleArray: ArrayAdapter[YeastSpinnerNode] = new ArrayAdapter[YeastSpinnerNode](this, android.R.layout.simple_spinner_item, nameArray)

        nameSpinner.setAdapter(styleArray)
                        
        val nameOnSelectListener: AdapterView.OnItemSelectedListener = createOnItemSelectedListener((av: AdapterView[_], v: View, i: Int, l: Long) => {
          val selectedNode: Node = nameSpinner.getItemAtPosition(i).asInstanceOf[YeastSpinnerNode].node

          formTextView.setText((selectedNode \ "FORM").text.toString())
          laboratoryTextView.setText((selectedNode \ "LABORATORY").text.toString())
          attenuationNumberPicker.setText((selectedNode \ "ATTENUATION").text.toDouble.toString())
          amountNumberPicker.setText((selectedNode \ "AMOUNT").text.toDouble.toString())

          notesTextView.setText("Notes:\n" + (selectedNode \ "NOTES").text.toString())
          flocculationTextView.setText("Flocculation: " + (selectedNode \ "FLOCCULATION").text.toString())

          v.requestLayout()
          v.invalidate()

        }, (av: AdapterView[_]) => {})

        nameSpinner.setOnItemSelectedListener(nameOnSelectListener)
        //Handle buttons
        val addButton = dialog.findViewById(R.id.yeastAddItemButton).asInstanceOf[Button]
        val cancelButton = dialog.findViewById(R.id.yeastCancelButton).asInstanceOf[Button]
        
        addButton.setOnClickListener((v: View) => {
                    
          if (amountNumberPicker.getText().toString != "") {
            val yeastContents: NodeSeq = (nameSpinner.getSelectedItem().asInstanceOf[YeastSpinnerNode].node)
            val node: NodeSeq = <YEAST>{
              (yeastContents \ "_").foldLeft(NodeSeq.Empty)((B: NodeSeq, myNode: Node) => {
                myNode match {
                  case <AMOUNT>{ ns @ _* }</AMOUNT> => B ++ <AMOUNT>{ "%.5e".format(amountNumberPicker.getText().toString.toDouble) }</AMOUNT>
                  case <ATTENUATION>{ ns @ _* }</ATTENUATION> => B ++ <ATTENUATION>{ "%.5e".format(attenuationNumberPicker.getText().toString.toDouble) }</ATTENUATION>
                  case _ => B ++ myNode
                }
              })
            }</YEAST>
            val spinnerNode = new YeastSpinnerNode(node.last)
            currentYeast = currentYeast ++ spinnerNode.node
            addYeastToTable(v, spinnerNode)
            updateGravity()
          }
          dialog.dismiss()
        })
                
        cancelButton.setOnClickListener((v: View) => {
          dialog.dismiss()
        })
        
        }
    }

    dialog
  }

  implicit def func2OnClickListener(func: (View) => Unit): View.OnClickListener = {
    return new View.OnClickListener() {
      override def onClick(v: View) = func(v)
    }
  }

  implicit def createOnItemSelectedListener(func: (AdapterView[_], View, Int, Long) => Unit, f: (AdapterView[_]) => Unit): AdapterView.OnItemSelectedListener = {
    return new AdapterView.OnItemSelectedListener() {
      override def onItemSelected(av: AdapterView[_], v: View, i: Int, l: Long) = func(av, v, i, l)

      override def onNothingSelected(av: AdapterView[_]) = f(av)
    }
  }
}

class FermentableSpinnerNode(val node: Node) {
  override def toString(): String = {
    ((node \ "NAME").text).toString()
  }
}

class HopsSpinnerNode(val node: Node) {
  override def toString(): String = {
    ((node \ "NAME").text).toString()
  }
}
  
class MiscSpinnerNode(val node: Node) {
  override def toString(): String = {
    ((node \ "NAME").text).toString()
  }
}
  
class YeastSpinnerNode(val node: Node) {
  override def toString(): String = {
    ((node \ "NAME").text).toString()
  }
}