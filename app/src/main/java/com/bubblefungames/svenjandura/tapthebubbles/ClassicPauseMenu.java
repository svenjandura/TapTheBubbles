package com.bubblefungames.svenjandura.tapthebubbles;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;

/**
 * Created by Sven.Jandura on 12/25/2015.
 */
public class ClassicPauseMenu extends PauseMenu {

    private int mScore;
    private int mLevel;
    private int mNextNumber;

    public ClassicPauseMenu(Context context, int surfaceWidth, int surfaceHeight,Bundle settings) {
        super(context, surfaceWidth, surfaceHeight,settings);
    }

    @Override
    public void loadGameData(Bundle gameData){
        mScore=gameData.getInt("score");
        mLevel=gameData.getInt("level");
        mNextNumber=gameData.getInt("nextNumber");
    }

    @Override
    public void draw(Canvas canvas){
        super.draw(canvas);

        Paint paint=new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(mSurfaceWidth / 10);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Pause", mSurfaceWidth / 2, mSurfaceHeight / 4, paint);

        paint.setTextSize(mSurfaceWidth / 15);
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("Score: " + mScore, mSurfaceWidth / 8, mSurfaceHeight * 2.7f / 8, paint);
        canvas.drawText("Level:  " + mLevel, mSurfaceWidth / 8, mSurfaceHeight * 3.2f / 8, paint);
        canvas.drawText("Next Bubble:  " + mNextNumber, mSurfaceWidth / 8, mSurfaceHeight * 3.7f / 8, paint);
    }
}
