package one.lop.mrsu.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import one.lop.mrsu.R

class AnimatedBackgroundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val gradientColors = intArrayOf(
        ContextCompat.getColor(context, R.color.purple_700),
        ContextCompat.getColor(context, R.color.purple_500),
        ContextCompat.getColor(context, R.color.purple_200)
    )

    private var animationOffset = 0f
    private val stripeWidth = 100f //Ширина цветной полоски
    private val blackStripeWidth = 10f //Ширина разделительной чёрной полоски
    private val animationSpeed = 0.6f //Скорость движения полосок

    init {
        postInvalidateOnAnimation()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas?.let {
            drawDiagonalStripes(it)
        }

        animationOffset += animationSpeed
        if (animationOffset >= stripeWidth + blackStripeWidth) {
            animationOffset = 0f
        }

        postInvalidateOnAnimation()
    }

    private fun drawDiagonalStripes(canvas: Canvas) {
        val width = width.toFloat()
        val height = height.toFloat()

        val diagonalLength = Math.sqrt((width * width + height * height).toDouble()).toFloat()

        canvas.save()
        canvas.translate(-diagonalLength / 2, -diagonalLength / 2)
        canvas.rotate(-45f, diagonalLength / 2, diagonalLength / 2)

        var currentOffset = -diagonalLength + animationOffset

        while (currentOffset < diagonalLength * 2) {
            val gradientShader = LinearGradient(
                currentOffset, 0f,
                currentOffset + stripeWidth, 0f,
                gradientColors, null, Shader.TileMode.CLAMP
            )
            paint.shader = gradientShader
            canvas.drawRect(
                currentOffset, 0f,
                currentOffset + stripeWidth, diagonalLength * 2,
                paint
            )

            paint.shader = null
            paint.color = ContextCompat.getColor(context, R.color.black)
            canvas.drawRect(
                currentOffset + stripeWidth, 0f,
                currentOffset + stripeWidth + blackStripeWidth, diagonalLength * 2,
                paint
            )

            currentOffset += stripeWidth + blackStripeWidth
        }

        canvas.restore()
    }
}
