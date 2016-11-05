package com.bubblefungames.svenjandura.tapthebubbles;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Sven.Jandura on 12/25/2015.
 */
public class MainMenu implements BubbleClickListener,BubbleOutListener {

    //Container for the bubbles in the lower region
    private BubbleContainer mContainer;

    //Surface Properties
    private int mSurfaceWidth;
    private int mSurfaceHeight;

    private GameActionHandler mGameActionHandler;

    private int mScore;
    private int mHighScore;
    private int mLevel;
    private int mBestLevel;

    //Media player for the pop sound
    private MediaPlayer mPopSound;

    //Context of the app
    private Context mContext;

    private Bundle mSettings;

    //Bubble indicating if the sound is on or off
    private TwoLineBubble mSoundBubble;

    public MainMenu(Context context, int surfaceWidth, int surfaceHeight, Bundle settings){
        mContext=context;
        mSurfaceWidth=surfaceWidth;
        mSurfaceHeight=surfaceHeight;
        mContainer=new BubbleContainer(0, (int) (Constants.MAIN_MENU_CUTOFF*mSurfaceHeight),mSurfaceWidth,mSurfaceHeight);
        mSettings=settings;

        //Play Bubble
        Bubble bubble=new Bubble(0.5f*mContainer.getWidth(),0.5f*mContainer.getHeight(),2 * Math.min(mSurfaceWidth / 4, mSurfaceHeight / 3) * 0.9f,
                Constants.MAIN_MENU_SPEED, (float) (Math.random()*2*Math.PI));
        bubble.setSurfaceProperties(mSurfaceWidth, mSurfaceHeight);
        bubble.setProperties("Play", Color.argb(255, 255, 102, 0), Color.BLACK, mSurfaceWidth * 0.08f);

        //Calculate the yOffset of the text
        Paint paint=new Paint();
        paint.setTextSize(mSurfaceWidth * 0.08f);
        paint.setTextAlign(Paint.Align.CENTER);
        Rect r = new Rect();
        paint.getTextBounds("Play", 0, "Play".length(), r);
        bubble.setTextOffset(0, -r.height() / 4);

        bubble.setContainer(mContainer);
        bubble.setBubbleOutListener(this);
        bubble.addBubbleClickListener(this);
        mContainer.add(bubble);

        //Sound bubble
        mSoundBubble=new TwoLineBubble(0.85f*mContainer.getWidth(),0.85f*mContainer.getHeight(),2 * Math.min(mSurfaceWidth / 4, mSurfaceHeight / 3) * 0.4f);
        mSoundBubble.setSurfaceProperties(mSurfaceWidth, mSurfaceHeight);
        setSoundBubbleProperties();
        mSoundBubble.setContainer(mContainer);
        mSoundBubble.setBubbleOutListener(this);
        mSoundBubble.addBubbleClickListener(this);
        mContainer.add(mSoundBubble);

        //Media player
        mPopSound=MediaPlayer.create(context,R.raw.bubble_pop2);
        mPopSound.setLooping(false);
    }

    public void setSoundBubbleProperties(){
        if(mSettings.getBoolean("sound")){
            mSoundBubble.setProperties("Sound","On",Color.CYAN,Color.BLACK,mSurfaceWidth*0.045f);
        }else{
            mSoundBubble.setProperties("Sound","Off",Color.CYAN,Color.BLACK,mSurfaceWidth*0.045f);
        }
    }

    public void setScoreValues(int score,int highScore,int level, int bestLevel){
        mScore=score;
        mHighScore=highScore;
        mLevel=level;
        mBestLevel=bestLevel;
    }

    public void setSurfaceSize(int width, int height){
        mSurfaceWidth=width;
        mSurfaceHeight=height;
        mContainer.setSurfaceSize(width,height);
        mContainer.setProperties(0, (int) (Constants.MAIN_MENU_CUTOFF*mSurfaceHeight),mSurfaceWidth,mSurfaceHeight);
    }

    public void setGameActionHandler(GameActionHandler handler){
        mGameActionHandler=handler;
    }


    @Override
    public void onClick(Bubble bubble) {
        if(bubble==mSoundBubble){
            boolean sound=mSettings.getBoolean("sound");
            mSettings.putBoolean("sound",!sound);
            setSoundBubbleProperties();
        }else {
            mGameActionHandler.onGameStart();
        }
        if(mSettings.getBoolean("sound")) {
            mPopSound.start();
        }
    }

    @Override
    public void onMissedClick(Bubble bubble, MotionEvent event) {

    }

    @Override
    public void onBubbleOut(Bubble bubble, BubbleContainer container) {
        bubble.reflect();
    }

    public void update(int timeDiff){
        mContainer.update(timeDiff);
    }

    public void draw(Canvas canvas){
        //Background
        Paint paint=new Paint();
        paint.setColor(Color.WHITE);
        canvas.drawRect(0,0,mSurfaceWidth,mSurfaceHeight,paint);

        //Bubbles
        mContainer.draw(canvas);

        //Rest
        Paint text=new Paint();
        text.setColor(Color.BLACK);
        text.setTextSize(mSurfaceWidth * 0.1f);
        text.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Tap the Bubbles!", mSurfaceWidth / 2, mSurfaceHeight / 6, text);

        text.setTextAlign(Paint.Align.LEFT);
        text.setTextSize(mSurfaceWidth / 15);
        canvas.drawText("Score: " + mScore, mSurfaceWidth / 8, 0.25f * mSurfaceHeight, text);
        text.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("Level: " + mLevel, mSurfaceWidth * (1 - 1.0f / 8), 0.33f * mSurfaceHeight, text);
        text.setTextSize(mSurfaceWidth / 20);
        text.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("(High Score: " + mHighScore + ")", mSurfaceWidth / 7, 0.285f * mSurfaceHeight, text);
        text.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("(Best: " + mBestLevel + ")", mSurfaceWidth*(1-1.0f/7), 0.365f * mSurfaceHeight, text);
    }

    public void onTouch(View view, MotionEvent event){
        if(event.getAction()!=MotionEvent.ACTION_DOWN) return;
        mContainer.onTouch(event);
    }
}
