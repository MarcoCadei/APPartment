package com.unison.appartment.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.unison.appartment.R;
import com.unison.appartment.model.Task;
import com.unison.appartment.utils.DateUtils;

import java.text.ParseException;
import java.util.Calendar;

/**
 * Classe che rappresenta l'Activity con il dettaglio del Task
 */
public class TaskDetailActivity extends AppCompatActivity {

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);
        // Supporto per la toolbar
        toolbar = findViewById(R.id.activity_task_detail_toolbar);
        setSupportActionBar(toolbar);
        // Gestione del click della freccia indietro presente sulla toolbar
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Recupero il riferimento agli elementi dell'interfaccia
        TextView name = findViewById(R.id.activity_task_detail_name);
        TextView points = findViewById(R.id.activity_task_detail_points_value);
        TextView description = findViewById(R.id.activity_task_detail_text_description_value);
        TextView creationDate = findViewById(R.id.activity_task_detail_text_creation_date_value);

        Intent i = getIntent();
        Task task = (Task) i.getSerializableExtra("task");
        // Popolo l'interfaccia con i dati del task ricevuto
        name.setText(task.getName());
        points.setText(String.valueOf(task.getPoints()));
        description.setText(task.getDescription());

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis((-1) * task.getCreationDate());
        creationDate.setText(DateUtils.formatDateWithCurrentDefaultLocale(calendar.getTime()));
    }
}
