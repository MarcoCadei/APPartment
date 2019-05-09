package com.unison.appartment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.unison.appartment.model.UserHome;

public class HomeListActivity extends AppCompatActivity implements HomeFragment.OnHomeListFragmentInteractionListener {

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_list);

        // Supporto per la toolbar
        toolbar = findViewById(R.id.activity_home_list_toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_home_list_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO da implementare, ora solo logout per poter fare il logout
        switch (item.getItemId()) {
            case R.id.activity_home_list_toolbar_logout:
                FirebaseAuth.getInstance().signOut();
                Intent i = new Intent(this, EnterActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Lascia il backstack inalterato, ma mette tutte le attività in background, esattamente
     * come se l'utente avesse premuto il bottone home
     * 2° RISPOSTA SU:
     * https://stackoverflow.com/questions/8631095/how-to-prevent-going-back-to-the-previous-activity
     */
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    public void onHomeListFragmentInteraction(UserHome item) {
        // TODO andare alla main activity della casa selezionata

        // (righe qui sotto solo temporanee, poi rifare meglio)
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra("homeName", item.getHomeName());
        startActivity(i);
    }
}
