package com.ingeniapps.cctulua.beans;

/**
 * Created by Ingenia Applications on 13/01/2018.
 */

public class Oferta
{
    private String codOferta;
    private String codLocal;
    private String nomOferta;
    private String imaOferta;
    private String desOferta;
    private String descOferta;
    private String fecPubOferta;
    private String nomLocal;

    public String getNomLocal() {
        return nomLocal;
    }

    public void setNomLocal(String nomLocal) {
        this.nomLocal = nomLocal;
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

    private String imgLocal;
    private String ubicacionLocal;

    public String getCodOferta() {
        return codOferta;
    }

    public void setCodOferta(String codOferta) {
        this.codOferta = codOferta;
    }

    public String getCodLocal() {
        return codLocal;
    }

    public void setCodLocal(String codLocal) {
        this.codLocal = codLocal;
    }

    public String getNomOferta() {
        return nomOferta;
    }

    public void setNomOferta(String nomOferta) {
        this.nomOferta = nomOferta;
    }

    public String getImaOferta() {
        return imaOferta;
    }

    public void setImaOferta(String imaOferta) {
        this.imaOferta = imaOferta;
    }

    public String getDesOferta() {
        return desOferta;
    }

    public void setDesOferta(String desOferta) {
        this.desOferta = desOferta;
    }

    public String getDescOferta() {
        return descOferta;
    }

    public void setDescOferta(String descOferta) {
        this.descOferta = descOferta;
    }

    public String getFecPubOferta() {
        return fecPubOferta;
    }

    public void setFecPubOferta(String fecPubOferta) {
        this.fecPubOferta = fecPubOferta;
    }

    public String getFecExpOferta() {
        return fecExpOferta;
    }

    public void setFecExpOferta(String fecExpOferta) {
        this.fecExpOferta = fecExpOferta;
    }

    public String getIndEstado() {
        return indEstado;
    }

    public void setIndEstado(String indEstado) {
        this.indEstado = indEstado;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    private String fecExpOferta;
    private String indEstado;
    private String type;
}
