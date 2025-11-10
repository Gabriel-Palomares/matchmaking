package com.meuprojeto.matchmaking.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode; // <<< NOVO IMPORT
import lombok.Getter; // <<< NOVO IMPORT
import lombok.NoArgsConstructor;
import lombok.Setter; // <<< NOVO IMPORT

@Entity
@Table(name = "JOGADOR_EQUIPE") // PERTENCE_A_EQUIPE
// @Data FOI REMOVIDO!
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"equipe"}) // <<< CORREÇÃO CRÍTICA
public class JogadorEquipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idJogador", nullable = false)
    private Jogador jogador;

    // Este campo causava o loop
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idEquipe", nullable = false)
    private Equipe equipe;

    public JogadorEquipe(Jogador jogador, Equipe equipe) {
        this.jogador = jogador;
        this.equipe = equipe;
    }
}