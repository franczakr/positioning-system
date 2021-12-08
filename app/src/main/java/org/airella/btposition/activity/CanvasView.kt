package org.airella.btposition.activity

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.ColorUtils
import kotlin.math.*


class CanvasView @JvmOverloads constructor(context: Context,
                                           attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : View(context, attrs, defStyleAttr) {

    data class Position(val x: Float, val y: Float)
    data class Point(val x: Float, val y: Float, val radius: Float)
    data class PointData(val point1: Point, val point2: Point, val point3: Point)
    data class Intersections(val result1: Position, val result2: Position)
    data class LineOrPoint(val a: Float, val b: Float, val point: Position? = null)
    data class RawSingleSensorData(val x: Float, val y: Float, val signalStrength: Float, val color: Int = Color.GRAY)
    data class RawSensorData(val sensor1: RawSingleSensorData, val sensor2: RawSingleSensorData, val sensor3: RawSingleSensorData)

    private val signalPointsSize = 15f
    private val intersectionPointsSize = 15f
    private val lineIntersectionsPointSize = 10f

    private val color1 = Color.parseColor("#0072BD")
    private val color2 = Color.parseColor("#D95319")
    private val color3 = Color.parseColor("#EDB120")

    private val color12 = ColorUtils.blendARGB(color1, color2, 0.5f)
    private val color23 = ColorUtils.blendARGB(color2, color3, 0.5f)
    private val color13 = ColorUtils.blendARGB(color1, color3, 0.5f)

    private val resultColor = Color.parseColor("#7E2F8E")

    // set to false to hide debug lines
    var visualDebug = true

    // real distance between point 1 and point 2
    var realDistance = 100f

    // -> x and y should be inside (0, 1) range
    // -> radius is part of distance between point 1 and point 2
    // do not position two points on the same x / y values, because it generates infinite numbers,
    // and the solution doesn't work :|
    // -> use color parameter of RawSingleSensorData for color coding different sensors / debugging
    var rawSensorData = RawSensorData(
        RawSingleSensorData(
            0.1f,
            0.1f,
            80f,
        ),
        RawSingleSensorData(
            0.9f,
            0.3f,
            70f,
        ),
        RawSingleSensorData(
            0.35f,
            0.9f,
            80f,
        ),
    )

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (canvas == null)
            return

        val width = canvas.width.toFloat()
        val height = canvas.height.toFloat()

        val screenDistance = distance2d(
            rawSensorData.sensor1.x * width,
            rawSensorData.sensor2.x * width,
            rawSensorData.sensor1.y * height,
            rawSensorData.sensor2.y * height,
        )
        val pointData = PointData(
            Point(
                rawSensorData.sensor1.x * width,
                rawSensorData.sensor1.y * width,
                rawSensorData.sensor1.signalStrength / realDistance * screenDistance,
            ),
            Point(
                rawSensorData.sensor2.x * width,
                rawSensorData.sensor2.y * width,
                rawSensorData.sensor2.signalStrength / realDistance * screenDistance,
            ),
            Point(
                rawSensorData.sensor3.x * width,
                rawSensorData.sensor3.y * width,
                rawSensorData.sensor3.signalStrength / realDistance * screenDistance,
            ),
        )

        canvas.translate(0f, (height - width) / 2)

        drawPointAndRadius(canvas, pointData.point1, rawSensorData.sensor1.color)
        drawPointAndRadius(canvas, pointData.point2, rawSensorData.sensor2.color)
        drawPointAndRadius(canvas, pointData.point3, rawSensorData.sensor3.color)

        val r12 = getAndDrawIntersections(canvas, pointData.point1, pointData.point2, color1)
        val r13 = getAndDrawIntersections(canvas, pointData.point1, pointData.point3, color2)
        val r23 = getAndDrawIntersections(canvas, pointData.point2, pointData.point3, color3)

        val l12 = getAndDrawApproxLineOrPoint(canvas, r12.result1, r12.result2, pointData.point1, pointData.point2, color1)
        val l13 = getAndDrawApproxLineOrPoint(canvas, r13.result1, r13.result2, pointData.point1, pointData.point3, color2)
        val l23 = getAndDrawApproxLineOrPoint(canvas, r23.result1, r23.result2, pointData.point2, pointData.point3, color3)


        val i1 = getAndDrawLineIntersections(canvas, l12, l13, color12)
        val i2 = getAndDrawLineIntersections(canvas, l12, l23, color13)
        val i3 = getAndDrawLineIntersections(canvas, l13, l23, color23)

        val mid = Position(
            (i1.x + i2.x + i3.x) / 3,
            (i1.y + i2.y + i3.y) / 3,
        )
        val d1 = getDistance(mid, r12, l12)
        val d2 = getDistance(mid, r13, l13)
        val d3 = getDistance(mid, r23, l23)
        val d = max(d1, max(d2, d3))
        val resultPoint = Point(mid.x, mid.y, d)
        drawGradientCircle(canvas, resultColor, resultPoint)
        drawPointAndRadius(canvas, resultPoint, resultColor)
    }

    fun distance2d(x1: Float, x2: Float, y1: Float, y2: Float): Float {
        return sqrt((x1 - x2).pow(2) + (y1 - y2).pow(2))
    }

    fun getDistance(midPoint: Position, doubleResult: Intersections, singleResult: LineOrPoint): Float {
        val distance: (Float, Float) -> Float = { x, y -> distance2d(midPoint.x, x, midPoint.y, y) }
        if (singleResult.point != null) {
            return distance(singleResult.point.x, singleResult.point.y)
        } else {
            val d1 = distance(doubleResult.result1.x, doubleResult.result1.y)
            val d2 = distance(doubleResult.result2.x, doubleResult.result2.y)
            return min(d1, d2)
        }
    }

    fun getAndDrawLineIntersections(canvas: Canvas, line1: LineOrPoint, line2: LineOrPoint, color: Int): Position {
        val x = (line2.b - line1.b) / (line1.a - line2.a)
        val y = line1.a * x + line1.b
        if (visualDebug)
            drawPoint(canvas, color, Point(x, y, lineIntersectionsPointSize))
        return Position(x, y)
    }

    fun getAndDrawApproxLineOrPoint(canvas: Canvas, point1: Position, point2: Position, circle1: Point, circle2: Point, color: Int): LineOrPoint {
        val width = canvas.width.toFloat()
        var result: LineOrPoint

        if (!(point1.x.isNaN() || point1.y.isNaN() || point2.x.isNaN() || point2.y.isNaN())) {
            val a = (point1.y - point2.y) / (point1.x - point2.x)
            val b = point1.y - (a * point1.x)
            result = LineOrPoint(a, b)
        } else {
            var o0: Position
            val R = sqrt((circle1.x - circle2.x).pow(2) + (circle1.y - circle2.y).pow(2))
            if (circle1.radius < R && circle2.radius < R) {
                val d = (R - circle1.radius - circle2.radius) / 2
                val shiftFactor = circle1.radius + d
                val xShift = (Math.abs(circle1.x - circle2.x) * shiftFactor) / R
                val yShift = (Math.abs(circle1.y - circle2.y) * shiftFactor) / R
                o0 = Position(
                    circle1.x + (if (circle1.x < circle2.x) xShift else -xShift),
                    circle1.y + (if (circle1.y < circle2.y) yShift else -yShift),
                )
            } else {
                val circleStart = if (circle1.radius < circle2.radius) circle2 else circle1
                val circleEnd = if (circle1.radius < circle2.radius) circle1 else circle2
                val d = (Math.abs(circle1.radius - circle2.radius) - R) / 2
                val shiftFactor = circleEnd.radius + d
                val xShift = (Math.abs(circle1.x - circle2.x) * shiftFactor) / R
                val yShift = (Math.abs(circle1.y - circle2.y) * shiftFactor) / R
                o0 = Position(
                    circleEnd.x + (if (circleEnd.x > circleStart.x) xShift else -xShift),
                    circleEnd.y + (if (circleEnd.y > circleStart.y) yShift else -yShift),
                )
            }

            if (visualDebug)
                drawPoint(canvas, color, Point(o0.x, o0.y, intersectionPointsSize))

            val a = (circle1.y - circle2.y) / (circle1.x - circle2.x)
            val b = circle1.y - (a * circle1.x)

            val c = o0.y + o0.x / a

            result = LineOrPoint(-1/a, c, o0)
        }

        val o1 = Position(
            -result.b / result.a,
            0f,
        )
        val o2 = Position(
            (width - result.b) / result.a,
            width,
        )

        if (visualDebug) {
            val paint = Paint()
            paint.color = color
            paint.strokeWidth = 3f
            paint.style = Paint.Style.STROKE
            canvas.drawLine(o1.x, o1.y, o2.x, o2.y, paint)
        }

        return result
    }

    fun getAndDrawIntersections(canvas: Canvas, point1: Point, point2: Point, color: Int): Intersections {
        val distance12 = sqrt((point1.x - point2.x).pow(2) + (point1.y - point2.y).pow(2))
        val d1to2 = (distance12.pow(2) + point1.radius.pow(2) - point2.radius.pow(2)) / (2 * distance12)
        val h12 = sqrt(point1.radius.pow(2) - d1to2.pow(2))

        val angle1to2 = asin(Math.abs(point1.x - point2.x) / distance12)

        val xForwardDistance = (if (point1.x < point2.x) 1 else -1) * d1to2 * sin(angle1to2)
        val yForwardDistance = (if (point1.y < point2.y) 1 else -1) * d1to2 * cos(angle1to2)
        val xHDistance = (if (point1.x < point2.x) 1 else -1) * h12 * cos(angle1to2)
        val yHDistance = (if (point1.y < point2.y) 1 else -1) * h12 * sin(angle1to2)

        val c1 = Position(
            point1.x + xForwardDistance - xHDistance,
            point1.y + yForwardDistance + yHDistance,
        )
        val c2 = Position(
            point1.x + xForwardDistance + xHDistance,
            point1.y + yForwardDistance - yHDistance,
        )

        if (visualDebug) {
            drawPoint(canvas, color, Point(c1.x, c1.y, intersectionPointsSize))
            drawPoint(canvas, color, Point(c2.x, c2.y, intersectionPointsSize))
        }

        return Intersections(c1, c2)
    }

    fun drawPointAndRadius(canvas: Canvas, point: Point, color: Int = Color.WHITE) {
        drawPoint(canvas, color, Point(point.x, point.y, signalPointsSize))
        drawCircle(canvas, color, point)
    }

    fun drawPoint(canvas: Canvas, color: Int, point: Point) {
        val paint = Paint()
        paint.color = color
        paint.style = Paint.Style.FILL
        canvas.drawCircle(point.x, point.y, point.radius, paint)
    }

    fun drawCircle(canvas: Canvas, color: Int, point: Point) {
        val paint = Paint()
        paint.color = color
        paint.strokeWidth = 3f
        paint.style = Paint.Style.STROKE
        canvas.drawCircle(point.x, point.y, point.radius, paint)
    }

    fun drawGradientCircle(canvas: Canvas, color: Int, point: Point) {
        val paint = Paint()
        val gradient = RadialGradient(
            point.x,
            point.y,
            point.radius,
            intArrayOf(ColorUtils.setAlphaComponent(color, 200), Color.TRANSPARENT),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.isDither = true
        paint.shader = gradient
        canvas.drawCircle(point.x, point.y, point.radius, paint)
    }
}