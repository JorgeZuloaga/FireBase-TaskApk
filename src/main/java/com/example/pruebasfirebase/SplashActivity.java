package com.example.pruebasfirebase;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.media.Image;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;

import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends AppCompatActivity {

     ImageView imagenLogo;
     private MediaPlayer sonido;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imagenLogo = findViewById(R.id.titulo1);
        sonido = MediaPlayer.create(this,R.raw.violines_fadeout);
        sonido.start();



        Intent intent = new Intent(SplashActivity.this,AuthActivity.class);
        startActivity(intent);


        SystemClock.sleep(2700);


    }


}