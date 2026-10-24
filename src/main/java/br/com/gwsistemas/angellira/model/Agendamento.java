package br.com.gwsistemas.angellira.model;

import java.util.ArrayList;
import java.util.List;

/** Lote de viagens; a versão é fixa conforme a documentação. */
public class Agendamento {
    private final List<Viagem> viagens = new ArrayList<>();
    public String getVersao() { return "1.2"; }
    public List<Viagem> getViagens() { return viagens; }
}
