package br.sendlook.yeslap;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.crystal.crystalrangeseekbar.interfaces.OnRangeSeekbarChangeListener;
import com.crystal.crystalrangeseekbar.interfaces.OnSeekbarChangeListener;
import com.crystal.crystalrangeseekbar.interfaces.OnSeekbarFinalValueListener;
import com.crystal.crystalrangeseekbar.widgets.CrystalRangeSeekbar;
import com.crystal.crystalrangeseekbar.widgets.CrystalSeekbar;
import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import br.sendlook.yeslap.model.Utils;
import im.delight.android.location.SimpleLocation;

public class SettingsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseUser mUser;

    private ImageView ivGoToProfile;
    private ImageView ivGoToChat;
    private Button btnLocationUser;
    private Button btnGenderUser;
    private String genderUser = "female";
    private TextView tvAgeUser;
    private CrystalSeekbar rbAgeUser;

    private Button btnLocationSearch;
    private Button btnGenderSearch;
    private String genderSearch = "female";
    private TextView tvAgeSearch;
    private CrystalRangeSeekbar rbAgeSearch;
    private Button btnPrivacyPolicy;
    private Button btnTermsService;
    private Button btnLicense;
    private Button btnLogOut;
    private Button btnDeleteAccount;
    private Button btnEmailUser;
    private Button btnPasswordUser;

    private SimpleLocation location;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();

        ivGoToProfile = (ImageView) findViewById(R.id.imgGoToProfile);
        ivGoToChat = (ImageView) findViewById(R.id.imgGoToChat);
        btnLocationUser = (Button) findViewById(R.id.btnLocationUser);
        btnGenderUser = (Button) findViewById(R.id.btnGenderUser);
        tvAgeUser = (TextView) findViewById(R.id.tvRangeAgeUser);
        rbAgeUser = (CrystalSeekbar) findViewById(R.id.rangeAgeUser);
        btnEmailUser = (Button) findViewById(R.id.btnEmailUser);
        btnPasswordUser = (Button) findViewById(R.id.btnPasswordUser);

        btnLocationSearch = (Button) findViewById(R.id.btnLocationSearch);
        btnGenderSearch = (Button) findViewById(R.id.btnGenderSearch);
        tvAgeSearch = (TextView) findViewById(R.id.tvRangeAgeSearch);
        rbAgeSearch = (CrystalRangeSeekbar) findViewById(R.id.rangeAgeSearch);

        btnPrivacyPolicy = (Button) findViewById(R.id.btnPrivacyPolicy);
        btnTermsService = (Button) findViewById(R.id.btnTermsService);
        btnLicense = (Button) findViewById(R.id.btnLicenses);
        btnLogOut = (Button) findViewById(R.id.btnLogOut);
        btnDeleteAccount = (Button) findViewById(R.id.btnDeleteAccount);


        getCurrentLocation();
        getUserConfig();

        // click events
        ivGoToProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ivGoToChat.setOnClickListener(callActivity(ChatActivity.class));

        btnGenderUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (genderUser) {
                    case "male":

                        btnGenderUser.setCompoundDrawablesWithIntrinsicBounds(R.drawable.settings_icon_female, 0, 0, 0);
                        genderUser = "female";
                        btnGenderUser.setText(getString(R.string.female));

                        DatabaseReference database = FirebaseDatabase.getInstance().getReference().child(Utils.USERS).child(mAuth.getCurrentUser().getUid());
                        HashMap<String, Object> gender = new HashMap<>();
                        gender.put(Utils.GENDER_USER, genderUser);
                        database.updateChildren(gender).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Log.d("Gender Updated", String.format("Gender Updated: %s", genderUser));
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Utils.toastyError(getApplicationContext(), e.getMessage());
                            }
                        });

                        break;
                    case "female":

                        btnGenderUser.setCompoundDrawablesWithIntrinsicBounds(R.drawable.settings_icon_gay, 0, 0, 0);
                        genderUser = "gay";
                        btnGenderUser.setText(getString(R.string.gay));

                        DatabaseReference database1 = FirebaseDatabase.getInstance().getReference().child(Utils.USERS).child(mAuth.getCurrentUser().getUid());
                        HashMap<String, Object> gender1 = new HashMap<>();
                        gender1.put(Utils.GENDER_USER, genderUser);
                        database1.updateChildren(gender1).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Log.d("Gender Updated", String.format("Gender Updated: %s", genderUser));
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Utils.toastyError(getApplicationContext(), e.getMessage());
                            }
                        });

                        break;
                    case "gay":

                        btnGenderUser.setCompoundDrawablesWithIntrinsicBounds(R.drawable.settings_icon_male, 0, 0, 0);
                        genderUser = "male";
                        btnGenderUser.setText(getString(R.string.male));

                        DatabaseReference database2 = FirebaseDatabase.getInstance().getReference().child(Utils.USERS).child(mAuth.getCurrentUser().getUid());
                        HashMap<String, Object> gender2 = new HashMap<>();
                        gender2.put(Utils.GENDER_USER, genderUser);
                        database2.updateChildren(gender2).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Log.d("Gender Updated", String.format("Gender Updated: %s", genderUser));
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Utils.toastyError(getApplicationContext(), e.getMessage());
                            }
                        });

                        break;
                }
            }
        });

        rbAgeUser.setOnSeekbarChangeListener(new OnSeekbarChangeListener() {
            @Override
            public void valueChanged(Number minValue) {
                tvAgeUser.setText(String.valueOf(minValue));
            }
        });

        rbAgeUser.setOnSeekbarFinalValueListener(new OnSeekbarFinalValueListener() {
            @Override
            public void finalValue(final Number value) {
                DatabaseReference database = FirebaseDatabase.getInstance().getReference().child(Utils.USERS).child(mAuth.getCurrentUser().getUid());
                HashMap<String, Object> age = new HashMap<>();
                age.put(Utils.AGE_USER, String.valueOf(value));
                database.updateChildren(age).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("age updated", "Age: " + value);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Utils.toastyError(getApplicationContext(), e.getMessage());
                    }
                });
            }
        });

        btnPasswordUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(SettingsActivity.this)
                        .title(R.string.reset_password)
                        .content(R.string.reset_password_msg)
                        .positiveText(getString(R.string.confirm))
                        .negativeText(getString(R.string.cancels))
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                            }
                        })
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                mAuth.sendPasswordResetEmail(mAuth.getCurrentUser().getEmail()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Utils.toastySuccess(getApplicationContext(), getString(R.string.email_sent));
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Utils.toastyError(getApplicationContext(), e.getMessage());
                                    }
                                });
                            }
                        }).show();
            }
        });

        btnLocationSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // implementar
                Utils.toastyInfo(getApplicationContext(), "Buinding");
            }
        });

        btnGenderSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (genderSearch) {
                    case "male":
                        btnGenderSearch.setCompoundDrawablesWithIntrinsicBounds(R.drawable.settings_icon_female, 0, 0, 0);
                        genderSearch = "female";
                        btnGenderSearch.setText(getString(R.string.female));
                        break;
                    case "female":
                        btnGenderSearch.setCompoundDrawablesWithIntrinsicBounds(R.drawable.settings_icon_gay, 0, 0, 0);
                        genderSearch = "gay";
                        btnGenderSearch.setText(getString(R.string.gay));
                        break;
                    case "gay":
                        btnGenderSearch.setCompoundDrawablesWithIntrinsicBounds(R.drawable.settings_icon_male, 0, 0, 0);
                        genderSearch = "male";
                        btnGenderSearch.setText(getString(R.string.male));
                        break;
                }
            }
        });

        rbAgeSearch.setOnRangeSeekbarChangeListener(new OnRangeSeekbarChangeListener() {
            @Override
            public void valueChanged(Number minValue, Number maxValue) {
                tvAgeSearch.setText(String.format("%s - %s", String.valueOf(minValue), String.valueOf(maxValue)));
            }
        });


        btnPrivacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.toastyInfo(getApplicationContext(), "Privacy Policy");
            }
        });

        btnTermsService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.toastyInfo(getApplicationContext(), "Terms of Service");
            }
        });

        btnLicense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.toastyInfo(getApplicationContext(), "Licenses");
            }
        });

        btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setStatusOffline();
                mAuth.signOut();
                Intent intent = new Intent(SettingsActivity.this, SignInActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                Utils.toastyInfo(getApplicationContext(), "See you soon!");
                callActivity(UserProfileActivity.class);
            }
        });

        btnDeleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.toastyInfo(getApplicationContext(), "Delete Account");
                callActivity(UserProfileActivity.class);
            }
        });

    }

    private void checkIfGpsIsOn() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            new MaterialDialog.Builder(SettingsActivity.this)
                    .title("GPS OFF")
                    .content("You need to call your location so we can find you.")
                    .positiveText("Turn On")
                    .negativeText(getString(R.string.cancels))
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.dismiss();
                        }
                    })
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    }).show();
        }
    }

    private void getCurrentLocation() {
        location = new SimpleLocation(this);

        if (!location.hasLocationEnabled()) {
            SimpleLocation.openSettings(this);
        }

        DatabaseReference database = FirebaseDatabase.getInstance().getReference().child(Utils.USERS).child(mAuth.getCurrentUser().getUid());
        HashMap<String, Object> loc = new HashMap<>();
        loc.put(Utils.LATITUDE_USER, location.getLatitude());
        loc.put(Utils.LONGITUDE_USER, location.getLongitude());
        database.updateChildren(loc).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    try {
                        Geocoder geocoder = new Geocoder(SettingsActivity.this, Locale.getDefault());
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        btnLocationUser.setText(addresses.get(0).getLocality());
                    } catch (IOException e) {
                        Utils.toastyError(getApplicationContext(), e.getMessage());
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Utils.toastyError(getApplicationContext(), e.getMessage());
            }
        });

    }

    private void getUserConfig() {
        dialog = new ProgressDialog(SettingsActivity.this);
        dialog.setMessage(getString(R.string.loading));
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        final DatabaseReference database = FirebaseDatabase.getInstance().getReference().child(Utils.USERS).child(mAuth.getCurrentUser().getUid());
        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String age = dataSnapshot.child(Utils.AGE_USER).getValue(String.class);
                String gender = dataSnapshot.child(Utils.GENDER_USER).getValue(String.class);

                //AGE
                if (age != null) {
                    tvAgeUser.setText(String.valueOf(age));
                    rbAgeUser.setMinStartValue(Float.valueOf(age)).apply();
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                } else {
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                }

                //GENDER
                if (gender != null) {
                    switch (gender) {
                        case "male":
                            btnGenderUser.setCompoundDrawablesWithIntrinsicBounds(R.drawable.settings_icon_male, 0, R.drawable.settings_right_arrow, 0);
                            genderUser = gender;
                            btnGenderUser.setText(gender);
                            break;
                        case "female":
                            btnGenderUser.setCompoundDrawablesWithIntrinsicBounds(R.drawable.settings_icon_female, 0, R.drawable.settings_right_arrow, 0);
                            genderUser = gender;
                            btnGenderUser.setText(gender);
                            break;
                        case "gay":
                            btnGenderUser.setCompoundDrawablesWithIntrinsicBounds(R.drawable.settings_icon_gay, 0, R.drawable.settings_right_arrow, 0);
                            genderUser = gender;
                            btnGenderUser.setText(gender);
                            break;
                    }
                }

                btnEmailUser.setText(mAuth.getCurrentUser().getEmail());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mUser = mAuth.getCurrentUser();
        if (mUser != null) {
            checkIfGpsIsOn();
            location.beginUpdates();
            setStatusOnline();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mUser = mAuth.getCurrentUser();
        if (mUser != null) {
            setStatusOffline();
            location.endUpdates();
        }
    }

    private void setStatusOnline() {
        mDatabase = FirebaseDatabase.getInstance().getReference().child(Utils.USERS).child(mAuth.getCurrentUser().getUid());
        HashMap<String, Object> status = new HashMap<>();
        status.put("status", "online");
        mDatabase.updateChildren(status);
    }

    private void setStatusOffline() {
        mDatabase = FirebaseDatabase.getInstance().getReference().child(Utils.USERS).child(mAuth.getCurrentUser().getUid());
        HashMap<String, Object> status = new HashMap<>();
        status.put("status", "offline");
        mDatabase.updateChildren(status);
    }


    private View.OnClickListener callActivity(final Class<?> activityClass) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // save data
                startActivity(new Intent(SettingsActivity.this, activityClass));
            }
        };
    }

}
