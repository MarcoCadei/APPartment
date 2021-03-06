package com.unison.appartment.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.unison.appartment.database.DatabaseConstants;
import com.unison.appartment.livedata.FirebaseQueryLiveData;
import com.unison.appartment.model.CompletedTask;
import com.unison.appartment.state.Appartment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.arch.core.util.Function;

public class CompletedTaskRepository {

    // Riferimento al nodo root del database
    private DatabaseReference rootRef;
    // Nodo del database a cui sono interessato
    private DatabaseReference completedTaskRef;
    // Livedata che rappresenta i dati nel nodo del database considerato che vengono convertiti
    // tramite un Deserializer in ogetti di tipo CompletedTask
    private FirebaseQueryLiveData liveData;
    private LiveData<List<CompletedTask>> completedTaskLiveData;
    private LiveData<List<CompletedTask>> recentCompletedTaskLiveData;

    public CompletedTaskRepository() {
        // Riferimento al nodo root del database
        rootRef = FirebaseDatabase.getInstance().getReference();
        // Riferimento al nodo del Database interessato (i task completati della casa corrente)
        completedTaskRef = FirebaseDatabase.getInstance().getReference(DatabaseConstants.COMPLETEDTASKS +
                DatabaseConstants.SEPARATOR + Appartment.getInstance().getHome().getName());
        Query orderedCompletedTasks = completedTaskRef.orderByChild(DatabaseConstants.COMPLETEDTASKS_HOMENAME_TASKNAME_NAME);
        liveData = new FirebaseQueryLiveData(orderedCompletedTasks);
        completedTaskLiveData = Transformations.map(liveData, new CompletedTaskRepository.Deserializer());

        // Task completati recenti
        Query recentOrderedCompletedTasks = completedTaskRef.orderByChild(DatabaseConstants.COMPLETEDTASKS_HOMENAME_TASKNAME_LASTCOMPLETIONDATE);
        liveData = new FirebaseQueryLiveData(recentOrderedCompletedTasks);
        recentCompletedTaskLiveData = Transformations.map(liveData, new CompletedTaskRepository.Deserializer());
    }

    @NonNull
    public LiveData<List<CompletedTask>> getCompletedTaskLiveData() {
        return completedTaskLiveData;
    }

    @NonNull
    public LiveData<List<CompletedTask>> getRecentCompletedTaskLiveData() {
        return recentCompletedTaskLiveData;
    }

    public void deleteCompletedTask(String taskName) {
        String completedTaskPath = DatabaseConstants.COMPLETEDTASKS + DatabaseConstants.SEPARATOR +
                Appartment.getInstance().getHome().getName() + DatabaseConstants.SEPARATOR +
                taskName;
        String completionPath = DatabaseConstants.COMPLETIONS + DatabaseConstants.SEPARATOR +
                Appartment.getInstance().getHome().getName() + DatabaseConstants.SEPARATOR +
                taskName;

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(completedTaskPath, null);
        childUpdates.put(completionPath, null);
        rootRef.updateChildren(childUpdates);
    }

    private class Deserializer implements Function<DataSnapshot, List<CompletedTask>> {
        @Override
        public List<CompletedTask> apply(DataSnapshot dataSnapshot) {
            List<CompletedTask> completedTasks = new ArrayList<>();
            for (DataSnapshot completedTaskSnapshot : dataSnapshot.getChildren()) {
                CompletedTask completedTask = completedTaskSnapshot.getValue(CompletedTask.class);
                completedTask.setId(completedTaskSnapshot.getKey());
                completedTask.setLastCompletionDate((-1) * completedTask.getLastCompletionDate());
                completedTasks.add(completedTask);
            }
            return completedTasks;
        }
    }
}
