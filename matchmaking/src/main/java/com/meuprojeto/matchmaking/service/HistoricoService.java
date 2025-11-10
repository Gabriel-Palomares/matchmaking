package com.meuprojeto.matchmaking.service;

import com.meuprojeto.matchmaking.model.Jogador;
import com.meuprojeto.matchmaking.model.JogadorEquipe;
import com.meuprojeto.matchmaking.model.Partida;
import com.meuprojeto.matchmaking.repository.JogadorEquipeRepository;
import com.meuprojeto.matchmaking.repository.JogadorRepository;
import com.meuprojeto.matchmaking.repository.PartidaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Serviço focado em CONSULTAS (Leitura) de dados do histórico.
 * Será usado pela interface para exibir os resultados.
 */
@Service
@Transactional(readOnly = true) // Otimiza para operações de leitura
public class HistoricoService {

    private final PartidaRepository partidaRepository;
    private final JogadorRepository jogadorRepository;
    private final JogadorEquipeRepository jogadorEquipeRepository;

    public HistoricoService(PartidaRepository partidaRepository,
                            JogadorRepository jogadorRepository,
                            JogadorEquipeRepository jogadorEquipeRepository) {
        this.partidaRepository = partidaRepository;
        this.jogadorRepository = jogadorRepository;
        this.jogadorEquipeRepository = jogadorEquipeRepository;
    }

    /**
     * Busca todas as partidas, da mais recente para a mais antiga.
     */
    public List<Partida> buscarHistoricoPartidas() {
        // Usa o método do repository refatorado
        return partidaRepository.findAllByOrderByDataHoraDesc();
    }

    /**
     * Busca o histórico de partidas de um jogador específico.
     */
    public List<JogadorEquipe> buscarHistoricoJogador(Long idJogador) {
        Jogador jogador = jogadorRepository.findById(idJogador)
                .orElseThrow(() -> new MatchmakingException("Jogador não encontrado. ID: " + idJogador));

        // <<< MUDANÇA AQUI >>>
        // Agora chama o novo método que usa JOIN FETCH.
        // Isso resolve a LazyInitializationException.
        return jogadorEquipeRepository.findByJogadorComDetalhesCompletos(jogador);
    }
    /**
     * Busca um jogador (para a UI exibir o perfil).
     */
    public Jogador buscarJogadorPorId(Long idJogador) {
        return jogadorRepository.findById(idJogador)
                .orElseThrow(() -> new MatchmakingException("Jogador não encontrado. ID: " + idJogador));
    }
}