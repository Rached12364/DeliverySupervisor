package com.example.applicationmobilesupervisiondeslivraisons;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class ChangePasswordFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private int livreurId;

    private TextInputEditText editCurrentPassword, editNewPassword, editConfirmPassword;
    private MaterialButton btnUpdatePassword, btnCancel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_change_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = new DatabaseHelper(requireContext());

        if (getArguments() != null) {
            livreurId = getArguments().getInt("livreur_id", -1);
        }

        editCurrentPassword = view.findViewById(R.id.edit_current_password);
        editNewPassword = view.findViewById(R.id.edit_new_password);
        editConfirmPassword = view.findViewById(R.id.edit_confirm_password);
        btnUpdatePassword = view.findViewById(R.id.btn_update_password);
        btnCancel = view.findViewById(R.id.btn_cancel);

        btnUpdatePassword.setOnClickListener(v -> changePassword());
        btnCancel.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
    }

    private void changePassword() {
        String currentPwd = editCurrentPassword.getText().toString().trim();
        String newPwd = editNewPassword.getText().toString().trim();
        String confirmPwd = editConfirmPassword.getText().toString().trim();

        if (currentPwd.isEmpty() || newPwd.isEmpty() || confirmPwd.isEmpty()) {
            Toast.makeText(requireContext(), "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPwd.equals(confirmPwd)) {
            Toast.makeText(requireContext(), "Les nouveaux mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPwd.length() < 4) {
            Toast.makeText(requireContext(), "Le mot de passe doit contenir au moins 4 caractères", Toast.LENGTH_SHORT).show();
            return;
        }

        // Vérifier l'ancien mot de passe
        Cursor cursor = dbHelper.getPersonnelById(livreurId);
        if (cursor != null && cursor.moveToFirst()) {
            String dbPassword = cursor.getString(cursor.getColumnIndexOrThrow(DbContract.Personnel.COLUMN_MOTP));

            if (!dbPassword.equals(currentPwd)) {
                Toast.makeText(requireContext(), "Mot de passe actuel incorrect", Toast.LENGTH_SHORT).show();
                cursor.close();
                return;
            }
            cursor.close();
        }

        boolean updated = dbHelper.updatePassword(livreurId, newPwd);
        if (updated) {
            Toast.makeText(requireContext(), "Mot de passe mis à jour avec succès", Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager().popBackStack();
        } else {
            Toast.makeText(requireContext(), "Erreur lors de la mise à jour", Toast.LENGTH_SHORT).show();
        }
    }
}