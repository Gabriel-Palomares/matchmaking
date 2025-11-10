package com.meuprojeto.matchmaking.service.dto;

import java.util.List;

/**
 * DTO para enviar uma partida com times JÁ DEFINIDOS pelo usuário.
 * Implementa a Sugestão 2 (Lógica de Times Fixos).
 */
public class RegistroPartidaFixaRequest {

    private Long idModoDeJogo;

    // Lista de IDs dos jogadores do Time A
    private List<Long> idsTimeA;

    // Lista de IDs dos jogadores do Time B
    private List<Long> idsTimeB;

    // Getters e Setters
    public Long getIdModoDeJogo() {
        return idModoDeJogo;
    }
    public void setIdModoDeJogo(Long idModoDeJogo) {
        this.idModoDeJogo = idModoDeJogo;
    }
    public List<Long> getIdsTimeA() {
        return idsTimeA;
    }
    public void setIdsTimeA(List<Long> idsTimeA) {
        this.idsTimeA = idsTimeA;
    }
    public List<Long> getIdsTimeB() {
        return idsTimeB;
    }
    public void setIdsTimeB(List<Long> idsTimeB) {
        this.idsTimeB = idsTimeB;
    }
}