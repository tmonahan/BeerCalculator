package com.joyousruction.beercalc

import scala.xml.{ Elem, Node, NodeSeq, XML }
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import R._
import java.io.File

class StartScreen extends Activity {

  val EXPORT_DIALOG = 0
  val IMPORT_DIALOG = 1
  
  lazy val loadButton = findViewById(R.id.loadButton).asInstanceOf[Button]
  lazy val exportButton = findViewById(R.id.exportButton).asInstanceOf[Button]
  lazy val importButton = findViewById(R.id.importButton).asInstanceOf[Button]
  lazy val startButton = findViewById(R.id.startButton).asInstanceOf[Button]

  lazy val recipeTable = findViewById(R.id.recipeTable).asInstanceOf[TableLayout]

  var recipes: NodeSeq = null

  var selectedTableRow: RecipeTableRow = null
  var recipeToLoad: FileTableRow = null
  val selectedColor: Int = 0xFFFF5900
  val notSelectedColor: Int = 0xFF000000

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.start_screen)

    loadButton.setOnClickListener((v: View) => {
      if (selectedTableRow != null) {
        Database.currentRecipe = selectedTableRow.getNode()
        startActivity(new Intent(StartScreen.this, classOf[RecipeStats]))
      }
    })

    importButton.setOnClickListener((v: View) => {
      showDialog(IMPORT_DIALOG)
    })

    startButton.setOnClickListener((v: View) => {
      startActivity(new Intent(StartScreen.this, classOf[StartNewRecipe]))
    })

  }

  override def onStart() {
    super.onStart()
    recipeTable.removeAllViews()
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

  override def onCreateDialog(id: Int): Dialog = {
    lazy val dialog: Dialog = new Dialog(this)

    id match {
      case EXPORT_DIALOG => {
        configureExportDialog(dialog)
      }
      case IMPORT_DIALOG => {
        configureImportDialog(dialog)
      }
      case _ => {}
    }
    dialog
  }

  def configureExportDialog(dialog: Dialog) {
    dialog.setContentView(R.layout.export_dialog)
    dialog.setTitle("Export Recipe")
  }

  def configureImportDialog(dialog: Dialog) {
    dialog.setContentView(R.layout.import_dialog)
    dialog.setTitle("Import Recipe")

    val mediaState = Environment.getExternalStorageState()
    if (Environment.MEDIA_MOUNTED.equals(mediaState) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(mediaState)) {
      var files = Environment.getExternalStorageDirectory().listFiles().toSeq
      files.map((p: java.io.File) => {
        if (p.exists() && !p.isHidden()) {
          addFileToTable(p, dialog.findViewById(R.id.importTable).asInstanceOf[TableLayout])
        }
      })
    }
  }

  def addFileToTable(f: java.io.File, t: TableLayout) {
    var tr = new TableRow(this)
    val fileName = new TextView(this)
    fileName.setText(f.getName())
    if (f.isDirectory()) {
      val tl = new TableLayout(this)
      val dr = new TableRow(this)
      //not sure why the implicit def did not work here...
      val onClick: View.OnClickListener = func2OnClickListener((v: View) => {
        var subFiles = f.listFiles()
        subFiles.map((p: java.io.File) => {
          if (!p.isHidden()) {
            addFileToTable(p, tl)
          }
        })
      })
      dr.setOnClickListener(onClick)
      //replace with file icon later
      val icon = new TextView(this)
      icon.setText("<DIR>")

      dr.addView(icon)
      dr.addView(fileName)
      tl.addView(dr)
      tr.addView(tl)
    } else {
      tr = new FileTableRow(this, f)
      tr.setOnClickListener((v: View) => {
        if (recipeToLoad != null) {
          recipeToLoad.setBackgroundColor(notSelectedColor)
        }
        recipeToLoad = tr.asInstanceOf[FileTableRow]
        recipeToLoad.setBackgroundColor(selectedColor)
      })
    }
    t.addView(tr)
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

class FileTableRow(context: Context, file: java.io.File) extends TableRow(context) {
  lazy val name = file.getName()

  var view1 = new TextView(context)

  view1.setText(name)

  this.addView(view1)

  def getFile(): java.io.File = file
}