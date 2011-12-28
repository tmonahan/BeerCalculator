package com.joyousruction.beercalc

import android.util.AttributeSet
import android.widget.ProgressBar
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Handler
import android.view.View
import android.view.View.MeasureSpec

import scala.Math

final class TargetedProgressBar(context: Context, attrs: AttributeSet, defStyle: Int) extends ProgressBar(context, attrs, defStyle) {

  val handler: Handler = new Handler()
  
  var originRect: RectF = new RectF(0.0f, 0.0f, 3.0f, 10.0f)
  var minTargetProgress: Int = 40
  var maxTargetProgress: Int = 90
  
  var minRect: RectF = progressRect(minTargetProgress)
  var maxRect: RectF = progressRect(maxTargetProgress)
  
  var rectPaint: Paint = new Paint
  rectPaint.setARGB(255,255,0,0)
  
  def this(context: Context, attrs: AttributeSet) { this(context, attrs, null.asInstanceOf[Int]) }

  def this(context: Context) { this(context, null) }

  def this() { this(null) }

  override def onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    canvas.drawRect(minRect, rectPaint)
    canvas.drawRect(maxRect, rectPaint)
  }
  
  def progressRect(progress: Int): RectF = {
    var newRect: RectF = new RectF(originRect)
    val percentProgress = progress.toFloat / getMax().toFloat

    val calculatedOffset = (percentProgress*getMeasuredWidth()) - (originRect.width()/2)
    val offset: Float = Math.min(Math.max(0.0, calculatedOffset), getMeasuredWidth()-originRect.width()).toFloat
    newRect.offset(offset, 0.0f)
    //Return newRect
    newRect
  }
  
  def setMinTargetProgress(newMinTargetProgress: Int) {
    minTargetProgress = newMinTargetProgress
    minRect = progressRect(minTargetProgress)
  }
    
  def setMaxTargetProgress(newMaxTargetProgress: Int) {
    maxTargetProgress = newMaxTargetProgress
    maxRect = progressRect(maxTargetProgress)
  }
  
  override def onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    val width: Int = chooseSize(widthMeasureSpec,getSuggestedMinimumWidth())
    val height: Int = chooseSize(heightMeasureSpec, getSuggestedMinimumHeight())

    setMeasuredDimension(width, height)    
    
    originRect.set(0.0f, 0.0f, originRect.width(), height)
    minRect = progressRect(minTargetProgress)
    maxRect = progressRect(maxTargetProgress)
  }

  override def getSuggestedMinimumWidth(): Int = {
    super.getSuggestedMinimumWidth()
  }
  
  //TODO - add extra size to allow for target bars extending beyond progress bar
  override def getSuggestedMinimumHeight(): Int = {
    super.getSuggestedMinimumHeight()
  }
  
  def chooseSize(measureSpec: Int, suggestedSize:Int):Int = MeasureSpec.getMode(measureSpec) match {
    case MeasureSpec.EXACTLY => MeasureSpec.getSize(measureSpec)
    case MeasureSpec.AT_MOST => Math.min(MeasureSpec.getSize(measureSpec),suggestedSize)
    case MeasureSpec.UNSPECIFIED => suggestedSize
  }

}