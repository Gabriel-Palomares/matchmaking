package com.meuprojeto.matchmaking.service.dto;

import com.meuprojeto.matchmaking.model.Equipe; // <<< IMPORT ADICIONADO
import com.meuprojeto.matchmaking.model.Jogador;
import com.meuprojeto.matchmaking.model.Partida;

import java.util.List;

/**
 * DTO (Data Transfer Object) para encapsular a resposta da criação de partida.
 * ATUALIZADO: Agora também carrega a lista de equipes,
 * pois o objeto 'partidaCriada' pode estar obsoleto (stale).
 */
public class CriacaoPartidaResponse {

    private Partida partidaCriada;
    private List<Equipe> equipesFormadas; // <<< CAMPO ADICIONADO
    private List<Jogador> jogadoresReserva;

    // Construtor e Getters
    public CriacaoPartidaResponse(Partida partidaCriada, List<Equipe> equipesFormadas, List<Jogador> jogadoresReserva) {
        this.partidaCriada = partidaCriada;
        this.equipesFormadas = equipesFormadas; // <<< LINHA ADICIONADA
        this.jogadoresReserva = jogadoresReserva;
    }

    public Partida getPartidaCriada() {
        return partidaCriada;
    }

    // <<< MÉTODO ADICIONADO
    public List<Equipe> getEquipesFormadas() {
        return equipesFormadas;
    }

    public List<Jogador> getJogadoresReserva() {
        return jogadoresReserva;
    }
}