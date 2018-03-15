package com.ingeniapps.cctulua.beans;

/**
 * Created by Ingenia Applications on 31/10/2017.
 */

public class Local
{
    private String codLocal;
    private String indHabilita;
    private String nomLocal;
    private String descLocal;

    public String getTelLocal() {
        return telLocal;
    }

    public void setTelLocal(String telLocal) {
        this.telLocal = telLocal;
    }

    private String telLocal;

    public Local()
    {

    }

    public String getIndicaHabilita() {
        return indicaHabilita;
    }

    public void setIndicaHabilita(String indicaHabilita) {
        this.indicaHabilita = indicaHabilita;
    }

    private String indicaHabilita;

    public String getCodLocal() {
        return codLocal;
    }

    public void setCodLocal(String codLocal) {
        this.codLocal = codLocal;
    }

    public String getIndHabilita() {
        return indHabilita;
    }

    public void setIndHabilita(String indHabilita) {
        this.indHabilita = indHabilita;
    }

    public String getNomLocal() {
        return nomLocal;
    }

    public void setNomLocal(String nomLocal) {
        this.nomLocal = nomLocal;
    }

    public String getDescLocal() {
        return descLocal;
    }

    public void setDescLocal(String descLocal) {
        this.descLocal = descLocal;
    }

    public String getImgLocal() {
        return imgLocal;
    }

    public void setImgLocal(String imgLocal) {
        this.imgLocal = imgLocal;
    }

    public String getUbicacionLocal() {
        return ubicacionLocal;
    }

    public void setUbicacionLocal(String ubicacionLocal) {
        this.ubicacionLocal = ubicacionLocal;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    private String imgLocal;
    private String ubicacionLocal;
    private String type;
}
