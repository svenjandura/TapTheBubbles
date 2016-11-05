package com.bubblefungames.svenjandura.tapthebubbles;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Sven.Jandura on 12/25/2015.
 */
public abstract class Game implements BubbleClickListener, BubbleOutListener {

    protected BubbleContainer mContainer;

    //Handles finish and pause
    protected GameActionHandler mGameActionHandler;

    //Surface
    protected int mSurfaceWidth;
    protected int mSurfaceHeight;

    //True if the Game is lost but we don't want to go to main menu yet
    protected boolean mLost=false;

    //Media Player for poping bubbles and loosing
    protected MediaPlayer mPopSound;
    protected MediaPlayer mLooseSound;

    protected Context mContext;

    protected Bundle mSettings;

    //Time at which the game was lost
    protected long mLooseTime;

    public Game(Context context,int surfaceWidth, int surfaceHeight,Bundle settings){
        mContext=context;
        mSurfaceWidth=surfaceWidth;
        mSurfaceHeight=surfaceHeight;
        mSettings=settings;

        mContainer=new BubbleContainer(0,(int)(Constants.HEIGHT_CUTOFF*mSurfaceHeight),mSurfaceWidth,mSurfaceHeight);

        mPopSound=MediaPlayer.create(context,R.raw.bubble_pop2);
        mPopSound.setLooping(false);
        mLooseSound=MediaPlayer.create(context,R.raw.lost_sound);
        mLooseSound.setLooping(false);
    }

    public void setGameActionHandler(GameActionHandler handler){
        mGameActionHandler=handler;
    }

    public void update(long timeDiff){
        if(!mLost) {
            mContainer.update(timeDiff);
        }
    }

    public void setSurfaceSize(int surfaceWidth, int surfaceHeight){
        mSurfaceWidth=surfaceWidth;
        mSurfaceHeight=surfaceHeight;
        mContainer.setSurfaceSize(surfaceWidth,surfaceHeight);
        mContainer.setProperties(0,(int)(Constants.HEIGHT_CUTOFF*mSurfaceHeight),mSurfaceWidth,mSurfaceHeight);
    }

    public void onClick(Bubble bubble){
        if(mSettings.getBoolean("sound")){
            mPopSound.start();
        }
    }


    public void draw(Canvas canvas){
        drawBackground(canvas);
        mContainer.draw(canvas);

        //Line over Container and Pause symbol
        Paint paint=new Paint();
        paint.setColor(Color.BLACK);
        //Line
        canvas.drawRect(0, (float)(mSurfaceHeight * Constants.HEIGHT_CUTOFF - 10),mSurfaceWidth,(float)(mSurfaceHeight*Constants.HEIGHT_CUTOFF),paint);
        //Pause symbol
        canvas.drawRect(mSurfaceWidth * 0.9f, mSurfaceHeight * 0.03f, mSurfaceWidth * 0.91f, mSurfaceHeight * 0.07f, paint);
        canvas.drawRect(mSurfaceWidth * 0.94f, mSurfaceHeight * 0.03f, mSurfaceWidth * 0.95f, mSurfaceHeight * 0.07f, paint);
    }

    public abstract void drawBackground(Canvas canvas);

    public void onTouch(View view, MotionEvent event){
        synchronized (mContainer) {
            if (event.getAction() != MotionEvent.ACTION_DOWN) return;

            if (!mLost) {
                if (event.getY() > Constants.HEIGHT_CUTOFF*mSurfaceHeight) {
                    mContainer.onTouch(event);
                } else if (event.getX() > 0.9 * mSurfaceWidth) {
                    mGameActionHandler.onGamePause();
                    System.out.println("Pause");
                }
            } else if(System.currentTimeMillis()-mLooseTime>Constants.DEAD_TIME_MAIN_MENU) {
                mGameActionHandler.onGameFinish();
            }
        }
    }

    public abstract Bundle getScore();
    public abstract Bundle getPauseMenuData();

    //Called imediatly before going to pause menu -> Stop the clocks
    public abstract void pause();

    //Called after coming back from pause menue -> Start the clocks
    public abstract void resume();

    public void saveState(Bundle map){
        map.putInt("bubbleNumber",mContainer.size());
        for(int i=0;i<mContainer.size();i++){
            map.putBundle("Bubble "+i,mContainer.get(i).saveState());
        }
        map.putBoolean("lost",mLost);
    }

    public void restoreState(Bundle savedState){
        mLost=savedState.getBoolean("lost");
        int bubbleNum=savedState.getInt("bubbleNumber");
        for(int i=0;i<bubbleNum;i++){
            Bubble bubble=new Bubble(savedState.getBundle("Bubble "+i));
            bubble.setContainer(mContainer);
            bubble.setBubbleOutListener(this);
            bubble.addBubbleClickListener(this);

            mContainer.add(bubble);
        }
    }

    public boolean isLost(){
        return mLost;
    }


}
