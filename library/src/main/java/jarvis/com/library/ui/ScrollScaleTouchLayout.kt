package jarvis.com.library.ui

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
import android.support.annotation.IntDef
import android.util.AttributeSet
import android.view.*
import android.widget.FrameLayout
import kotlin.annotation.Retention

/**
 * @author yyf @ Zhihu Inc.
 * @since 05-27-2019
 */
open class ScrollScaleTouchLayout: FrameLayout {

    @IntDef(Direction.HORIZONTAL, Direction.VERTICAL)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Direction {
        companion object {
            const val HORIZONTAL = 0x001
            const val VERTICAL = -0x001
        }
    }

    var mDirection = Direction.HORIZONTAL

    private val onScaleGestureListener by lazy { ScaleGestureListener() }

    private val scaleGestureDetector by lazy { ScaleGestureDetector(context, onScaleGestureListener) }

    private var velocityTracker: VelocityTracker? = VelocityTracker.obtain()

    private val viewConfiguration by lazy { ViewConfiguration.get(context) }

    protected lateinit var originRect: Rect

    private var mLastFocusX: Float = 0.toFloat()
    private var mLastFocusY: Float = 0.toFloat()
    private var mDownFocusX: Float = 0.toFloat()
    private var mDownFocusY: Float = 0.toFloat()

    private var mScaleMax = 2f
    private var mScaleMin = 1f

    private var mMatrix = Matrix()

    private var isCheckLeftAndRight: Boolean = false
    private var isCheckTopAndBottom: Boolean = false

    constructor(c:Context): this(c,null)

    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs,0)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr,0)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    init {
        preOperator()
    }

    private fun preOperator() {
        setWillNotDraw(false)
        originRect = Rect(0, 0, width, height)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        originRect.set(0, 0, width, height)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        visibility = INVISIBLE
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
        canvas?.concat(mMatrix)
        setLayerType(View.LAYER_TYPE_NONE, null)
        visibility = VISIBLE


    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        originRect.set(0, 0, width, height)

    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        velocityTracker?.addMovement(ev)

        val pointerUp = ev.action and MotionEvent.ACTION_MASK == MotionEvent.ACTION_POINTER_UP
        val skipIndex = if (pointerUp) ev.actionIndex else -1

        var sumX = 0F
        var sumY = 0F

        var focusX: Float
        var focusY: Float

        loop@ for (i in 0 until ev.pointerCount) {
            if (skipIndex == i) continue@loop
            sumX += ev.getX(i)
            sumY += ev.getY(i)
        }
        (if (pointerUp) ev.pointerCount - 1 else ev.pointerCount).apply {
            focusX = sumX / this
            focusY = sumY / this
        }

        if (ev.pointerCount > 1) {
            scaleGestureDetector.onTouchEvent(ev)
            if (scaleGestureDetector.isInProgress) {
                return true
            }
        }

        when (ev.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_POINTER_DOWN -> {
                mLastFocusX = focusX
                mDownFocusX = mLastFocusX
                mLastFocusY = focusY
                mDownFocusY = mLastFocusY
                return true
            }

            MotionEvent.ACTION_POINTER_UP -> {
                if (velocityTracker == null) {
                    return false
                }

                mLastFocusX = focusX
                mDownFocusX = mLastFocusX
                mLastFocusY = focusY
                mDownFocusY = mLastFocusY

                velocityTracker?.run {
                    computeCurrentVelocity(1000, viewConfiguration.scaledMaximumFlingVelocity.toFloat())
                    val upIndex = ev.actionIndex
                    val id1 = ev.getPointerId(upIndex)
                    val xVelocity1 = getXVelocity(id1)
                    val yVelocity1 = getYVelocity(id1)

                    loop@ for (i in 0 until ev.pointerCount) {
                        if (skipIndex == i) continue@loop
                        val id2 = ev.getPointerId(i)
                        val xVelocity = xVelocity1 * getXVelocity(id2)
                        val yVelocity = yVelocity1 * getYVelocity(id2)
                        val dot = xVelocity + yVelocity
                        if (dot < 0) {
                            clear()
                            break@loop
                        }
                    }
                }
                return true
            }

            MotionEvent.ACTION_DOWN -> {
                mLastFocusX = focusX
                mDownFocusX = mLastFocusX
                mLastFocusY = focusY
                mDownFocusY = mLastFocusY

                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val deltaX = focusX - mLastFocusX
                val deltaY = focusY - mLastFocusY
//                if (isMoveAction(deltaX, deltaY)) {
                    onScrollBy(deltaX, deltaY)
//                }
                mLastFocusX = focusX
                mLastFocusY = focusY
                return true
            }

            MotionEvent.ACTION_UP -> {
                if (velocityTracker == null) {
                    return false
                }
                val pointerId = ev.getPointerId(0)

                velocityTracker?.run {
                    computeCurrentVelocity(1000, viewConfiguration.scaledMaximumFlingVelocity.toFloat())
                    val xVelocity = getXVelocity(pointerId)
                    val yVelocity = getYVelocity(pointerId)

                    if (Math.abs(xVelocity) > viewConfiguration.scaledMinimumFlingVelocity
                            || Math.abs(yVelocity) > viewConfiguration.scaledMinimumFlingVelocity) {
                        invalidate()
                    }
                }
                return true
            }

            MotionEvent.ACTION_CANCEL -> {
                if (velocityTracker != null) {
                    velocityTracker?.recycle()
                    velocityTracker = null
                }
                return true
            }
        }
        return super.onTouchEvent(ev)
    }

    private fun isMoveAction(dx: Float, dy: Float): Boolean = run {
        Math.sqrt((dx * dx + dy * dy).toDouble()) > viewConfiguration.scaledTouchSlop
    }

    private fun onScrollBy(dx: Float, dy: Float) {
        var x = dx
        var y = dy
        val rectF = getMatrixRectF()
        isCheckTopAndBottom = true
        isCheckLeftAndRight = isCheckTopAndBottom
        if (rectF.width() <= width) {
            isCheckLeftAndRight = false
            x = 0f
        }
        if (rectF.height() <= height) {
            isCheckTopAndBottom = false
            y = 0f
        }
        mMatrix.postTranslate(x, y)
        checkBorderWhenTranslate()
    }

    private fun checkBorderWhenTranslate() {
        val rectF = getMatrixRectF()
        var deltaX = 0f
        var deltaY = 0f
        val width = width
        val height = height
        if (rectF.top > 0 && isCheckTopAndBottom) {
            deltaY = -rectF.top
        }
        if (rectF.bottom < height && isCheckTopAndBottom) {
            deltaY = height - rectF.bottom
        }
        if (rectF.left > 0 && isCheckLeftAndRight) {
            deltaX = -rectF.left
        }
        if (rectF.right < width && isCheckLeftAndRight) {
            deltaX = width - rectF.right
        }
        mMatrix.postTranslate(deltaX, deltaY)
    }

    private fun onScaleChange(detector: ScaleGestureDetector) {
        var scaleFactor: Float = detector.scaleFactor
        val scale = getScale()
        if (scale < mScaleMax && scaleFactor > 1.0f || scale > mScaleMin && scaleFactor < 1.0f) {
            if (scaleFactor * scale < mScaleMin) {
                scaleFactor = mScaleMin / scale
            }
            if (scale * scaleFactor > mScaleMax) {
                scaleFactor = mScaleMax / scale
            }
            mMatrix.postScale(scaleFactor, scaleFactor, detector.focusX, detector.focusY)
            checkBorderAndCenterWhenScale()
        }
    }

    private fun checkBorderAndCenterWhenScale() {
        val rectF = getMatrixRectF()
        var deltaX = 0f
        var deltaY = 0f
        val width = width
        val height = height
        if (rectF.width() >= width) {
            if (rectF.left > 0)
                deltaX = -rectF.left
            if (rectF.right < width)
                deltaX = width - rectF.right
        }
        if (rectF.height() >= height) {
            if (rectF.top > 0)
                deltaY = -rectF.top
            if (rectF.bottom < height)
                deltaY = height - rectF.bottom
        }
        if (rectF.width() < width) {
            deltaX = width / 2f - rectF.right + rectF.width() / 2
        }
        if (rectF.height() < height) {
            deltaY = height / 2f - rectF.bottom + rectF.height() / 2
        }
        mMatrix.postTranslate(deltaX, deltaY)
    }

    private fun getMatrixRectF(): RectF {
        val rect = RectF()
        rect.set(0f, 0f, width.toFloat(), height.toFloat())
        mMatrix.mapRect(rect)
        return rect
    }

    private fun getScale(): Float {
        val values = FloatArray(9)
        mMatrix.getValues(values)
        return values[Matrix.MSCALE_X]
    }

    inner class ScaleGestureListener: ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            onScaleChange(detector)
            return true
        }
    }
}