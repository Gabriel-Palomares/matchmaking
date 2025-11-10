package com.meuprojeto.matchmaking.controller;

import com.meuprojeto.matchmaking.model.*; // Importa todas as models
import com.meuprojeto.matchmaking.repository.ModoDeJogoRepository;
import com.meuprojeto.matchmaking.service.JogadorService;
import com.meuprojeto.matchmaking.service.MatchmakingException;
import com.meuprojeto.matchmaking.service.MatchmakingService;
import com.meuprojeto.matchmaking.service.HistoricoService; // <<< NOVO IMPORT
import com.meuprojeto.matchmaking.service.dto.CriacaoPartidaResponse;
import com.meuprojeto.matchmaking.service.dto.RegistroPartidaFixaRequest;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.paint.Color; // <<< NOVO IMPORT
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter; // <<< NOVO IMPORT
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MainController {

    // --- Injeção de Dependência (Spring) ---
    private final JogadorService jogadorService;
    private final MatchmakingService matchmakingService;
    private final ModoDeJogoRepository modoDeJogoRepository;
    private final ConfigurableApplicationContext springContext;
    private final HistoricoService historicoService; // <<< NOVO SERVIÇO INJETADO

    // --- Mapeamento da UI (JavaFX) ---

    // Painel Esquerdo (Comum)
    @FXML private ListView<Jogador> listaJogadoresView;
    @FXML private TextField nomeJogadorTextField;
    @FXML private Label statusLabel;

    // Aba 1: Balanceado
    @FXML private ComboBox<ModoDeJogo> modoDeJogoComboBox;
    @FXML private ListView<Jogador> jogadoresDisponiveisView;

    // Aba 2: Times Fixos
    @FXML private ComboBox<ModoDeJogo> modoDeJogoFixoComboBox;
    @FXML private ComboBox<Jogador> jogadoresDisponiveisFixoComboBox;
    @FXML private ListView<Jogador> listaTimeA;
    @FXML private ListView<Jogador> listaTimeB;

    // Aba 3: Histórico de Partidas
    @FXML private ListView<Partida> historicoPartidasListView;
    @FXML private TextArea detalhesPartidaTextArea;

    // Aba 4: Perfil do Jogador
    @FXML private ComboBox<Jogador> perfilJogadorComboBox;
    @FXML private Label perfilRatingLabel;
    @FXML private Label perfilPartidasLabel;
    @FXML private Label perfilMvpLabel;
    @FXML private Label perfilDestaqueLabel;
    @FXML private ListView<JogadorEquipe> perfilJogadorListView;


    // Listas de dados para a Aba 2
    private ObservableList<Jogador> jogadoresDisponiveisFixo = FXCollections.observableArrayList();
    private ObservableList<Jogador> jogadoresTimeA = FXCollections.observableArrayList();
    private ObservableList<Jogador> jogadoresTimeB = FXCollections.observableArrayList();

    /**
     * Construtor atualizado para injetar o HistoricoService.
     */
    public MainController(JogadorService jogadorService,
                          MatchmakingService matchmakingService,
                          ModoDeJogoRepository modoDeJogoRepository,
                          ConfigurableApplicationContext springContext,
                          HistoricoService historicoService) { // <<< NOVO PARÂMETRO
        this.jogadorService = jogadorService;
        this.matchmakingService = matchmakingService;
        this.modoDeJogoRepository = modoDeJogoRepository;
        this.springContext = springContext;
        this.historicoService = historicoService; // <<< NOVA ATRIBUIÇÃO
    }

    /**
     * Método Initialize (chamado quando o FXML é carregado)
     */
    @FXML
    public void initialize() {
        // --- Configuração Comum ---
        setupJogadorListViewCellFactory(listaJogadoresView);
        statusLabel.setText("Bem-vindo! Atualize as listas para começar.");

        // --- Configuração Aba 1 (Balanceado) ---
        setupJogadorListViewCellFactory(jogadoresDisponiveisView);
        jogadoresDisponiveisView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        setupModoDeJogoComboBoxCellFactory(modoDeJogoComboBox);

        // --- Configuração Aba 2 (Fixo) ---
        setupModoDeJogoComboBoxCellFactory(modoDeJogoFixoComboBox);
        setupJogadorComboBoxCellFactory(jogadoresDisponiveisFixoComboBox);
        setupJogadorListViewCellFactory(listaTimeA);
        setupJogadorListViewCellFactory(listaTimeB);

        jogadoresDisponiveisFixoComboBox.setItems(jogadoresDisponiveisFixo);
        listaTimeA.setItems(jogadoresTimeA);
        listaTimeB.setItems(jogadoresTimeB);

        // --- Configuração Aba 3 (Histórico de Partidas) ---
        setupPartidaListViewCellFactory(); // Configura a lista de partidas
        // Adiciona um listener para mostrar detalhes ao clicar
        historicoPartidasListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> handleExibirDetalhesPartida(newSelection)
        );

        // --- Configuração Aba 4 (Perfil do Jogador) ---
        setupJogadorComboBoxCellFactory(perfilJogadorComboBox); // Reutiliza o setup
        setupPerfilJogadorListViewCellFactory(); // Configura a lista de histórico do jogador

        // Carrega os dados do DB em todas as listas
        handleAtualizarListas();
    }

    // --- Métodos de Configuração da UI (Helpers) ---

    // (setupJogadorListViewCellFactory, setupJogadorComboBoxCellFactory, setupModoDeJogoComboBoxCellFactory permanecem os mesmos)
    private void setupJogadorListViewCellFactory(ListView<Jogador> listView) {
        listView.setCellFactory(lv -> new ListCell<Jogador>() {
            @Override
            protected void updateItem(Jogador jogador, boolean empty) {
                super.updateItem(jogador, empty);
                setText(empty || jogador == null ? null : String.format("%s (Rating: %.0f)", jogador.getNome(), jogador.getRating()));
            }
        });
    }

    private void setupJogadorComboBoxCellFactory(ComboBox<Jogador> comboBox) {
        comboBox.setCellFactory(lv -> new ListCell<Jogador>() {
            @Override
            protected void updateItem(Jogador jogador, boolean empty) {
                super.updateItem(jogador, empty);
                setText(empty || jogador == null ? null : String.format("%s (%.0f)", jogador.getNome(), jogador.getRating()));
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

    private void setupModoDeJogoComboBoxCellFactory(ComboBox<ModoDeJogo> comboBox) {
        comboBox.setCellFactory(lv -> new ListCell<ModoDeJogo>() {
            @Override
            protected void updateItem(ModoDeJogo item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getNome());
            }
        });

        comboBox.setButtonCell(new ListCell<ModoDeJogo>() {
            @Override
            protected void updateItem(ModoDeJogo item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getNome());
            }
        });
    }

    /**
     * NOVO HELPER (Aba 3)
     * Formata a exibição da lista de partidas no Histórico.
     */
    private void setupPartidaListViewCellFactory() {
        historicoPartidasListView.setCellFactory(lv -> new ListCell<Partida>() {
            private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            @Override
            protected void updateItem(Partida partida, boolean empty) {
                super.updateItem(partida, empty);
                if (empty || partida == null) {
                    setText(null);
                } else {
                    setText(String.format("Partida #%d: %s (%s)",
                            partida.getIdPartida(),
                            partida.getModoDeJogo().getNome(),
                            partida.getDataHora().format(formatter)
                    ));
                }
            }
        });
    }

    /**
     * NOVO HELPER (Aba 4)
     * Formata a exibição do histórico de partidas de um jogador.
     */
    private void setupPerfilJogadorListViewCellFactory() {
        perfilJogadorListView.setCellFactory(lv -> new ListCell<JogadorEquipe>() {
            private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            @Override
            protected void updateItem(JogadorEquipe participacao, boolean empty) {
                super.updateItem(participacao, empty);
                if (empty || participacao == null) {
                    setText(null);
                    setTextFill(Color.BLACK); // Reseta a cor
                } else {
                    Partida partida = participacao.getEquipe().getPartida();
                    EquipePartida resultado = participacao.getEquipe().getResultado();
                    String status = (resultado != null) ? resultado.getStatusResultado() : "PENDENTE";

                    setText(String.format("Partida #%d (%s) - Time: %s - Resultado: %s",
                            partida.getIdPartida(),
                            partida.getDataHora().format(formatter),
                            participacao.getEquipe().getNome(),
                            status
                    ));

                    // Pinta de verde para vitória, vermelho para derrota
                    if ("VITÓRIA".equals(status)) {
                        setTextFill(Color.GREEN);
                    } else if ("DERROTA".equals(status)) {
                        setTextFill(Color.RED);
                    } else {
                        setTextFill(Color.GRAY);
                    }
                }
            }
        });
    }


    // --- Handlers de Evento (Painel Esquerdo) ---

    /**
     * ATUALIZADO (Passo 6)
     * Agora atualiza TODAS as listas em TODAS as abas.
     */
    @FXML
    private void handleAtualizarListas() {
        try {
            // 1. Busca dados gerais
            List<Jogador> todosJogadores = jogadorService.listarTodosJogadores();
            List<ModoDeJogo> todosModos = modoDeJogoRepository.findAll();
            List<Partida> todasPartidas = historicoService.buscarHistoricoPartidas(); // <-- NOVO

            // 2. Filtra os modos de jogo por tipo
            List<ModoDeJogo> modosBalanceados = todosModos.stream()
                    .filter(ModoDeJogo::isBalanceamentoAutomatico)
                    .collect(Collectors.toList());
            List<ModoDeJogo> modosFixos = todosModos.stream()
                    .filter(m -> !m.isBalanceamentoAutomatico())
                    .collect(Collectors.toList());

            // 3. Atualiza os componentes da UI
            ObservableList<Jogador> obsJogadores = FXCollections.observableArrayList(todosJogadores);

            // Comum
            listaJogadoresView.setItems(obsJogadores);

            // Aba 1
            jogadoresDisponiveisView.setItems(obsJogadores);
            modoDeJogoComboBox.setItems(FXCollections.observableArrayList(modosBalanceados));

            // Aba 2
            jogadoresDisponiveisFixo.setAll(todosJogadores);
            jogadoresTimeA.clear();
            jogadoresTimeB.clear();
            modoDeJogoFixoComboBox.setItems(FXCollections.observableArrayList(modosFixos));

            // Aba 3 (NOVO)
            historicoPartidasListView.setItems(FXCollections.observableArrayList(todasPartidas));
            detalhesPartidaTextArea.clear();

            // Aba 4 (NOVO)
            perfilJogadorComboBox.setItems(obsJogadores);
            // Limpa os campos do perfil
            perfilRatingLabel.setText("Rating (ELO): -");
            perfilPartidasLabel.setText("Partidas Jogadas: -");
            perfilMvpLabel.setText("Total de MVPs: -");
            perfilDestaqueLabel.setText("Total de Destaques (Perdendo): -");
            perfilJogadorListView.getItems().clear();


            statusLabel.setText("Listas atualizadas. (" + todosJogadores.size() + " jogadores, " + todasPartidas.size() + " partidas)");
            statusLabel.setStyle("-fx-text-fill: green;");
        } catch (Exception e) {
            mostrarErro(e.getMessage());
        }
    }

    @FXML
    private void handleCriarJogador() {
        // (Este método permanece o mesmo)
        String nome = nomeJogadorTextField.getText();
        if (nome == null || nome.trim().isEmpty()) {
            mostrarErro("O nome do jogador não pode ser vazio.");
            return;
        }
        try {
            jogadorService.criarJogador(nome);
            nomeJogadorTextField.clear();
            handleAtualizarListas();
            statusLabel.setText("Jogador '" + nome + "' criado com sucesso!");
            statusLabel.setStyle("-fx-text-fill: blue;");
        } catch (MatchmakingException e) {
            mostrarErro(e.getMessage());
        }
    }

    @FXML
    private void handleDeletarJogador() {
        // (Este método permanece o mesmo)
        Jogador selecionado = listaJogadoresView.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            mostrarErro("Nenhum jogador selecionado na lista 'Gerenciar Jogadores'.");
            return;
        }
        try {
            jogadorService.deletarJogador(selecionado.getIdJogador());
            handleAtualizarListas();
            statusLabel.setText("Jogador '" + selecionado.getNome() + "' deletado.");
            statusLabel.setStyle("-fx-text-fill: blue;");
        } catch (Exception e) {
            mostrarErro("Erro ao deletar: " + e.getMessage());
        }
    }

    // --- Handlers de Evento (Aba 1: Balanceado) ---

    @FXML
    private void handleCriarPartidaBalanceada() {
        // (Este método permanece o mesmo - ele já chama o abrirJanelaResultado)
        ModoDeJogo modo = modoDeJogoComboBox.getSelectionModel().getSelectedItem();
        List<Jogador> selecionados = jogadoresDisponiveisView.getSelectionModel().getSelectedItems();

        if (modo == null) {
            mostrarErro("Selecione um Modo de Jogo (Balanceado).");
            return;
        }

        List<Long> idsJogadores = selecionados.stream()
                .map(Jogador::getIdJogador)
                .collect(Collectors.toList());
        try {
            CriacaoPartidaResponse resposta = matchmakingService.criarPartida(idsJogadores, modo.getIdModoDeJogo());

            Partida partida = resposta.getPartidaCriada();
            List<Jogador> reservas = resposta.getJogadoresReserva();
            List<Equipe> equipes = resposta.getEquipesFormadas();

            StringBuilder sb = new StringBuilder();
            sb.append("[BALANCEADO] Partida #" + partida.getIdPartida() + " criada!\n");

            for(Equipe eq : equipes) {
                sb.append(eq.getNome() + ": ");
                sb.append(eq.getMembros().stream()
                        .map(membro -> membro.getJogador().getNome())
                        .collect(Collectors.joining(", ")));
                sb.append("\n");
            }
            if (!reservas.isEmpty()) {
                sb.append("Reservas: ");
                sb.append(reservas.stream().map(Jogador::getNome).collect(Collectors.joining(", ")));
            }
            statusLabel.setText(sb.toString());
            statusLabel.setStyle("-fx-text-fill: blue; -fx-font-weight: bold;");

            // Abre a janela de pop-up para registrar o resultado
            abrirJanelaResultado(partida, equipes);

        } catch (MatchmakingException e) {
            mostrarErro(e.getMessage());
        }
    }

    // --- Handlers de Evento (Aba 2: Times Fixos) ---

    // (handleAddTimeA, handleRemoveTimeA, handleAddTimeB, handleRemoveTimeB permanecem os mesmos)
    @FXML
    private void handleAddTimeA() {
        Jogador selecionado = jogadoresDisponiveisFixoComboBox.getSelectionModel().getSelectedItem();
        if (selecionado != null) {
            jogadoresTimeA.add(selecionado);
            jogadoresDisponiveisFixo.remove(selecionado);
        }
    }
    @FXML
    private void handleRemoveTimeA() {
        Jogador selecionado = listaTimeA.getSelectionModel().getSelectedItem();
        if (selecionado != null) {
            jogadoresDisponiveisFixo.add(selecionado);
            jogadoresTimeA.remove(selecionado);
        }
    }
    @FXML
    private void handleAddTimeB() {
        Jogador selecionado = jogadoresDisponiveisFixoComboBox.getSelectionModel().getSelectedItem();
        if (selecionado != null) {
            jogadoresTimeB.add(selecionado);
            jogadoresDisponiveisFixo.remove(selecionado);
        }
    }
    @FXML
    private void handleRemoveTimeB() {
        Jogador selecionado = listaTimeB.getSelectionModel().getSelectedItem();
        if (selecionado != null) {
            jogadoresDisponiveisFixo.add(selecionado);
            jogadoresTimeB.remove(selecionado);
        }
    }

    @FXML
    private void handleRegistrarPartidaFixa() {
        // (Este método permanece o mesmo - ele já chama o abrirJanelaResultado)
        ModoDeJogo modoFixo = modoDeJogoFixoComboBox.getSelectionModel().getSelectedItem();
        if (modoFixo == null) {
            mostrarErro("Selecione um Modo de Jogo (Fixo).");
            return;
        }

        RegistroPartidaFixaRequest request = new RegistroPartidaFixaRequest();
        request.setIdModoDeJogo(modoFixo.getIdModoDeJogo());
        request.setIdsTimeA(jogadoresTimeA.stream().map(Jogador::getIdJogador).collect(Collectors.toList()));
        request.setIdsTimeB(jogadoresTimeB.stream().map(Jogador::getIdJogador).collect(Collectors.toList()));

        try {
            Partida partida = matchmakingService.registrarPartidaFixa(request);

            statusLabel.setText("[FIXO] Partida #" + partida.getIdPartida() + " registrada!\n" +
                    "Time A: " + jogadoresTimeA.size() + " jogadores.\n" +
                    "Time B: " + jogadoresTimeB.size() + " jogadores.");
            statusLabel.setStyle("-fx-text-fill: blue; -fx-font-weight: bold;");

            handleAtualizarListas();

            // Abre a janela de pop-up para registrar o resultado
            abrirJanelaResultado(partida, partida.getEquipes());

        } catch (MatchmakingException e) {
            mostrarErro(e.getMessage());
        }
    }

    // --- Handlers de Evento (Aba 3: Histórico de Partidas) ---

    /**
     * NOVO HANDLER (Aba 3)
     * Chamado quando o usuário clica em um item na lista de histórico de partidas.
     */
    private void handleExibirDetalhesPartida(Partida partida) {
        if (partida == null) {
            detalhesPartidaTextArea.clear();
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Partida #%d\n", partida.getIdPartida()));
        sb.append(String.format("Modo: %s\n", partida.getModoDeJogo().getNome()));
        sb.append(String.format("Data: %s\n", partida.getDataHora().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
        sb.append("\n--- EQUIPES E RESULTADOS ---\n");

        // <<< A CORREÇÃO ESTÁ AQUI >>>
        // Filtra equipes duplicadas (causadas pelo JOIN FETCH com 'membros')
        // usando .stream().distinct() antes de iterar.
        for (Equipe equipe : partida.getEquipes().stream().distinct().collect(Collectors.toList())) {

            sb.append(String.format("\n%s (ID: %d)\n", equipe.getNome(), equipe.getIdEquipe()));

            // Exibe o resultado
            EquipePartida resultado = equipe.getResultado();
            if (resultado != null) {
                sb.append(String.format("Resultado: %s\n", resultado.getStatusResultado()));
            } else {
                sb.append("Resultado: PENDENTE\n");
            }

            // Exibe os membros
            sb.append("Membros: ");
            String membros = equipe.getMembros().stream()
                    .map(m -> m.getJogador().getNome())
                    .collect(Collectors.joining(", "));
            sb.append(membros);
            sb.append("\n");
        }

        detalhesPartidaTextArea.setText(sb.toString());
    }


    // --- Handlers de Evento (Aba 4: Perfil do Jogador) ---

    /**
     * NOVO HANDLER (Aba 4)
     * Chamado quando o usuário seleciona um jogador na ComboBox da aba "Perfil".
     */
    @FXML
    private void handleBuscarPerfilJogador() {
        Jogador jogador = perfilJogadorComboBox.getSelectionModel().getSelectedItem();
        if (jogador == null) {
            return; // Nenhum jogador selecionado
        }

        try {
            // 1. Atualiza as estatísticas básicas (Sugestão 1)
            // (Não precisamos do serviço, pois já temos o objeto Jogador)
            perfilRatingLabel.setText(String.format("Rating (ELO): %.0f", jogador.getRating()));
            perfilPartidasLabel.setText(String.format("Partidas Jogadas: %d (%s)",
                    jogador.getPartidasJogadas(),
                    jogador.isEmCalibracao() ? "EM CALIBRAÇÃO" : "CALIBRADO"));
            perfilMvpLabel.setText(String.format("Total de MVPs: %d", jogador.getTotalMvp()));
            perfilDestaqueLabel.setText(String.format("Total de Destaques (Perdendo): %d", jogador.getTotalDestaquePerdedor()));

            // 2. Busca o histórico de partidas do jogador no backend
            List<JogadorEquipe> historico = historicoService.buscarHistoricoJogador(jogador.getIdJogador());

            // 3. Atualiza a ListView de histórico
            perfilJogadorListView.setItems(FXCollections.observableArrayList(historico));

        } catch (MatchmakingException e) {
            mostrarErro(e.getMessage());
        }
    }


    // --- Helper para Abrir Janela (PASSO 5) ---

    private void abrirJanelaResultado(Partida partida, List<Equipe> equipes) {
        // (Este método permanece o mesmo)
        try {
            FXMLLoader loader = new FXMLLoader();
            URL fxmlUrl = getClass().getResource("/view/RegistroResultadoView.fxml");
            if (fxmlUrl == null) {
                throw new IOException("Não foi possível encontrar /view/RegistroResultadoView.fxml");
            }
            loader.setLocation(fxmlUrl);

            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();

            RegistroResultadoController controller = loader.getController();

            // Passa a partida E o callback 'handleAtualizarListas'
            // Quando o pop-up salvar, ele chamará handleAtualizarListas
            controller.iniciarDados(partida, equipes, this::handleAtualizarListas);

            Stage stage = new Stage();
            stage.setTitle("Registrar Resultado - Partida #" + partida.getIdPartida());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait(); // Mostra e espera

        } catch (IOException e) {
            e.printStackTrace();
            mostrarErro("Erro ao abrir janela de resultado: " + e.getMessage());
        }
    }


    // --- Helper de Erro ---
    private void mostrarErro(String mensagem) {
        statusLabel.setText("ERRO: " + mensagem);
        statusLabel.setStyle("-fx-text-fill: red;");
    }
}