package com.meuprojeto.matchmaking.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode; // <<< NOVO IMPORT
import lombok.Getter; // <<< NOVO IMPORT
import lombok.NoArgsConstructor;
import lombok.Setter; // <<< NOVO IMPORT

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "EQUIPE")
// @Data FOI REMOVIDO!
@Getter // Adiciona todos os Getters
@Setter // Adiciona todos os Setters
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"partida", "membros", "resultado"}) // <<< CORREÇÃO CRÍTICA
public class Equipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idEquipe")
    private Long idEquipe;

    @Column(nullable = false)
    private String nome;

    // Este é um dos campos que causava o loop
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idPartida", nullable = false)
    private Partida partida;

    // OneToMany (listas) devem ser LAZY por padrão para performance
    @OneToMany(mappedBy = "equipe", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<JogadorEquipe> membros = new HashSet<>();

    // Este é um dos campos que causava o loop
    @OneToOne(mappedBy = "equipe", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private EquipePartida resultado;

    public Equipe(String nome, Partida partida) {
        this.nome = nome;
        this.partida = partida;
    }

    /**
     * NOVO MÉTODO HELPER
     * Simplifica adicionar um jogador ao time no MatchmakingService.
     */
    public void adicionarJogador(Jogador jogador) {
        JogadorEquipe jogadorEquipe = new JogadorEquipe(jogador, this);
        this.membros.add(jogadorEquipe);
    }
}