package com.joyousruction.beercalc

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Button
import android.content.Intent
import R._

class StartScreen extends Activity {

  lazy val loadButton = findViewById(R.id.loadButton).asInstanceOf[Button]
  lazy val exportButton = findViewById(R.id.exportButton).asInstanceOf[Button]
  lazy val importButton = findViewById(R.id.importButton).asInstanceOf[Button]
  lazy val startButton = findViewById(R.id.startButton).asInstanceOf[Button]

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.start_screen)

    startButton.setOnClickListener((v: View) => {
      startActivity(new Intent(StartScreen.this, classOf[StartNewRecipe]))
    })

  }

  implicit def func2OnClickListener(func: (View) => Unit): View.OnClickListener = {
    return new View.OnClickListener() {
      override def onClick(v: View) = func(v)
    }
  }
}

