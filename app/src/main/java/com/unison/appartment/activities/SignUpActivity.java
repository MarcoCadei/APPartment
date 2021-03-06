package com.unison.appartment.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DatabaseError;
import com.unison.appartment.database.DatabaseReader;
import com.unison.appartment.database.DatabaseReaderListener;
import com.unison.appartment.database.FirebaseDatabaseReader;
import com.unison.appartment.model.UserHome;
import com.unison.appartment.state.Appartment;
import com.unison.appartment.database.Auth;
import com.unison.appartment.database.AuthListener;
import com.unison.appartment.database.DatabaseWriter;
import com.unison.appartment.database.DatabaseWriterListener;
import com.unison.appartment.database.FirebaseAuth;
import com.unison.appartment.database.FirebaseDatabaseWriter;
import com.unison.appartment.fragments.DatePickerFragment;
import com.unison.appartment.fragments.FirebaseProgressDialogFragment;
import com.unison.appartment.utils.DateUtils;
import com.unison.appartment.utils.KeyboardUtils;
import com.unison.appartment.R;
import com.unison.appartment.model.User;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;


/**
 * Classe che rappresenta l'Activity per effettuare la registrazione all'applicazione
 */
public class SignUpActivity extends FormActivity implements DatePickerDialog.OnDateSetListener {

    // Request code per aprire l'activity usata per caricare un'immagine
    private final static int RESULT_LOAD_IMAGE = 1;

    private final static String BUNDLE_KEY_DATE_OBJECT = "dateObject";
    private final static String BUNDLE_KEY_SELECTED_IMAGE = "selectedImage";
    private final static String BUNDLE_KEY_NEW_USER = "newUser";
    private final static String BUNDLE_KEY_OLD_USER = "oldUser";

    private final static int MIN_USER_PASSWORD_LENGTH = 6;
    private final static String PLACEHOLDER_CORRECT_PASSWORD = "passuord123";

    public final static String EXTRA_USER_DATA = "userData";

    private Date birthdate;
    private String selectedImage;
    // Utente che verrà creato in questa activity
    private User newUser;
    // Vecchio utente: serve nel caso l'activity venga aperta per la modifica
    private User oldUser;

    private Auth auth;
    private DatabaseWriter databaseWriter;
    private DatabaseReader databaseReader;

    private TextView txtTitle;
    private ImageView imgTitle;
    private EditText inputEmail;
    private EditText inputPassword;
    private EditText inputRepeatPassword;
    private EditText inputBirthdate;
    private EditText inputNickname;
    private TextInputLayout layoutEmail;
    private TextInputLayout layoutPassword;
    private TextInputLayout layoutRepeatPassword;
    private TextInputLayout layoutBirthdate;
    private TextInputLayout layoutNickname;
    private ImageView imgPhoto;
    private RadioGroup inputGender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Precondizione: Se viene acceduta questa activity, vuol dire che non c'è nessun utente loggato

        auth = new FirebaseAuth();
        databaseWriter = new FirebaseDatabaseWriter();
        databaseReader = new FirebaseDatabaseReader();

        txtTitle = findViewById(R.id.activity_signup_text_title);
        imgTitle = findViewById(R.id.activity_signup_img_title);
        inputEmail = findViewById(R.id.activity_signup_input_email_value);
        inputPassword = findViewById(R.id.activity_signup_input_password_value);
        inputRepeatPassword = findViewById(R.id.activity_signup_input_repeat_password_value);
        inputBirthdate = findViewById(R.id.activity_signup_input_birthdate_value);
        inputNickname = findViewById(R.id.activity_signup_input_nickname_value);
        inputGender = findViewById(R.id.activity_signup_radio_gender);
        layoutEmail = findViewById(R.id.activity_signup_input_email);
        layoutPassword = findViewById(R.id.activity_signup_input_password);
        layoutRepeatPassword = findViewById(R.id.activity_signup_input_repeat_password);
        layoutBirthdate = findViewById(R.id.activity_signup_input_birthdate);
        layoutNickname = findViewById(R.id.activity_signup_input_nickname);
        imgPhoto = findViewById(R.id.activity_signup_img_photo);
        FloatingActionButton floatFinish = findViewById(R.id.activity_signup_float_finish);

        Intent i = getIntent();
        oldUser = (User) i.getParcelableExtra(EXTRA_USER_DATA);
        if (oldUser != null) {
            // Imposto il titolo opportunamente se devo modificare e non creare un utente
            txtTitle.setText(R.string.activity_signup_title_edit);
            imgTitle.setImageDrawable(getDrawable(R.drawable.ic_person));

            /*
            Visualizzazione della toolbar (che normalmente in questa activity non è presente) ed
            impostazione del comportamento della freccia presente sulla toolbar
            (alla pressione l'activity viene terminata).
             */
            AppBarLayout appbar = findViewById(R.id.activity_signup_appbar);
            appbar.setVisibility(View.VISIBLE);
            Toolbar toolbar = findViewById(R.id.activity_signup_toolbar);
            setSupportActionBar(toolbar);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });

            /*
            Nota: I campi non visualizzati vengono riempiti ugualmente con il valore corretto per
            assicurare che i controlli sulla form eseguiti in checkInput diano esito positivo.
             */
            inputEmail.setText(oldUser.getEmail());
            layoutEmail.setVisibility(View.GONE);
            inputPassword.setText(PLACEHOLDER_CORRECT_PASSWORD);
            layoutPassword.setVisibility(View.GONE);
            inputRepeatPassword.setText(PLACEHOLDER_CORRECT_PASSWORD);
            layoutRepeatPassword.setVisibility(View.GONE);
            try {
                birthdate = DateUtils.parseDateWithStandardLocale(oldUser.getBirthdate());
                inputBirthdate.setText(DateUtils.formatDateWithCurrentDefaultLocale(birthdate));
            } catch (ParseException e) {
                /*
                Questa eccezione non si può mai verificare se si assume che nel database la data è
                sempre salvata nel formato corretto.
                 */
                Log.e(getClass().getCanonicalName(), "Formato data non corretto");
            }
            inputNickname.setText(oldUser.getName());
            if (oldUser.getImage() != null) {
                selectedImage = oldUser.getImage();
                Glide.with(imgPhoto.getContext()).load(selectedImage).apply(RequestOptions.circleCropTransform()).into(imgPhoto);
                imgPhoto.setVisibility(View.VISIBLE);
            }
            /*
                Il "+1" serve perchè gli indice dei bottoni del RadioGroup sono 1 e 2, non 0 e 1
                in quanto c'è anche il titolo al suo interno (che ha indice 0)
            */
            inputGender.check(inputGender.getChildAt(oldUser.getGender() + 1).getId());

            floatFinish.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    KeyboardUtils.hideKeyboard(SignUpActivity.this);
                    if (checkInput()) {
                        progressDialog = FirebaseProgressDialogFragment.newInstance(
                                getString(R.string.activity_signup_edit_title),
                                getString(R.string.activity_signup_edit_description));
                        progressDialog.show(getSupportFragmentManager(), FirebaseProgressDialogFragment.TAG_FIREBASE_PROGRESS_DIALOG);

                        /*
                        Qui si utilizza la stessa funzione createUser definita per la creazione del
                        nuovo oggetto User in fase di registrazione, che funziona come previsto in
                        quanto anche i campi nascosti all'utente sono popolati con il loro valore
                        corretto.
                         */
                        newUser = createUser();

                        databaseReader.retrieveUserHomes(auth.getCurrentUserUid(), dbUserHomesReaderListener);
                    }
                }
            });
        } else {
            floatFinish.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    KeyboardUtils.hideKeyboard(SignUpActivity.this);
                    if (checkInput()) {
                        progressDialog = FirebaseProgressDialogFragment.newInstance(
                                getString(R.string.activity_signup_signup_title),
                                getString(R.string.activity_signup_signup_description));
                        progressDialog.show(getSupportFragmentManager(), FirebaseProgressDialogFragment.TAG_FIREBASE_PROGRESS_DIALOG);

                        newUser = createUser();
                        // Salvataggio delle informazioni in Auth
                        auth.signUp(newUser, inputPassword.getText().toString(), authListener);
                    }
                }
            });
        }

        /*
        I listener sono duplicati anche se fanno la stessa cosa poiché la view che prende il focus
        è il textInput mentre la view su cui bisogna operare per rimuovere il messaggio di errore
        è il textLayout, e per qualche motivo invocando getParent sul textInput NON viene restituito
        il textLayout.
         */
        inputEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) resetErrorMessage(layoutEmail);
            }
        });
        inputPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) resetErrorMessage(layoutPassword);
            }
        });
        inputRepeatPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) resetErrorMessage(layoutRepeatPassword);
            }
        });
        inputNickname.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) resetErrorMessage(layoutNickname);
            }
        });

        inputBirthdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetErrorMessage(layoutBirthdate);
                showDatePickerDialog();
            }
        });

        MaterialButton btnPhoto = findViewById(R.id.activity_signup_btn_photo);
        btnPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"),
                        RESULT_LOAD_IMAGE);
            }
        });

        /*
        Se lo schermo è stato ruotato mentre il date picker era aperto, l'activity è stata distrutta
        e ora sta venendo ricreata. Si vuole mantenere aperto lo stesso date picker, a cui però
        deve essere cambiato il listener dal momento che altrimenti farebbe riferimento all'activity
        distrutta non più esistente.
         */
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(DatePickerFragment.TAG_DATE_PICKER);
        if (fragment != null) {
            ((DatePickerFragment) fragment).setListener(this);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putSerializable(BUNDLE_KEY_DATE_OBJECT, birthdate);
        outState.putString(BUNDLE_KEY_SELECTED_IMAGE, selectedImage);
        outState.putParcelable(BUNDLE_KEY_NEW_USER, newUser);
        outState.putParcelable(BUNDLE_KEY_OLD_USER, oldUser);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        birthdate = (Date) savedInstanceState.getSerializable(BUNDLE_KEY_DATE_OBJECT);
        selectedImage = savedInstanceState.getString(BUNDLE_KEY_SELECTED_IMAGE);
        newUser = savedInstanceState.getParcelable(BUNDLE_KEY_NEW_USER);
        oldUser = savedInstanceState.getParcelable(BUNDLE_KEY_OLD_USER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK && null != data) {
            selectedImage = data.getData().toString();
            Glide.with(imgPhoto.getContext()).load(selectedImage).apply(RequestOptions.circleCropTransform()).into(imgPhoto);
            imgPhoto.setVisibility(View.VISIBLE);
        }
    }

    protected boolean checkInput() {
        resetErrorMessage(layoutEmail);
        resetErrorMessage(layoutPassword);
        resetErrorMessage(layoutRepeatPassword);
        resetErrorMessage(layoutBirthdate);
        resetErrorMessage(layoutNickname);

        inputEmail.setText(inputEmail.getText().toString().trim());
        inputNickname.setText(inputNickname.getText().toString().trim());

        String emailValue = inputEmail.getText().toString();
        String passwordValue = inputPassword.getText().toString();
        String repeatPasswordValue = inputRepeatPassword.getText().toString();
        String birthdateValue = inputBirthdate.getText().toString();
        String nicknameValue = inputNickname.getText().toString();

        boolean result = true;

        // Controllo che la mail inserita sia valida
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailValue).matches()) {
            layoutEmail.setError(getString(R.string.form_error_incorrect_email));
            result = false;
        }

        // Controllo che le due password inserite coincidano
        if (!passwordValue.equals(repeatPasswordValue)) {
            layoutPassword.setError(getString(R.string.form_error_mismatch_password));
            layoutRepeatPassword.setError(getString(R.string.form_error_mismatch_password));
            result = false;
        }

        // Controllo che nei campi "password" e "ripeti password" sia stata specificata una
        // password avente il numero minimo di caratteri richiesto
        if (passwordValue.trim().length() < MIN_USER_PASSWORD_LENGTH) {
            layoutPassword.setError(null);
            layoutPassword.setError(String.format(getString(R.string.form_error_short_password), MIN_USER_PASSWORD_LENGTH));
            result = false;
        }
        if (repeatPasswordValue.trim().length() < MIN_USER_PASSWORD_LENGTH) {
            layoutRepeatPassword.setError(null);
            layoutRepeatPassword.setError(String.format(getString(R.string.form_error_short_password), MIN_USER_PASSWORD_LENGTH));
            result = false;
        }

        // Controllo che la data scelta non sia successiva alla data odierna
        Date today = new Date();
        if (birthdate != null) {
            if (birthdate.compareTo(today) > 0) {
                layoutBirthdate.setError(getString(R.string.form_error_invalid_date));
                result = false;
            }
        }

        // Controllo che tutti i campi siano compilati
        if (emailValue.trim().length() == 0) {
            /*
            Bisogna prima resettare l'errore eventualmente impostato nell'if precedente, altrimenti
            (per qualche motivo) il messaggio non è visualizzato correttamente.
             */
            layoutEmail.setError(null);
            layoutEmail.setError(getString(R.string.form_error_missing_value));
            result = false;
        }
        if (birthdateValue.length() == 0) {
            layoutBirthdate.setError(null);
            layoutBirthdate.setError(getString(R.string.form_error_missing_value));
            result = false;
        }
        if (nicknameValue.trim().length() == 0) {
            layoutNickname.setError(getString(R.string.form_error_missing_value));
            result = false;
        }

        return result;
    }

    private User createUser() {
        // Precondizione: Tutti i campi della form sono corretti

        // Recupero i valori dei campi della form
        String email = inputEmail.getText().toString();
        String nickname = inputNickname.getText().toString();
        int gender;
        switch (inputGender.getCheckedRadioButtonId()) {
            default:
            case R.id.activity_signup_radio_gender_male:
                gender = User.GENDER_MALE;
                break;
            case R.id.activity_signup_radio_gender_female:
                gender = User.GENDER_FEMALE;
                break;
        }

        return new User(email, nickname, DateUtils.formatDateWithStandardLocale(birthdate), gender, selectedImage);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, dayOfMonth);
        birthdate = cal.getTime();
        inputBirthdate.setText(DateUtils.formatDateWithCurrentDefaultLocale(birthdate));
    }

    public void showDatePickerDialog() {
        int year, month, day;
        final Calendar cal = Calendar.getInstance();

        // Se la data non è ancora stata selezionata, uso la data corrente come quella di default nel date picker
        // (non faccio niente perché Calendar.getInstance() mi restituisce già la data corrente)

        // Altrimenti, uso la data precedentemente selezionata come quella di default
        if (birthdate != null) {
            cal.setTime(birthdate);
        }

        year = cal.get(Calendar.YEAR);
        month = cal.get(Calendar.MONTH);
        day = cal.get(Calendar.DAY_OF_MONTH);

        DatePickerFragment.newInstance(year, month, day, this).show(getSupportFragmentManager(), DatePickerFragment.TAG_DATE_PICKER);
    }

    final DatabaseReaderListener dbUserHomesReaderListener = new DatabaseReaderListener() {
        @Override
        public void onReadSuccess(String key, Object object) {
            Map<String, UserHome> userHomes = (Map<String, UserHome>) object;

            // Scrittura dei nuovi dati nel db
            databaseWriter.writeUser(newUser, oldUser, auth.getCurrentUserUid(), userHomes.values(), dbUserEditWriterListener);
        }

        @Override
        public void onReadEmpty() {
            // Scrittura dei nuovi dati nel db
            databaseWriter.writeUser(newUser, oldUser, auth.getCurrentUserUid(), null, dbUserEditWriterListener);
        }

        @Override
        public void onReadCancelled(DatabaseError databaseError) {
            /*
            onCancelled viene invocato solo se si verifica un errore a lato server oppure se
            le regole di sicurezza impostate in Firebase non permettono l'operazione richiesta.
            In questo caso perciò viene visualizzato un messaggio di errore generico, dato che
            la situazione non può essere risolta dall'utente.
            */
            showErrorDialog();
            dismissProgress();
        }
    };

    // Listener processo di aggiornamento di un utente già esistente
    final DatabaseWriterListener dbUserEditWriterListener = new DatabaseWriterListener() {
        @Override
        public void onWriteSuccess() {
            Appartment.getInstance().setUser(newUser);
            Intent i = new Intent();
            i.putExtra(UserProfileActivity.EXTRA_NEW_USER, newUser);
            setResult(Activity.RESULT_OK, i);
            finish();
            dismissProgress();
        }

        @Override
        public void onWriteFail(Exception exception) {
            try {
                throw exception;
            } catch (Exception e) {
                // (DatabaseException se si verifica una violazione delle regole di sicurezza)
                // Generico
                showErrorDialog();
            }
            dismissProgress();
        }
    };

    // Listener processo di scrittura nel database del nuovo utente
    final DatabaseWriterListener dbNewUserWriterListener = new DatabaseWriterListener() {
        @Override
        public void onWriteSuccess() {
            Appartment.getInstance().setUser(newUser);
            moveToNextActivity(UserProfileActivity.class);
            dismissProgress();
        }

        @Override
        public void onWriteFail(Exception exception) {
            try {
                throw exception;
            } catch (Exception e) {
                // (DatabaseException se si verifica una violazione delle regole di sicurezza)
                // Generico
                showErrorDialog();
            }
            dismissProgress();
        }
    };

    // Listener processo di registrazione
    final AuthListener authListener = new AuthListener() {
        @Override
        public void onSuccess() {
            databaseWriter.writeUser(newUser, null, auth.getCurrentUserUid(), null, dbNewUserWriterListener);
        }

        @Override
        public void onFail(Exception exception) {
            try {
                throw exception;
            } catch (FirebaseAuthUserCollisionException e) {
                // Email già in uso
                layoutEmail.setError(getString(R.string.form_error_duplicate_email));
            } catch (FirebaseAuthWeakPasswordException e) {
                // Password non abbastanza robusta
                /*
                Questa eccezione non dovrebbe mai verificarsi in quanto sono già
                eseguiti controlli lato client sulla lunghezza della password.
                Se si entra in questo blocco c'è qualche problema!
                 */
                Log.w(getClass().getCanonicalName(), "Password non sufficientemente robusta");
            } catch (FirebaseAuthInvalidCredentialsException e) {
                // Email malformata
                /*
                Questa eccezione non dovrebbe mai verificarsi in quanto sono già
                eseguiti controlli lato client sulla struttura dell'indirizzo mail.
                Se si entra in questo blocco c'è qualche problema!
                 */
                Log.w(getClass().getCanonicalName(), "Email malformata");
            } catch (Exception e) {
                // Generico
                showErrorDialog();
            }
            dismissProgress();
        }
    };
}
