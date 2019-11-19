package jackburt.me.uk.application;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();


    EditText emailText, passwordText;
    Button loginButton;
    Button forgotPassword, signupLink;
    SignInButton googleLogin;
    LoginButton facebookLogin;
    Switch darkMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//will hide the title
        getSupportActionBar().hide(); //hide the title bar

        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.DarkTheme);
        } else {
            setTheme(R.style.LightTheme);
        }

        FacebookSdk.fullyInitialize();
        setContentView(R.layout.activity_login);




        emailText = findViewById(R.id.input_email);
        passwordText = findViewById(R.id.input_password);
        loginButton = findViewById(R.id.btn_login);
        forgotPassword = findViewById(R.id.forgot_password);
        signupLink = findViewById(R.id.link_signup);
        facebookLogin = findViewById(R.id.login_facebook);
        googleLogin = findViewById(R.id.login_google);
        darkMode = findViewById(R.id.dark_mode);

        if(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            darkMode.setChecked(true);
        } else {
            darkMode.setChecked(false);
        }

        googleLogin.setColorScheme(SignInButton.COLOR_DARK);

        loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                forgotPassword();
            }
        });

        darkMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton compoundButton, boolean isChecked) {

                if(!darkMode.isPressed()) {
                    return;
                }

                String ableType;

                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                final boolean enabled = (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES);
                if(enabled) {
                    ableType = "disable";
                } else {
                    ableType = "enable";
                }
                builder.setTitle("Dark Mode");
                builder.setMessage("Would you like to " +ableType+ " dark mode?");
                builder.setCancelable(true);
                builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        if(enabled) {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        } else {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        }


                        dialogInterface.dismiss();
                        finish();
                        startActivity(new Intent(LoginActivity.this, LoginActivity.this.getClass()));

                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        darkMode.setChecked(enabled);
                        dialogInterface.dismiss();

                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
            }
        });


        signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity
//                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
//                startActivityForResult(intent, REQUEST_SIGNUP);

                Toast.makeText(getBaseContext(), "Not yet!", Toast.LENGTH_LONG).show();

            }
        });
    }

    public void login() {
        Log.d(TAG, "Login");

        if (!validate()) {
            onLoginFailed();
            return;
        }

        loginButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        // TODO: Implement your own authentication logic here.

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    onLoginSuccess();
                    progressDialog.dismiss();
                    //do something with user
                } else {
                    onLoginFailed();
                    progressDialog.dismiss();
                }
            }
        });
    }

    public void forgotPassword() {

        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setTitle("Forgot Password");
        builder.setMessage("Please enter the email for the account you are trying to access.");
        builder.setCancelable(true);

        final EditText emailForgot = new EditText(LoginActivity.this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        emailForgot.setLayoutParams(layoutParams);

        builder.setPositiveButton("Confirm", null);
        builder.setNegativeButton(
                "Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        final AlertDialog alert = builder.create();

        alert.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button confirm = alert.getButton(DialogInterface.BUTTON_POSITIVE);
                confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        FirebaseAuth.getInstance().sendPasswordResetEmail(emailForgot.getText().toString())
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d(TAG, "Email sent.");
                                            Toast.makeText(getBaseContext(), "Reset email sent!", Toast.LENGTH_LONG).show();
                                            alert.dismiss();

                                        } else {
                                            try {
                                                throw task.getException();
                                            } catch(FirebaseAuthInvalidUserException e) {
                                                emailForgot.setError("Sorry, we don't have an account with this email.");
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                emailForgot.setError("Something went wrong!");

                                            }
                                        }
                                    }
                                });
                    }
                });
            }
        });

        alert.setView(emailForgot, 50, 0, 50, 0);
        alert.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {

                // TODO: Implement successful signup logic here
                // By default we just finish the Activity and log them in automatically

                this.finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginSuccess() {
        loginButton.setEnabled(true);
        FirebaseUser user = mAuth.getCurrentUser();
        finish();
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();
        loginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.setError("Enter a valid email address");
            valid = false;
        } else {
            emailText.setError(null);
        }

        if (!password.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{4,16}$")) {
            passwordText.setError("Must be between 4 and 16 characters, and include at least one letter and one number");
            valid = false;
        } else {
            passwordText.setError(null);
        }

        return valid;
    }
}
