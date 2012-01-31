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
import android.widget.EditText
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import R._
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

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
  var recipeToImport: FileTableRow = null
  var recipeToExport: FileTableRow = null
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

    exportButton.setOnClickListener((v: View) => {
      if (selectedTableRow == null) {
        Toast.makeText(this, "No recipe selected, please select a recipe to export", Toast.LENGTH_SHORT).show()
      } else {
        showDialog(EXPORT_DIALOG)
      }
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

  override def onBackPressed() = {
    moveTaskToBack(true)
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

  override def onPrepareDialog(id: Int, dialog: Dialog) = {

    id match {
      case EXPORT_DIALOG => {
        prepareExportDialog(dialog)
      }
      case IMPORT_DIALOG => {
        prepareImportDialog(dialog)
      }
      case _ => {}
    }
    super.onPrepareDialog(id, dialog)
  }

  def configureExportDialog(dialog: Dialog) {
    dialog.setContentView(R.layout.export_dialog)
    dialog.setTitle("Export Recipe")

    val okButton = dialog.findViewById(R.id.okExportButton).asInstanceOf[Button]
    val cancelButton = dialog.findViewById(R.id.cancelExportButton).asInstanceOf[Button]

    cancelButton.setOnClickListener((v: View) => {
      dialog.dismiss()
    })

    okButton.setOnClickListener((v: View) => {
      try {
        val exportNameEditText = dialog.findViewById(R.id.fileExportNameEditText).asInstanceOf[EditText]
        val newFileName = exportNameEditText.getText().toString()
        val fileDirectory = recipeToExport.getFile()
        val newFile = new java.io.File(fileDirectory, newFileName)
        val os = new java.io.FileOutputStream(newFile)

        Database.exportRecipe(os, selectedTableRow.getNode())

      } catch {
        case e: Exception => { Toast.makeText(this, "File export failed", Toast.LENGTH_SHORT).show() }
      }
      dialog.dismiss()
    })
  }

  def configureImportDialog(dialog: Dialog) {
    dialog.setContentView(R.layout.import_dialog)
    dialog.setTitle("Import Recipe")

    val okButton = dialog.findViewById(R.id.okImportButton).asInstanceOf[Button]
    val cancelButton = dialog.findViewById(R.id.cancelImportButton).asInstanceOf[Button]

    cancelButton.setOnClickListener((v: View) => {
      dialog.dismiss()
    })

    okButton.setOnClickListener((v: View) => {
      if (recipeToImport != null) {
        try {
          val file = recipeToImport.getFile()
          val is = new FileInputStream(file)
          Database.importRecipe(is, this)

          val intent = getIntent()
          finish()
          startActivity(intent)

        } catch {
          case e: Exception => { Toast.makeText(this, "File import failed", Toast.LENGTH_SHORT).show() } //Add failed to open message here
        }
      }
      dialog.dismiss()
    })

  }

  def prepareExportDialog(dialog: Dialog) {
    val table = dialog.findViewById(R.id.exportTable).asInstanceOf[TableLayout]
    table.removeAllViews()
    recipeToExport = null

    val mediaState = Environment.getExternalStorageState()
    if (Environment.MEDIA_MOUNTED.equals(mediaState) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(mediaState)) {
      var files = Environment.getExternalStorageDirectory().listFiles().toSeq
      files.map((p: java.io.File) => {
        if (p.exists() && p.canWrite() && !p.isHidden() && p.isDirectory()) {
          addFileToTable(p, table,
            (p: java.io.File) => { p.exists() && p.canRead() && !p.isHidden() && p.isDirectory() },
            (tr: FileTableRow) => {
              if (recipeToExport != null) {
                recipeToExport.setBackgroundColor(notSelectedColor)
              }
              recipeToExport = tr.asInstanceOf[FileTableRow]
              recipeToExport.setBackgroundColor(selectedColor)
            },
            (tr: FileTableRow) => {})
        }
      })
    }
  }

  def prepareImportDialog(dialog: Dialog) {
    val table = dialog.findViewById(R.id.importTable).asInstanceOf[TableLayout]
    table.removeAllViews()
    recipeToImport = null

    val mediaState = Environment.getExternalStorageState()
    if (Environment.MEDIA_MOUNTED.equals(mediaState) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(mediaState)) {
      var files = Environment.getExternalStorageDirectory().listFiles().toSeq
      files.map((p: java.io.File) => {
        if (p.exists() && p.canRead() && !p.isHidden()) {
          addFileToTable(p, table,
            (p: java.io.File) => { p.exists() && p.canRead() && !p.isHidden() },
            (tr: FileTableRow) => {},
            (tr: FileTableRow) => {
              if (recipeToImport != null) {
                recipeToImport.setBackgroundColor(notSelectedColor)
              }
              recipeToImport = tr.asInstanceOf[FileTableRow]
              recipeToImport.setBackgroundColor(selectedColor)
            })
        }
      })
    }
  }

  def addFileToTable(f: java.io.File, t: TableLayout,
    fileOk: (java.io.File) => Boolean,
    onDirClick: (FileTableRow) => Unit, onFileClick: (FileTableRow) => Unit) {
    var tr = new TableRow(this)
    val fileName = new TextView(this)
    fileName.setText(f.getName())
    if (f.isDirectory()) {
      val tl = new TableLayout(this)
      val dr = new FileTableRow(this, f)
      //not sure why the implicit def did not work here...
      val onClick: View.OnClickListener = func2OnClickListener((v: View) => {
        if (tl.getChildCount() > 1) {
          tl.removeViews(1, tl.getChildCount - 1)
        } else {
          var subFiles = f.listFiles()
          subFiles.map((p: java.io.File) => {
            if (fileOk(p)) {
              addFileToTable(p, tl, fileOk, onDirClick, onFileClick)
            }
          })
          if (tl.getChildCount() == 1) {
            val emptyRow = new TableRow(this)
            val emptyText = new TextView(this)
            emptyText.setText("--- no files to open ---")
            emptyRow.addView(new View(this))
            emptyRow.addView(emptyText)
            tl.addView(emptyRow)
          }
        }
        onDirClick(dr)
      })
      dr.setOnClickListener(onClick)
      //replace with file icon later

      tl.addView(dr)
      tr.addView(tl)
    } else {
      tr = new FileTableRow(this, f)
      tr.setOnClickListener((v: View) => {
        onFileClick(tr.asInstanceOf[FileTableRow])
      })
    }
    val indent = new TextView(this)
    indent.setText("  ")
    tr.addView(indent, 0)
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

  //If file happens to be a directory, then add directory icon
  if (file.isDirectory()) {
    val icon = new TextView(context)
    icon.setText("<DIR>")
    this.addView(icon)
  }

  //Add file name
  var view1 = new TextView(context)
  view1.setText(name)
  this.addView(view1)

  def getFile(): java.io.File = file
}