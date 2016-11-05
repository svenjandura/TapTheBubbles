package com.bubblefungames.svenjandura.tapthebubbles;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.WindowManager;

import com.google.android.gms.ads.InterstitialAd;

//Ad branches test

public class MainActivity extends Activity {

    //Saves infos between stop and start
    private Bundle mSavedState;

    //Saves the scores between to creates of the app
    private Bundle mSavedScores;

    //Settings like sound, etc.
    private Bundle mSettings;

    private MainView mView;

    //Contains ads
    private InterstitialAd mInterstitialAd;

    //Called when the app is started
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideStatusBar();
        setContentView(R.layout.activity_main);

        mView=(MainView)findViewById(R.id.view);
        readFromSharedPrefs();
        mView.setSavedData(mSavedScores, mSettings);

        //Setup ads
        if(mSettings.getBoolean("ads")) {
            mInterstitialAd = new InterstitialAd(this);
            mInterstitialAd.setAdUnitId(Constants.PUBLISHER_ID);
            mView.setInterstitialAd(mInterstitialAd);
        }

        //Give this Activity to the view
        mView.setMainActivity(this);
    }

    @Override
    protected void onPause(){
        super.onPause();
        mView.getThread().onPause();
    }

    @Override
    protected  void onResume(){
        super.onResume();
        if(mView.getThread()!=null) {
            mView.getThread().onResume();
        }
    }

    @Override
    protected void onStart(){
        super.onStart();

        mView.setSavedState(mSavedState);
        mView.setAppStarted(true);
    }

    @Override
    protected void onStop(){
        super.onStop();

        //Save the state before stopping the thread
        mSavedState=mView.getThread().saveState();
        writeToSharedPrefs();

        mView.setAppStarted(false);

        //Stop the Thread
        boolean retry = true;
        mView.getThread().setRunning(false);
        while (retry) {
            try {
                mView.getThread().join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }
    }

    public void writeToSharedPrefs(){
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        //write scores
        editor.putInt("Score", mSavedState.getInt("score"));
        editor.putInt("Level", mSavedState.getInt("level"));
        editor.putInt("HighScore", mSavedState.getInt("highScore"));
        editor.putInt("BestLevel", mSavedState.getInt("bestLevel"));

        //write Settings
        editor.putBoolean("sound",mSettings.getBoolean("sound",true));
        editor.putBoolean("ads",mSettings.getBoolean("ads",false));

        editor.commit();
    }

    public void readFromSharedPrefs(){
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);

        //read scores
        mSavedScores =new Bundle();
        mSavedScores.putInt("score", sharedPref.getInt("Score", 0));
        mSavedScores.putInt("level", sharedPref.getInt("Level", 0));
        mSavedScores.putInt("highScore", sharedPref.getInt("HighScore", 0));
        mSavedScores.putInt("bestLevel", sharedPref.getInt("BestLevel", 0));

        //read settings
        mSettings=new Bundle();
        mSettings.putBoolean("sound",sharedPref.getBoolean("sound",true));
        mSettings.putBoolean("ads",sharedPref.getBoolean("ads",false));
    }

    public void hideStatusBar(){
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

}
