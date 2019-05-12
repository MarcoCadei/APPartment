package com.unison.appartment;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.unison.appartment.livedata.FirebaseQueryLiveData;
import com.unison.appartment.model.Task;

import java.util.ArrayList;
import java.util.List;

public class TodoTaskRepository {

    private String separator;
    private String uncompletedTasks;
    // Nodo del database a cui sono interessato
    private static DatabaseReference UNCOMPLETED_TASKS_REF;
    // Livedata che rappresenta i dati nel nodo del database considerato che vengono convertiti
    // tramite un Deserializer in ogetti di tipo Task
    private FirebaseQueryLiveData liveData;
    private LiveData<List<Task>> taskLiveData;

    public TodoTaskRepository() {
        separator = Appartment.getInstance().getContext().getString(R.string.db_separator);
        uncompletedTasks = Appartment.getInstance().getContext().getString(R.string.db_uncompleted_tasks);
        UNCOMPLETED_TASKS_REF =
                FirebaseDatabase.getInstance().getReference(separator + uncompletedTasks + separator + Appartment.getInstance().getHome());
        liveData = new FirebaseQueryLiveData(UNCOMPLETED_TASKS_REF);
        taskLiveData = Transformations.map(liveData, new TodoTaskRepository.Deserializer());
    }

    @NonNull
    public LiveData<List<Task>> getTaskLiveData() {
        return taskLiveData;
    }

    public void addTask(Task newTask) {
        String key = UNCOMPLETED_TASKS_REF.push().getKey();
        newTask.setId(key);
        UNCOMPLETED_TASKS_REF.child(key).setValue(newTask);
    }

    private class Deserializer implements Function<DataSnapshot, List<Task>> {
        @Override
        public List<Task> apply(DataSnapshot dataSnapshot) {
            List<Task> tasks = new ArrayList<>();
            for (DataSnapshot taskSnapshot : dataSnapshot.getChildren()) {
                Task newTask = taskSnapshot.getValue(Task.class);
                newTask.setId(taskSnapshot.getKey());
                tasks.add(newTask);
            }
            return tasks;
        }
    }
}