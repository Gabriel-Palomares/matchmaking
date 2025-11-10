package com.meuprojeto.matchmaking.service;

import com.meuprojeto.matchmaking.model.Jogador;
import com.meuprojeto.matchmaking.repository.JogadorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Serviço dedicado ao Gerenciamento (CRUD) de Jogadores.
 * Resolve a Sugestão 3 (Identidade) - é o ponto de entrada para criar/editar/listar.
 */
@Service
@Transactional // Garante que todas as operações sejam atômicas
public class JogadorService {

    private final JogadorRepository jogadorRepository;

    // Injeção de dependência via construtor
    public JogadorService(JogadorRepository jogadorRepository) {
        this.jogadorRepository = jogadorRepository;
    }

    /**
     * Cria um novo jogador com o rating de calibração padrão.
     */
    public Jogador criarJogador(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new MatchmakingException("O nome do jogador não pode ser vazio.");
        }
        // Validação (graças ao repository refatorado)
        if (jogadorRepository.findByNome(nome).isPresent()) {
            throw new MatchmakingException("Um jogador com o nome '" + nome + "' já existe.");
        }
        // O construtor define o nome e o rating padrão (1000.0)
        Jogador novoJogador = new Jogador(nome);
        return jogadorRepository.save(novoJogador);
    }

    /**
     * Busca todos os jogadores (para a UI listar).
     */
    @Transactional(readOnly = true)
    public List<Jogador> listarTodosJogadores() {
        return jogadorRepository.findAll();
    }

    /**
     * Busca um jogador pelo nome (usado na validação).
     */
    @Transactional(readOnly = true)
    public Optional<Jogador> buscarPorNome(String nome) {
        return jogadorRepository.findByNome(nome);
    }

    /**
     * Edita o nome de um jogador.
     */
    public Jogador editarJogador(Long idJogador, String novoNome) {
        if (novoNome == null || novoNome.trim().isEmpty()) {
            throw new MatchmakingException("O novo nome não pode ser vazio.");
        }

        Jogador jogador = jogadorRepository.findById(idJogador)
                .orElseThrow(() -> new MatchmakingException("Jogador não encontrado. ID: " + idJogador));

        // Validação de duplicidade
        Optional<Jogador> outroJogador = jogadorRepository.findByNome(novoNome);
        if (outroJogador.isPresent() && !outroJogador.get().getIdJogador().equals(idJogador)) {
            throw new MatchmakingException("O nome '" + novoNome + "' já está em uso por outro jogador.");
        }

        jogador.setNome(novoNome);
        return jogadorRepository.save(jogador);
    }

    /**
     * Deleta um jogador.
     * CUIDADO: Em V2, idealmente isso seria uma "desativação" (soft delete)
     * para não quebrar o histórico de partidas antigas.
     */
    public void deletarJogador(Long idJogador) {
        Jogador jogador = jogadorRepository.findById(idJogador)
                .orElseThrow(() -> new MatchmakingException("Jogador não encontrado. ID: " + idJogador));

        // TODO V2: Adicionar lógica para verificar se o jogador tem histórico
        // Se houver histórico (JogadorEquipe), isso vai falhar (ConstraintViolationException).
        // A UI terá que tratar isso.

        jogadorRepository.delete(jogador);
    }
}