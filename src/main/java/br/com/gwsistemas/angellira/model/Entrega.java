package br.com.gwsistemas.angellira.model;

import java.math.BigDecimal;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.ArrayList;
import java.util.List;

/** Dados da documentação de Agendamento 1.2, independentes de manifesto/romaneio. */
public class Entrega {
    private Integer seqEntrega;
    private String cliente;
    private XMLGregorianCalendar prevInicio;
    private XMLGregorianCalendar prevFim;
    private BigDecimal peso;
    private BigDecimal volume;
    private String observacao;
    private final List<Docto> documentos = new ArrayList<>();

    public Integer getSeqEntrega() { return seqEntrega; }
    public void setSeqEntrega(Integer seqEntrega) { this.seqEntrega = seqEntrega; }

    public String getCliente() { return cliente; }
    public void setCliente(String cliente) { this.cliente = cliente; }

    public XMLGregorianCalendar getPrevInicio() { return prevInicio; }
    public void setPrevInicio(XMLGregorianCalendar prevInicio) { this.prevInicio = prevInicio; }

    public XMLGregorianCalendar getPrevFim() { return prevFim; }
    public void setPrevFim(XMLGregorianCalendar prevFim) { this.prevFim = prevFim; }

    public BigDecimal getPeso() { return peso; }
    public void setPeso(BigDecimal peso) { this.peso = peso; }

    public BigDecimal getVolume() { return volume; }
    public void setVolume(BigDecimal volume) { this.volume = volume; }

    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }

    /** Lista mutável: a ordem de inserção será preservada no XML. */
    public List<Docto> getDocumentos() { return documentos; }
}
