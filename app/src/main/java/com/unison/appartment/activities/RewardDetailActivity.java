package com.unison.appartment.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.unison.appartment.R;
import com.unison.appartment.model.Home;
import com.unison.appartment.model.Reward;
import com.unison.appartment.state.Appartment;

import java.util.Locale;

/**
 * Classe che rappresenta l'Activity con il dettaglio del Reward
 */
public class RewardDetailActivity extends AppCompatActivity {

    // FIXME poi probabilmente con la lettura dal db questo diventerà un REWARD_ID o REWARD_NAME
    public final static String EXTRA_REWARD_OBJECT = "rewardObject";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reward_detail);

        /*
        Impostazione del comportamento della freccia presente sulla toolbar
        (alla pressione l'activity viene terminata).
         */
        Toolbar toolbar = findViewById(R.id.activity_reward_detail_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        /*
        Impostazione del contenuto delle TextView in base al premio per il quale
        è costruita l'activity.
         */
        Intent creationIntent = getIntent();
        Reward reward = (Reward) creationIntent.getSerializableExtra(EXTRA_REWARD_OBJECT);

        TextView textName = findViewById(R.id.activity_reward_detail_text_name);
        TextView textDescription = findViewById(R.id.activity_reward_detail_text_description_value);
        TextView textPoints = findViewById(R.id.activity_reward_detail_text_points_value);

        textName.setText(reward.getName());
        textPoints.setText(String.format(Locale.getDefault(), "%d", reward.getPoints()));
        // Viene gestito il caso in cui la descrizione sia vuota
        String shownDescription = reward.getDescription();
        if (shownDescription == null || shownDescription.isEmpty()) {
            shownDescription = getString(R.string.general_no_description_available);
            textDescription.setTypeface(null, Typeface.ITALIC);
        }
        textDescription.setText(shownDescription);

        if (reward.isRequested()) {
            TextView textReservationTitle = findViewById(R.id.activity_reward_detail_text_reservation_title);
            TextView textReservation = findViewById(R.id.activity_reward_detail_text_reservation_value);
            textReservationTitle.setVisibility(View.VISIBLE);
            textReservation.setVisibility(View.VISIBLE);
            // FIXME cambiare quando si decide cosa mettere nel campo reservation
            textReservation.setText(reward.getReservation());
        }

        MaterialButton btnReserve = findViewById(R.id.activity_reward_detail_btn_reserve);
        btnReserve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestReward();
            }
        });

        if (Appartment.getInstance().getUserHome().getRole() == Home.ROLE_SLAVE) {
            /*
            Se l'utente è uno slave, l'unico bottone che viene visualizzato è quello per richiedere il
            premio (disabilitato se il premio se è già stato richiesto).
             */
            if (reward.isRequested()) {
                btnReserve.setEnabled(false);
                if (true) { // FIXME questo if deve controllare: se sono stato io ad aver prenotato il premio
                    btnReserve.setText(getString(R.string.activity_reward_detail_btn_reserve_reward_requested));
                }
                else {
                    btnReserve.setText(getString(R.string.activity_reward_detail_btn_reserve_reward_unavailable));
                }
            }
        }
        else {
            /*
            Se l'utente è un master o un owner:
            - viene visualizzato il bottone per l'eliminazione del premio;
            - se il premio è stato richiesto da uno slave, vengono visualizzati i bottoni per confermare
              o rifiutare la richiesta di premio;
            - se il premio è ancora disponibile, viene modificato il testo del bottone di richiesta.
             */
            MaterialButton btnConfirm = findViewById(R.id.activity_reward_detail_btn_confirm_reservation);
            MaterialButton btnCancel = findViewById(R.id.activity_reward_detail_btn_cancel_reservation);
            MaterialButton btnDelete = findViewById(R.id.activity_reward_detail_btn_delete);

            btnDelete.setVisibility(View.VISIBLE);
            if (reward.isRequested()) {
                btnReserve.setVisibility(View.GONE);
                btnConfirm.setVisibility(View.VISIBLE);
                btnCancel.setVisibility(View.VISIBLE);
            }
            else {
                btnReserve.setText(getString(R.string.activity_reward_detail_btn_reserve_reward_available_masters));
            }
        }
    }

    private void requestReward() {

    }
}
