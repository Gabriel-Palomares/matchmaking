package com.meuprojeto.matchmaking.controller;

import com.meuprojeto.matchmaking.model.Equipe;
import com.meuprojeto.matchmaking.model.Jogador;
import com.meuprojeto.matchmaking.model.Partida;
import com.meuprojeto.matchmaking.service.MatchmakingException;
import com.meuprojeto.matchmaking.service.MatchmakingService;
import com.meuprojeto.matchmaking.service.dto.RegistroResultadoRequest;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador para a janela de Registro de Resultado.
 *
 * @Component: Diz ao Spring para gerenciar esta classe.
 * @Scope("prototype"): ISSO É CRÍTICO!
 * Diz ao Spring para criar uma *nova instância* deste controlador
 * toda vez que a janela for aberta (diferente do MainController, que é singleton).
 */
@Component
@Scope("prototype")
public class RegistroResultadoController {

    // --- Injeção de Dependência (Spring) ---
    private final MatchmakingService matchmakingService;

    // --- Mapeamento da UI (.fxml) ---
    @FXML private Label partidaInfoLabel;
    @FXML private Label timeALabel;
    @FXML private Label timeBLabel;
    @FXML private ToggleGroup vencedorToggleGroup;
    @FXML private RadioButton timeARadioButton;
    @FXML private RadioButton timeBRadioButton;
    @FXML private RadioButton empateRadioButton;
    @FXML private ComboBox<Jogador> mvpComboBox;
    @FXML private ComboBox<Jogador> destaqueComboBox;
    @FXML private Button salvarButton;

    // --- Variáveis de Estado ---
    private Partida partidaAtual;
    private Equipe equipeA;
    private Equipe equipeB;
    private Runnable onJanelaFechadaCallback; // Callback para atualizar a MainView

    public RegistroResultadoController(MatchmakingService matchmakingService) {
        this.matchmakingService = matchmakingService;
    }

    /**
     * Este método é chamado pelo MainController para "passar" os dados da partida
     * para esta nova janela.
     */
    public void iniciarDados(Partida partida, List<Equipe> equipes, Runnable onJanelaFechadaCallback) {
        this.partidaAtual = partida;
        this.onJanelaFechadaCallback = onJanelaFechadaCallback;

        // Assume 2 equipes
        this.equipeA = equipes.get(0);
        this.equipeB = equipes.get(1);

        // 1. Preenche os labels da UI
        partidaInfoLabel.setText(String.format("Partida #%d (%s)",
                partida.getIdPartida(), partida.getModoDeJogo().getNome()));

        String nomesTimeA = equipeA.getMembros().stream()
                .map(m -> m.getJogador().getNome()).collect(Collectors.joining(", "));
        String nomesTimeB = equipeB.getMembros().stream()
                .map(m -> m.getJogador().getNome()).collect(Collectors.joining(", "));

        timeALabel.setText(String.format("Time A: %s", nomesTimeA));
        timeBLabel.setText(String.format("Time B: %s", nomesTimeB));

        timeARadioButton.setText("Vitória Time A");
        timeBRadioButton.setText("Vitória Time B");

        // 2. Preenche as ComboBoxes (Sugestão 1)
        List<Jogador> todosJogadores = new ArrayList<>();
        equipeA.getMembros().forEach(m -> todosJogadores.add(m.getJogador()));
        equipeB.getMembros().forEach(m -> todosJogadores.add(m.getJogador()));

        ObservableList<Jogador> obsJogadores = FXCollections.observableArrayList(todosJogadores);

        // Configura as ComboBoxes para mostrar o nome do jogador
        setupJogadorComboBoxCellFactory(mvpComboBox);
        setupJogadorComboBoxCellFactory(destaqueComboBox);

        mvpComboBox.setItems(obsJogadores);
        destaqueComboBox.setItems(obsJogadores);
    }

    /**
     * Chamado pelo botão "Salvar Resultado".
     * Monta o DTO e chama o backend.
     */
    @FXML
    private void handleSalvarResultado() {
        try {
            // 1. Montar o DTO (RegistroResultadoRequest)
            RegistroResultadoRequest request = new RegistroResultadoRequest();
            request.setIdPartida(partidaAtual.getIdPartida());

            // 2. Verificar o vencedor
            RadioButton selecionado = (RadioButton) vencedorToggleGroup.getSelectedToggle();
            if (selecionado == empateRadioButton) {
                request.setEmpate(true);
            } else {
                request.setEmpate(false);
                request.setIdEquipeVencedora(selecionado == timeARadioButton ? equipeA.getIdEquipe() : equipeB.getIdEquipe());
            }

            // 3. Pegar MVP e Destaque (Opcionais)
            if (mvpComboBox.getSelectionModel().getSelectedItem() != null) {
                request.setIdMvp(mvpComboBox.getSelectionModel().getSelectedItem().getIdJogador());
            }
            if (destaqueComboBox.getSelectionModel().getSelectedItem() != null) {
                request.setIdDestaquePerdedor(destaqueComboBox.getSelectionModel().getSelectedItem().getIdJogador());
            }

            // 4. Chamar o Backend (MatchmakingService)
            matchmakingService.registrarResultado(request);

            // 5. Chamar o Callback (para atualizar a MainView)
            if (onJanelaFechadaCallback != null) {
                onJanelaFechadaCallback.run();
            }

            // 6. Fechar esta janela (o pop-up)
            fecharJanela();

        } catch (MatchmakingException e) {
            // TODO: Mostrar erro em um Label na tela de pop-up
            System.err.println("Erro ao salvar resultado: " + e.getMessage());
        }
    }

    /**
     * Helper para fechar a janela atual.
     */
    private void fecharJanela() {
        // Pega o "Stage" (a janela) a partir de qualquer componente, como o botão
        Stage stage = (Stage) salvarButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Helper para configurar a aparência das ComboBoxes de Jogador
     */
    private void setupJogadorComboBoxCellFactory(ComboBox<Jogador> comboBox) {
        comboBox.setCellFactory(lv -> new ListCell<Jogador>() {
            @Override
            protected void updateItem(Jogador jogador, boolean empty) {
                super.updateItem(jogador, empty);
                setText(empty || jogador == null ? null : String.format("%s (Rating: %.0f)", jogador.getNome(), jogador.getRating()));
            }
        });
        comboBox.setButtonCell(new ListCell<Jogador>() {
            @Override
            protected void updateItem(Jogador jogador, boolean empty) {
                super.updateItem(jogador, empty);
                setText(empty || jogador == null ? null : String.format("%s (%.0f)", jogador.getNome(), jogador.getRating()));
            }
        });
    }
}