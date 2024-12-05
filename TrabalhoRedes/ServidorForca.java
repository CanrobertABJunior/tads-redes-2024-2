import java.io.*;
import java.net.*;
import java.util.*;

public class ServidorForca {
    private static final int PORTA = 12345;
    private static final String[] PALAVRAS = {"computador", "internet", "java", "servidor", "cliente"};
    private String palavraSecreta;
    private StringBuilder estadoAtual;
    private int tentativasRestantes;
    private Set<Character> letrasErradas; //Armazena as letras de forma única, sem permitir repetição
    private final List<String> partesDoCorpo = Arrays.asList("cabeça", "pescoço", "tronco", "braços", "mãos", "pernas", "pés");
    private List<String> corpoAtual;
    private List<String> palavrasTentadas;
    private int letrasDescobertas = 0; // Contador de letras corretas descobertas

    public static void main(String[] args) {
        new ServidorForca().iniciar();
    }

    public void iniciar() {
        try (ServerSocket servidor = new ServerSocket(PORTA)) {
            System.out.println("Servidor aguardando conexões...");

            // Escolhe uma palavra aleatória
            palavraSecreta = PALAVRAS[new Random().nextInt(PALAVRAS.length)]; //Busca uma palavra pelo indice do array através de um número aleatório gerado
            estadoAtual = new StringBuilder("_".repeat(palavraSecreta.length()));
            tentativasRestantes = partesDoCorpo.size();
            letrasErradas = new HashSet<>();
            corpoAtual = new ArrayList<>();
            palavrasTentadas = new ArrayList<>();

            // Aguarda dois jogadores
            Socket jogador1 = servidor.accept();
            System.out.println("Jogador 1 conectado.");
            enviarMensagem(jogador1, "Aguardando Jogador 2...");

            Socket jogador2 = servidor.accept();
            System.out.println("Jogador 2 conectado.");
            enviarMensagem(jogador1, "Jogador 2 conectado. Vamos começar!");
            enviarMensagem(jogador2, "Você está conectado. Vamos começar!");
            enviarMensagem(jogador2, "Palavra secreta: " + estadoAtual + " [" + palavraSecreta.length() + " letras]");
            enviarMensagem(jogador2, "Jogador 1 começa!");

            // Lógica do jogo
            gerenciarJogo(jogador1, jogador2);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void gerenciarJogo(Socket jogador1, Socket jogador2) throws IOException {
        BufferedReader leitor1 = new BufferedReader(new InputStreamReader(jogador1.getInputStream()));
        BufferedReader leitor2 = new BufferedReader(new InputStreamReader(jogador2.getInputStream()));
        PrintWriter escritor1 = new PrintWriter(jogador1.getOutputStream(), true);
        PrintWriter escritor2 = new PrintWriter(jogador2.getOutputStream(), true);

        Socket jogadorAtual = jogador1;
        PrintWriter escritorAtual = escritor1;
        BufferedReader leitorAtual = leitor1;

        while (tentativasRestantes > 0 && estadoAtual.indexOf("_") != -1) {
            escritorAtual.println("-------------------------------------");
            escritorAtual.println("Estado atual: " + estadoAtual + " [" + palavraSecreta.length() + " letras]");
            escritorAtual.println("Letras erradas: " + letrasErradas);
            escritorAtual.println("Palavras erradas: " + palavrasTentadas);
            escritorAtual.println("Tentativas restantes: " + tentativasRestantes);
            escritorAtual.println("Corpo atual: " + String.join(", ", corpoAtual));
            escritorAtual.println("Digite uma letra ou tente adivinhar a palavra:");

            String entrada = leitorAtual.readLine();
            if (entrada == null) {
                //escritor1.println("O jogador foi desconectado. O jogo está sendo encerrado.");
                escritor1.println("O Jogador " + (jogadorAtual == jogador1 ? "1" : "2") + " se desconectou. O jogo está sendo encerrado.");
                escritor2.println("O Jogador " + (jogadorAtual == jogador1 ? "1" : "2") + " se desconectou. O jogo está sendo encerrado.");
                //escritor2.println("O jogador foi desconectado. O jogo está sendo encerrado.");
                fecharConexoes(jogador1, jogador2);
                return; // Finaliza o servidor
            }
            if (entrada.isEmpty()) {
                escritorAtual.println("Entrada inválida. Tente novamente.");
                continue;
            }

            if (entrada.length() > 1 && letrasDescobertas >= 2) {
                // Tentativa de adivinhar a palavra
                if (entrada.equalsIgnoreCase(palavraSecreta)) {
                    // Mensagem de vitória para quem acertou a palavra
                    escritor1.println("Parabéns! O Jogador " + (jogadorAtual == jogador1 ? "1" : "2") + " acertou a palavra: " + palavraSecreta + ".");
                    escritor2.println("Parabéns! O Jogador " + (jogadorAtual == jogador1 ? "1" : "2") + " acertou a palavra: " + palavraSecreta + ".");
                    break;
                } else {
                    if (palavrasTentadas.contains(entrada)){
                        escritorAtual.println("Palavra já tentada. Tente novamente!");
                        continue;
                    }
                    else
                    {
                        palavrasTentadas.add(entrada);
                        tentativasRestantes--;
                        corpoAtual.add(partesDoCorpo.get(partesDoCorpo.size() - tentativasRestantes - 1));
                        escritorAtual.println("Tentativa incorreta! Adicionando uma parte do corpo.");
                        escritorAtual.println("Estado atual: " + estadoAtual + " [" + palavraSecreta.length() + " letras]");
                        escritorAtual.println("Letras erradas: " + letrasErradas);
                        escritorAtual.println("Palavras erradas: " + palavrasTentadas);
                        escritorAtual.println("Tentativas restantes: " + tentativasRestantes);
                        escritorAtual.println("Corpo atual: " + String.join(", ", corpoAtual));
                        escritorAtual.println("Vez do ou jogador. Aguarde!");
                    }
                }
            } else if (entrada.length() == 1) {
                // Tentativa de adivinhar uma letra
                char letra = entrada.toLowerCase().charAt(0);

                if (palavraSecreta.indexOf(letra) >= 0) {
                    boolean novaLetraDescoberta = false;
                    for (int i = 0; i < palavraSecreta.length(); i++) {
                        if (palavraSecreta.charAt(i) == letra && estadoAtual.charAt(i) == '_') {
                            estadoAtual.setCharAt(i, letra);
                            novaLetraDescoberta = true;
                        }
                    }
                    if (novaLetraDescoberta) {
                        letrasDescobertas++;
                        escritorAtual.println("Boa! A letra " + letra + " está na palavra.");
                        escritorAtual.println("Estado atual: " + estadoAtual + " [" + palavraSecreta.length() + " letras]");
                        escritorAtual.println("Letras erradas: " + letrasErradas);
                        escritorAtual.println("Palavras erradas: " + palavrasTentadas);
                        escritorAtual.println("Tentativas restantes: " + tentativasRestantes);
                        escritorAtual.println("Corpo atual: " + String.join(", ", corpoAtual));
                        escritorAtual.println("Vez do ou jogador. Aguarde!");
                    } else {
                        escritorAtual.println("A letra " + letra + " já foi descoberta.");
                        continue;
                    }
                } else {
                    if (letrasErradas.contains(letra)) {
                        escritorAtual.println("Já foi feito uma tentativa com a letra '" + letra + "'. Tente outra letra.");
                        continue;
                    }
                    else {
                        letrasErradas.add(letra);
                        tentativasRestantes--;
                        corpoAtual.add(partesDoCorpo.get(partesDoCorpo.size() - tentativasRestantes - 1));
                    
                        escritorAtual.println("Erro! A letra " + letra + " não está na palavra.");
                        escritorAtual.println("Estado atual: " + estadoAtual + " [" + palavraSecreta.length() + " letras]");
                        escritorAtual.println("Letras erradas: " + letrasErradas);
                        escritorAtual.println("Palavras erradas: " + palavrasTentadas);
                        escritorAtual.println("Tentativas restantes: " + tentativasRestantes);
                        escritorAtual.println("Corpo atual: " + String.join(", ", corpoAtual));
                        escritorAtual.println("Vez do ou jogador. Aguarde!");
                    }
                }
            } else {
                escritorAtual.println("Entrada inválida. Tente novamente.");
                continue;
            }

            // Se a palavra for completamente descoberta, encerra o jogo
            if (estadoAtual.indexOf("_") == -1) {
                escritor1.println("Parabéns! A palavra era: " + palavraSecreta + ". Vocês descobriram!");
                escritor2.println("Parabéns! A palavra era: " + palavraSecreta + ". Vocês descobriram!");
                break;
            }

            // Alterna entre os jogadores
            if (jogadorAtual == jogador1) {
                jogadorAtual = jogador2;
                escritorAtual = escritor2;
                leitorAtual = leitor2;
            } else {
                jogadorAtual = jogador1;
                escritorAtual = escritor1;
                leitorAtual = leitor1;
            }
        }

        // Caso o corpo seja completado sem descoberta da palavra
        if (tentativasRestantes == 0 && estadoAtual.indexOf("_") != -1) {
            escritor1.println("Vocês perderam! A palavra era: " + palavraSecreta + ".");
            escritor2.println("Vocês perderam! A palavra era: " + palavraSecreta + ".");
        }
    }

    private void enviarMensagem(Socket socket, String mensagem) throws IOException {
        PrintWriter escritor = new PrintWriter(socket.getOutputStream(), true);
        escritor.println(mensagem);
    }

    private void fecharConexoes(Socket jogador1, Socket jogador2) {
        try {
            if (jogador1 != null) {
                jogador1.close();
            }
            if (jogador2 != null) {
                jogador2.close();
            }
            System.out.println("Conexões encerradas. O servidor está sendo finalizado.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
