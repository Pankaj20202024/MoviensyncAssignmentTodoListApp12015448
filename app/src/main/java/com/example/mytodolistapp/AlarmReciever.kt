package com.example.mytodolistapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer

class AlarmReceiver :BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {
        val mediaPlayer = MediaPlayer.create(context, R.raw.alarm)
        mediaPlayer.start()
    }
}
