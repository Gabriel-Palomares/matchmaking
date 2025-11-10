package com.meuprojeto.matchmaking.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode; // <<< NOVO IMPORT
import lombok.Getter; // <<< NOVO IMPORT
import lombok.NoArgsConstructor;
import lombok.Setter; // <<< NOVO IMPORT

@Entity
@Table(name = "EQUIPE_PARTIDA") // Tabela de Resultados
// @Data FOI REMOVIDO!
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"equipe"}) // <<< CORREÇÃO CRÍTICA
public class EquipePartida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Este campo causava o loop
    @OneToOne
    @JoinColumn(name = "idEquipe", nullable = false, unique = true)
    private Equipe equipe;

    @Column(nullable = false)
    private String statusResultado; // "VITÓRIA", "DERROTA", "EMPATE"

    public EquipePartida(Equipe equipe, String statusResultado) {
        this.equipe = equipe;
        this.statusResultado = statusResultado;
    }
}