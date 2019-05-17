package com.unison.appartment.database;

import com.unison.appartment.model.User;

public interface Auth {
    void writeAuthInfo(final User newUser, final AuthListener listener);

    String getCurrentUserUid();

    void performSignIn(final String email, final String password, final AuthListener listener);
}
