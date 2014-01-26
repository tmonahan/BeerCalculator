package com.joyousruction.beercalc

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import R._

class BoilActivity extends FragmentActivity {

  // define the ViewPager stuff here
  val BOIL_SETTINGS = 0
  val BOIL_TIMER = 1
  val BOIL_HISTORY = 2
  val NUM_PAGES = 3
  var mAdapter: MyFragmentAdapter = null
  var mPager: ViewPager = null

  // views and buttons and things
  var settingsHistoryButton: Button = null
  var settingsTimerButton: Button = null
  var timerSettingsButton: Button = null
  var timerHistoryButton: Button = null
  var historyTimerButton: Button = null
  var historySettingsButton: Button = null

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.boilview)

    mAdapter = new MyFragmentAdapter(this.getSupportFragmentManager())
    mPager = findViewById(R.id.boilPager).asInstanceOf[ViewPager]
    mPager.setAdapter(mAdapter)
    mPager.setCurrentItem(BOIL_TIMER, false)
  }

  //fragment adapter controls changes between pages
  class MyFragmentAdapter(fm: FragmentManager) extends FragmentPagerAdapter(fm) {

    override def getCount(): Int = {
      NUM_PAGES
    }
    override def getItem(position: Int): Fragment = {
      position match {
        case BOIL_SETTINGS => BoilSettingsFragment
        case BOIL_TIMER => BoilTimerFragment
        case BOIL_HISTORY => BoilHistoryFragment
      }
    }
  }

  object BoilSettingsFragment extends Fragment {
    override def onCreate(savedInstanceState: Bundle) {
      super.onCreate(savedInstanceState)
      this.setRetainInstance(true)
    }

    override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
      val v: View = inflater.inflate(R.layout.boilsettings, container, false)

      // Make the buttons work
      settingsTimerButton = v.findViewById(R.id.settingsTimerButton).asInstanceOf[Button]
      settingsHistoryButton = v.findViewById(R.id.settingsHistoryButton).asInstanceOf[Button]

      settingsTimerButton.setOnClickListener((v: View) => {
        mPager.setCurrentItem(BOIL_TIMER, false)
      })
      
      settingsHistoryButton.setOnClickListener((v: View) => {
        mPager.setCurrentItem(BOIL_HISTORY, false)
      })

      v
    }
 
  }

  object BoilTimerFragment extends Fragment {
    override def onCreate(savedInstanceState: Bundle) {
      super.onCreate(savedInstanceState)
      this.setRetainInstance(true)
    }
    override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
      val v: View = inflater.inflate(R.layout.timer, container, false)

      // Make the buttons work
      timerSettingsButton = v.findViewById(R.id.timerSettingsButton).asInstanceOf[Button]
      timerHistoryButton = v.findViewById(R.id.timerHistoryButton).asInstanceOf[Button]

      timerSettingsButton.setOnClickListener((v: View) => {
        mPager.setCurrentItem(BOIL_SETTINGS, false)
      })

      timerHistoryButton.setOnClickListener((v: View) => {
        mPager.setCurrentItem(BOIL_HISTORY, false)
      })

      v
    }
 
  }

  object BoilHistoryFragment extends Fragment {
    override def onCreate(savedInstanceState: Bundle) {
      super.onCreate(savedInstanceState)
      this.setRetainInstance(true)
    }
    override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
      val v: View = inflater.inflate(R.layout.boilhistory, container, false)

      // Make the buttons work
      historyTimerButton = v.findViewById(R.id.historyTimerButton).asInstanceOf[Button]
      historySettingsButton = v.findViewById(R.id.historySettingsButton).asInstanceOf[Button]

      historyTimerButton.setOnClickListener((v: View) => {
        mPager.setCurrentItem(BOIL_TIMER, false)
      })

      historySettingsButton.setOnClickListener((v: View) => {
        mPager.setCurrentItem(BOIL_SETTINGS, false)
      })

      v
    }
 
  }

  // helper functions to let us do things in the Scala way
  implicit def func2OnClickListener(func: (View) => Unit): View.OnClickListener = {
    return new View.OnClickListener() {
      override def onClick(v: View) = func(v)
    }
  }
}
