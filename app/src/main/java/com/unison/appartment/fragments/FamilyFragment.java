package com.unison.appartment.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.unison.appartment.R;
import com.unison.appartment.activities.ChartActivity;
import com.unison.appartment.activities.FamilyMemberDetailActivity;
import com.unison.appartment.activities.UserProfileActivity;
import com.unison.appartment.database.FirebaseAuth;
import com.unison.appartment.model.HomeUser;
import com.unison.appartment.state.Appartment;

import java.util.Set;

/**
 * Fragment che contiene la lista dei membri di una famiglia e le relative statistiche
 */
public class FamilyFragment extends Fragment implements FamilyMemberListFragment.OnFamilyMemberListFragmentInteractionListener{

    public final static String EXTRA_USER_ID = "userId";
    public final static String EXTRA_NEW_OWNER_ID = "newOwnerId";
    public final static String EXTRA_OWN_POSTS = "ownPosts";
    public final static String EXTRA_REQUESTED_REWARDS = "requestedRewards";
    public final static String EXTRA_ASSIGNED_TASKS = "assignedTasks";
    public final static String EXTRA_NEW_ROLE = "newRole";
    public final static String EXTRA_NEW_NICKNAME = "newNickname";
    public final static String EXTRA_OPERATION_TYPE = "operationType";
    public final static int OPERATION_CHANGE_ROLE = 0;
    public final static int OPERATION_REMOVE_USER = 1;
    public final static int OPERATION_REMOVE_HOME = 2;

    private static final int MEMBER_DETAIL_REQUEST_CODE = 1;

    /**
     * Costruttore vuoto obbligatorio che viene usato nella creazione del fragment
     */
    public FamilyFragment() {
    }

    public static FamilyFragment newInstance() {
        return new FamilyFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View myView = inflater.inflate(R.layout.fragment_family, container, false);

        TextView textMembers = myView.findViewById(R.id.fragment_family_text_members);
        textMembers.setText(getResources().getQuantityString(R.plurals.fragment_family_text_members, 0, 0, Appartment.getInstance().getHome().getName()));
        /*
        Nota: Il numero di membri all'inizio viene impostato a 0 solo per non visualizzare la
        format string finché non si carica la lista.
         */

        MaterialButton btnStats = myView.findViewById(R.id.fragment_family_btn_stats);
        btnStats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), ChartActivity.class);
                startActivity(i);
            }
        });

        return myView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MEMBER_DETAIL_REQUEST_CODE) {
            FamilyMemberListFragment listFragment = (FamilyMemberListFragment) getChildFragmentManager()
                    .findFragmentById(R.id.fragment_family_member_list);
            if (resultCode == FamilyMemberDetailActivity.RESULT_OK) {
                switch (data.getIntExtra(EXTRA_OPERATION_TYPE, -1)) {
                    case OPERATION_CHANGE_ROLE:
                        listFragment.changeRole(data.getStringExtra(EXTRA_USER_ID), data.getIntExtra(EXTRA_NEW_ROLE, -1));
                        break;

                    case OPERATION_REMOVE_USER:
                        boolean isDeletingSelf = data.getStringExtra(EXTRA_USER_ID).equals(new FirebaseAuth().getCurrentUserUid());
                        listFragment.leaveHome(data.getStringExtra(EXTRA_USER_ID),
                                (Set<String>) data.getSerializableExtra(EXTRA_REQUESTED_REWARDS),
                                (Set<String>) data.getSerializableExtra(EXTRA_ASSIGNED_TASKS),
                                data.getStringExtra(EXTRA_NEW_OWNER_ID));
                        if (isDeletingSelf) {
                            kickOutOfHome();
                        }
                        break;

                    case OPERATION_REMOVE_HOME:
                        listFragment.deleteHome();
                        kickOutOfHome();
                        break;

                    default:
                        Log.e(getClass().getCanonicalName(), "Operation type non riconosciuto");
                        break;
                }
            }
            else if (resultCode == FamilyMemberDetailActivity.RESULT_EDITED) {
                listFragment.changeNickname(data.getStringExtra(EXTRA_USER_ID),
                        (Set<String>) data.getSerializableExtra(EXTRA_REQUESTED_REWARDS),
                        (Set<String>) data.getSerializableExtra(EXTRA_ASSIGNED_TASKS),
                        (Set<String>) data.getSerializableExtra(EXTRA_OWN_POSTS),
                        data.getStringExtra(EXTRA_NEW_NICKNAME));
            }
        }
    }

    public void deleteHome() {
        FamilyMemberListFragment listFragment = (FamilyMemberListFragment) getChildFragmentManager()
                .findFragmentById(R.id.fragment_family_member_list);
        listFragment.deleteHome();
        kickOutOfHome();
    }

    private void kickOutOfHome() {
        Intent i = new Intent(getActivity(), UserProfileActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        getActivity().finish();
    }

    @Override
    public void onFamilyMemberListFragmentOpenMember(HomeUser member) {
        Intent i = new Intent(getActivity(), FamilyMemberDetailActivity.class);
        i.putExtra(FamilyMemberDetailActivity.EXTRA_MEMBER_OBJECT, member);
        startActivityForResult(i, MEMBER_DETAIL_REQUEST_CODE);
    }

    @Override
    public void onFamilyMemberListElementsLoaded(int elements) {
        // Sia che la lista abbia elementi o meno, una volta fatta la lettura la
        // progress bar deve interrompersi
        ProgressBar progressBar = getView().findViewById(R.id.fragment_family_progress);
        progressBar.setVisibility(View.GONE);
        TextView textMembers = getView().findViewById(R.id.fragment_family_text_members);
        textMembers.setText(getResources().getQuantityString(R.plurals.fragment_family_text_members, elements, elements, Appartment.getInstance().getHome().getName()));
    }

    @Override
    public void onFamilyMemberListError(boolean error) {
        if (error) {
            View snackbarView = getActivity().findViewById(R.id.fragment_family);
            Snackbar.make(snackbarView, getString(R.string.snackbar_family_members_error_message),
                    Snackbar.LENGTH_LONG).show();
        }
    }
}
