package com.meuprojeto.matchmaking;

import com.meuprojeto.matchmaking.model.Jogador;
import com.meuprojeto.matchmaking.model.ModoDeJogo;
import com.meuprojeto.matchmaking.repository.JogadorRepository;
import com.meuprojeto.matchmaking.repository.ModoDeJogoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.logging.Logger;

/**
 * Carga de dados iniciais (Seed Data) para teste do MVP.
 * Atualizado para o novo sistema de ELO e Modos de Jogo.
 */
@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger LOGGER = Logger.getLogger(DataLoader.class.getName());

    private final JogadorRepository jogadorRepository;
    private final ModoDeJogoRepository modoDeJogoRepository;

    public DataLoader(JogadorRepository jogadorRepository, ModoDeJogoRepository modoDeJogoRepository) {
        this.jogadorRepository = jogadorRepository;
        this.modoDeJogoRepository = modoDeJogoRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Carregar Modos de Jogo
        if (modoDeJogoRepository.count() == 0) {
            LOGGER.info("Carregando Modos de Jogo...");
            // Implementa Sugestão 2: 'balanceamentoAutomatico' flag
            modoDeJogoRepository.save(new ModoDeJogo("Futebol 5v5 (Balanceado)", 5, true));
            modoDeJogoRepository.save(new ModoDeJogo("Basquete 3v3 (Balanceado)", 3, true));
            modoDeJogoRepository.save(new ModoDeJogo("CS 5v5 (Times Fixos)", 5, false));
            LOGGER.info("Modos de Jogo carregados.");
        }

        // Carregar Jogadores de Teste
        if (jogadorRepository.count() == 0) {
            LOGGER.info("Carregando Jogadores de Teste...");
            // Implementa Sugestão 1 & 3: Construtor agora só precisa do nome
            // A entidade define o rating padrão como 1000.0
            jogadorRepository.save(new Jogador("Gui"));
            jogadorRepository.save(new Jogador("Rafa"));
            jogadorRepository.save(new Jogador("Duda"));
            jogadorRepository.save(new Jogador("Leo"));
            jogadorRepository.save(new Jogador("Ana"));
            jogadorRepository.save(new Jogador("Bia"));
            jogadorRepository.save(new Jogador("Caio"));
            jogadorRepository.save(new Jogador("Vini"));
            jogadorRepository.save(new Jogador("Gabi"));
            jogadorRepository.save(new Jogador("Lucas"));
            jogadorRepository.save(new Jogador("Fer")); // 11º jogador (reserva)
            LOGGER.info("Jogadores de Teste carregados.");
        }
    }
}