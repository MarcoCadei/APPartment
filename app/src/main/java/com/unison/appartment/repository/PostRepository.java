package com.unison.appartment.repository;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.unison.appartment.database.DatabaseConstants;
import com.unison.appartment.livedata.FirebaseQueryLiveData;
import com.unison.appartment.model.Post;
import com.unison.appartment.model.UserHome;
import com.unison.appartment.state.Appartment;

import java.util.ArrayList;
import java.util.List;

public class PostRepository {
    // Nodo del database a cui sono interessato
    private DatabaseReference postRef;
    // Livedata che rappresenta i dati nel nodo del database considerato che vengono convertiti
    // tramite un Deserializer in ogetti di tipo UncompletedTask
    private FirebaseQueryLiveData liveData;
    private LiveData<List<Post>> postLiveData;

    public PostRepository() {
        // Riferimento al nodo del Database interessato (i task non completati della casa corrente)
        postRef =
                FirebaseDatabase.getInstance().getReference(
                        DatabaseConstants.SEPARATOR + DatabaseConstants.POSTS +
                                DatabaseConstants.SEPARATOR + Appartment.getInstance().getHome().getName());
        Query orderedPosts = postRef.orderByChild(DatabaseConstants.POSTS_HOMENAME_POSTID_TIMESTAMP);
        liveData = new FirebaseQueryLiveData(orderedPosts);
        postLiveData = Transformations.map(liveData, new PostRepository.Deserializer());
    }

    @NonNull
    public LiveData<List<Post>> getPostLiveData() {
        return postLiveData;
    }

    public void addPost(Post newPost) {
        String key = postRef.push().getKey();
        newPost.setId(key);
        postRef.child(key).setValue(newPost);
    }

    private class Deserializer implements Function<DataSnapshot, List<Post>> {
        @Override
        public List<Post> apply(DataSnapshot dataSnapshot) {
            List<Post> posts = new ArrayList<>();
            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                Post post = postSnapshot.getValue(Post.class);
                post.setId(postSnapshot.getKey());
                posts.add(post);
            }
            return posts;
        }
    }
}
