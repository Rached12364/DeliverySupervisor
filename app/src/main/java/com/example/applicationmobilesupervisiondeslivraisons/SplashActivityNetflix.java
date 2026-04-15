package com.example.applicationmobilesupervisiondeslivraisons;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivityNetflix extends AppCompatActivity {

    private LinearLayout animationContainer;
    private ImageView logoImage;
    private TextView appName, tvPercentage;
    private ProgressBar progressBar;
    private TextView subtitle;

    private static final int SPLASH_DURATION = 2500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_netflix);

        initViews();
        startAnimation();
    }

    private void initViews() {
        animationContainer = findViewById(R.id.animation_container);
        logoImage = findViewById(R.id.logo_image);
        appName = findViewById(R.id.app_name);
        tvPercentage = findViewById(R.id.tv_percentage);
        progressBar = findViewById(R.id.progress_bar);
        subtitle = findViewById(R.id.subtitle);
    }

    private void startAnimation() {
        // Animation du conteneur (fade in)
        animationContainer.animate()
                .alpha(1f)
                .setDuration(500)
                .start();

        // Animation du logo (zoom)
        logoImage.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(800)
                .withEndAction(() -> {
                    logoImage.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(400)
                            .start();
                })
                .start();

        // Animation du titre (fade in + translation)
        appName.animate()
                .alpha(1f)
                .translationY(0)
                .setDuration(800)
                .start();

        // Animation du sous-titre
        if (subtitle != null) {
            subtitle.animate()
                    .alpha(1f)
                    .setDuration(800)
                    .start();
        }

        // Animation de la barre de progression
        if (progressBar != null) {
            progressBar.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .start();

            // Simuler la progression
            final Handler handler = new Handler();
            for (int i = 0; i <= 100; i++) {
                final int progress = i;
                handler.postDelayed(() -> {
                    if (progressBar != null) {
                        progressBar.setProgress(progress);
                    }
                    if (tvPercentage != null) {
                        tvPercentage.setText(progress + "%");
                        tvPercentage.animate().alpha(1f).setDuration(100).start();
                    }
                }, (long) (SPLASH_DURATION * i / 100));
            }
        }

        // Redirection après délai
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivityNetflix.this, LoginActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }, SPLASH_DURATION);
    }
}