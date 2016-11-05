package com.bubblefungames.svenjandura.tapthebubbles;

/**
 * Created by Sven.Jandura on 12/25/2015.
 */
public interface GameActionHandler {

    public void onGameFinish();
    public void onGamePause();
    public void onGameResume();
    public void onGameStart();

}
