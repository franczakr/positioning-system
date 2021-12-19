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
    data class Intersections(val result1: Position? = null, val result2: Position? = null, val outsideResult: Position? = null)
    data class Line(val a: Float, val b: Float, val constX: Float? = null)

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
    private var visualDebug = true
    fun setVisualDebug(value: Boolean) {
        visualDebug = value
        invalidate()
    }

    // canvas margin in meters - use purely for visuals
    private var margin = 0.1f
    fun setMargin(value: Float) {
        margin = value
        invalidate()
    }

    // use for the purpose of distinguishing different sensors when debugging
    private var inputColor1 = Color.GRAY
    fun setColor1(value: Int) {
        inputColor1 = value
        invalidate()
    }
    private var inputColor2 = Color.GRAY
    fun setColor2(value: Int) {
        inputColor2 = value
        invalidate()
    }
    private var inputColor3 = Color.GRAY
    fun setColor3(value: Int) {
        inputColor3 = value
        invalidate()
    }

    // -> input x and input y - position of sensor 1 and sensor 2 in meters
    // -> input signal - strength of signal in meters

    private var inputX1 = 0f
    private var inputY1 = 0f
    private var inputSignal1 = 0.8f

    private var inputX2 = 1f
    private var inputY2 = 0f
    private var inputSignal2 = 0.7f

    private var inputX3 = 0.5f
    private var inputY3 = 1f
    private var inputSignal3 = 0.8f

    fun setPositions(pos1: Position, pos2: Position, pos3: Position) {
        inputX1 = pos1.x
        inputY1 = pos1.y
        inputX2 = pos2.x
        inputY2 = pos2.y
        inputX3 = pos3.x
        inputY3 = pos3.y
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (canvas == null)
            return

        val pointData = translateCoords(canvas)

        drawPointAndRadius(canvas, pointData.point1, inputColor1)
        drawPointAndRadius(canvas, pointData.point2, inputColor2)
        drawPointAndRadius(canvas, pointData.point3, inputColor3)

        val r12 = getAndDrawIntersections(canvas, pointData.point1, pointData.point2, color1)
        val r13 = getAndDrawIntersections(canvas, pointData.point1, pointData.point3, color2)
        val r23 = getAndDrawIntersections(canvas, pointData.point2, pointData.point3, color3)

        val l12 = getAndDrawApproxLine(canvas, r12, pointData.point1, pointData.point2, color1)
        val l13 = getAndDrawApproxLine(canvas, r13, pointData.point1, pointData.point3, color2)
        val l23 = getAndDrawApproxLine(canvas, r23, pointData.point2, pointData.point3, color3)


        val i1 = getAndDrawLineIntersections(canvas, l12, l13, color12)
        val i2 = getAndDrawLineIntersections(canvas, l12, l23, color13)
        val i3 = getAndDrawLineIntersections(canvas, l13, l23, color23)

        val mid = Position(
            (i1.x + i2.x + i3.x) / 3,
            (i1.y + i2.y + i3.y) / 3,
        )
        val d1 = getDistance(mid, r12)
        val d2 = getDistance(mid, r13)
        val d3 = getDistance(mid, r23)
        val d = max(d1, max(d2, d3))
        val resultPoint = Point(mid.x, mid.y, d)

        drawGradientCircle(canvas, resultColor, resultPoint)
        drawPoint(canvas, resultColor, Point(resultPoint.x, resultPoint.y, signalPointsSize))
    }

    fun translateCoords(canvas: Canvas): PointData {
        val canvasWidth = canvas.width.toFloat()
        val canvasHeight = canvas.height.toFloat()
        val minX = min(inputX1, min(inputX2, inputX3))
        val inputWidth = max(inputX1, max(inputX2, inputX3)) - minX + 2 * margin
        val minY = min(inputY1, min(inputY2, inputY3))

        canvas.translate(0f, (canvasHeight - canvasWidth) / 2)

        return PointData(
            Point(
                (inputX1 - minX + margin) / inputWidth * canvasWidth,
                (inputY1 - minY + margin) / inputWidth * canvasWidth,
                inputSignal1 / inputWidth * canvasWidth,
            ),
            Point(
                (inputX2 - minX + margin) / inputWidth * canvasWidth,
                (inputY2 - minY + margin) / inputWidth * canvasWidth,
                inputSignal2 / inputWidth * canvasWidth,
            ),
            Point(
                (inputX3 - minX + margin) / inputWidth * canvasWidth,
                (inputY3 - minY + margin) / inputWidth * canvasWidth,
                inputSignal3 / inputWidth * canvasWidth,
            ),
        )
    }

    fun distance2d(x1: Float, x2: Float, y1: Float, y2: Float): Float {
        return sqrt((x1 - x2).pow(2) + (y1 - y2).pow(2))
    }

    fun getDistance(midPoint: Position, intersections: Intersections): Float {
        val distance: (Float, Float) -> Float = { x, y -> distance2d(midPoint.x, x, midPoint.y, y) }
        if (intersections.outsideResult != null) {
            return distance(intersections.outsideResult.x, intersections.outsideResult.y)
        } else if (intersections.result1 != null && intersections.result2 != null) {
            val d1 = distance(intersections.result1.x, intersections.result1.y)
            val d2 = distance(intersections.result2.x, intersections.result2.y)
            return min(d1, d2)
        } else return Float.NaN
    }

    fun getAndDrawLineIntersections(canvas: Canvas, line1: Line, line2: Line, color: Int): Position {
        val x: Float
        val y: Float

        if (line1.constX == null && line2.constX == null) {
            x = (line2.b - line1.b) / (line1.a - line2.a)
            y = line1.a * x + line1.b
        } else if (line1.constX != null) {
            x = line1.constX
            y = line2.a * x + line2.b
        } else if (line2.constX != null) {
            x = line2.constX
            y = line1.a * x + line1.b
        } else {
            x = Float.NaN
            y = Float.NaN
        }

        if (visualDebug)
            drawPoint(canvas, color, Point(x, y, lineIntersectionsPointSize))
        return Position(x, y)
    }

    fun getAndDrawApproxLine(canvas: Canvas, intersections: Intersections, circle1: Point, circle2: Point, color: Int): Line {
        val width = canvas.width.toFloat()
        val height = canvas.height.toFloat()
        var result: Line

        val point1 = intersections.result1
        val point2 = intersections.result2
        val outsidePoint = intersections.outsideResult

        val sameYCoord = circle1.y == circle2.y

        if (point1 != null && point2 != null) {
            if (sameYCoord) {
                result = Line(Float.NaN, Float.NaN, point1.x)
            } else {
                val a = (point1.y - point2.y) / (point1.x - point2.x)
                val b = point1.y - (a * point1.x)
                result = Line(a, b)
            }
        } else if (outsidePoint != null) {
            if (sameYCoord) {
                result = Line(Float.NaN, Float.NaN, outsidePoint.x)
            } else {
                val a = (circle1.y - circle2.y) / (circle1.x - circle2.x)
                val b = circle1.y - (a * circle1.x)
                val c = outsidePoint.y + outsidePoint.x / a
                result = Line(-1 / a, c)
            }
        } else {
            result = Line(Float.NaN, Float.NaN)
        }

        val o1: Position
        val o2: Position

        if (result.constX != null) {
            o1 = Position(result.constX!!, 0f)
            o2 = Position(result.constX!!, height)
        } else if (result.a != 0f) {
            o1 = Position(
                -result.b / result.a,
                0f,
            )
            o2 = Position(
                (width - result.b) / result.a,
                width,
            )
        } else {
            o1 = Position(0f, result.b)
            o2 = Position(width, result.b)
        }

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

        if (!(c1.x.isNaN() || c1.y.isNaN() || c2.x.isNaN() || c2.y.isNaN())) {
            if (visualDebug) {
                drawPoint(canvas, color, Point(c1.x, c1.y, intersectionPointsSize))
                drawPoint(canvas, color, Point(c2.x, c2.y, intersectionPointsSize))
            }
            return Intersections(c1, c2, null)
        } else {
            var o0: Position
            val R = sqrt((point1.x - point2.x).pow(2) + (point1.y - point2.y).pow(2))
            if (point1.radius < R && point2.radius < R) {
                val d = (R - point1.radius - point2.radius) / 2
                val shiftFactor = point1.radius + d
                val xShift = (Math.abs(point1.x - point2.x) * shiftFactor) / R
                val yShift = (Math.abs(point1.y - point2.y) * shiftFactor) / R
                o0 = Position(
                    point1.x + (if (point1.x < point2.x) xShift else -xShift),
                    point1.y + (if (point1.y < point2.y) yShift else -yShift),
                )
            } else {
                val circleStart = if (point1.radius < point2.radius) point2 else point1
                val circleEnd = if (point1.radius < point2.radius) point1 else point2
                val d = (Math.abs(point1.radius - point2.radius) - R) / 2
                val shiftFactor = circleEnd.radius + d
                val xShift = (Math.abs(point1.x - point2.x) * shiftFactor) / R
                val yShift = (Math.abs(point1.y - point2.y) * shiftFactor) / R
                o0 = Position(
                    circleEnd.x + (if (circleEnd.x > circleStart.x) xShift else -xShift),
                    circleEnd.y + (if (circleEnd.y > circleStart.y) yShift else -yShift),
                )
            }

            if (visualDebug)
                drawPoint(canvas, color, Point(o0.x, o0.y, intersectionPointsSize))

            return Intersections(null, null, o0)
        }
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