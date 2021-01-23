package com.example.projectapplication.utils;

import android.graphics.Path;
import android.graphics.RectF;

public class PathUtils {

    public static Path getSolidArcPath(Path solidArcPath,
                                       RectF outerCircleBounds, RectF innerCircleBounds,
                                       float startAngle, float sweepAngle) {
        solidArcPath.reset();

        // Move to start point
        float startX = MathUtils.getPointX(
                innerCircleBounds.centerX(),
                innerCircleBounds.width() / 2f,
                startAngle);
        float startY = MathUtils.getPointY(
                innerCircleBounds.centerY(),
                innerCircleBounds.height() / 2f,
                startAngle);
        solidArcPath.moveTo(startX, startY);


        // Add inner hole arc
        solidArcPath.addArc(innerCircleBounds, startAngle, sweepAngle);


        // Line from inner to outer arc
        solidArcPath.lineTo(
                MathUtils.getPointX(
                        outerCircleBounds.centerX(),
                        outerCircleBounds.width() / 2f,
                        startAngle + sweepAngle),
                MathUtils.getPointY(
                        outerCircleBounds.centerY(),
                        outerCircleBounds.height() / 2f,
                        startAngle + sweepAngle));

        // Add outer arc
        solidArcPath.addArc(outerCircleBounds, startAngle + sweepAngle, -sweepAngle);

        // Close (drawing last line and connecting arcs)
        solidArcPath.lineTo(startX, startY);

        return solidArcPath;
    }
}
