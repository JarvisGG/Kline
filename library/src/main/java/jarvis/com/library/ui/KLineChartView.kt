package jarvis.com.library.ui

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import jarvis.com.library.R

/**
 * @author yyf @ Zhihu Inc.
 * @since 05-28-2019
 */
class KLineCharView: ScrollScaleTouchLayout {

    private var screenWidth = Resources.getSystem().displayMetrics.widthPixels
    private var screenHeight = Resources.getSystem().displayMetrics.heightPixels

    val Number.px: Int get() = (toInt() * Resources.getSystem().displayMetrics.density.toInt())
    val Number.dp: Int get() = (toInt() / Resources.getSystem().displayMetrics.density.toInt())

    private var linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 1.toFloat()
        color = resources.getColor(R.color.chart_grid_line)
    }

    private var bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = resources.getColor(R.color.chart_background)
    }

    constructor(c: Context): this(c,null)

    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs,0)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr,0)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(screenWidth, screenWidth / 2)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        drawBackground(canvas)
    }

    private fun drawBackground(canvas: Canvas?) {
        canvas?.drawColor(Color.WHITE)
        (originRect.height().toFloat() / 4).apply {
            for (i in 0..4) {
                canvas?.drawLine(0f, this * i + originRect.top, originRect.width().toFloat(), this * i + originRect.top, linePaint)
            }
        }
    }

    override fun getDirection(): Int {
        return Direction.HORIZONTAL
    }

}