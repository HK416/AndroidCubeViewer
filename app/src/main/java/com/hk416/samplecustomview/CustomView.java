package com.hk416.samplecustomview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class CustomView extends View {
    private static final String TAG = CustomView.class.getSimpleName();
    private static final float[][] CUBE_VERTICES = new float[][] {
            {
                    -1.0f, +1.0f, -1.0f,
                    -1.0f, +1.0f, +1.0f,
                    +1.0f, +1.0f, +1.0f,
                    +1.0f, +1.0f, -1.0f,
                    -1.0f, +1.0f, -1.0f, // top rectangle
            },
            {
                    -1.0f, -1.0f, -1.0f,
                    -1.0f, -1.0f, +1.0f,
                    +1.0f, -1.0f, +1.0f,
                    +1.0f, -1.0f, -1.0f,
                    -1.0f, -1.0f, -1.0f, // bottom rectangle
            },
            {
                    -1.0f, -1.0f, +1.0f,
                    -1.0f, +1.0f, +1.0f,
                    +1.0f, +1.0f, +1.0f,
                    +1.0f, -1.0f, +1.0f,
                    -1.0f, -1.0f, +1.0f, // front rectangle
            },
            {
                    -1.0f, -1.0f, -1.0f,
                    -1.0f, +1.0f, -1.0f,
                    +1.0f, +1.0f, -1.0f,
                    +1.0f, -1.0f, -1.0f,
                    -1.0f, -1.0f, -1.0f, // back rectangle
            },
    };
    private static final int NUM_ELEMENTS = 3;
    private static final float F_PI = 3.14159265359f;

    private float[] worldMatrix = new float[] {
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f,
    };
    private float[] cameraMatrix = new float[] {
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 5.0f,
            0.0f, 0.0f, 0.0f, 1.0f,
    };
    private float viewTop = 0.0f;
    private float viewLeft = 0.0f;
    private float contentWidth = 0.0f;
    private float contentHeight = 0.0f;
    private float fovyRadians = 60.0f * F_PI / 180.0f;
    private Paint paint;


    public CustomView(Context context) {
        super(context);
        init(null, 0);
    }

    public CustomView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public CustomView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyleAttr) {
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(5.0f);
        paint.setColor(Color.BLACK);
    }

    public void rotate(float radians) {
        float[] rotation = new float[16];
        rotation[0] = (float)Math.cos(radians);
        rotation[2] = (float) Math.sin(radians);
        rotation[8] = -(float)Math.sin(radians);
        rotation[10] = (float)Math.cos(radians);
        rotation[5] = 1.0f;
        rotation[15] = 1.0f;

        worldMatrix = multiply(worldMatrix, rotation);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int top = getPaddingTop();
        int left = getPaddingLeft();
        int bottom = getPaddingBottom();
        int right = getPaddingRight();
        viewTop = top;
        viewLeft = left;
        contentWidth = w - left - right;
        contentHeight = h - top - bottom;
    }

    private float[] prevPoint3 = new float[3];
    private float[] currPoint3 = new float[3];
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        for (float[] cubeVertex : CUBE_VERTICES) {
            for (int i = 1; i < cubeVertex.length / NUM_ELEMENTS; i += 1) {
                prevPoint3[0] = cubeVertex[(i - 1) * NUM_ELEMENTS];
                prevPoint3[1] = cubeVertex[(i - 1) * NUM_ELEMENTS + 1];
                prevPoint3[2] = cubeVertex[(i - 1) * NUM_ELEMENTS + 2];

                prevPoint3 = worldTransform(prevPoint3);
                prevPoint3 = cameraTransform(prevPoint3);
                prevPoint3 = projectionTransform(prevPoint3);
                prevPoint3 = screenTransform(prevPoint3);

                currPoint3[0] = cubeVertex[i * NUM_ELEMENTS];
                currPoint3[1] = cubeVertex[i * NUM_ELEMENTS + 1];
                currPoint3[2] = cubeVertex[i * NUM_ELEMENTS + 2];

                currPoint3 = worldTransform(currPoint3);
                currPoint3 = cameraTransform(currPoint3);
                currPoint3 = projectionTransform(currPoint3);
                currPoint3 = screenTransform(currPoint3);

                canvas.drawLine(prevPoint3[0], prevPoint3[1], currPoint3[0], currPoint3[1], paint);
            }
        }
    }

    private float[] worldTransform(float[] modelPoint3) {
        float[] worldPosition = new float[3];
        worldPosition[0] = modelPoint3[0] * worldMatrix[0]
                + modelPoint3[1] * worldMatrix[1]
                + modelPoint3[2] * worldMatrix[2]
                + worldMatrix[3];
        worldPosition[1] = modelPoint3[0] * worldMatrix[4]
                + modelPoint3[1] * worldMatrix[5]
                + modelPoint3[2] * worldMatrix[6]
                + worldMatrix[7];
        worldPosition[2] = modelPoint3[0] * worldMatrix[8]
                + modelPoint3[1] * worldMatrix[9]
                + modelPoint3[2] * worldMatrix[10]
                + worldMatrix[11];
        return worldPosition;
    }

    private float[] cameraTransform(float[] worldPoint3) {
        float[] axis = new float[3];
        float[] eyePosition = new float[3];
        float[] cameraPosition = new float[3];

        eyePosition[0] = cameraMatrix[3];
        eyePosition[1] = cameraMatrix[7];
        eyePosition[2] = cameraMatrix[11];

        axis[0] = cameraMatrix[0]; axis[1] = cameraMatrix[4]; axis[2] = cameraMatrix[8];
        cameraPosition[0] = worldPoint3[0] * cameraMatrix[0]
                + worldPoint3[1] * cameraMatrix[4]
                + worldPoint3[2] * cameraMatrix[8]
                -dotProduct3(axis, eyePosition);

        axis[0] = cameraMatrix[1]; axis[1] = cameraMatrix[5]; axis[2] = cameraMatrix[9];
        cameraPosition[1] = worldPoint3[0] * cameraMatrix[1]
                + worldPoint3[1] * cameraMatrix[5]
                + worldPoint3[2] * cameraMatrix[9]
                -dotProduct3(axis, eyePosition);

        axis[0] = cameraMatrix[2]; axis[1] = cameraMatrix[6]; axis[2] = cameraMatrix[10];
        cameraPosition[2] = worldPoint3[0] * cameraMatrix[2]
                + worldPoint3[1] * cameraMatrix[6]
                + worldPoint3[2] * cameraMatrix[10]
                -dotProduct3(axis, eyePosition);

        return cameraPosition;
    }

    private float[] projectionTransform(float[] cameraPoint3) {
        float[] projectionPoint3 = new float[3];
        float aspectRatio = contentWidth / contentHeight;
        float tanHalfFovy = (float)Math.tan(0.5 * (double)fovyRadians);

        projectionPoint3[0] = cameraPoint3[0] / (aspectRatio * tanHalfFovy * cameraPoint3[2]);
        projectionPoint3[1] = cameraPoint3[1] / (tanHalfFovy * cameraPoint3[2]);
        projectionPoint3[2] = 1.0f;

        return projectionPoint3;
    }

    private float[] screenTransform(float[] projectionPoint3) {
        float[] screenPoint3 = new float[3];
        float halfWidth = 0.5f * contentWidth;
        float halfHeight = 0.5f * contentHeight;

        screenPoint3[0] = projectionPoint3[0] * halfWidth + viewLeft + halfWidth;
        screenPoint3[1] = projectionPoint3[1] * halfHeight + viewTop + halfHeight;
        screenPoint3[2] = 0.0f;

        return screenPoint3;
    }

    private float[] multiply(float[] aMat4, float[] bMat4) {
        float[] mat4 = new float[16];
        mat4[0] = aMat4[0] * bMat4[0] + aMat4[1] * bMat4[4] + aMat4[2] * bMat4[8] + aMat4[3] * bMat4[12];
        mat4[1] = aMat4[0] * bMat4[1] + aMat4[1] * bMat4[5] + aMat4[2] * bMat4[9] + aMat4[3] * bMat4[13];
        mat4[2] = aMat4[0] * bMat4[2] + aMat4[1] * bMat4[6] + aMat4[2] * bMat4[10] + aMat4[3] * bMat4[14];
        mat4[3] = aMat4[0] * bMat4[3] + aMat4[1] * bMat4[7] + aMat4[2] * bMat4[11] + aMat4[3] * bMat4[15];

        mat4[4] = aMat4[4] * bMat4[0] + aMat4[5] * bMat4[4] + aMat4[6] * bMat4[8] + aMat4[7] * bMat4[12];
        mat4[5] = aMat4[4] * bMat4[1] + aMat4[5] * bMat4[5] + aMat4[6] * bMat4[9] + aMat4[7] * bMat4[13];
        mat4[6] = aMat4[4] * bMat4[2] + aMat4[5] * bMat4[6] + aMat4[6] * bMat4[10] + aMat4[7] * bMat4[14];
        mat4[7] = aMat4[4] * bMat4[3] + aMat4[5] * bMat4[7] + aMat4[6] * bMat4[11] + aMat4[7] * bMat4[15];

        mat4[8] = aMat4[8] * bMat4[0] + aMat4[9] * bMat4[4] + aMat4[10] * bMat4[8] + aMat4[11] * bMat4[12];
        mat4[9] = aMat4[8] * bMat4[1] + aMat4[9] * bMat4[5] + aMat4[10] * bMat4[9] + aMat4[11] * bMat4[13];
        mat4[10] = aMat4[8] * bMat4[2] + aMat4[9] * bMat4[6] + aMat4[10] * bMat4[10] + aMat4[11] * bMat4[14];
        mat4[11] = aMat4[8] * bMat4[3] + aMat4[9] * bMat4[7] + aMat4[10] * bMat4[11] + aMat4[11] * bMat4[15];

        mat4[12] = aMat4[12] * bMat4[0] + aMat4[13] * bMat4[4] + aMat4[14] * bMat4[8] + aMat4[15] * bMat4[12];
        mat4[13] = aMat4[12] * bMat4[1] + aMat4[13] * bMat4[5] + aMat4[14] * bMat4[9] + aMat4[15] * bMat4[13];
        mat4[14] = aMat4[12] * bMat4[2] + aMat4[13] * bMat4[6] + aMat4[14] * bMat4[10] + aMat4[15] * bMat4[14];
        mat4[15] = aMat4[12] * bMat4[3] + aMat4[13] * bMat4[7] + aMat4[14] * bMat4[11] + aMat4[15] * bMat4[15];
        return mat4;
    }

    private float dotProduct3(float[] a, float[] b) {
        return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
    }
}
