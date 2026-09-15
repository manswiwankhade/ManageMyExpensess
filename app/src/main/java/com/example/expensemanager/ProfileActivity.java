package com.example.expensemanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    // =========================================================
    // VIEWS
    // =========================================================

    TextView txtBack;
    TextView txtProfileName;
    TextView txtProfileEmail;
    TextView txtEmail;
    TextView txtPhone;

    EditText edtName;

    ImageView imgProfile;
    TextView btnChangePhoto;

    Button btnSaveProfile;
    Button btnLogout;

    Switch switchNotifications;
    Switch switchDarkMode;

    LinearLayout layoutChangePassword;


    // =========================================================
    // FIREBASE
    // =========================================================

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;
    FirebaseUser currentUser;


    // =========================================================
    // LOCAL STORAGE
    // =========================================================

    SharedPreferences preferences;


    // =========================================================
    // IMAGE PICKER
    // =========================================================

    ActivityResultLauncher<String> imagePickerLauncher;


    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile);


        // =====================================================
        // CONNECT XML
        // =====================================================

        txtBack = findViewById(R.id.txtBack);

        txtProfileName = findViewById(R.id.txtProfileName);
        txtProfileEmail = findViewById(R.id.txtProfileEmail);

        edtName = findViewById(R.id.edtName);

        txtEmail = findViewById(R.id.txtEmail);
        txtPhone = findViewById(R.id.txtPhone);

        imgProfile = findViewById(R.id.imgProfile);
        btnChangePhoto = findViewById(R.id.btnChangePhoto);

        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnLogout = findViewById(R.id.btnLogout);

        switchNotifications =
                findViewById(R.id.switchNotifications);

        switchDarkMode =
                findViewById(R.id.switchDarkMode);

        layoutChangePassword =
                findViewById(R.id.layoutChangePassword);


        // =====================================================
        // FIREBASE
        // =====================================================

        firebaseAuth = FirebaseAuth.getInstance();

        firestore = FirebaseFirestore.getInstance();

        currentUser = firebaseAuth.getCurrentUser();


        // =====================================================
        // SHARED PREFERENCES
        // =====================================================

        preferences =
                getSharedPreferences(
                        "UserData",
                        MODE_PRIVATE
                );


        // =====================================================
        // IMAGE PICKER
        // =====================================================

        imagePickerLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.GetContent(),
                        uri -> {

                            if (uri != null) {

                                saveProfileImage(uri);
                            }
                        }
                );


        // =====================================================
        // LOAD PROFILE
        // =====================================================

        loadProfile();


        // =====================================================
        // LOAD SETTINGS
        // =====================================================

        boolean notifications =
                preferences.getBoolean(
                        "notifications",
                        true
                );

        switchNotifications.setChecked(
                notifications
        );


        boolean darkMode =
                preferences.getBoolean(
                        "darkMode",
                        false
                );

        switchDarkMode.setChecked(
                darkMode
        );


        // =====================================================
        // BACK
        // =====================================================

        txtBack.setOnClickListener(v -> {

            finish();

        });


        // =====================================================
        // PROFILE PHOTO
        // =====================================================

        btnChangePhoto.setOnClickListener(v -> {

            openImagePicker();

        });


        imgProfile.setOnClickListener(v -> {

            openImagePicker();

        });


        // =====================================================
        // SAVE PROFILE
        // =====================================================

        btnSaveProfile.setOnClickListener(v -> {

            saveProfile();

        });


        // =====================================================
        // CHANGE PASSWORD
        // =====================================================

        layoutChangePassword.setOnClickListener(v -> {

            showChangePasswordDialog();

        });


        // =====================================================
        // NOTIFICATIONS
        // =====================================================

        switchNotifications.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    preferences
                            .edit()
                            .putBoolean(
                                    "notifications",
                                    isChecked
                            )
                            .apply();

                    if (isChecked) {

                        Toast.makeText(
                                ProfileActivity.this,
                                "Notifications enabled",
                                Toast.LENGTH_SHORT
                        ).show();

                    } else {

                        Toast.makeText(
                                ProfileActivity.this,
                                "Notifications disabled",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );


        // =====================================================
        // DARK MODE
        // =====================================================

        switchDarkMode.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    preferences
                            .edit()
                            .putBoolean(
                                    "darkMode",
                                    isChecked
                            )
                            .apply();

                    if (isChecked) {

                        AppCompatDelegate
                                .setDefaultNightMode(
                                        AppCompatDelegate
                                                .MODE_NIGHT_YES
                                );

                    } else {

                        AppCompatDelegate
                                .setDefaultNightMode(
                                        AppCompatDelegate
                                                .MODE_NIGHT_NO
                                );
                    }
                }
        );


        // =====================================================
        // LOGOUT
        // =====================================================

        btnLogout.setOnClickListener(v -> {

            showLogoutDialog();

        });
    }


    // =========================================================
    // LOAD PROFILE
    // =========================================================

    private void loadProfile() {

        String savedName =
                preferences.getString(
                        "name",
                        ""
                );

        String savedPhone =
                preferences.getString(
                        "phone",
                        ""
                );


        // =====================================================
        // NAME
        // =====================================================

        if (currentUser != null &&
                currentUser.getDisplayName() != null &&
                !currentUser.getDisplayName().isEmpty()) {

            savedName =
                    currentUser.getDisplayName();
        }

        if (savedName.isEmpty()) {

            savedName = "User";
        }

        edtName.setText(savedName);

        txtProfileName.setText(savedName);


        // =====================================================
        // PHONE
        // =====================================================

        if (!savedPhone.isEmpty()) {

            txtPhone.setText(
                    "+91 " + savedPhone
            );

        } else {

            txtPhone.setText(
                    "Not available"
            );
        }


        // =====================================================
        // EMAIL
        // =====================================================

        String email = null;

        if (currentUser != null) {

            email = currentUser.getEmail();
        }

        if (email == null || email.isEmpty()) {

            email =
                    preferences.getString(
                            "email",
                            "Not available"
                    );
        }

        txtProfileEmail.setText(email);

        txtEmail.setText(email);


        // =====================================================
        // SAVE EMAIL LOCALLY
        // =====================================================

        if (email != null &&
                !email.equals("Not available")) {

            preferences
                    .edit()
                    .putString(
                            "email",
                            email
                    )
                    .apply();
        }


        // =====================================================
        // LOAD PROFILE IMAGE
        // =====================================================

        loadSavedProfileImage();
    }


    // =========================================================
    // SAVE PROFILE
    // =========================================================

    private void saveProfile() {

        String newName =
                edtName.getText()
                        .toString()
                        .trim();


        // =====================================================
        // VALIDATION
        // =====================================================

        if (newName.isEmpty()) {

            edtName.setError(
                    "Please enter your name"
            );

            edtName.requestFocus();

            return;
        }


        if (newName.length() < 2) {

            edtName.setError(
                    "Name is too short"
            );

            edtName.requestFocus();

            return;
        }


        // =====================================================
        // SAVE LOCALLY
        // =====================================================

        preferences
                .edit()
                .putString(
                        "name",
                        newName
                )
                .apply();


        // =====================================================
        // UPDATE SCREEN
        // =====================================================

        txtProfileName.setText(newName);


        // =====================================================
        // UPDATE FIREBASE PROFILE
        // =====================================================

        if (currentUser != null) {

            UserProfileChangeRequest profileUpdates =
                    new UserProfileChangeRequest.Builder()
                            .setDisplayName(newName)
                            .build();

            currentUser
                    .updateProfile(profileUpdates)
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {

                            Toast.makeText(
                                    ProfileActivity.this,
                                    "Profile updated successfully",
                                    Toast.LENGTH_SHORT
                            ).show();

                        } else {

                            Toast.makeText(
                                    ProfileActivity.this,
                                    "Name saved locally",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    });

        } else {

            Toast.makeText(
                    ProfileActivity.this,
                    "Profile saved",
                    Toast.LENGTH_SHORT
            ).show();
        }


        // =====================================================
        // SAVE NAME TO FIRESTORE
        // =====================================================

        if (currentUser != null) {

            String userId =
                    currentUser.getUid();

            Map<String, Object> userData =
                    new HashMap<>();

            userData.put(
                    "name",
                    newName
            );

            if (currentUser.getEmail() != null) {

                userData.put(
                        "email",
                        currentUser.getEmail()
                );
            }

            if (!preferences
                    .getString("phone", "")
                    .isEmpty()) {

                userData.put(
                        "phone",
                        preferences.getString(
                                "phone",
                                ""
                        )
                );
            }

            firestore
                    .collection("users")
                    .document(userId)
                    .set(
                            userData,
                            com.google.firebase.firestore
                                    .SetOptions.merge()
                    );
        }
    }


    // =========================================================
    // OPEN IMAGE PICKER
    // =========================================================

    private void openImagePicker() {

        imagePickerLauncher.launch("image/*");
    }


    // =========================================================
    // SAVE PROFILE IMAGE
    // =========================================================

    private void saveProfileImage(Uri uri) {

        try {

            InputStream inputStream =
                    getContentResolver()
                            .openInputStream(uri);

            if (inputStream == null) {

                Toast.makeText(
                        this,
                        "Unable to open image",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }


            // =================================================
            // INTERNAL APP STORAGE
            // =================================================

            File profileFile =
                    new File(
                            getFilesDir(),
                            "profile_image.jpg"
                    );


            FileOutputStream outputStream =
                    new FileOutputStream(
                            profileFile
                    );


            byte[] buffer =
                    new byte[8192];

            int length;

            while (
                    (length =
                            inputStream.read(buffer)) != -1
            ) {

                outputStream.write(
                        buffer,
                        0,
                        length
                );
            }


            outputStream.flush();

            outputStream.close();

            inputStream.close();


            // =================================================
            // SAVE PATH
            // =================================================

            preferences
                    .edit()
                    .putString(
                            "profileImagePath",
                            profileFile.getAbsolutePath()
                    )
                    .apply();


            // =================================================
            // DISPLAY IMAGE
            // =================================================

            Bitmap bitmap =
                    BitmapFactory.decodeFile(
                            profileFile.getAbsolutePath()
                    );

            if (bitmap != null) {

                imgProfile.setImageBitmap(bitmap);
            }


            Toast.makeText(
                    this,
                    "Profile picture updated",
                    Toast.LENGTH_SHORT
            ).show();


        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Unable to save profile picture",
                    Toast.LENGTH_LONG
            ).show();
        }
    }


    // =========================================================
    // LOAD PROFILE IMAGE
    // =========================================================

    private void loadSavedProfileImage() {

        String imagePath =
                preferences.getString(
                        "profileImagePath",
                        ""
                );


        if (!imagePath.isEmpty()) {

            File imageFile =
                    new File(imagePath);


            if (imageFile.exists()) {

                Bitmap bitmap =
                        BitmapFactory.decodeFile(
                                imageFile.getAbsolutePath()
                        );


                if (bitmap != null) {

                    imgProfile.setImageBitmap(bitmap);
                }
            }
        }
    }


    // =========================================================
    // CHANGE PASSWORD DIALOG
    // =========================================================

    private void showChangePasswordDialog() {

        if (currentUser == null) {

            Toast.makeText(
                    this,
                    "Please login again",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        String email =
                currentUser.getEmail();


        if (email == null ||
                email.isEmpty()) {

            Toast.makeText(
                    this,
                    "No email address found",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        AlertDialog dialog =
                new AlertDialog.Builder(this)

                        .setTitle(
                                "Change Password"
                        )

                        .setMessage(
                                "A password reset link will be sent to:\n\n"
                                        + email
                        )

                        .setNegativeButton(
                                "Cancel",
                                null
                        )

                        .setPositiveButton(
                                "Send Link",
                                null
                        )

                        .create();


        dialog.setOnShowListener(
                d -> {

                    dialog
                            .getButton(
                                    AlertDialog.BUTTON_POSITIVE
                            )
                            .setOnClickListener(v -> {

                                sendPasswordResetEmail(
                                        email,
                                        dialog
                                );

                            });
                }
        );


        dialog.show();
    }


    // =========================================================
    // SEND PASSWORD RESET EMAIL
    // =========================================================

    private void sendPasswordResetEmail(
            String email,
            AlertDialog dialog) {

        firebaseAuth
                .sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        dialog.dismiss();

                        Toast.makeText(
                                ProfileActivity.this,
                                "Password reset link sent to your email",
                                Toast.LENGTH_LONG
                        ).show();

                    } else {

                        String message =
                                "Unable to send reset email";

                        if (task.getException() != null) {

                            message =
                                    task.getException()
                                            .getMessage();
                        }

                        Toast.makeText(
                                ProfileActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }


    // =========================================================
    // LOGOUT DIALOG
    // =========================================================

    private void showLogoutDialog() {

        new AlertDialog.Builder(this)

                .setTitle(
                        "Logout"
                )

                .setMessage(
                        "Are you sure you want to logout?"
                )

                .setNegativeButton(
                        "Cancel",
                        null
                )

                .setPositiveButton(
                        "Logout",
                        (dialog, which) -> {

                            logoutUser();

                        }
                )

                .show();
    }


    // =========================================================
    // LOGOUT
    // =========================================================

    private void logoutUser() {

        firebaseAuth.signOut();


        // =====================================================
        // CLEAR TASK
        // =====================================================

        Intent intent =
                new Intent(
                        ProfileActivity.this,
                        LoginActivity.class
                );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );


        startActivity(intent);

        finish();
    }


    // =========================================================
    // BACK PRESSED
    // =========================================================

    @Override
    public void onBackPressed() {

        finish();

    }
}