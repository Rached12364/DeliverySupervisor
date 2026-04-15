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
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivityNetflix extends AppCompatActivity {

    private LinearLayout animationContainer;
    private ImageView logoImage;
    private TextView appName;
    private TextView truckEmoji;
    private View progressLine;
    private TextView tvPercentage;

    private static final int SPLASH_DURATION = 3000; // 3 secondes

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
        truckEmoji = findViewById(R.id.truck_emoji);
        progressLine = findViewById(R.id.progress_line);
        tvPercentage = findViewById(R.id.tv_percentage);
    }

    private void startAnimation() {
        // 1. Faire apparaître le conteneur principal
        animationContainer.animate()
                .alpha(1f)
                .setDuration(500)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        // 2. Animation du logo (zoom et rotation douce)
        logoImage.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(800)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    logoImage.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(400)
                            .start();
                })
                .start();

        // 3. Animation du texte du titre (fade in + translation)
        AnimationSet titleAnim = new AnimationSet(true);
        AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(800);
        TranslateAnimation translate = new TranslateAnimation(0, 0, 50, 0);
        translate.setDuration(800);
        titleAnim.addAnimation(fadeIn);
        titleAnim.addAnimation(translate);
        appName.startAnimation(titleAnim);
        appName.setAlpha(1f);

        // 4. Animation du camion qui avance vers la GAUCHE
        animateTruck();

        // 5. Animation de la barre de progression
        animateProgressBar();

        // 6. Redirection après le délai
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivityNetflix.this, LoginActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }, SPLASH_DURATION);
    }

    private void animateTruck() {
        // Le camion se déplace de droite à gauche (X: de +200 à 0)
        ObjectAnimator truckAnimator = ObjectAnimator.ofFloat(truckEmoji, "translationX", 200f, 0f);
        truckAnimator.setDuration(SPLASH_DURATION);
        truckAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        truckAnimator.start();

        // Effet de rebond du camion
        truckEmoji.animate()
                .scaleX(1.1f)
                .scaleY(1.1f)
                .setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    truckEmoji.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(300)
                            .start();
                })
                .start();
    }

    private void animateProgressBar() {
        // Largeur cible : 220dp (convertir en pixels)
        int targetWidth = (int) (220 * getResources().getDisplayMetrics().density);

        ValueAnimator widthAnimator = ValueAnimator.ofInt(0, targetWidth);
        widthAnimator.setDuration(SPLASH_DURATION);
        widthAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        widthAnimator.addUpdateListener(animation -> {
            int currentWidth = (int) animation.getAnimatedValue();
            progressLine.getLayoutParams().width = currentWidth;
            progressLine.requestLayout();

            // Mettre à jour le pourcentage
            int percentage = (currentWidth * 100) / targetWidth;
            tvPercentage.setText(percentage + "%");
        });
        widthAnimator.start();
    }

    // CORRECTION : Version avec effet de poussière (optionnel)
    private void addDustEffect() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // CORRECTION : Utiliser SplashActivityNetflix.this au lieu de this
                TextView dust = new TextView(SplashActivityNetflix.this);
                dust.setText("💨");
                dust.setTextSize(24);
                dust.setAlpha(0.7f);
                // Ajouter à la vue parente
                LinearLayout container = findViewById(R.id.animation_container);
                if (container != null) {
                    container.addView(dust);
                    // Animer la poussière
                    dust.animate()
                            .translationX(-100f)
                            .alpha(0f)
                            .setDuration(500)
                            .withEndAction(() -> {
                                if (dust.getParent() != null) {
                                    container.removeView(dust);
                                }
                            })
                            .start();
                }
            }
        }, 500);
    }
}