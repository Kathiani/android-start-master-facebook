package com.google.firebase.codelab.friendlychat;

/**
 * Created by Kathiani Souza on 9/20/2017.
 */

public class Pseudonimo {

    private String userUid;
    private String nomePseudonimo;
    private int radioOpcao;

    public Pseudonimo(String userUid, String nomePseudonimo, int radioOpcao) {
        this.userUid = userUid;
        this.nomePseudonimo = nomePseudonimo;
        this.radioOpcao = radioOpcao;
    }

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

    public int getRadioOpcao() {
        return radioOpcao;
    }

    public void setRadioOpcao(int radioOpcao) {
        this.radioOpcao = radioOpcao;
    }
}
