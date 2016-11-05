package com.bubblefungames.svenjandura.tapthebubbles;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

/**
 * Everything actually happens here.
 * Created by Sven.Jandura on 12/25/2015.
 */
public class GameThread extends Thread implements GameActionHandler{

    //True if the thread is running
    private boolean mRun;

    //Contains the size of our surface
    private int mSurfaceWidth;
    private int mSurfaceHeight;

    private SurfaceHolder mSurfaceHolder;

    //Last time update() was executed
    private long mLastUpdateTime;

    private int mMode;
    public static final int MODE_MAIN_MENU=1;
    public static final int MODE_GAME=2;
    public static final int MODE_PAUSE_MENU=3;

    private MainMenu mMainMenu;
    private Game mGame;
    private PauseMenu mPauseMenu;

    //Score Values
    private int mScore=0;
    private int mHighScore=0;
    private int mLevel=0;
    private int mBestLevel=0;

    private Context mContext;

    //Ads
    private InterstitialAd mInterstitialAd;

    //Settings
    private Bundle mSettings;

    public GameThread(Context context, SurfaceHolder surfaceHolder,int surfaceWidth, int surfaceHeight, Bundle settings){
        mContext=context;
        mSurfaceWidth=surfaceWidth;
        mSurfaceHeight=surfaceHeight;
        mSurfaceHolder=surfaceHolder;
        mSettings=settings;
        mMode=MODE_MAIN_MENU;

        mMainMenu=new MainMenu(mContext,surfaceWidth,surfaceHeight,mSettings);
        mMainMenu.setScoreValues(mScore,mHighScore,mLevel,mBestLevel);
        mMainMenu.setGameActionHandler(this);
    }

    //Changes the surface size of the Thread after it has been created
    public synchronized void setSurfaceSize(int surfaceWidth, int surfaceHeight){
        mSurfaceWidth=surfaceWidth;
        mSurfaceHeight=surfaceHeight;

        if(mMode==MODE_MAIN_MENU){
            mMainMenu.setSurfaceSize(surfaceWidth, surfaceHeight);
        }
        else if(mMode==MODE_GAME) {
            mGame.setSurfaceSize(surfaceWidth, surfaceHeight);
        }
        else if(mMode==MODE_PAUSE_MENU){
            mPauseMenu.setSurfaceSize(surfaceWidth,surfaceHeight);
        }
    }

    public void setInterstitialAd(InterstitialAd ad){
        if(ad==null) return;
        mInterstitialAd=ad;
        //Sets up the ad
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                requestNewInterstitial();
            }
        });
        requestNewInterstitial();
    }

    private void requestNewInterstitial() {
        if(mInterstitialAd==null) return;
        /*AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("29EC821B7598C98194F08ACF64EA2CEA")
                .build();*/
        AdRequest adRequest=new AdRequest.Builder().build();

        mInterstitialAd.loadAd(adRequest);
    }

    @Override
    public void run(){
        mLastUpdateTime=System.currentTimeMillis();
        while(mRun){
            Canvas canvas = null;
            try {
                canvas = mSurfaceHolder.lockCanvas(null);
                if(canvas!=null) {
                    synchronized (mSurfaceHolder) {
                        draw(canvas);
                    }
                }
                update((int)(System.currentTimeMillis()-mLastUpdateTime));
                mLastUpdateTime=System.currentTimeMillis();
            } finally {
                // do this in a finally so that if an exception is thrown
                // during the above, we don't leave the Surface in an
                // inconsistent state
                if (canvas != null) {
                    mSurfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }

    //The game logic happens here
    public synchronized void update(int timeDiff){
        if(mMode==MODE_MAIN_MENU){
            mMainMenu.update(timeDiff);
        }
        else if(mMode==MODE_GAME){
            mGame.update(timeDiff);
        }
    }

    //everything gets drawn here
    public synchronized void draw(Canvas canvas){
        if(mMode==MODE_MAIN_MENU){
            mMainMenu.draw(canvas);
        }
        else if(mMode==MODE_GAME){
            mGame.draw(canvas);
        }
        else if(mMode==MODE_PAUSE_MENU){
            mPauseMenu.draw(canvas);
        }
    }

    public void setRunning(boolean run){
        mRun=run;
    }

    //Save the state (score, highscore, etc) when the activity gets closed
    public Bundle saveState(){
        Bundle savedState=new Bundle();
        savedState.putInt("mode",mMode);
        savedState.putInt("score",mScore);
        savedState.putInt("highScore",mHighScore);
        savedState.putInt("level",mLevel);
        savedState.putInt("bestLevel",mBestLevel);
        if(mGame!=null){
            mGame.saveState(savedState);
        }

        return savedState;
    }

    //Restores the state after the activity starts
    public void restoreState(Bundle savedState){
        mMode=savedState.getInt("mode");
        mScore=savedState.getInt("score");
        mHighScore=savedState.getInt("highScore");
        mLevel=savedState.getInt("level");
        mBestLevel=savedState.getInt("bestLevel");
        if(mMode==MODE_GAME||mMode==MODE_PAUSE_MENU) {
            //Rebuild the Game and the Pause Menu object
            mGame=new ClassicGame(mContext,mSurfaceWidth,mSurfaceHeight,mSettings,true);
            mGame.setGameActionHandler(this);
            mGame.restoreState(savedState);

            mPauseMenu=new ClassicPauseMenu(mContext,mSurfaceWidth,mSurfaceHeight,mSettings);
            mPauseMenu.loadGameData(mGame.getPauseMenuData());
            mPauseMenu.setGameActionHandler(this);
        }
        else{
            //Tell the Main Menu our score
            mMainMenu.setScoreValues(mScore, mHighScore, mLevel, mBestLevel);
        }
    }

    public void setSavedScores(Bundle savedScores){
        if(savedScores==null) return;
        mScore=savedScores.getInt("score");
        mHighScore=savedScores.getInt("highScore");
        mLevel=savedScores.getInt("level");
        mBestLevel=savedScores.getInt("bestLevel");

        //Tell the main menu the scores, if it is on right now
        if(mMode==MODE_MAIN_MENU){
            mMainMenu.setScoreValues(mScore, mHighScore, mLevel, mBestLevel);
        }
    }

    public void onPause(){
        if(mMode!=MODE_MAIN_MENU&&mGame!=null) {
            onGamePause();
        }
        //If we have already lost, we can go to Main Menu when closing
        if(mMode==MODE_PAUSE_MENU&&mGame.isLost()){
            finishGame();
        }
    }

    public void onResume() {

    }

    public void finishGame(){
        mMode=MODE_MAIN_MENU;

        Bundle score=mGame.getScore();
        mScore=score.getInt("score",-1);
        mLevel=score.getInt("level",-1);

        if(mScore>mHighScore){
            mHighScore=mScore;
        }
        if(mLevel>mBestLevel){
            mBestLevel=mLevel;
        }

        mMainMenu=new MainMenu(mContext,mSurfaceWidth,mSurfaceHeight,mSettings);
        mMainMenu.setScoreValues(mScore,mHighScore,mLevel,mBestLevel);
        mMainMenu.setGameActionHandler(this);
    }

    @Override
    public synchronized void onGameFinish() {
        finishGame();
        if(mInterstitialAd!=null&&mInterstitialAd.isLoaded()&&Math.random()<Constants.AD_PROBABILITY){
            mInterstitialAd.show();
        }
    }

    @Override
    public synchronized void onGamePause() {
        mMode=MODE_PAUSE_MENU;
        mGame.pause();
        mPauseMenu=new ClassicPauseMenu(mContext,mSurfaceWidth,mSurfaceHeight,mSettings);
        mPauseMenu.loadGameData(mGame.getPauseMenuData());
        mPauseMenu.setGameActionHandler(this);
    }

    @Override
    public synchronized void onGameResume() {
        mMode=MODE_GAME;
        mGame.resume();
    }

    @Override
    public synchronized void onGameStart() {
        mMode=MODE_GAME;
        mGame=new ClassicGame(mContext,mSurfaceWidth,mSurfaceHeight,mSettings,false);
        mGame.setGameActionHandler(this);
    }

    public synchronized boolean onTouch(View v, MotionEvent event) {
        if(mMode==MODE_MAIN_MENU){
            mMainMenu.onTouch(v,event);
        }
        else if(mMode==MODE_GAME){
            mGame.onTouch(v,event);
        }
        else if(mMode==MODE_PAUSE_MENU){
            mPauseMenu.onTouch(v,event);
        }
        return true;
    }


    public boolean isRunning(){
        return mRun;
    }
}
