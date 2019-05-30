package jarvis.com.library.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
import android.support.annotation.IntDef
import android.util.AttributeSet
import android.view.*
import android.view.animation.DecelerateInterpolator
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

    private var mLastFocusX: Float = 0f
    private var mLastFocusY: Float = 0f
    private var mDownFocusX: Float = 0f
    private var mDownFocusY: Float = 0f

    private var mScaleMax = 4f
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
        canvas?.concat(mMatrix)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        originRect.set(0, 0, width, height)
    }

    private fun getActionAndPointerIndex(event: MotionEvent): IntArray {
        var action = event.action
        var ptrIndex = 0
        if (event.pointerCount > 1) {
            val ptrId = (action and MotionEvent.ACTION_POINTER_INDEX_MASK).ushr(MotionEvent.ACTION_POINTER_INDEX_SHIFT)
            action = action and MotionEvent.ACTION_MASK
            if (action in 5..6) {
                action -= 5
            }
            ptrIndex = event.findPointerIndex(ptrId)
        }
        return intArrayOf(action, ptrIndex)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return true
    }

    private fun getDispatchEvent(ev: MotionEvent, ac: Int): MotionEvent = run {
        val centerRectF = getMatrixRectF()
        val realX = (ev.getX() + Math.abs(centerRectF.left)) / getScale()
        val realY = (ev.getY() + Math.abs(centerRectF.top)) / getScale()
        MotionEvent.obtain(ev).apply {
            action = ac
            setLocation(realX, realY)
        }
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

        val canMove = isMoveAction(focusX - mDownFocusX, focusY - mDownFocusY).apply {
            for (index in 0 until childCount) {
                getDispatchEvent(ev, if (this) { MotionEvent.ACTION_CANCEL } else { ev.action }).apply {
                    val child = getChildAt(index)
                    val param = child.layoutParams as FrameLayout.LayoutParams
                    if (x >= param.leftMargin.toFloat() &&
                            y >= param.topMargin.toFloat() &&
                            x <= param.leftMargin.toFloat() + child.width &&
                            y <= param.topMargin.toFloat() + child.height) {
                        offsetLocation(param.leftMargin.toFloat(), param.topMargin.toFloat())
                        child.dispatchTouchEvent(this)
                    }
                    recycle()
                }

            }
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
                if (canMove) {
                    onScrollBy(deltaX, deltaY)
                }
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
                        onFling(xVelocity, yVelocity)
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
        invalidate()
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
            invalidate()
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

    private fun getMatrixRectF(): RectF {
        val rect = RectF()
        rect.set(0f, 0f, width.toFloat(), height.toFloat())
        mMatrix.mapRect(rect)
        return rect
    }

    private fun onFling(velocityX: Float, velocityY: Float) {
        val scaledDistanceX = velocityX / viewConfiguration.scaledMaximumFlingVelocity * (width * getScale())
        val scaledDistanceY = velocityY / viewConfiguration.scaledMaximumFlingVelocity * (height * getScale())
        val total = Math.sqrt(Math.pow(scaledDistanceX.toDouble(), 2.0) + Math.pow(scaledDistanceY.toDouble(), 2.0))
        scrollFling(scaledDistanceX, scaledDistanceY, Math.min(Math.max(400.0, total / 3), 800.0).toLong())
    }

    private fun scrollFling(distanceX: Float, distanceY: Float, durationMs: Long) {
        ValueAnimator.ofFloat(0f, 1.0f).apply {
            duration = durationMs
            interpolator = DecelerateInterpolator()
            var oldValueX = 0f
            var oldValueY = 0f
            addUpdateListener {
                animation -> run {
                    val value = animation.animatedValue as Float
                    onScrollBy(distanceX * value - oldValueX, distanceY * value - oldValueY)
                    oldValueX = distanceX * value
                    oldValueY = distanceY * value
                }
            }
            start()
        }
    }
}