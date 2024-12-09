import java.io.*;
import java.net.*;

public class ClienteForca {
    private static final String SERVIDOR = "localhost";
    private static final int PORTA = 12345;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVIDOR, PORTA);
             BufferedReader leitor = new BufferedReader(new InputStreamReader(socket.getInputStream())); //Le as mensagens que vem do servidor
             PrintWriter escritor = new PrintWriter(socket.getOutputStream(), true); //Envia as mensagens para o servidor
             BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in))) { //Le as entradas do usuário

            System.out.println("Conectado ao servidor da Forca!");

            // Loop para leitura contínua de mensagens do servidor
            String mensagem;
            while ((mensagem = leitor.readLine()) != null) {
                System.out.println(mensagem);
                if ((mensagem.contains("Digite uma letra ou tente adivinhar a palavra:")) || mensagem.contains("Digite uma letra:") || mensagem.contains("Você quer continuar? Responda com 1 para Sim ou 0 para Não.") || mensagem.contains("Bem-vindo ao jogo da forca! Digite seu nickname:")) {
                    System.out.print("> ");
                    String entrada = teclado.readLine();
                    escritor.println(entrada);
                }
            }

        } catch (IOException e) {
            System.out.println("Conexão com o servidor encerrada. Jogo finalizado.");
        }
    }
}
