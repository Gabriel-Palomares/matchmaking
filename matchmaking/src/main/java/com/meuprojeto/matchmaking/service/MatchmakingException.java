package com.meuprojeto.matchmaking.service;

/**
 * Exceção customizada para erros na lógica de matchmaking.
 * (Ex: jogadores insuficientes, nome duplicado, etc.)
 */
public class MatchmakingException extends RuntimeException {
    public MatchmakingException(String message) {
        super(message);
    }
}