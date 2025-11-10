package com.meuprojeto.matchmaking;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import java.net.URL;

/**
 * A classe principal do JavaFX que gerencia o ciclo de vida da UI.
 * Ela é responsável por inicializar o Spring e dizer ao JavaFX
 * para usar o Spring para criar seus Controladores.
 */
public class JavaFxApplication extends Application {

    private ConfigurableApplicationContext springContext;

    /**
     * Inicializa o Contexto do Spring Boot.
     * Isso é executado ANTES do start().
     */
    @Override
    public void init() throws Exception {
        // Inicializa o Spring
        springContext = new SpringApplicationBuilder(MatchmakingApplication.class).run();
    }

    /**
     * O ponto de entrada principal do JavaFX.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        // 1. Carrega o FXML (a View)
        // Usamos um método especial (ver abaixo) para carregar o FXML
// O caminho DEVE começar com /
        FXMLLoader fxmlLoader = getFxmlLoader("/view/MainView.fxml");
        Parent root = fxmlLoader.load();

        // 2. Configura a Janela (Stage)
        primaryStage.setTitle("Gerenciador de Matchmaking");
        Scene scene = new Scene(root, 800, 600); // Define o tamanho
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Garante que o Spring seja desligado ao fechar a janela.
     */
    @Override
    public void stop() throws Exception {
        springContext.close();
        Platform.exit();
    }

    /**
     * Método helper crucial.
     * Cria um FXMLLoader que usa o Spring Context para
     * instanciar os controladores.
     *
     * Isso permite que @Autowired (ou Injeção de Construtor) funcione
     * nos seus Controladores JavaFX.
     */
    private FXMLLoader getFxmlLoader(String fxmlPath) {
        FXMLLoader loader = new FXMLLoader();
        // O caminho deve começar com / para ser absoluto no classpath
        URL location = getClass().getResource(fxmlPath);
        if (location == null) {
            // Se falhar, é porque o caminho do FXML (Passo 4.3) está errado
            throw new RuntimeException("Não foi possível encontrar o arquivo FXML: " + fxmlPath);
        }
        loader.setLocation(location);

        // Define o "Controller Factory" do Spring
        // Isso diz ao FXML: "Quando precisar de um controller, pegue do Spring"
        loader.setControllerFactory(springContext::getBean);
        return loader;
    }
}