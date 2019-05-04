package com.unison.appartment;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.unison.appartment.model.Member;

public class CreateMemberActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_member);

        final Intent i = getIntent();
        final String origin = i.getStringExtra("origin");

        final EditText inputEmail = findViewById(R.id.activity_create_member_input_email_value);
        final EditText inputName = findViewById(R.id.activity_create_member_input_name_value);
        final EditText inputAge = findViewById(R.id.activity_create_member_input_age_value);
        final RadioGroup inputGender = findViewById(R.id.activity_create_member_radio_gender);
        final RadioGroup inputRole = findViewById(R.id.activity_create_member_radio_role);

        // Gestione click sul bottone per aggiungere un nuovo membro
        FloatingActionButton floatNewMember = findViewById(R.id.activity_create_member_float_new_member);
        floatNewMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(CreateMemberActivity.this, CreateMemberActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });

        // Gestione click sul bottone per completare l'inserimento
        FloatingActionButton floatFinish = findViewById(R.id.activity_create_member_float_finish);
        floatFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Recupero i valori dei campi della form
                String email = inputEmail.getText().toString();
                String name = inputName.getText().toString();
                int age = Integer.parseInt(inputAge.getText().toString());
                RadioButton selectedGender = findViewById(inputGender.getCheckedRadioButtonId());
                String gender = selectedGender.getText().toString();
                RadioButton selectedRole = findViewById(inputRole.getCheckedRadioButtonId());
                String role = selectedRole.getText().toString();
                // Creo l'oggetto membero
                Member newMember = new Member(name, email, age, gender, role, 0);

                // Effettuo la registrazione del nuovo membro
                registerMember(newMember);

                // In base all'activity da cui provengo andrò in activity differenti
                if(origin.equals("fromEnter")) {
                    Intent i = new Intent(CreateMemberActivity.this, MainActivity.class);
                    startActivity(i);
                }
                else if(origin.equals("fromFamily")){
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("newMember", newMember);
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                }
            }
        });
    }

    private void registerMember(Member newMember) {

    }
}
