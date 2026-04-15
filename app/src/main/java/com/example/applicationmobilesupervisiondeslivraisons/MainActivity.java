package com.example.applicationmobilesupervisiondeslivraisons;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;

public class MainActivity extends AppCompatActivity {

    private MaterialCardView cardController, cardDeliveryman;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cardController = findViewById(R.id.card_controller);
        cardDeliveryman = findViewById(R.id.card_deliveryman);

        // Fade-in animation
        View root = findViewById(android.R.id.content).getRootView();
        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        root.startAnimation(fadeIn);

        // Set touch listener for both cards
        setTouchAnimation(cardController);
        setTouchAnimation(cardDeliveryman);

        cardController.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ActivityLoginController.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });

        cardDeliveryman.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ActivityLoginLivreur.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });
    }

    private void setTouchAnimation(final View view) {
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, android.view.MotionEvent event) {
                switch (event.getAction()) {
                    case android.view.MotionEvent.ACTION_DOWN:
                        // Scale down
                        ScaleAnimation scaleDown = new ScaleAnimation(1.0f, 0.95f, 1.0f, 0.95f,
                                Animation.RELATIVE_TO_SELF, 0.5f,
                                Animation.RELATIVE_TO_SELF, 0.5f);
                        scaleDown.setDuration(100);
                        scaleDown.setFillAfter(true);
                        v.startAnimation(scaleDown);
                        break;
                    case android.view.MotionEvent.ACTION_UP:
                    case android.view.MotionEvent.ACTION_CANCEL:
                        // Scale back
                        ScaleAnimation scaleUp = new ScaleAnimation(0.95f, 1.0f, 0.95f, 1.0f,
                                Animation.RELATIVE_TO_SELF, 0.5f,
                                Animation.RELATIVE_TO_SELF, 0.5f);
                        scaleUp.setDuration(100);
                        scaleUp.setFillAfter(false);
                        v.startAnimation(scaleUp);
                        break;
                }
                return false;
            }
        });
    }
}