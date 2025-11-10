package com.meuprojeto.matchmaking.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "MODO_DE_JOGO")
@Data
@NoArgsConstructor
public class ModoDeJogo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idModoDeJogo")
    private Long idModoDeJogo;

    @Column(nullable = false, unique = true)
    private String nome; // Ex: "Futebol 5v5 (Balanceado)"

    @Column(nullable = false)
    private int jogadoresPorEquipe;

    /**
     * NOVO CAMPO (Implementa a Sugestão 2)
     * true: O sistema deve embaralhar e balancear os times (ex: "Futebol 5v5 Balanceado").
     * false: Os times são definidos pelo usuário (ex: "Registrar Campeonato 5v5").
     */
    @Column(nullable = false)
    private boolean balanceamentoAutomatico = true;


    /**
     * Construtor para o DataLoader (Seed Data).
     */
    public ModoDeJogo(String nome, int jogadoresPorEquipe, boolean balanceamentoAutomatico) {
        this.nome = nome;
        this.jogadoresPorEquipe = jogadoresPorEquipe;
        this.balanceamentoAutomatico = balanceamentoAutomatico;
    }
}