package my.Detection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.View;

import android.util.AttributeSet;

public class SensorReader
{
    public double threshold;
    public boolean simpleDetect = false;

    public boolean bigACCDetected = false;
    public boolean blockedstate = false;
    private String stateText;

    public Bitmap mBitmap;
    public Paint mPaint = new Paint();
    public Canvas mCanvas = new Canvas();
    private Path mPath = new Path();
    private RectF mRect = new RectF();
    public float mLastValues[] = new float[3 * 2];
    public float mOrientationValues[] = new float[3];
    public int mColors[] = new int[3 * 2];
    public float mLastX;
    public float mScale[] = new float[2];
    public float mYOffset;
    private float mMaxX;
    public float mSpeed = 3.0f;// 1.0f;
    private float mWidth;
    private float mHeight;

    private String TAG = "DJP_testing_Readerview";

    // initialization
    protected SensorReader()
    {
        mColors[0] = Color.argb(192, 255, 64, 64);
        mColors[1] = 0xFF00FF00;// green

        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mRect.set(-0.5f, -0.5f, 0.5f, 0.5f);
        mPath.arcTo(mRect, 0, 180);

        mimic(1,1,1,1);
    }


    protected void mimic(int w, int full_h, int oldw, int oldh)
    {
        int h = full_h;// -50; // the real height of view
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
        mCanvas.setBitmap(mBitmap);
        mCanvas.drawColor(0xFFFFFFFF);

        mYOffset = h * 0.5f;
        mScale[0] = -(h * 0.5f * (1.0f / (SensorManager.STANDARD_GRAVITY * 1.4f)));
        // mScale[1] = - (h * 0.5f * (1.0f /
        // (SensorManager.MAGNETIC_FIELD_EARTH_MAX)));
        mScale[1] = -(h * 0.5f * (1.0f / 120));
        mWidth = w;
        mHeight = h;
        if (mWidth < mHeight)
        {
            mMaxX = w;
            //Log.i(TAG, "Width is less than Height. mMaxX is " + mMaxX);
            //Log.i(TAG, "mWidth is " + w + "mHeight is " + h);
        }
        else
        {
            mMaxX = w - 50;
            //Log.i(TAG, "Width is lager than Height. mMaxX is " + mMaxX);
            //Log.i(TAG, "mWidth is " + mWidth + "mHeight is " + h);
        }
        mLastX = mMaxX;
    }

}