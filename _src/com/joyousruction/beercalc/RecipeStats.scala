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
  var bitterness_ibu = 0.0

  lazy val fermentablesAddButton = findViewById(R.id.fermentablesAddButton).asInstanceOf[Button]
  lazy val fermentableTable = findViewById(R.id.fermentableTable).asInstanceOf[TableLayout]

  lazy val hopsAddButton = findViewById(R.id.hopsAddButton).asInstanceOf[Button]
  lazy val hopsTable = findViewById(R.id.hopsTable).asInstanceOf[TableLayout]

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
  }

  private def updateGravity() = {
    sugar_kg = Calculation.getSugarFromFermentables(currentFermentables)
    degrees_plato = Calculation.getDegreesPlato(sugar_kg, liters)
    sg = Calculation.getSGfromPlato(degrees_plato)

    originalGravityTPB.setProgress(((sg - 1.0) * 1000).toInt)
    originalGravityValue.setText("%.3f".format(sg))
  }

  private def updateBitterness() = {
    bitterness_ibu = Calculation.getIbuFromHops(currentHops, sg, liters)

    bitternessUnitsTPB.setProgress(bitterness_ibu.toInt)
    bitternessUnitsValue.setText("%.1f".format(bitterness_ibu))
  }

  private def addFermentableToTable(v: View, node: FermentableSpinnerNode) = {
    val tr: TableRow = new TableRow(this)
    val text: TextView = new TextView(this)
    val amount = (node.node \ "AMOUNT").text.toDouble
    val amountText: TextView = new TextView(this)

    amountText.setText("%.3f".format(amount))

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

    amountText.setText("%.2f".format(amount))
    timeText.setText("%.0f".format(time))

    text.setText((node.node \ "NAME").text.toString())

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

          val amountTextView = dialog.findViewById(R.id.fermentableAmountNumberPicker).asInstanceOf[TextView]
          if (amountTextView.getText().toString != "") {
            val fermentableContents: NodeSeq = (nameSpinner.getSelectedItem().asInstanceOf[FermentableSpinnerNode].node)
            val node: NodeSeq = <FERMENTABLE>{
              (fermentableContents \ "_").foldLeft(NodeSeq.Empty)((B: NodeSeq, myNode: Node) => {
                myNode match {
                  case <AMOUNT>{ ns @ _* }</AMOUNT> => B ++ <AMOUNT>{ "%.5e".format(amountTextView.getText().toString.toDouble) }</AMOUNT>
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
                  case <AMOUNT>{ ns @ _* }</AMOUNT> => B ++ <AMOUNT>{ "%.5e".format(amountNumberPicker.getText().toString.toDouble) }</AMOUNT>
                  case <TIME>{ ns @ _* }</TIME> => B ++ <TIME>{ "%.5e".format(minutesNumberPicker.getText().toString.toDouble) }</TIME>
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

      case MISC_DIALOG => {}

      case YEAST_DIALOG => {}
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