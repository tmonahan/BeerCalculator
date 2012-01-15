package com.joyousruction.beercalc

import com.joyousruction.scalacompatibility.CompatibleAsyncTask
import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Button
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import R._

class Main extends Activity {

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.main)

    new InitDatabase(getResources(), this).execute()
  }

  class InitDatabase(res: Resources, context: Context) extends CompatibleAsyncTask {

    //Force the database to be loaded
    override def backgroundTask(): java.lang.Long = {
      Database.init(res, context)
      0L
    }

    override protected def onPostExecute(result: java.lang.Long) = {
      startActivity(new Intent(Main.this, classOf[StartScreen]))
    }
  }
}
