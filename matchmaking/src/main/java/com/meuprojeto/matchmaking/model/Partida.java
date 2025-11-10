package com.meuprojeto.matchmaking.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode; // <<< NOVO IMPORT
import lombok.Getter; // <<< NOVO IMPORT
import lombok.NoArgsConstructor;
import lombok.Setter; // <<< NOVO IMPORT

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "PARTIDA")
// @Data FOI REMOVIDO!
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"equipes"}) // <<< CORREÇÃO CRÍTICA
public class Partida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idPartida")
    private Long idPartida;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idModoDeJogo", nullable = false)
    private ModoDeJogo modoDeJogo;

    @OneToMany(mappedBy = "partida", fetch = FetchType.LAZY)
    private List<Equipe> equipes = new ArrayList<>();

    public Partida(ModoDeJogo modoDeJogo) {
        this.modoDeJogo = modoDeJogo;
        this.dataHora = LocalDateTime.now();
    }
}