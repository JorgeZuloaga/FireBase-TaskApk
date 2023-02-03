package com.example.pruebasfirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.PatternsCompat;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class AuthActivity extends AppCompatActivity {

    //DECLARACIÓN DE VARIABLES

    Button botonAcceder;
    Button googleBoton;
    TextView botonRegisro;
    private FirebaseAuth mAuth;
    EditText emailText, passText;
    private GoogleSignInClient mGoogleSignInClient;
    private static final String TAG = "GoogleActivity";
    private static final int RC_SIGN_IN = 9001;


    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

//-------------------------------------ESCONDEMOS LA ACTIONBAR------------------------------------//

        getSupportActionBar().hide();

//---------------------------------------INICIAMOS CAJAS DE TEXTO----------------------------------//

        emailText = findViewById(R.id.emailTextView);
        passText = findViewById(R.id.passwordTextView);

//-----------------------------------------GOOGLE----------------------------------------------//

        googleBoton = findViewById(R.id.buttonGoogle);
        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


//------------------------------BOTON DE LOGIN-----------------------------//

        botonAcceder = findViewById(R.id.loginButton);
        botonAcceder.setOnClickListener(view -> {

            String email = emailText.getText().toString();
            String password = passText.getText().toString();

            mAuth.signInWithEmailAndPassword(email, password)

                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Intent intent = new Intent(AuthActivity.this, HomeActivity.class);
                                startActivity(intent);
                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(AuthActivity.this, "Usuario no válido",
                                        Toast.LENGTH_LONG).show();

                            }
                        }
                    });

        });

        botonRegisro = findViewById(R.id.registroTextView);
        botonRegisro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//----------------------------CASTING A STRING DE LAS CAJAS DE TEXTO------------------------------//

                String email = emailText.getText().toString();
                String password = passText.getText().toString();

//-------------------------------------VALIDACIÓN FORMULARIO--------------------------------------//

                if (email.isEmpty()) {
                    emailText.setError("Campo vacío");
                } else if (password.isEmpty()) {
                    passText.setError("Campo vacío");
                } else if (password.contains(" ")) {
                    passText.setError("No admite espacios");
                } else if (password.length() < 6) {
                    passText.setError("La contraseña debe tener mínimo 6 caracteres");
                } else if (!PatternsCompat.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailText.setError("Escriba un correo válido");
                } else {

//---------------------------------CREAR USUARIO EN FIREBASE--------------------------------------//

                    mAuth.createUserWithEmailAndPassword(email, password)


                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Toast.makeText(AuthActivity.this, "Usuario Registrado", Toast.LENGTH_LONG).show();
                                        Intent intent = new Intent(AuthActivity.this, HomeActivity.class);
                                        startActivity(intent);

                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Toast.makeText(AuthActivity.this, "No se ha podido registrar",
                                                Toast.LENGTH_SHORT).show();

                                    }
                                }
                            });

                }
            }
        });
        googleBoton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });


    }
    //----------------------GOOGLE COMPRUEBA SI EL USUARIO YA EXISTE--------------------------//


    // [START signin]
    public void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    // [START onactivityresult]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
            }
        }
    }
    // [END onactivityresult]

    // [START auth_with_google]
    public void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        FirebaseUser user = mAuth.getCurrentUser();
                        irHome();
                        AuthActivity.this.finish();
                        updateUI(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        updateUI(null);
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        user = mAuth.getCurrentUser();
        if (user != null) {
            irHome();
        }

    }

    private void irHome() {
        Intent intent = new Intent(AuthActivity.this, HomeActivity.class);
        startActivity(intent);

    }


}