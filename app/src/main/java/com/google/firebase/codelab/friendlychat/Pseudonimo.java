package com.google.firebase.codelab.friendlychat;

/**
 * Created by Kathiani Souza on 9/20/2017.
 */

public class Pseudonimo {

    private String userUid;
    private String nomePseudonimo;

    public Pseudonimo(String userUid, String nomePseudonimo) {
        this.userUid = userUid;
        this.nomePseudonimo = nomePseudonimo;
    }

    public Pseudonimo() {

    }

    public String getUserUid() {
        return userUid;
    }

    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }

    public CharSequence getNomePseudonimo() {
        return nomePseudonimo;
    }

    public void setNomePseudonimo(String nomePseudonimo) {
        this.nomePseudonimo = nomePseudonimo;
    }
}
