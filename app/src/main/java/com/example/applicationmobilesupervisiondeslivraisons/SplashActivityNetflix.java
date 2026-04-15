package com.example.applicationmobilesupervisiondeslivraisons;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivityNetflix extends AppCompatActivity {

    private LinearLayout animationContainer;
    private TextView appName;
    private TextView tvPercentage;
    private TextView truckEmoji;
    private View rootView;
    private ImageView logoImage;
    private View progressLine;
    private ObjectAnimator blinkAnimator;
    private ObjectAnimator truckAnimator;

    private static final int ROAD_WIDTH = 220; // dp
    private static final int SPLASH_DURATION = 5000; // 5 secondes (modifié)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_netflix);

        animationContainer = findViewById(R.id.animation_container);
        appName = findViewById(R.id.app_name);
        tvPercentage = findViewById(R.id.tv_percentage);
        logoImage = findViewById(R.id.logo_image);
        truckEmoji = findViewById(R.id.truck_emoji);
        progressLine = findViewById(R.id.progress_line);
        rootView = getWindow().getDecorView().getRootView();

        // Modifier le texte du camion pour qu'il aille vers la GAUCHE
        truckEmoji.setText("🚚 "); // 💨 devant, 🚚 vers la gauche

        startNetflixAnimation();
    }

    private void startNetflixAnimation() {
        // 1. Effet "pop" sur le logo
        logoImage.setScaleX(0.7f);
        logoImage.setScaleY(0.7f);
        logoImage.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(600)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> startBlinkingEffect())
                .start();

        // 2. Fade in du conteneur
        animationContainer.animate()
                .alpha(1.0f)
                .setDuration(800)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        // 3. Fade in du texte
        new Handler().postDelayed(() -> {
            appName.animate()
                    .alpha(1.0f)
                    .setDuration(600)
                    .start();
        }, 400);

        // 4. Animation du camion emoji vers la GAUCHE
        animateTruckAndProgress();

        // 5. Transition après SPLASH_DURATION (5 secondes)
        new Handler().postDelayed(() -> {
            stopBlinkingEffect();
            stopTruckAnimation();

            rootView.animate()
                    .alpha(0f)
                    .setDuration(500)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            Intent intent = new Intent(SplashActivityNetflix.this, MainActivity.class);
                            startActivity(intent);
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                            finish();
                        }
                    });
        }, SPLASH_DURATION);
    }

    private void animateTruckAndProgress() {
        int roadWidthPx = (int) (ROAD_WIDTH * getResources().getDisplayMetrics().density);

        // ANIMATION VERS LA GAUCHE : translation de 0 à -roadWidthPx
        truckAnimator = ObjectAnimator.ofFloat(truckEmoji, "translationX", 0f, (float) -roadWidthPx);
        truckAnimator.setDuration(SPLASH_DURATION);
        truckAnimator.setInterpolator(new LinearInterpolator());
        truckAnimator.start();

        // Animation de la barre de progression (de DROITE vers GAUCHE)
        // La barre commence à droite (largeur max) et diminue vers 0
        ValueAnimator progressAnimator = ValueAnimator.ofInt(roadWidthPx, 0);
        progressAnimator.setDuration(SPLASH_DURATION);
        progressAnimator.setInterpolator(new LinearInterpolator());
        progressAnimator.addUpdateListener(animation -> {
            int width = (int) animation.getAnimatedValue();
            progressLine.getLayoutParams().width = width;
            progressLine.requestLayout();

            // Pourcentage inversé : quand largeur diminue, pourcentage augmente
            int percentage = (int) ((float) (roadWidthPx - width) / roadWidthPx * 100);
            tvPercentage.setText(percentage + "%");
        });
        progressAnimator.start();
    }

    private void startBlinkingEffect() {
        blinkAnimator = ObjectAnimator.ofFloat(logoImage, "alpha", 1f, 0.3f, 1f);
        blinkAnimator.setDuration(800);
        blinkAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        blinkAnimator.setRepeatMode(ObjectAnimator.REVERSE);
        blinkAnimator.start();
    }

    private void stopBlinkingEffect() {
        if (blinkAnimator != null && blinkAnimator.isRunning()) {
            blinkAnimator.cancel();
        }
        logoImage.setAlpha(1f);
    }

    private void stopTruckAnimation() {
        if (truckAnimator != null && truckAnimator.isRunning()) {
            truckAnimator.cancel();
        }
    }
}