package com.bubblefungames.svenjandura.tapthebubbles;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;

/**
 * Created by Sven.Jandura on 12/28/2015.
 */

//Bubble to show two lines of text
public class TwoLineBubble extends Bubble {

    String mLine2;

    public TwoLineBubble(float x, float y, float size, float speed, float angle) {
        super(x, y, size, speed, angle);
    }

    public TwoLineBubble(float x, float y, float size){
        super(x,y,size);
    }

    public  TwoLineBubble(Bundle bundle){
        super(bundle);
    }

    public void setProperties(String line1, String line2,int color, int textColor,float textSize){
        super.setProperties(line1,color,textColor,textSize);
        mLine2=line2;

        //Offset the first line
        Rect r = new Rect();
        Paint paint=new Paint();
        paint.setColor(textColor);
        paint.setTextSize(textSize);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.getTextBounds(line1, 0, line1.length(), r);

        super.setTextOffset(0, -0.7f*r.height());
    }

    @Override
    public void draw(Canvas canvas){
        super.draw(canvas);
        Paint paint=new Paint();
        paint.setColor(mTextColor);
        paint.setTextSize(mTextSize);
        paint.setTextAlign(Paint.Align.CENTER);

        Rect r=new Rect();
        paint.getTextBounds(mLine2, 0, mLine2.length(), r);
        canvas.drawText(mLine2,mX+mContainer.getX(),mY+mContainer.getY()+1.5f*r.height(),paint);
    }
}
