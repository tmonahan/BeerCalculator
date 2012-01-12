package com.joyousruction.beercalc

import scala.xml.{ Elem, Node, NodeSeq, XML }

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import R._

class StartScreen extends Activity {

  lazy val loadButton = findViewById(R.id.loadButton).asInstanceOf[Button]
  lazy val exportButton = findViewById(R.id.exportButton).asInstanceOf[Button]
  lazy val importButton = findViewById(R.id.importButton).asInstanceOf[Button]
  lazy val startButton = findViewById(R.id.startButton).asInstanceOf[Button]

  lazy val recipeTable = findViewById(R.id.recipeTable).asInstanceOf[TableLayout]

  var recipes: NodeSeq = null

  var selectedTableRow: RecipeTableRow = null
  val selectedColor: Int = 0xFFFF5900
  val notSelectedColor: Int = 0xFF000000

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.start_screen)

    startButton.setOnClickListener((v: View) => {
      startActivity(new Intent(StartScreen.this, classOf[StartNewRecipe]))
    })

    loadButton.setOnClickListener((v: View) => {
      if (selectedTableRow != null) {
        Database.currentRecipe = selectedTableRow.getNode()
        startActivity(new Intent(StartScreen.this, classOf[RecipeStats]))
      }
    })

    recipes = Database.recipes

    (recipes \ "_").map((node: NodeSeq) => {
      val tr = new RecipeTableRow(this, node)
      tr.setOnClickListener((v: View) => {
        if (selectedTableRow != null) {
          selectedTableRow.setBackgroundColor(notSelectedColor)
        }
        selectedTableRow = tr
        selectedTableRow.setBackgroundColor(selectedColor)
      })
      recipeTable.addView(tr)

    })

  }

  implicit def func2OnClickListener(func: (View) => Unit): View.OnClickListener = {
    return new View.OnClickListener() {
      override def onClick(v: View) = func(v)
    }
  }
}

class RecipeTableRow(context: Context, node: NodeSeq) extends TableRow(context) {
  lazy val name: String = (node \ "NAME").text.toString
  lazy val recipeType: String = (node \ "TYPE").text.toString
  lazy val brewer: String = (node \ "BREWER").text.toString
  lazy val date: String = (node \ "DATE").text.toString

  var view1 = new TextView(context)
  var view2 = new TextView(context)
  var view3 = new TextView(context)
  var view4 = new TextView(context)

  view1.setText(name)
  view2.setText(recipeType)
  view3.setText(brewer)
  view4.setText(date)

  this.addView(view1)
  this.addView(view2)
  this.addView(view3)
  this.addView(view4)

  def getNode(): NodeSeq = node

}
