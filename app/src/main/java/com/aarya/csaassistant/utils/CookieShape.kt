package com.aarya.csaassistant.utils

import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.toShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.graphics.shapes.RoundedPolygon
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class CookieShape(
    private val numSides: Int = 9,
    private val biteDepthFactor: Float = 0.3f // Adjust this for deeper/shallower bites
) : Shape {

    init {
        require(numSides >= 3) { "Number of sides must be at least 3." }
        require(biteDepthFactor >= 0f && biteDepthFactor < 1f) { "Bite depth factor must be between 0 and <1." }
    }

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path()
        val radius = min(size.width, size.height) / 2f
        val centerX = size.width / 2f
        val centerY = size.height / 2f

        // Angle between the points of the star/cookie
        val angleIncrement = (2 * PI / numSides).toFloat()

        // Outer points (the "tips" of the cookie)
        val outerPoints = List(numSides) { i ->
            val angle = i * angleIncrement
            Offset(
                x = centerX + radius * cos(angle),
                y = centerY + radius * sin(angle)
            )
        }

        // Control points for the quadratic Bezier curves (the "bite" points)
        // These are shifted inwards towards the center from the midpoint of two outer points.
        val controlPoints = List(numSides) { i ->
            val angle1 = i * angleIncrement
            val angle2 = (i + 1) % numSides * angleIncrement
            val midAngle = (angle1 + angle2) / 2f

            // Calculate a point slightly inwards for the bite
            val controlRadius = radius * (1 - biteDepthFactor)
            Offset(
                x = centerX + controlRadius * cos(midAngle),
                y = centerY + controlRadius * sin(midAngle)
            )
        }

        path.moveTo(outerPoints[0].x, outerPoints[0].y)

        for (i in 0 until numSides) {
            val nextOuterPointIndex = (i + 1) % numSides
            // Draw a quadratic Bezier curve from the current outer point
            // to the next outer point, using the control point for the "bite"
            path.quadraticBezierTo(
                x1 = controlPoints[i].x, // Control point (defines the curve of the "bite")
                y1 = controlPoints[i].y,
                x2 = outerPoints[nextOuterPointIndex].x, // Next outer point
                y2 = outerPoints[nextOuterPointIndex].y
            )
        }
        path.close()
        return Outline.Generic(path)
    }
}

//fun RoundedPolygon.toShape(): Shape = object : Shape {
//    override fun createOutline(
//        size: androidx.compose.ui.geometry.Size,
//        layoutDirection: LayoutDirection,
//        density: Density
//    ): Outline {
//        val bounds = Rect(0f, 0f, size.width, size.height)
//        val path = this@toShape.toShape(bounds, layoutDirection, density)
//        return Outline.Generic(path)
//    }
//}