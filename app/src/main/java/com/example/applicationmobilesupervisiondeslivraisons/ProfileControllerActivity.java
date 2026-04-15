package com.example.applicationmobilesupervisiondeslivraisons;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileControllerActivity extends AppCompatActivity {

    private TextInputEditText editNom, editPrenom, editAdresse, editTelephone;
    private MaterialButton btnSaveProfile, btnChangePassword, btnLogout, btnBack;
    private SwitchCompat switchNotifications, switchUrgenceAlerts;
    private TextView tvProfileName, tvProfileRole, tvChangePhoto;
    private CircleImageView profileImage;
    private DatabaseHelper dbHelper;
    private int controllerId;
    private String controllerName;
    private Uri selectedImageUri = null;

    // Launcher pour sélectionner une image depuis la galerie
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        selectedImageUri = imageUri;
                        profileImage.setImageURI(imageUri);
                        saveImageToInternalStorage(imageUri);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_controller);

        dbHelper = new DatabaseHelper(this);

        // Récupérer les données du contrôleur
        if (getIntent() != null) {
            controllerId = getIntent().getIntExtra("controller_id", -1);
            controllerName = getIntent().getStringExtra("controller_name");
            if (controllerName == null) {
                controllerName = "Contrôleur";
            }
        }

        initViews();
        loadProfileData();
        setupListeners();
    }

    private void initViews() {
        editNom = findViewById(R.id.edit_nom);
        editPrenom = findViewById(R.id.edit_prenom);
        editAdresse = findViewById(R.id.edit_adresse);
        editTelephone = findViewById(R.id.edit_telephone);
        btnSaveProfile = findViewById(R.id.btn_save_profile);
        btnChangePassword = findViewById(R.id.btn_change_password);
        btnLogout = findViewById(R.id.btn_logout);
        btnBack = findViewById(R.id.btn_back);
        switchNotifications = findViewById(R.id.switch_notifications);
        switchUrgenceAlerts = findViewById(R.id.switch_urgence_alerts);
        tvProfileName = findViewById(R.id.tv_livreur_name_header);
        tvProfileRole = findViewById(R.id.tv_livreur_role);
        profileImage = findViewById(R.id.profile_image);
        tvChangePhoto = findViewById(R.id.tv_change_photo);
    }

    private void loadProfileData() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        String nom = prefs.getString("controller_nom", "");
        String prenom = prefs.getString("controller_prenom", "");
        String adresse = prefs.getString("controller_adresse", "");
        String telephone = prefs.getString("controller_telephone", "");
        boolean notifications = prefs.getBoolean("notifications", true);
        boolean urgenceAlerts = prefs.getBoolean("urgence_alerts", true);

        // Charger l'image de profil sauvegardée
        String imagePath = prefs.getString("profile_image_path", "");
        if (!imagePath.isEmpty()) {
            File imgFile = new File(imagePath);
            if (imgFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                if (bitmap != null) {
                    profileImage.setImageBitmap(bitmap);
                }
            }
        }

        editNom.setText(nom);
        editPrenom.setText(prenom);
        editAdresse.setText(adresse);
        editTelephone.setText(telephone);
        switchNotifications.setChecked(notifications);
        switchUrgenceAlerts.setChecked(urgenceAlerts);

        if (tvProfileName != null) {
            if (prenom.isEmpty() && nom.isEmpty()) {
                tvProfileName.setText(controllerName);
            } else {
                tvProfileName.setText(prenom + " " + nom);
            }
        }
        if (tvProfileRole != null) {
            tvProfileRole.setText("👤 Contrôleur");
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnSaveProfile.setOnClickListener(v -> saveProfile());

        btnChangePassword.setOnClickListener(v ->
                Toast.makeText(ProfileControllerActivity.this, "Fonctionnalité à venir", Toast.LENGTH_SHORT).show()
        );

        btnLogout.setOnClickListener(v -> logout());

        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) ->
                savePreference("notifications", isChecked)
        );

        switchUrgenceAlerts.setOnCheckedChangeListener((buttonView, isChecked) ->
                savePreference("urgence_alerts", isChecked)
        );

        // Ouvrir la galerie pour changer la photo
        tvChangePhoto.setOnClickListener(v -> openGallery());

        // Clic sur l'image aussi
        profileImage.setOnClickListener(v -> openGallery());
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void saveImageToInternalStorage(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            // Réduire la taille de l'image pour économiser de l'espace
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 300, 300, true);

            // Sauvegarder dans le stockage interne
            String filename = "profile_controller_" + controllerId + ".jpg";
            File directory = getFilesDir();
            File file = new File(directory, filename);

            FileOutputStream fos = new FileOutputStream(file);
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos);
            fos.close();

            // Sauvegarder le chemin dans SharedPreferences
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("profile_image_path", file.getAbsolutePath());
            editor.apply();

            Toast.makeText(this, "Photo de profil mise à jour", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erreur lors de la sauvegarde de l'image", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveProfile() {
        String nom = editNom.getText().toString().trim();
        String prenom = editPrenom.getText().toString().trim();
        String adresse = editAdresse.getText().toString().trim();
        String telephone = editTelephone.getText().toString().trim();

        if (nom.isEmpty() || prenom.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir le nom et prénom", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("controller_nom", nom);
        editor.putString("controller_prenom", prenom);
        editor.putString("controller_adresse", adresse);
        editor.putString("controller_telephone", telephone);
        editor.apply();

        if (tvProfileName != null) {
            tvProfileName.setText(prenom + " " + nom);
        }

        Toast.makeText(this, "Profil mis à jour", Toast.LENGTH_SHORT).show();
    }

    private void savePreference(String key, boolean value) {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(key, value);
        editor.apply();

        String message = key.equals("notifications") ? "Notifications" : "Alertes urgences";
        Toast.makeText(this, message + " " + (value ? "activées" : "désactivées"), Toast.LENGTH_SHORT).show();
    }

    private void logout() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(ProfileControllerActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}