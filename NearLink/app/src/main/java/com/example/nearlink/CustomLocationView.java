package com.example.nearlink;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.util.AttributeSet;
import android.view.View;
import java.util.HashMap;
import java.util.Map;

public class CustomLocationView extends View {
    private Paint paint;
    private Location myLocation;
    private Map<String, Location> nearbyLocations;
    private float scale = 1.0f; // メートル/ピクセルの比率
    private static final int MAX_RANGE = 1000; // メートル単位での表示範囲

    public CustomLocationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomLocationView(Context context) {
        super(context);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        nearbyLocations = new HashMap<>();
    }

    public void updateMyLocation(Location location) {
        myLocation = location;
        invalidate();
    }

    public void updateNearbyLocations(Map<String, Location> locations) {
        nearbyLocations = locations;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (myLocation == null) return;

        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;

        // 自分の位置を中心に描画
        paint.setColor(Color.BLUE);
        canvas.drawCircle(centerX, centerY, 10, paint);

        // 近くのユーザーを描画
        paint.setColor(Color.RED);
        for (Map.Entry<String, Location> entry : nearbyLocations.entrySet()) {
            Location otherLocation = entry.getValue();
            float[] results = new float[3];

            Location.distanceBetween(
                    myLocation.getLatitude(), myLocation.getLongitude(),
                    otherLocation.getLatitude(), otherLocation.getLongitude(),
                    results
            );

            float distance = results[0];
            float bearing = results[1];

            // 表示範囲内の場合のみ描画
            if (distance <= MAX_RANGE) {
                // 相対座標に変換
                float x = (float) (Math.sin(Math.toRadians(bearing)) * distance * scale);
                float y = (float) (Math.cos(Math.toRadians(bearing)) * distance * scale);

                canvas.drawCircle(
                        centerX + x,
                        centerY - y,
                        8,
                        paint
                );
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int size = Math.min(getMeasuredWidth(), getMeasuredHeight());
        setMeasuredDimension(size, size);
    }
}