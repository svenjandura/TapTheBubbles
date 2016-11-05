package com.bubblefungames.svenjandura.tapthebubbles;

import android.view.MotionEvent;

/**
 * Listener if a Bubble is clicked
 * Created by Sven.Jandura on 12/25/2015.
 */
public interface BubbleClickListener {

    //bubble is clicked
    public void onClick(Bubble bubble);

    //there is a click, but it's not on bubble
    public void onMissedClick(Bubble bubble,MotionEvent event);

}
