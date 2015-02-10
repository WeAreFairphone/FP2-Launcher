package com.fairphone.fplauncher3.widgets.peoplewidget.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;

public class CircleTransform
{

    public static final float FLOAT_TWO = 2f;
    public static final int INTEGER_TWO = 2;

    public Bitmap transform(Bitmap source, float fixedSize)
    {
    	int floorFixedSize = (int) Math.floor(fixedSize);
        Bitmap reduced = Bitmap.createScaledBitmap(source, floorFixedSize, floorFixedSize, true);
        if (reduced != source)
        {
        	source.recycle();
        }
        
        int size = Math.min(reduced.getWidth(), reduced.getHeight());

        int x = (reduced.getWidth() - size) / INTEGER_TWO;
        int y = (reduced.getHeight() - size) / INTEGER_TWO;

        Bitmap squaredBitmap = Bitmap.createBitmap(reduced, x, y, size, size);
        if (squaredBitmap != reduced)
        {
        	reduced.recycle();
        }


        Bitmap bitmap = Bitmap.createBitmap(size, size, reduced.getConfig());

        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        BitmapShader shader = new BitmapShader(squaredBitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
        paint.setShader(shader);
        paint.setAntiAlias(true);

        float r = size / FLOAT_TWO;
        canvas.drawCircle(r, r, r, paint);

        squaredBitmap.recycle();
        return bitmap;
    }

}
