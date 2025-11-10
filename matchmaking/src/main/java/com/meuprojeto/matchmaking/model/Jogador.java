package com.meuprojeto.matchmaking.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "JOGADOR")
@Data // (From Lombok: Getters, Setters, toString, etc.)
@NoArgsConstructor // (From Lombok: Empty constructor for JPA)
public class Jogador {

    // --- Constantes de Rating (ELO) - Sugestão 1 ---
    /**
     * O Rating inicial (provisório) para novos jogadores.
     * Usamos 1000.0 como um padrão de ELO para permitir granularidade.
     */
    public static final double RATING_CALIBRACAO_INICIAL = 1000.0;

    /**
     * Partidas necessárias para o rating sair da calibração.
     */
    public static final int PARTIDAS_PARA_CALIBRAR = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idJogador")
    private Long idJogador;

    /**
     * O nome de exibição do jogador. Deve ser único (Implementa Sugestão 3).
     */
    @Column(nullable = false, unique = true)
    private String nome;

    /**
     * O "ELO" / "MMR" do jogador. Este é o novo núcleo do balanceamento.
     */
    @Column(nullable = false)
    private double rating = RATING_CALIBRACAO_INICIAL;

    /**
     * Total de partidas jogadas (usado para calibração).
     */
    @Column(nullable = false)
    private int partidasJogadas = 0;

    /**
     * Contadores de histórico (MVP e Destaque Perdedor).
     */
    @Column(nullable = false)
    private int totalMvp = 0;

    @Column(nullable = false)
    private int totalDestaquePerdedor = 0;


    /**
     * Construtor para novos jogadores (usado pelo JogadorService).
     */
    public Jogador(String nome) {
        this.nome = nome;
        // O rating e contadores já começam com os valores padrão (1000.0 e 0).
    }

    /**
     * Verifica se o jogador ainda está em calibração.
     * @Transient: Indica ao JPA que este método não é uma coluna no DB.
     */
    @Transient
    public boolean isEmCalibracao() {
        return this.partidasJogadas < PARTIDAS_PARA_CALIBRAR;
    }

    // --- Métodos de Atualização de Histórico (Chamados pelo Service) ---

    /**
     * Centraliza a atualização do histórico do jogador após uma partida.
     */
    public void registrarNovaPartida(double novoRating, boolean mvp, boolean destaquePerdedor) {
        this.partidasJogadas++;
        this.rating = novoRating;
        if (mvp) this.totalMvp++;
        if (destaquePerdedor) this.totalDestaquePerdedor++;
    }
}