package com.bubblefungames.svenjandura.tapthebubbles;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.MotionEvent;

/**
 * Created by Sven.Jandura on 12/25/2015.
 */
public class ClassicGame extends Game {

    //Time at which the current level was started
    protected long mStartTime;

    //Time after which the level is lost
    protected int mMaxTime;

    //Time since the level is running (for saving between stop and start)
    protected int mElapsedTime;

    //Ratio time in the level/maxTime
    protected double mRatio;

    //number of the current level
    protected int mLevelNumber;

    //Score
    protected int mScore;

    protected int mNextNumber;

    //True if Lost by time
    protected boolean mLostTime=false;

    //The position where we taped when we lost
    protected float mLostX;
    protected float mLostY;

    public ClassicGame(Context context, int surfaceWidth, int surfaceHeight,Bundle settings, boolean rebuild){
        super(context, surfaceWidth,surfaceHeight,settings);
        mLevelNumber=1;
        mScore=0;

        if(!rebuild) {
            startNewLevel(mLevelNumber);
        }
    }

    @Override
    public void saveState(Bundle map){
        super.saveState(map);
        map.putInt("maxTime",mMaxTime);
        map.putInt("elapsedTime",mElapsedTime);
        map.putInt("levelNumber",mLevelNumber);
        map.putInt("scoreGame",mScore);
        map.putInt("nextNumber", mNextNumber);
        map.putBoolean("lostTime", mLostTime);
        map.putFloat("lostX", mLostX);
        map.putFloat("lostY", mLostY);
    }

    @Override
    public  void restoreState(Bundle savedState){
        //Rebuild the container, because the constructor might have filled it
        mContainer=new BubbleContainer(0,(int)(Constants.HEIGHT_CUTOFF*mSurfaceHeight),mSurfaceWidth,mSurfaceHeight);
        super.restoreState(savedState);
        mMaxTime=savedState.getInt("maxTime");
        mElapsedTime=savedState.getInt("elapsedTime");
        mLevelNumber=savedState.getInt("levelNumber");
        mScore=savedState.getInt("scoreGame");
        mNextNumber=savedState.getInt("nextNumber");
        mLostTime=savedState.getBoolean("lostTime");
        mLostX=savedState.getFloat("lostX");
        mLostY=savedState.getFloat("lostY");
    }

    @Override
    public void drawBackground(Canvas canvas) {
        Paint paint=new Paint();

        //White Background
        paint.setColor(Color.WHITE);
        canvas.drawRect(0,0,mSurfaceWidth,mSurfaceHeight,paint);

        //Time display
        float[] hsv={(float) (90*(1-mRatio)),0.9f,0.97f};
        int color= Color.HSVToColor(hsv);
        paint.setColor(color);
        canvas.drawRect(0, (float) ((1 - mRatio) * mSurfaceHeight), mSurfaceWidth, mSurfaceHeight, paint);
    }

    @Override
    public Bundle getScore() {
        Bundle result=new Bundle();
        result.putInt("score", mScore);
        result.putInt("level", mLevelNumber);
        return result;
    }

    @Override
    public Bundle getPauseMenuData() {
        Bundle result=new Bundle();
        result.putInt("score",mScore);
        result.putInt("level", mLevelNumber);
        result.putInt("nextNumber",mNextNumber);
        return result;
    }

    @Override
    public void pause() {
        mElapsedTime= (int) (System.currentTimeMillis()-mStartTime);
        System.out.println("Game paused");
    }

    @Override
    public void resume() {
        mStartTime=System.currentTimeMillis()-mElapsedTime;
        System.out.println("Game resumed");
    }

    public void startNewLevel(int levelNum){
        mNextNumber=1;
        mMaxTime=Constants.MAX_TIME;
        mStartTime=System.currentTimeMillis();

        //Decides how hard the level will be
        int numBubbles= (int) (5+7*(1-Math.exp(-(levelNum-1)*Constants.DIFFICULTY_FACTOR_BUBBLES)));
        double prob=0.4+0.4*(1-Math.exp(-(levelNum-1)*Constants.DIFFICULTY_FACTOR_PROB));
        float speed= (float) (Constants.START_SPEED+(Constants.MAX_SPEED-Constants.START_SPEED)*(1-Math.exp(-(levelNum-1)*Constants.DIFFICULTY_FACTOR_SPEED)));

        for(int i=0;i<numBubbles;i++){
            Bubble bubble;
            if(Math.random()<prob) {
                //Create bubble
                bubble = new Bubble(0, 0, (float) (mSurfaceWidth * Constants.BUBBLE_SIZE), speed, (float) (Math.random() * 2 * Math.PI));
            }
            else {
                bubble = new Bubble(0, 0, (float) (mSurfaceWidth * Constants.BUBBLE_SIZE));
            }
            bubble.setProperties((i + 1) + "", Color.BLACK, Color.WHITE, mSurfaceWidth / 20);
            bubble.setSurfaceProperties(mSurfaceWidth, mSurfaceHeight);
            bubble.setContainer(mContainer);
            bubble.setBubbleOutListener(this);
            bubble.addBubbleClickListener(this);
            bubble.initialize();

            mContainer.add(bubble);
        }
    }


    //If you hit the right bubble, remove it
    @Override
    public void onClick(Bubble bubble) {
        if(bubble.getText().equals(""+mNextNumber)){
            if(mSettings.getBoolean("sound")) {
                mPopSound.start();
            }
            mContainer.remove(bubble);
            mNextNumber++;

            //Score for poping a bubble
            mScore+=(int)(1+mLevelNumber*Constants.SCORE_LEVEL_FACTOR);
            if(mContainer.size()==0){
                //Score for finishing a level early
                int remainingTime=mMaxTime-(int)(System.currentTimeMillis()-mStartTime);
                mScore+=(int) (mLevelNumber*remainingTime*Constants.SCORE_TIME_FACTOR);

                mLevelNumber++;
                startNewLevel(mLevelNumber);

            }
        }
    }

    //If you hit the wrong bubble, you lost
    @Override
    public void onMissedClick(Bubble bubble,MotionEvent event) {
        synchronized (bubble){
            if(event.getY()>=mSurfaceHeight*Constants.HEIGHT_CUTOFF&&bubble.getText().equals(""+mNextNumber)){
                mLost=true;
                mLooseTime=System.currentTimeMillis();
                mLostX=event.getX();
                mLostY=event.getY();
                if(mSettings.getBoolean("sound")){
                    mLooseSound.start();
                }
            }
        }
    }

    @Override
    public void onBubbleOut(Bubble bubble, BubbleContainer container) {
        bubble.reflect();
    }

    @Override
    public void update(long timeDiff){
        super.update(timeDiff);
        if(!mLost) {
            mRatio = (System.currentTimeMillis() - mStartTime) * 1.0 / mMaxTime;

            if (mRatio > 1) {
                mLost = true;
                mLostTime=true;
                mLooseTime=System.currentTimeMillis();
                if(mSettings.getBoolean("sound")){
                    mLooseSound.start();
                }
            }
        }
    }

    @Override
    public void draw(Canvas canvas){
        super.draw(canvas);
        Paint text = new Paint();
        text.setTextSize(mSurfaceWidth / 20);
        canvas.drawText("Level:  " + mLevelNumber, mSurfaceWidth * 0.05f, mSurfaceHeight * 0.08f, text);
        canvas.drawText("Score: " + mScore, mSurfaceWidth * 0.05f, mSurfaceHeight * 0.04f, text);

        if(mLost){
            if(!mLostTime){
                Paint paint=new Paint();
                paint.setColor(Color.argb(200, 0, 0, 255));
                canvas.drawCircle(mLostX, mLostY, mSurfaceWidth/14, paint);
            }

            Paint paint=new Paint();
            paint.setColor(Color.BLACK);
            canvas.drawRect(0.15f * mSurfaceWidth, 0.4f * mSurfaceHeight, 0.85f * mSurfaceWidth, 0.6f * mSurfaceHeight, paint);
            paint.setColor(Color.WHITE);
            canvas.drawRect(0.15f * mSurfaceWidth + 10, 0.4f * mSurfaceHeight + 10, 0.85f * mSurfaceWidth - 10, 0.6f * mSurfaceHeight - 10, paint);

            text.setColor(Color.BLACK);
            text.setTextSize(mSurfaceWidth / 10);
            text.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("Game Over!", mSurfaceWidth / 2, mSurfaceHeight * 0.47f, text);
            text.setTextSize(mSurfaceWidth / 15);
            canvas.drawText("Tap for Main Menu", mSurfaceWidth / 2, mSurfaceHeight * 0.55f, text);

        }
    }

}
