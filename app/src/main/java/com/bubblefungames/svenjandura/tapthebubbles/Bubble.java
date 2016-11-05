package com.bubblefungames.svenjandura.tapthebubbles;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;

import java.util.ArrayList;

/**
 * Created by Sven.Jandura on 12/25/2015.
 */
public class Bubble {

    //State of the Bubble
    protected float mX;
    protected float mY;
    private float mSize;
    private float mSpeed;
    private float mAngle;
    private boolean mMoving;
    private long mLastCollisionTime=System.currentTimeMillis();

    //Properties of the Bubble
    private String mText;
    protected float mTextXOffset=0;
    protected float mTextYOffset=0;
    private int mColor;
    protected int mTextColor;
    protected float mTextSize;

    //Properties of the Surface
    private int mSurfaceWidth;
    private int mSurfaceHeight;

    //Listeners
    private ArrayList<BubbleClickListener> mClickListeners;
    private BubbleOutListener mOutListener;

    //Container of the Bubbles
    protected BubbleContainer mContainer;

    //Constructor for moving bubbles
    public Bubble(float x, float y, float size, float speed, float angle){
        mX=x;
        mY=y;
        mSize=size;
        mSpeed=speed;
        mAngle=angle;
        mMoving=true;

        mClickListeners=new ArrayList<BubbleClickListener>();
    }

    //Constructor for stationary bubbles
    public Bubble(float x, float y, float size){
        mX=x;
        mY=y;
        mSize=size;
        mMoving=false;

        mClickListeners=new ArrayList<BubbleClickListener>();
    }

    //Constructor from saved state
    public Bubble(Bundle savedState){
        mX=savedState.getFloat("x");
        mY=savedState.getFloat("y");
        mSize=savedState.getFloat("size");
        mSpeed=savedState.getFloat("speed");
        mAngle=savedState.getFloat("angle");
        mText=savedState.getString("text");
        mLastCollisionTime=savedState.getLong("lastCollisionTime");
        mMoving=savedState.getBoolean("moving");

        mTextXOffset=savedState.getFloat("textXOffset");
        mTextYOffset=savedState.getFloat("textYOffset");
        mColor=savedState.getInt("color");
        mTextColor=savedState.getInt("textColor");
        mTextSize=savedState.getFloat("textSize");

        mClickListeners=new ArrayList<BubbleClickListener>();
    }

    public void setSurfaceProperties(int surfaceWidth, int surfaceHeight){
        mSurfaceWidth=surfaceWidth;
        mSurfaceHeight=surfaceHeight;
    }

    public void setProperties(String text, int color, int textColor, float textSize){
        mText=text;
        mColor=color;
        mTextColor=textColor;
        mTextSize=textSize;
    }

    public void setTextOffset(float xOffset, float yOffset){
        mTextXOffset=xOffset;
        mTextYOffset=yOffset;
    }

    public void setContainer(BubbleContainer container){
        mContainer=container;
    }

    public void setBubbleOutListener(BubbleOutListener listener){
        mOutListener=listener;
    }

    public void addBubbleClickListener(BubbleClickListener listener){
        mClickListeners.add(listener);
    }

    public String getText(){
        return mText;
    }

    public float getX(){
        return  mX;
    }

    public float getY(){
        return mY;
    }

    public float getSize(){
        return mSize;
    }

    //Initializes the position of bubbles
    public void initialize(){
        if(mContainer==null) return;
        boolean repeat=false;
        do{
            repeat=false;
            mX=(float) (mSize+Math.random()*(mContainer.getWidth()-2*mSize));
            mY=(float) (mSize+Math.random()*(mContainer.getHeight()-2*mSize));
            for(Bubble bubble : mContainer){
                if(bubble!=this&&doesTouchOtherBubble(bubble,1.5f)){  //Factor 1.5 prevents bubbles from spawning to close to each other
                    repeat=true;
                }
            }
        }while(repeat);

        //Adjust the speed to the size of the container.
        mSpeed*=mContainer.getWidth()*Constants.SPEED_SCALAR;
    }

    public boolean doesTouchOtherBubble(Bubble bubble){
        double distance=Math.sqrt((mX-bubble.getX())*(mX-bubble.getX())+(mY-bubble.getY())*(mY-bubble.getY()));
        return distance<=mSize/2+bubble.getSize()/2;
    }

    public boolean doesTouchOtherBubble(Bubble bubble,float toleraceFactor){
        double distance=Math.sqrt((mX-bubble.getX())*(mX-bubble.getX())+(mY-bubble.getY())*(mY-bubble.getY()));
        return distance<=(mSize/2+bubble.getSize()/2)*toleraceFactor;
    }

    public void draw(Canvas canvas){
        float xOffset=mContainer.getX();
        float yOffset=mContainer.getY();

        Paint paint=new Paint();
        paint.setColor(mColor);
        canvas.drawCircle(mX+xOffset, mY+yOffset, mSize / 2, paint);

        paint.setColor(mTextColor);
        paint.setTextSize(mTextSize);
        paint.setTextAlign(Paint.Align.CENTER);

        Rect r = new Rect();
        paint.getTextBounds(mText, 0, mText.length(), r);
        canvas.drawText(mText, mX+xOffset+mTextXOffset, mY+yOffset+r.height()/2+mTextYOffset, paint);
    }

    public void update(long timeDiff) {
        mX += Math.cos(mAngle) * mSpeed * timeDiff;
        mY += Math.sin(mAngle) * mSpeed * timeDiff;

        if (System.currentTimeMillis() - mLastCollisionTime > Constants.COLLISION_DIFFERENCE) {
            //Collision with walls
            if(mX <= mSize / 2 || mX >= mContainer.getWidth() - mSize / 2||mY <= mSize / 2 || mY>= mContainer.getHeight() - mSize / 2){
                mOutListener.onBubbleOut(this,mContainer);
                mLastCollisionTime = System.currentTimeMillis();
            }

            //Collision with other bubbles
            for (Bubble bubble : mContainer) {
                if (bubble == this) {
                    continue;
                }
                if (doesTouchOtherBubble(bubble)) {
                    float phi = -(float) Math.atan2((mX - bubble.getX()), (mY - bubble.getY()));
                    mAngle = -mAngle + 2 * phi;
                    mLastCollisionTime = System.currentTimeMillis();
                }
            }
        }
    }

    public void reflect(){
        if (mX <= mSize / 2 || mX >= mContainer.getWidth() - mSize / 2) {
            mAngle = (float) (Math.PI - mAngle);
            mLastCollisionTime = System.currentTimeMillis();
        }
        if (mY <= mSize / 2 || mY>= mContainer.getHeight() - mSize / 2) {
            mAngle *= -1;
            mLastCollisionTime = System.currentTimeMillis();
        }
    }

    public void onTouch(MotionEvent event){
        float touchX=event.getX();
        float touchY=event.getY();
        //Compensate the offset through the Container
        touchX-=mContainer.getX();
        touchY-=mContainer.getY();

        float distance= (float) Math.sqrt((mX-touchX)*(mX-touchX)+(mY-touchY)*(mY-touchY));
        if(distance<=mSize/2*Constants.TOLERANCE){
            for(BubbleClickListener listener : mClickListeners){
                listener.onClick(this);
            }
        }
        else{
            for (BubbleClickListener listener : mClickListeners){
                listener.onMissedClick(this,event);
            }
        }
    }

    public Bundle saveState(){
        Bundle map = new Bundle();
        map.putFloat("x",mX);
        map.putFloat("y", mY);
        map.putFloat("size", mSize);
        map.putFloat("speed",mSpeed);
        map.putFloat("angle",mAngle);
        map.putString("text", mText);
        map.putBoolean("moving", mMoving);
        map.putLong("lastCollisionTime",mLastCollisionTime);

        map.putFloat("textXOffset",mTextXOffset);
        map.putFloat("textYOffset",mTextYOffset);
        map.putInt("color",mColor);
        map.putInt("textColor",mTextColor);
        map.putFloat("textSize",mTextSize);
        return map;
    }


}
