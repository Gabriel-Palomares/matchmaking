package com.meuprojeto.matchmaking.service;

import com.meuprojeto.matchmaking.model.*;
import com.meuprojeto.matchmaking.repository.*;
import com.meuprojeto.matchmaking.service.dto.CriacaoPartidaResponse;
import com.meuprojeto.matchmaking.service.dto.RegistroPartidaFixaRequest;
import com.meuprojeto.matchmaking.service.dto.RegistroResultadoRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class MatchmakingService {

    // --- Constantes de Rating (ELO) - (Sugestão 1) ---
    /** K-Factor (velocidade de mudança de rating) para jogadores em calibração */
    private static final double K_FACTOR_CALIBRACAO = 50.0;
    /** K-Factor para jogadores normais */
    private static final double K_FACTOR_NORMAL = 30.0;
    /** Bônus de rating para o MVP */
    private static final double RATING_BONUS_MVP = 10.0;
    /** Mitigação (redução de perda) para o Destaque do time perdedor */
    private static final double RATING_MITIGACAO_DESTAQUE = 5.0;

    // --- Repositórios ---
    private final JogadorRepository jogadorRepository;
    private final ModoDeJogoRepository modoDeJogoRepository;
    private final PartidaRepository partidaRepository;
    private final EquipeRepository equipeRepository;
    private final EquipePartidaRepository equipePartidaRepository;

    // Injeção de Construtor
    public MatchmakingService(JogadorRepository jogadorRepository, ModoDeJogoRepository modoDeJogoRepository, PartidaRepository partidaRepository, EquipeRepository equipeRepository, EquipePartidaRepository equipePartidaRepository) {
        this.jogadorRepository = jogadorRepository;
        this.modoDeJogoRepository = modoDeJogoRepository;
        this.partidaRepository = partidaRepository;
        this.equipeRepository = equipeRepository;
        this.equipePartidaRepository = equipePartidaRepository;
    }

    /**
     * Ponto de entrada para criar uma partida.
     * Implementa Sugestão 2 (Reservas e Modos de Balanceamento).
     */
    public CriacaoPartidaResponse criarPartida(List<Long> idJogadores, Long idModoDeJogo) {
        // 1. Buscar Entidades do DB
        ModoDeJogo modoDeJogo = modoDeJogoRepository.findById(idModoDeJogo)
                .orElseThrow(() -> new MatchmakingException("Modo de Jogo não encontrado. ID: " + idModoDeJogo));

        List<Jogador> jogadoresDisponiveis = jogadorRepository.findAllById(idJogadores);

        // 2. Validação Mínima
        int minJogadores = modoDeJogo.getJogadoresPorEquipe() * 2; // Mínimo para 2 equipes
        if (jogadoresDisponiveis.size() < minJogadores) {
            throw new MatchmakingException("Jogadores insuficientes (" + jogadoresDisponiveis.size() + ") para formar duas equipes de " + modoDeJogo.getJogadoresPorEquipe() + ".");
        }

        // 3. Lógica de Reservas (Sugestão 2)
        int jogadoresPorEquipe = modoDeJogo.getJogadoresPorEquipe();
        int numTimes = jogadoresDisponiveis.size() / jogadoresPorEquipe;
        int numJogadoresAtivos = numTimes * jogadoresPorEquipe;
        int numReservas = jogadoresDisponiveis.size() - numJogadoresAtivos;

        // Ordena por rating (DESC) para que os piores fiquem de reserva
        jogadoresDisponiveis.sort(Comparator.comparingDouble(Jogador::getRating).reversed());

        List<Jogador> jogadoresAtivos = new ArrayList<>(jogadoresDisponiveis.subList(0, numJogadoresAtivos));
        List<Jogador> jogadoresReserva = (numReservas > 0) ? new ArrayList<>(jogadoresDisponiveis.subList(numJogadoresAtivos, jogadoresDisponiveis.size())) : new ArrayList<>();

        // 4. Salvar a Partida (ainda sem equipes)
        Partida novaPartida = new Partida(modoDeJogo);
        partidaRepository.save(novaPartida); // Salva para obter o ID da Partida

        List<Equipe> equipesFormadas;

        // 5. Lógica de Modos (Sugestão 2)
        if (modoDeJogo.isBalanceamentoAutomatico()) {
            // Se for balanceado, usa o algoritmo ELO
            equipesFormadas = balancearTimes(jogadoresAtivos, novaPartida, numTimes);
        } else {
            // Se for "times definidos", a UI deveria ter enviado as equipes.
            // Para o MVP, vamos assumir que "não balanceado" apenas agrupa os jogadores.
            equipesFormadas = criarTimesFixos(jogadoresAtivos, novaPartida, numTimes, jogadoresPorEquipe);
        }

        // 6. Salvar Equipes e Associações
        equipeRepository.saveAll(equipesFormadas);
        novaPartida.setEquipes(equipesFormadas);
        return new CriacaoPartidaResponse(novaPartida, equipesFormadas, jogadoresReserva);
    }

    /**
     * Registra o resultado e recalcula o ELO de todos.
     * Implementa Sugestão 1 (ELO, MVP, Destaque).
     */
    @Transactional
    public void registrarResultado(RegistroResultadoRequest request) {
        Partida partida = partidaRepository.findById(request.getIdPartida())
                .orElseThrow(() -> new MatchmakingException("Partida não encontrada: " + request.getIdPartida()));

        if (partida.getEquipes() == null || partida.getEquipes().isEmpty() || partida.getEquipes().size() < 2) {
            throw new MatchmakingException("Partida não possui equipes suficientes para registrar resultado.");
        }

        // TODO V2: Adaptar para mais de 2 times (ex: 3 times de 3)
        Equipe equipeA = partida.getEquipes().get(0);
        Equipe equipeB = partida.getEquipes().get(1);

        // Define quem venceu/perdeu/empatou
        double scoreA = 0.5, scoreB = 0.5; // Empate

        if (!request.isEmpate()) {
            if (request.getIdEquipeVencedora() == null) {
                throw new MatchmakingException("Se não é empate, um time vencedor deve ser selecionado.");
            }
            if (equipeA.getIdEquipe().equals(request.getIdEquipeVencedora())) {
                scoreA = 1.0; scoreB = 0.0;
            } else {
                scoreA = 0.0; scoreB = 1.0;
            }
        }

        // Calcula o Rating Médio de cada time ANTES de atualizar
        double ratingMedioA = equipeA.getMembros().stream()
                .mapToDouble(je -> je.getJogador().getRating()).average().orElse(Jogador.RATING_CALIBRACAO_INICIAL);
        double ratingMedioB = equipeB.getMembros().stream()
                .mapToDouble(je -> je.getJogador().getRating()).average().orElse(Jogador.RATING_CALIBRACAO_INICIAL);

        // Atualiza os jogadores da Equipe A
        atualizarRatingEquipe(equipeA, ratingMedioB, scoreA, request.getIdMvp(), request.getIdDestaquePerdedor());
        // Atualiza os jogadores da Equipe B
        atualizarRatingEquipe(equipeB, ratingMedioA, scoreB, request.getIdMvp(), request.getIdDestaquePerdedor());

        // Salva o resultado (EQUIPE_PARTIDA)
        salvarResultadoEquipe(equipeA, scoreA == 1.0 ? "VITÓRIA" : (scoreA == 0.0 ? "DERROTA" : "EMPATE"));
        salvarResultadoEquipe(equipeB, scoreB == 1.0 ? "VITÓRIA" : (scoreB == 0.0 ? "DERROTA" : "EMPATE"));
    }

    // --- MÉTODOS PRIVADOS DE LÓGICA ---

    /**
     * ALGORITMO DE BALANCEAMENTO (Sugestão 1: Rating)
     * Tenta criar times com o ELO médio mais próximo possível.
     */
    private List<Equipe> balancearTimes(List<Jogador> jogadores, Partida partida, int numTimes) {
        // Ordena por rating (melhor para o pior)
        jogadores.sort(Comparator.comparingDouble(Jogador::getRating).reversed());

        List<Equipe> equipes = new ArrayList<>();
        List<Double> ratingsEquipes = new ArrayList<>();
        for(int i=0; i < numTimes; i++) {
            equipes.add(new Equipe("Time " + (char)('A' + i), partida));
            ratingsEquipes.add(0.0);
        }

        // Distribuição "Adicionar ao time mais fraco"
        for (Jogador jogador : jogadores) {
            // Encontra o time com o menor rating total no momento
            int indiceTimeMaisFraco = 0;
            double menorRating = Double.MAX_VALUE;
            for (int i = 0; i < ratingsEquipes.size(); i++) {
                if (ratingsEquipes.get(i) < menorRating) {
                    menorRating = ratingsEquipes.get(i);
                    indiceTimeMaisFraco = i;
                }
            }

            // Adiciona o jogador a esse time
            equipes.get(indiceTimeMaisFraco).adicionarJogador(jogador); // Usa o helper
            ratingsEquipes.set(indiceTimeMaisFraco, menorRating + jogador.getRating());
        }
        return equipes;
    }

    /**
     * Lógica para Modo "Times Definidos" (Sugestão 2)
     * Apenas agrupa os jogadores (UI deveria ter definido, mas MVP agrupa)
     */
    private List<Equipe> criarTimesFixos(List<Jogador> jogadores, Partida partida, int numTimes, int jogadoresPorEquipe) {
        List<Equipe> equipes = new ArrayList<>();
        int jogadorIndex = 0;
        for (int i = 0; i < numTimes; i++) {
            Equipe equipe = new Equipe("Time " + (char)('A' + i), partida);
            for (int j = 0; j < jogadoresPorEquipe; j++) {
                equipe.adicionarJogador(jogadores.get(jogadorIndex++)); // Usa o helper
            }
            equipes.add(equipe);
        }
        return equipes;
    }

    /**
     * Salva o resultado na tabela de associação EQUIPE_PARTIDA
     */
    private void salvarResultadoEquipe(Equipe equipe, String resultado) {
        EquipePartida resultadoEquipe = new EquipePartida(equipe, resultado);
        equipe.setResultado(resultadoEquipe);
        // Salvamos explicitamente para garantir
        equipePartidaRepository.save(resultadoEquipe);
    }

    /**
     * Itera sobre os jogadores de uma equipe e atualiza o rating de cada um.
     */
    private void atualizarRatingEquipe(Equipe equipe, double ratingMedioOponente, double scoreReal, Long idMvp, Long idDestaquePerdedor) {
        for (JogadorEquipe je : equipe.getMembros()) {
            Jogador jogador = je.getJogador();
            boolean isMvp = jogador.getIdJogador().equals(idMvp);
            boolean isDestaque = (scoreReal == 0.0) && jogador.getIdJogador().equals(idDestaquePerdedor); // Destaque SÓ se aplica se perdeu

            double novoRating = calcularNovoRating(jogador, ratingMedioOponente, scoreReal, isMvp, isDestaque);

            // Atualiza o jogador no DB
            jogador.registrarNovaPartida(novoRating, isMvp, isDestaque);
            jogadorRepository.save(jogador);
        }
    }

    /**
     * O CORAÇÃO DO ELO (Sugestão 1: Rating e MVP/Destaque)
     * Calcula o novo rating de UM jogador.
     */
    private double calcularNovoRating(Jogador jogador, double ratingOponente, double scoreReal, boolean isMvp, boolean isDestaque) {
        // 1. Define o K-Factor (velocidade de mudança)
        double kFactor = jogador.isEmCalibracao() ? K_FACTOR_CALIBRACAO : K_FACTOR_NORMAL;

        // 2. Calcula a "Expectativa de Resultado" (Fórmula ELO)
        double expectativa = 1.0 / (1.0 + Math.pow(10.0, (ratingOponente - jogador.getRating()) / 400.0));

        // 3. Calcula o Bônus/Penalidade de MVP/Destaque
        double bonusMvp = isMvp ? RATING_BONUS_MVP : 0.0;
        double mitigacaoDestaque = (scoreReal == 0.0 && isDestaque) ? RATING_MITIGACAO_DESTAQUE : 0.0;

        // 4. Fórmula Final
        double mudancaBase = kFactor * (scoreReal - expectativa);
        double novoRating = jogador.getRating() + mudancaBase + bonusMvp + mitigacaoDestaque;

        // Garante que o rating não caia abaixo de um piso (ex: 100)
        return Math.max(100.0, novoRating);
    }
    // ... (construtor e outros métodos como criarPartida e registrarResultado) ...

    /**
     * NOVO MÉTODO (Implementa a Sugestão 2: Times Fixos)
     * Registra uma partida onde os times foram definidos pelo usuário.
     * Não executa balanceamento.
     */

    @Transactional
    public Partida registrarPartidaFixa(RegistroPartidaFixaRequest request) {
        // 1. Validação
        ModoDeJogo modoDeJogo = modoDeJogoRepository.findById(request.getIdModoDeJogo())
                .orElseThrow(() -> new MatchmakingException("Modo de Jogo não encontrado. ID: " + request.getIdModoDeJogo()));

        if (modoDeJogo.isBalanceamentoAutomatico()) {
            throw new MatchmakingException("Este modo de jogo é para balanceamento automático, não para times fixos.");
        }

        int jogadoresPorEquipe = modoDeJogo.getJogadoresPorEquipe();
        if (request.getIdsTimeA().size() != jogadoresPorEquipe || request.getIdsTimeB().size() != jogadoresPorEquipe) {
            throw new MatchmakingException("O número de jogadores no Time A (" + request.getIdsTimeA().size() +
                    ") ou Time B (" + request.getIdsTimeB().size() +
                    ") não corresponde ao modo de jogo (" + jogadoresPorEquipe + ").");
        }

        // 2. Buscar Jogadores
        List<Jogador> jogadoresTimeA = jogadorRepository.findAllById(request.getIdsTimeA());
        List<Jogador> jogadoresTimeB = jogadorRepository.findAllById(request.getIdsTimeB());

        // 3. Salvar a Partida
        Partida novaPartida = new Partida(modoDeJogo);
        partidaRepository.save(novaPartida); // Salva para obter o ID

        // 4. Criar e Salvar Equipes
        Equipe equipeA = new Equipe("Time A (Fixo)", novaPartida);
        jogadoresTimeA.forEach(equipeA::adicionarJogador); // Adiciona membros

        Equipe equipeB = new Equipe("Time B (Fixo)", novaPartida);
        jogadoresTimeB.forEach(equipeB::adicionarJogador); // Adiciona membros

        // Salva as equipes (o Cascade salva os JogadorEquipe)
        List<Equipe> equipesSalvas = equipeRepository.saveAll(List.of(equipeA, equipeB));

        novaPartida.setEquipes(equipesSalvas);

        return novaPartida;
    }
}