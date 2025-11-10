package com.meuprojeto.matchmaking.repository;

import com.meuprojeto.matchmaking.model.Jogador;
import com.meuprojeto.matchmaking.model.JogadorEquipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // <<< NOVO IMPORT
import org.springframework.data.repository.query.Param; // <<< NOVO IMPORT
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JogadorEquipeRepository extends JpaRepository<JogadorEquipe, Long> {

    /**
     * Busca simples (usada internamente).
     */
    List<JogadorEquipe> findByJogador(Jogador jogador);


    /**
     * <<< NOVO MÉTODO (A CORREÇÃO) >>>
     * * Busca as participações de um jogador, mas também força o carregamento (JOIN FETCH)
     * de todos os dados relacionados (Equipe, Partida, Resultado) que a UI
     * precisará. Isso evita a LazyInitializationException.
     */
    @Query("SELECT je FROM JogadorEquipe je " +
            "JOIN FETCH je.equipe e " +
            "JOIN FETCH e.partida p " +
            "JOIN FETCH p.modoDeJogo " +
            "LEFT JOIN FETCH e.resultado " + // LEFT JOIN porque o resultado pode ser nulo (pendente)
            "WHERE je.jogador = :jogador " +
            "ORDER BY p.dataHora DESC") // Ordena pela data da partida
    List<JogadorEquipe> findByJogadorComDetalhesCompletos(@Param("jogador") Jogador jogador);
}