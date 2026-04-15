package com.example.applicationmobilesupervisiondeslivraisons;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;

    private DatabaseHelper dbHelper;
    private int livreurId;
    private String currentPhotoPath;

    // Views
    private CircleImageView profileImage;
    private TextInputEditText editNom, editPrenom, editAdresse, editTelephone;
    private SwitchCompat switchNotifications, switchUrgenceAlerts;
    private MaterialButton btnSaveProfile, btnChangePassword, btnLogout;
    private TextView tvChangePhoto, tvLivreurName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile_livreur, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = new DatabaseHelper(requireContext());

        if (getArguments() != null) {
            livreurId = getArguments().getInt("livreur_id", -1);
        }

        initViews(view);
        loadUserData();
        loadPreferences();
        setupListeners();
    }

    private void initViews(View view) {
        profileImage = view.findViewById(R.id.profile_image);
        tvChangePhoto = view.findViewById(R.id.tv_change_photo);
        editNom = view.findViewById(R.id.edit_nom);
        editPrenom = view.findViewById(R.id.edit_prenom);
        editAdresse = view.findViewById(R.id.edit_adresse);
        editTelephone = view.findViewById(R.id.edit_telephone);
        switchNotifications = view.findViewById(R.id.switch_notifications);
        switchUrgenceAlerts = view.findViewById(R.id.switch_urgence_alerts);
        btnSaveProfile = view.findViewById(R.id.btn_save_profile);
        btnChangePassword = view.findViewById(R.id.btn_change_password);
        btnLogout = view.findViewById(R.id.btn_logout);
        tvLivreurName = view.findViewById(R.id.tv_livreur_name_header);
    }

    private void loadUserData() {
        Cursor cursor = dbHelper.getPersonnelById(livreurId);
        if (cursor != null && cursor.moveToFirst()) {
            String nom = cursor.getString(cursor.getColumnIndexOrThrow(DbContract.Personnel.COLUMN_NOMPERS));
            String prenom = cursor.getString(cursor.getColumnIndexOrThrow(DbContract.Personnel.COLUMN_PRENOMPERS));
            String adresse = cursor.getString(cursor.getColumnIndexOrThrow(DbContract.Personnel.COLUMN_ADRPERS));
            String telephone = cursor.getString(cursor.getColumnIndexOrThrow(DbContract.Personnel.COLUMN_TELPERS));

            editNom.setText(nom != null ? nom : "");
            editPrenom.setText(prenom != null ? prenom : "");
            editAdresse.setText(adresse != null ? adresse : "");
            editTelephone.setText(telephone != null ? telephone : "");
            tvLivreurName.setText((prenom != null ? prenom : "") + " " + (nom != null ? nom : ""));

            cursor.close();
        }

        // Charger la photo de profil si elle existe
        SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        currentPhotoPath = prefs.getString("profile_photo_" + livreurId, null);
        if (currentPhotoPath != null) {
            File imgFile = new File(currentPhotoPath);
            if (imgFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
                profileImage.setImageBitmap(bitmap);
            }
        }
    }

    private void loadPreferences() {
        SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);

        // Charger les notifications
        boolean notifications = prefs.getBoolean("notifications_" + livreurId, true);
        switchNotifications.setChecked(notifications);

        // Charger les alertes urgences
        boolean urgenceAlerts = prefs.getBoolean("urgence_alerts_" + livreurId, true);
        switchUrgenceAlerts.setChecked(urgenceAlerts);
    }

    private void setupListeners() {
        // Changer la photo de profil
        tvChangePhoto.setOnClickListener(v -> openGallery());

        // Sauvegarder le profil
        btnSaveProfile.setOnClickListener(v -> saveProfile());

        // Changer le mot de passe
        btnChangePassword.setOnClickListener(v -> {
            if (getActivity() instanceof DeliverymanDashboardActivity) {
                ((DeliverymanDashboardActivity) getActivity()).loadChangePassword();
            }
        });

        // Déconnexion
        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ActivityLoginLivreur.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        // Switch notifications
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
            prefs.edit().putBoolean("notifications_" + livreurId, isChecked).apply();
            if (isChecked) {
                Toast.makeText(requireContext(), "Notifications activées", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Notifications désactivées", Toast.LENGTH_SHORT).show();
            }
        });

        // Switch alertes urgences
        switchUrgenceAlerts.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
            prefs.edit().putBoolean("urgence_alerts_" + livreurId, isChecked).apply();
        });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                profileImage.setImageBitmap(bitmap);

                // Sauvegarder l'image localement
                String fileName = "profile_" + livreurId + ".jpg";
                File file = new File(requireContext().getFilesDir(), fileName);
                FileOutputStream fos = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
                fos.close();
                currentPhotoPath = file.getAbsolutePath();

                SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                prefs.edit().putString("profile_photo_" + livreurId, currentPhotoPath).apply();

                Toast.makeText(requireContext(), "Photo mise à jour", Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(requireContext(), "Erreur lors du chargement de l'image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveProfile() {
        String nom = editNom.getText().toString().trim();
        String prenom = editPrenom.getText().toString().trim();
        String adresse = editAdresse.getText().toString().trim();
        String telephone = editTelephone.getText().toString().trim();

        if (nom.isEmpty() || prenom.isEmpty()) {
            Toast.makeText(requireContext(), "Nom et prénom sont obligatoires", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean updated = dbHelper.updatePersonnel(livreurId, nom, prenom, adresse, telephone);
        if (updated) {
            Toast.makeText(requireContext(), "Profil mis à jour", Toast.LENGTH_SHORT).show();
            tvLivreurName.setText(prenom + " " + nom);
        } else {
            Toast.makeText(requireContext(), "Erreur lors de la mise à jour", Toast.LENGTH_SHORT).show();
        }
    }
}