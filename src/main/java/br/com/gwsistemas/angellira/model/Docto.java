package br.com.gwsistemas.angellira.model;

import java.math.BigDecimal;
import javax.xml.datatype.XMLGregorianCalendar;

/** Dados da documentação de Agendamento 1.2, independentes de manifesto/romaneio. */
public class Docto {
    private Integer numero;
    private String serie;
    private BigDecimal peso;
    private BigDecimal volume;
    private BigDecimal valor;
    private BigDecimal frete;

    public Integer getNumero() { return numero; }
    public void setNumero(Integer numero) { this.numero = numero; }

    public String getSerie() { return serie; }
    public void setSerie(String serie) { this.serie = serie; }

    public BigDecimal getPeso() { return peso; }
    public void setPeso(BigDecimal peso) { this.peso = peso; }

    public BigDecimal getVolume() { return volume; }
    public void setVolume(BigDecimal volume) { this.volume = volume; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public BigDecimal getFrete() { return frete; }
    public void setFrete(BigDecimal frete) { this.frete = frete; }
}
