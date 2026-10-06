package br.com.fiap.leitesolidario.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Nationalized;
import java.time.LocalDate;

@Entity
@Table(name = "doacoes")
public class Doacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doadora_id", nullable = false, foreignKey = @ForeignKey(name = "fk_doacoes_doadoras"))
    private Doadora doadora;

    @Column(name = "volume_ml", nullable = false)
    private Integer volumeMl;

    @Column(name = "data_coleta", nullable = false)
    private LocalDate dataColeta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusDoacao status;

    @Nationalized
    @Column(length = 500)
    private String observacoes;

    public Doacao() {}

    public Long getId() { return id; }
    public Doadora getDoadora() { return doadora; }
    public void setDoadora(Doadora doadora) { this.doadora = doadora; }
    public Integer getVolumeMl() { return volumeMl; }
    public void setVolumeMl(Integer volumeMl) { this.volumeMl = volumeMl; }
    public LocalDate getDataColeta() { return dataColeta; }
    public void setDataColeta(LocalDate dataColeta) { this.dataColeta = dataColeta; }
    public StatusDoacao getStatus() { return status; }
    public void setStatus(StatusDoacao status) { this.status = status; }
    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
}
