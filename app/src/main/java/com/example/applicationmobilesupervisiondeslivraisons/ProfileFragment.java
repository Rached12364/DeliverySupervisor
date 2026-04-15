package com.example.applicationmobilesupervisiondeslivraisons;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private TextInputEditText editNom, editPrenom, editEmail, editPhone;
    private MaterialButton btnUpdateProfile;
    private MaterialButton btnLogout;
    private TextView tvProfileName, tvProfileRole;
    private CircleImageView profileImage;
    private DatabaseHelper databaseHelper;
    private SharedPreferences sharedPreferences;
    private int userId;
    private String userRole;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        initViews(view);
        loadUserData();
        setupListeners();

        return view;
    }

    private void initViews(View view) {
        editNom = view.findViewById(R.id.edit_nom);
        editPrenom = view.findViewById(R.id.edit_prenom);
        editEmail = view.findViewById(R.id.edit_email);
        editPhone = view.findViewById(R.id.edit_phone);
        btnUpdateProfile = view.findViewById(R.id.btn_update_profile);
        btnLogout = view.findViewById(R.id.btn_logout);
        tvProfileName = view.findViewById(R.id.tv_profile_name);
        tvProfileRole = view.findViewById(R.id.tv_profile_role);
        profileImage = view.findViewById(R.id.profile_image);

        databaseHelper = new DatabaseHelper(requireContext());

        // Correction : utiliser requireActivity().getSharedPreferences avec MODE_PRIVATE
        // MODE_PRIVATE est une constante de Context, donc accessible directement
        sharedPreferences = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        // OU plus simplement :
        // sharedPreferences = requireActivity().getSharedPreferences("UserSession", 0);

        userId = sharedPreferences.getInt("userId", -1);
        userRole = sharedPreferences.getString("role", "");
    }

    private void loadUserData() {
        if (userId != -1) {
            Cursor cursor = databaseHelper.getPersonnelById(userId);
            if (cursor != null && cursor.moveToFirst()) {
                try {
                    String nom = cursor.getString(cursor.getColumnIndexOrThrow(DbContract.Personnel.COLUMN_NOMPERS));
                    String prenom = cursor.getString(cursor.getColumnIndexOrThrow(DbContract.Personnel.COLUMN_PRENOMPERS));
                    String telephone = cursor.getString(cursor.getColumnIndexOrThrow(DbContract.Personnel.COLUMN_TELPERS));

                    editNom.setText(nom);
                    editPrenom.setText(prenom);
                    editPhone.setText(telephone);

                    if (tvProfileName != null) {
                        tvProfileName.setText(prenom + " " + nom);
                    }
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } finally {
                    cursor.close();
                }
            }
        }

        if (tvProfileRole != null) {
            if ("controleur".equals(userRole)) {
                tvProfileRole.setText("👤 Contrôleur");
            } else if ("livreur".equals(userRole)) {
                tvProfileRole.setText("🚚 Livreur");
            }
        }
    }

    private void setupListeners() {
        btnUpdateProfile.setOnClickListener(v -> updateProfile());
        btnLogout.setOnClickListener(v -> logout());
    }

    private void updateProfile() {
        String nom = editNom.getText().toString().trim();
        String prenom = editPrenom.getText().toString().trim();
        String telephone = editPhone.getText().toString().trim();

        if (nom.isEmpty() || prenom.isEmpty()) {
            Toast.makeText(requireContext(), "Veuillez remplir le nom et prénom", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean updated = databaseHelper.updatePersonnel(userId, nom, prenom, "", telephone);

        if (updated) {
            Toast.makeText(requireContext(), "Profil mis à jour avec succès", Toast.LENGTH_SHORT).show();
            if (tvProfileName != null) {
                tvProfileName.setText(prenom + " " + nom);
            }
        } else {
            Toast.makeText(requireContext(), "Erreur lors de la mise à jour", Toast.LENGTH_SHORT).show();
        }
    }

    private void logout() {
        // Effacer la session
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        // Rediriger vers LoginActivity
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}