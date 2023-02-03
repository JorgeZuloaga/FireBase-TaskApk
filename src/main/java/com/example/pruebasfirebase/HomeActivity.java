package com.example.pruebasfirebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    String emailUsuario;
    ListView listViewTareas;
    List<String> listaTareas = new ArrayList<>();
    List<String> listaIdTareas = new ArrayList<>();
    ArrayAdapter<String> mAdapterTareas;
    MediaPlayer trashSound;
    MediaPlayer okey;
    private GoogleSignInOptions gso;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


//--------------------------------------DECLARACIÓN VARIABLES-------------------------------------//

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        emailUsuario = mAuth.getCurrentUser().getEmail();
        listViewTareas = findViewById(R.id.todasLasTareas);
        trashSound = MediaPlayer.create(this, R.raw.trash);
        okey = MediaPlayer.create(this, R.raw.checked);

//-----------------------------------ACTUALIZAR LAS TAREAS----------------------------------------//

        actualizarUI();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

    }


    private void actualizarUI() {

        db.collection("Tareas")
                .whereEqualTo("emailUsuario", emailUsuario)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {

                            return;
                        }

                        listaTareas.clear();
                        listaIdTareas.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            listaIdTareas.add(doc.getId());
                            listaTareas.add(doc.getString("nombreTarea"));


                        }
                        if (listaTareas.size() == 0) {
                            listViewTareas.setAdapter(null);
                        } else {
                            mAdapterTareas = new ArrayAdapter<String>(HomeActivity.this, R.layout.tareas, R.id.tareaHecha, listaTareas);
                            listViewTareas.setAdapter(mAdapterTareas);

                        }
                    }

                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mas:
                final EditText taskEditText = new EditText(this);
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle("Nueva tarea")
                        .setMessage("Introduce una tarea")
                        .setView(taskEditText)
                        .setPositiveButton("Añadir", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

//------------------------------AÑADIMOS TAREA A LA BASE DE DATOS---------------------------------//

                                okey.start();
                                String miTarea = taskEditText.getText().toString();

                                Map<String, Object> tarea = new HashMap<>();
                                tarea.put("nombreTarea", miTarea);
                                tarea.put("emailUsuario", emailUsuario);

                                db.collection("Tareas").add(tarea);
                                Toast.makeText(HomeActivity.this, "Tarea añadida", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Cancelar", null)
                        .create();
                dialog.show();
                return true;
            case R.id.logut:

//---------------------CERRAMOS SESION EN FIREBASE Y VOLVEMOS AL LOGIN---------------------------//

                mAuth.signOut();
                mGoogleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Intent loginActivity = new Intent(getApplicationContext(), AuthActivity.class);
                            startActivity(loginActivity);
                            HomeActivity.this.finish();

                        } else {
                            Toast.makeText(getApplicationContext(), "Error al cerrar sesión"
                                    , Toast.LENGTH_LONG).show();
                        }
                    }
                });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

//-------------------------------------BORRADO DE TAREAS------------------------------------------//

    public void borrarTarea(View view) {
        View parent = (View) view.getParent();
        TextView tareaText = parent.findViewById(R.id.tareaHecha);
        String contenido = tareaText.getText().toString();
        trashSound.start();
        int posicion = listaTareas.indexOf(contenido);
        db.collection("Tareas").document(listaIdTareas.get(posicion)).delete();
        Toast.makeText(this, "Tarea completada", Toast.LENGTH_SHORT).show();


    }
//----------------------------------------EDITAR TAREAS-------------------------------------------//

    private String getTaskTextByView(View view) {
        View parent = (View) view.getParent();
        TextView task_name_view = parent.findViewById(R.id.tareaHecha);

        return task_name_view.getText().toString();

    }

    public void ediTask(View view) {
        EditText taskEditText = new EditText(this);
        final String older_name = this.getTaskTextByView(view);
        taskEditText.setText(older_name);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Edición de tarea")
                .setView(taskEditText)
                .setPositiveButton("Actualizar",
                        (dialogInterface, i) -> {
                            String miTarea = taskEditText.getText().toString();
                            int index = listaTareas.indexOf(this.getTaskTextByView(view));

                            Map<String, Object> tarea = new HashMap<>();
                            tarea.put("nombreTarea", miTarea);
                            tarea.put("emailUsuario", emailUsuario);

                            db.collection("Tareas").document(listaIdTareas.get(index))
                                    .set(tarea);
                            okey.start();
                            Toast.makeText(this, "Tarea modificada", Toast.LENGTH_SHORT).show();
                        })
                .setNegativeButton("Cancelar", null)
                .create();
        dialog.show();
    }
//------------------------------------MÉTODO LOGOUT-----------------------------------------------//
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mGoogleSignInClient.signOut();
        Intent intent = new Intent(HomeActivity.this,AuthActivity.class);
        startActivity(intent);
    }

}
