package com.unison.appartment.fragments;

import android.app.Activity;
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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.unison.appartment.R;
import com.unison.appartment.activities.CreateTaskActivity;
import com.unison.appartment.activities.TaskDetailActivity;
import com.unison.appartment.database.FirebaseAuth;
import com.unison.appartment.model.Home;
import com.unison.appartment.model.UncompletedTask;
import com.unison.appartment.state.Appartment;

/**
 * Fragment che rappresenta una lista di attività da svolgere
 */
public class TodoFragment extends Fragment implements TodoListFragment.OnTodoListFragmentInteractionListener {

    public final static String EXTRA_NEW_TASK = "newTask";
    public final static String EXTRA_TASK_DATA = "taskData";
    public final static String EXTRA_USER_NAME = "userName";
    public final static String EXTRA_USER_ID = "userId";
    public final static String EXTRA_TASK = "task";
    public final static String EXTRA_OPERATION_TYPE = "operationType";
    public final static int OPERATION_DELETE = 0;
    public final static int OPERATION_ASSIGN = 1;
    public final static int OPERATION_REMOVE_ASSIGNMENT = 2;
    public final static int OPERATION_MARK_AS_COMPLETED = 3;
    public final static int OPERATION_UNMARK = 4;
    public final static int OPERATION_CONFIRM_COMPLETION = 5;

    private static final int ADD_TASK_REQUEST_CODE = 1;
    private static final int DETAIL_TASK_REQUEST_CODE = 2;

    private View emptyListLayout;

    private boolean snackbarShown = false;

    /**
     * Costruttore vuoto obbligatorio che viene usato nella creazione del fragment
     */
    public TodoFragment() {
    }

    public static TodoFragment newInstance() {
        return new TodoFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View myView = inflater.inflate(R.layout.fragment_todo, container, false);

        emptyListLayout = myView.findViewById(R.id.fragment_todo_layout_empty_list);

        final FloatingActionButton floatAddTask = myView.findViewById(R.id.fragment_todo_float_add_task);
        if (Appartment.getInstance().getHomeUser(new FirebaseAuth().getCurrentUserUid()).getRole() == Home.ROLE_SLAVE) {
            // Se l'utente è uno slave, non viene visualizzato il bottone per aggiungere un nuovo task.
            floatAddTask.hide();
        } else {
            /*
            In caso contrario, viene impostato l'onClickListener per il FAB che permette di aggiungere
            un nuovo task.
             */
            floatAddTask.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Appartment.getInstance().getHomeUser(new FirebaseAuth().getCurrentUserUid()).getRole() == Home.ROLE_SLAVE) {
                        floatAddTask.hide();
                        TodoListFragment listFragment = (TodoListFragment) getChildFragmentManager()
                                .findFragmentById(R.id.fragment_todo_todolist);
                        listFragment.refresh();
                        View snackbarView = getActivity().findViewById(R.id.fragment_todo);
                        Snackbar.make(snackbarView, getString(R.string.snackbar_downgrade_error_message),
                                Snackbar.LENGTH_LONG).show();
                    } else {
                        Intent i = new Intent(getActivity(), CreateTaskActivity.class);
                        startActivityForResult(i, ADD_TASK_REQUEST_CODE);
                    }
                }
            });
        }

        return myView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_TASK_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                TodoListFragment tlf = (TodoListFragment) getChildFragmentManager()
                        .findFragmentById(R.id.fragment_todo_todolist);
                tlf.addTask((UncompletedTask) data.getParcelableExtra(EXTRA_NEW_TASK));
            }
        }
        else if (requestCode == DETAIL_TASK_REQUEST_CODE) {
            TodoListFragment listFragment = (TodoListFragment) getChildFragmentManager()
                    .findFragmentById(R.id.fragment_todo_todolist);
            if (resultCode == TaskDetailActivity.RESULT_OK) {
                switch (data.getIntExtra(EXTRA_OPERATION_TYPE, -1)) {
                    case OPERATION_DELETE:
                        UncompletedTask taskToDelete = data.getParcelableExtra(EXTRA_TASK_DATA);
                        if (taskToDelete.getAssignedUserId() != null) {
                            listFragment.removeAssignmentAndDelete(taskToDelete.getId(),
                                    taskToDelete.getAssignedUserId(),
                                    taskToDelete.getVersion());
                        }
                        else {
                            listFragment.deleteTask(taskToDelete.getId(), taskToDelete.getVersion());
                        }
                        break;
                    case OPERATION_ASSIGN:
                        UncompletedTask taskToAssign = data.getParcelableExtra(EXTRA_TASK_DATA);
                        if (taskToAssign.isAssigned()) {
                            listFragment.switchAssignment(taskToAssign.getId(),
                                    taskToAssign.getAssignedUserId(),
                                    data.getStringExtra(EXTRA_USER_ID),
                                    data.getStringExtra(EXTRA_USER_NAME),
                                    taskToAssign.getVersion());
                        }
                        else {
                            listFragment.assignTask(taskToAssign.getId(),
                                    data.getStringExtra(EXTRA_USER_ID),
                                    data.getStringExtra(EXTRA_USER_NAME),
                                    taskToAssign.getVersion());
                        }
                        break;
                    case OPERATION_REMOVE_ASSIGNMENT:
                        UncompletedTask task = data.getParcelableExtra(EXTRA_TASK_DATA);
                        listFragment.removeAssignment(task.getId(),
                                task.getAssignedUserId(),
                                task.getVersion());
                        break;
                    case OPERATION_MARK_AS_COMPLETED:
                        UncompletedTask taskToMark = data.getParcelableExtra(EXTRA_TASK_DATA);
                        listFragment.markTask(taskToMark.getId(),
                                data.getStringExtra(EXTRA_USER_ID),
                                data.getStringExtra(EXTRA_USER_NAME),
                                taskToMark.getVersion());
                        break;
                    case OPERATION_UNMARK:
                        UncompletedTask taskToUnmark = data.getParcelableExtra(EXTRA_TASK_DATA);
                        listFragment.cancelCompletion(taskToUnmark.getId(),
                                taskToUnmark.getAssignedUserId(),
                                taskToUnmark.getVersion());
                        break;
                    case OPERATION_CONFIRM_COMPLETION:
                        listFragment.confirmCompletion((UncompletedTask)data.getParcelableExtra(EXTRA_TASK),
                                data.getStringExtra(EXTRA_USER_ID));
                        break;
                    default:
                        Log.e(getClass().getCanonicalName(), "Operation type non riconosciuto");
                }
            } else if (resultCode == TaskDetailActivity.RESULT_EDITED) {
                listFragment.editTask((UncompletedTask)data.getParcelableExtra(EXTRA_NEW_TASK));
            } else if (resultCode == TaskDetailActivity.RESULT_ERROR) {
                onTodoListError(true);
            }
        }
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
    public void onTodoListFragmentOpenTask(UncompletedTask uncompletedTask) {
        Intent i = new Intent(getActivity(), TaskDetailActivity.class);
        i.putExtra(TaskDetailActivity.EXTRA_TASK_OBJECT, uncompletedTask);
        startActivityForResult(i, DETAIL_TASK_REQUEST_CODE);
    }

    @Override
    public void onTodoListElementsLoaded(long elements) {
        // Sia che la lista abbia elementi o meno, una volta fatta la lettura la
        // progress bar deve interrompersi
        ProgressBar progressBar = getView().findViewById(R.id.fragment_todo_progress);
        progressBar.setVisibility(View.GONE);

        // Se gli elementi sono 0 allora mostro un testo che lo indichi all'utente
        if (elements == 0) {
            emptyListLayout.setVisibility(View.VISIBLE);
        } else {
            emptyListLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onTodoListError(boolean error) {
        // È necessario controllare se la snackbar è già mostrata perché alcune operazioni
        // in realtà fanno chiamate diverse al repository e ognuna dà errore. Pertanto senza
        // questo controllo si vederebbe uno strano glitch dovuto al fatto che si mostrano
        // molto velocemente due (o più) snackbar di fila
        if (error && !snackbarShown) {
            View snackbarView = getActivity().findViewById(R.id.fragment_todo);
            final Snackbar snackbar =  Snackbar.make(snackbarView, getString(R.string.snackbar_todo_error_message),
                    Snackbar.LENGTH_LONG);
            // snackbarShown mi serve che diventi subito true, ma con la callback non lo diventa subito
            // perché c'è l'animazione
            snackbarShown = true;
            snackbar.addCallback(new Snackbar.Callback() {
                @Override
                public void onDismissed(Snackbar transientBottomBar, int event) {
                    snackbarShown = false;
                }

                @Override
                public void onShown(Snackbar transientBottomBar) {
                    snackbarShown = true;
                }
            });
            snackbar.show();
        }
    }
}
