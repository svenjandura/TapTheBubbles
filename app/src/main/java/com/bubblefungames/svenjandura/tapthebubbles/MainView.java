package com.bubblefungames.svenjandura.tapthebubbles;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.google.android.gms.ads.InterstitialAd;

/**
 * Holds the Main View of the Game
 * Created by Sven.Jandura on 12/25/2015.
 */
public class MainView extends SurfaceView implements SurfaceHolder.Callback, View.OnTouchListener {

    private GameThread thread;

    //Saves infos between start and stop
    private Bundle mSavedState;

    //Saves scores between two creats of the app
    private Bundle mSavedScores;

    //Saves settings
    private Bundle mSettings;

    private boolean mAppStarted=false;
    private boolean mSurfaceCreated=false;

    private int mSurfaceWidth;
    private int mSurfaceHeight;

    private InterstitialAd mInterstitialAd;
    private MainActivity mActivity;

    public MainView(Context context, AttributeSet attrs) {
        super(context, attrs);

        //So we get noticed when the surface changes
        getHolder().addCallback(this);

        //So we notice touch
        setOnTouchListener(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    //This function is called always after the surface is created, so we do everything here
    //The surface size can't change after the surface is created, so we don't worry about actually changing size
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mSurfaceWidth=width;
        mSurfaceHeight=height;
        if(thread==null||!thread.isRunning()) {
            mSurfaceCreated=true;
            if(mAppStarted){
                start();
            }
        }else{
            thread.setSurfaceSize(width,height);
        }
    }

    //To be executed after the app has started and the surface has been created
    public void start(){
        thread = new GameThread(getContext(),getHolder(), mSurfaceWidth, mSurfaceHeight,mSettings);
        if (mSavedState != null){
            thread.restoreState(mSavedState);
        }else{
            thread.setSavedScores(mSavedScores);
        }


        thread.setRunning(true);
        thread.setInterstitialAd(mInterstitialAd);
        thread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mSurfaceCreated=false;
    }

    public void setAppStarted(boolean started){
        mAppStarted=started;
        if(mAppStarted&&mSurfaceCreated){
            start();
        }
    }

    public void setSavedState(Bundle savedState){
        mSavedState=savedState;
        if(thread!=null){
            thread.restoreState(savedState);
        }
    }

    public void setSavedData(Bundle savedScores, Bundle settings) {
        mSavedScores=savedScores;
        mSettings=settings;
        if(thread!=null){
            thread.setSavedScores(mSavedScores);
        }
    }

    public void setInterstitialAd(InterstitialAd ad){
        mInterstitialAd=ad;
    }

    public void setMainActivity(MainActivity activity){
        mActivity=activity;
    }

    public GameThread getThread(){
        return thread;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(thread!=null){
            return thread.onTouch(v,event);
        }
        return false;
    }
}
