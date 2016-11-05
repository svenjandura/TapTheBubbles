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
public class PauseMenu implements BubbleClickListener {

    protected int mSurfaceWidth;
    protected int mSurfaceHeight;

    protected BubbleContainer mContainer;

    protected GameActionHandler mGameActionHandler;

    protected Context mContext;

    protected MediaPlayer mPopSound;

    protected Bundle mSettings;

    public PauseMenu(Context context,int surfaceWidth, int surfaceHeight,Bundle settings){
        mContext=context;
        mSurfaceWidth=surfaceWidth;
        mSurfaceHeight=surfaceHeight;
        mContainer=new BubbleContainer(0,0,mSurfaceWidth,mSurfaceHeight);
        mSettings=settings;

        //Continue-Bubble
        Bubble continueBubble=new Bubble(mSurfaceWidth / 4, mSurfaceHeight * 2 / 3, 2*Math.min(mSurfaceWidth / 4, mSurfaceHeight / 3) * 0.9f);
        continueBubble.setProperties("Continue", Color.GREEN,Color.BLACK,mSurfaceWidth/15);
        continueBubble.setSurfaceProperties(mSurfaceWidth,mSurfaceHeight);
        continueBubble.setContainer(mContainer);
        continueBubble.addBubbleClickListener(this);
        mContainer.add(continueBubble);

        //Main Menu-Bubble
        Bubble mainMenuBubble=new Bubble(mSurfaceWidth / 4*3, mSurfaceHeight * 2 / 3, 2*Math.min(mSurfaceWidth / 4, mSurfaceHeight / 3) * 0.9f);
        mainMenuBubble.setProperties("Main Menu", Color.RED,Color.BLACK,mSurfaceWidth/15);
        mainMenuBubble.setSurfaceProperties(mSurfaceWidth, mSurfaceHeight);
        mainMenuBubble.setContainer(mContainer);
        mainMenuBubble.addBubbleClickListener(this);
        mContainer.add(mainMenuBubble);

        mPopSound=MediaPlayer.create(context,R.raw.bubble_pop2);
        mPopSound.setLooping(false);
    }

    public void setSurfaceSize(int width, int height){
        mSurfaceWidth=width;
        mSurfaceHeight=height;
    }

    public void loadGameData(Bundle gameData){

    }

    public void setGameActionHandler(GameActionHandler handler){
        mGameActionHandler=handler;
    }

    @Override
    public void onClick(Bubble bubble) {
        if(mSettings.getBoolean("sound")) {
            mPopSound.start();
        }
        if(bubble.getText().equals("Continue")){
            mGameActionHandler.onGameResume();
        }else{
            mGameActionHandler.onGameFinish();
        }
    }

    @Override
    public void onMissedClick(Bubble bubble, MotionEvent event) {

    }

    public void draw(Canvas canvas){
        //Background
        Paint paint=new Paint();
        paint.setColor(Color.WHITE);
        canvas.drawRect(0,0,mSurfaceWidth,mSurfaceHeight,paint);

        mContainer.draw(canvas);

    }

    public void onTouch(View view, MotionEvent event){
        if(event.getAction()==MotionEvent.ACTION_DOWN)
            mContainer.onTouch(event);
    }
}
