package jarvis.com.library.ui

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
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

    private var linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 1.toFloat()
        color = resources.getColor(R.color.chart_white)
    }

    private var bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = resources.getColor(R.color.chart_background)
    }

    private var topPadding = resources.getDimension(R.dimen.chart_top_padding).toInt()
    private var bottomPadding = resources.getDimension(R.dimen.chart_bottom_padding).toInt()

    constructor(c: Context): this(c,null)

    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs,0)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr,0)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val heightSize = View.MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)

        setMeasuredDimension(screenWidth, screenWidth)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        drawBackground(canvas)
    }

    private fun drawBackground(canvas: Canvas?) {
        canvas?.drawColor(bgPaint.color)
        (originRect.height().toFloat() / 4).apply {
            for (i in 0..4) {
                canvas?.drawLine(0f, this * i + originRect.top, originRect.width().toFloat(), this * i + originRect.top, linePaint)
            }
        }

        (originRect.width().toFloat() / 4).apply {
            for (i in 0..4) {
                canvas?.drawLine(this * i, 0f, this * i, originRect.height().toFloat(), linePaint)
            }
        }
    }

}