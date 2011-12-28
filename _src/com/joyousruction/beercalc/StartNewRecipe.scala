package com.joyousruction.beercalc

import xml.{Elem, Node, NodeSeq, XML}

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.content.Intent
import R._

class StartNewRecipe extends Activity {
  
  lazy val style_spinner = findViewById(R.id.spinnerStyles).asInstanceOf[Spinner]
  lazy val createButton = findViewById(R.id.createRecipeButton).asInstanceOf[Button]
  lazy val recipeName = findViewById(R.id.recipeName).asInstanceOf[EditText]
  
	override def onCreate(savedInstanceState: Bundle) {
	        super.onCreate(savedInstanceState)
	        setContentView(R.layout.startnewrecipe)
	        
//	        //Load the styles from the database, and populate the spinner
//	        val resources = getResources()
//	        val databaseStream = resources.getAssets().open("database.xml")
//	        
//	        //maybe move this later and pass it in... or maybe not.
//            var database = XML .load(databaseStream)
//            
//            var styles = database \\ "STYLE"
	        
	        var styles = Database.getStyles
            
            var styleNameArray: Array[SpinnerNode] = new Array(styles.length)
            styles.map { node: Node => 
              new SpinnerNode(node)
            }.copyToArray(styleNameArray)
            
            scala.util.Sorting.quickSort(styleNameArray)
            
            var spinnerStyleArray: ArrayAdapter[SpinnerNode] = new ArrayAdapter[SpinnerNode] (this, android.R.layout.simple_spinner_item, styleNameArray)
            
            style_spinner.setAdapter(spinnerStyleArray)
            
            //Set up action for button:
            createButton.setOnClickListener((v: View) => {
              if(recipeName.getText() != "") {
                val name: NodeSeq = <NAME>{recipeName.getText()}</NAME>
                val style = style_spinner.getSelectedItem().asInstanceOf[SpinnerNode].node
                Database.setCurrentRecipe(<RECIPE>{name ++ style}</RECIPE>)
	            startActivity(new Intent(StartNewRecipe.this, classOf[RecipeStats]))
              }
	        })
	}
    
  implicit def func2OnClickListener(func : (View) => Unit) : View.OnClickListener = {
	        return new View.OnClickListener() {
	                override def onClick(v: View) = func(v)
	        }
	}
	                
}

class SpinnerNode(val node: Node) extends Ordered[SpinnerNode] {
  
  def compare(that: SpinnerNode): Int = {
    val thisCat = (this.node \ "CATEGORY_NUMBER").text
    val thatCat = (that.node \ "CATEGORY_NUMBER").text
    
    var ret: Int = 0;
    
    try {
      ret = (this.node \ "CATEGORY_NUMBER").text.toInt compare (that.node \ "CATEGORY_NUMBER").text.toInt
    } catch {
      case e: Exception => ret = (this.node \ "CATEGORY_NUMBER").text compare (that.node \ "CATEGORY_NUMBER").text
    }
    
    if (ret == 0) {
      ret = (this.node \ "STYLE_LETTER").text compare (that.node \ "STYLE_LETTER").text
    }
    if (ret == 0) {
      ret = (this.node \ "NAME").text compare (that.node \ "NAME").text
    }
    
    ret
  }
  
  override def toString(): String = {
    ((node \ "CATEGORY_NUMBER").text + (node \ "STYLE_LETTER").text + " - " + (node \ "NAME").text).toString()
  }
}