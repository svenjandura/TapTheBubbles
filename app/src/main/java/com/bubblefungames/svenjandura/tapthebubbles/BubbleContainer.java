package com.bubblefungames.svenjandura.tapthebubbles;

import android.graphics.Canvas;
import android.view.MotionEvent;

import java.util.ArrayList;

/**
 * Created by Sven.Jandura on 12/25/2015.
 */
public class BubbleContainer extends  ArrayList<Bubble> {

    //Size and position of the Conatiner
    private int mX;
    private int mY;
    private int mWidth;
    private int mHeight;


    public BubbleContainer(int x1, int y1, int x2, int y2){
        super();
        setProperties(x1,y1,x2,y2);
    }

    //To set the properties (especially the width and height that might change through the status
    //bar, after the container has been created
    public void setProperties(int x1, int y1, int x2, int y2){
        mX=x1;
        mY=y1;
        mWidth=x2-x1;
        mHeight=y2-y1;
    }

    public synchronized void draw(Canvas canvas){
        for(Bubble bubble : this){
            bubble.draw(canvas);
        }
    }

    public synchronized void update(long timeDiff){
        for(Bubble bubble : this){
            bubble.update(timeDiff);
        }
    }

    public synchronized void onTouch(MotionEvent event){
        for(int i=0;i<size();i++){
            get(i).onTouch(event);
        }
    }

    public synchronized void setSurfaceSize(int width, int height){
        for(Bubble bubble: this){
            bubble.setSurfaceProperties(width,height);
        }
    }

    public int getWidth(){
        return mWidth;
    }

    public int getHeight(){
        return mHeight;
    }

    public int getX(){
        return mX;
    }

    public int getY(){
        return mY;
    }

}
