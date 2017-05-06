package model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Um programa servidor o qual aceita conex�es com clientes, os quais podem pedir informa��es e modificar dados do servidor. 
 * Cada cliente utiliza uma thread pr�pria, permitindo multiplas conex�es simult�neas ao servidor.
 *
 * O servidor � executado num loop infinito, sendo ent�o necess�rio o encerramento manual do servidor. Caso seja executado no console
 * Java, normalmente o atalho Ctrl+C encerra o processo.
 */

/**
 * PROTOCOLO
 * 
 * FLUXO DE COMUNICA��O
 * 
 * Primeiro o servidor dever� ser iniciado. Ap�s isso, incont�veis clientes
 * podem ser conectados ao servidor ao mesmo tempo, cada um com uma inst�ncia de
 * Cliente.
 * 
 * Ao instanciar um cliente, ele ir� pedir o endere�o Ipv4 do servidor para
 * poder conectar-se. Se o usu�rio informar um endere�o inv�lido, receber� uma
 * notifica��o de erro, e poder� digitar o endere�o Ipv4 do servidor incont�veis
 * vezes at� desistir ou informar o endere�o correto. O cliente tamb�m pode
 * simplesmente apertar OK ou Cancelar, no pop-up que pede o endere�o IP. Isso
 * ir� conectar o cliente em localhost.
 * 
 * Ap�s informar o endere�o correto, ser� estabelecida a conex�o entre o cliente
 * e o servidor num socket �nico, na porta 9898, permitindo a troca de
 * mensagens.
 * 
 * A��ES
 * 
 * Segue a lista de comandos que o cliente pode enviar ao servidor, e o retorno
 * que o servidor ir� entregar ao cliente:
 * 
 * Comandos v�lidos:
 * 
 * 'comandos' : Lista todos os comandos v�lidos do sistema;
 * 
 * 'listar tudo' : Lista todos os dados do sistema;
 * 
 * 'listar tipos' : Lista os tipos sangu�neos e a preval�ncia de cada tipo de
 * sangue na popula��o;
 * 
 * 'listar estoque' : lista a quantidade de sangue armazenada nesta unidade de
 * coleta de sangue;
 * 
 * 'listar compatibilidade' : Lista quais tipos sangu�neos podem doar e receber
 * sangue de outros tipos sangu�neos;
 * 
 * 'adicionar TIPO, VALOR' : Adiciona a quantidade VALOR de litros de sangue do
 * tipo sangu�neo TIPO no banco de dados;
 * 
 * 'remover TIPO, VALOR' : remove a quantidade VALOR de litros de sangue do tipo
 * sangu�neo TIPO no banco de dados; e
 * 
 * 'desconectar' : encerra a conex�o com o servidor.
 * 
 * Os valores v�lidos para TIPO s�o: O+, O-, A+, A-, B+, B-, AB+, AB-
 * 
 */
public class ServidorSangue {
	private final static String path = System.getProperty("user.home") + File.separator + "SERVIDOR_SANGUE"
			+ File.separator;
	private final static String databaseName = "bloodDatabase";
	private final static ArrayList<String> validTypes = new ArrayList<>(
			Arrays.asList("O+", "O-", "A+", "A-", "B+", "B-", "AB+", "AB-"));

	/**
	 * Inicia o servidor num loop infinito na porta 9898.
	 */
	public static void main(String[] args) throws Exception {
		ensureConnection(); // garante que exista um banco de dados
		System.out.println("O servidor entrou em execu��o.");
		int numCliente = 0;
		ServerSocket socketServidor = new ServerSocket(9898);
		try {
			while (true) {
				new Instancia(socketServidor.accept(), numCliente++).start();
			}
		} finally {
			socketServidor.close();
		}
	}

	/**
	 * Conex�o servidor-cliente.
	 */
	private static class Instancia extends Thread {
		private Socket socket;
		private int numCliente;

		public Instancia(Socket socket, int clientNumber) {
			this.socket = socket;
			this.numCliente = clientNumber;
			System.out.println("Nova conex�o com o cliente #" + clientNumber + " em " + socket);
		}

		/**
		 * Envia uma mensagem de boas vindas para os cliente rec�m-criados. Ap�s
		 * isso, escuta mensagens do cliente e envia respostas enquanto
		 * estiverem conectados.
		 */
		public void run() {
			String instrucoes = "Comandos v�lidos:" + "\n'comandos' : Lista todos os comandos v�lidos do sistema;"
					+ "\n'listar tudo' : Lista todos os dados do sistema;"
					+ "\n'listar tipos' : Lista os tipos sangu�neos e a preval�ncia de cada tipo de sangue na popula��o;"
					+ "\n'listar estoque' : lista a quantidade de sangue armazenada nesta unidade de coleta de sangue;"
					+ "\n'listar compatibilidade' : Lista quais tipos sangu�neos podem doar e receber sangue de outros tipos sangu�neos;"
					+ "\n'adicionar TIPO, VALOR' : Adiciona a quantidade VALOR de litros de sangue do tipo sangu�neo TIPO no banco de dados;"
					+ "\n'remover TIPO, VALOR' : remove a quantidade VALOR de litros de sangue do tipo sangu�neo TIPO no banco de dados; e"
					+ "\n'desconectar' : encerra a conex�o com o servidor."
					+ "\nOs valores v�lidos para TIPO s�o: O+, O-, A+, A-, B+, B-, AB+, AB-\n";

			try {
				// l� as entradas de String do cliente
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				// retorna sa�das ao cliente
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

				// envia mensagem de boas vindas para o cliente
				out.println("Ol�, voc� � o cliente #" + numCliente + ".");
				// envia as instru��es do sistema para o cliente
				out.println(instrucoes);

				// loop para receber mensagens do cliente
				while (true) {
					String entrada = in.readLine();
					// responde a entrada do cliente
					if (entrada.equals("listar tudo")) {
						out.println(List(0));
					} else if (entrada.equals("listar tipos")) {
						out.println(List(1));
					} else if (entrada.equals("listar estoque")) {
						out.println(List(2));
					} else if (entrada.equals("listar compatibilidade")) {
						out.println(List(3));
					} else if (entrada.equals("desconectar")) {
						break;
					} else if (entrada.equals("comandos")) {
						out.println(instrucoes);
					} else {
						// separa a entrada do cliente a cada espa�o encontrado
						String[] comando = entrada.split(" ");
						// s� � v�lido se tiver 3 palavras no comando
						if (comando.length == 3) {
							// checa se a primeira palavra � adicionar ou
							// remover
							if (comando[0].equalsIgnoreCase("adicionar") || comando[0].equalsIgnoreCase("remover")) {
								// checa se a segunda palavra cont�m v�rgula no
								// final
								if (comando[1].substring(comando[1].length() - 1).equals(",")) {
									// checa se o tipo � algum dos 8 v�lidos
									String tipoSangue = comando[1].substring(0, comando[1].length() - 1);
									if (validTypes.contains(tipoSangue)) {
										try {
											// recebe o valor
											float valor = Float.valueOf(comando[2]);
											if (valor > 0) {
												int operacao = 0;
												// determina a opera��o
												if (comando[0].equalsIgnoreCase("remover")) {
													operacao = 1;
												}
												// executa a opera��o
												if (changeValue(operacao, tipoSangue, valor)) {
													// informa o cliente
													String[] palavras = { "Foi adicionado ", " litro" };
													if (operacao == 0 && valor >= 2) {
														palavras = new String[] { "Foram adicionados ", " litros" };
													} else if (operacao == 1 && valor < 2) {
														palavras = new String[] { "Foi removido ", " litro" };
													} else if (operacao == 1 && valor >= 2) {
														palavras = new String[] { "Foram removidos ", " litros" };
													}
													out.println(
															palavras[0] + valor + palavras[1] + " de sangue do tipo "
																	+ tipoSangue + " no banco de dados.\n");
												} else {
													if (operacao == 1) {
														out.println(
																"N�o foi poss�vel completar a opera��o. Provavelmente o valor a ser "
																		+ "removido � maior do que o estoque para este tipo sangu�neo"
																		+ " no banco de dados.\n");
													} else {
														out.println("N�o foi poss�vel completar a opera��o.\n");
													}
												}
											} else
												out.println("O valor precisa ser maior que 0.\n");
										} catch (Exception e) {
											out.println("O valor informado � inv�lido.\n");
										}
									} else
										out.println(
												"Tipo de sangue n�o reconhecido. Digite 'comandos' para ver os tipos v�lidos.\n");
								} else
									out.println(
											"Aparentemente a v�rgula est� faltando. Padr�o: 'adicionar/remover TIPO, VALOR'.\n");
							} else
								out.println(
										"Comando n�o reconhecido. Digite 'comandos' para ver os comandos v�lidos.\n");
						} else
							out.println("Comando n�o reconhecido. Digite 'comandos' para ver os comandos v�lidos.\n");
					}
				}
			} catch (IOException e) {
				System.out.println("Erro ao lidar com o cliente #" + numCliente + ": " + e.getMessage());
			} finally {
				try {
					// fecha o socket
					socket.close();
				} catch (IOException e) {
					System.out.println("N�o foi poss�vel fechar o socket.");
				}
				System.out.println("Conex�o com o cliente #" + numCliente + " encerrada.");
			}
		}
	}

	/**
	 * Garante que exista um banco de dados
	 * 
	 * Author: Madson
	 * 
	 * @throws IOException
	 */
	private static void ensureConnection() {
		try {
			File databaseDirectory = new File(path);
			if (databaseDirectory.exists()) {// pasta existe

				File databaseFile = new File(path + databaseName);
				if (databaseFile.exists()) {// banco de dados existe
					// transforma todas as linhas num arraylist
					Path path = databaseFile.toPath();
					ArrayList<String> linhas = (ArrayList<String>) Files.readAllLines(path);
					// checa se existe o n�mero m�nimo de linhas
					if (linhas.size() < 8) {
						System.out.println(
								"O banco de dados existente � inv�lido e ser� exclu�do. Ser� criado um banco de dados com os"
										+ " valores padr�o.");
						databaseFile.delete();
						createDatabase();
					} else {
						// percorre todas as linhas
						for (int i = 0; i < linhas.size(); i++) {
							try {
								Float.valueOf(linhas.get(i));// checa se � um
																// n�mero
							} catch (Exception e) {
								linhas.set(i, "0.0");
								System.out.println("O valor na linha " + i + " era inv�lido e foi substitu�do por 0.");
							}
						}
					}
					// reescreve o arquivo ap�s checar os dados
					Files.write(path, linhas);
				} else {// banco de dados n�o existe
					createDatabase();
				}
			} else {// pasta n�o existe
				databaseDirectory.mkdirs();
				System.out.println("Pasta do banco de dados n�o existia, ent�o foi criada.");
				createDatabase();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Cria o banco de dados com valores padr�o(fict�cios, para preencher o
	 * banco de dados) para cada tipo sangu�neo
	 * 
	 * Author: Madson
	 * 
	 * @throws IOException
	 */
	private static void createDatabase() {
		try {
			FileWriter fW = new FileWriter(path + databaseName, true);
			PrintWriter printWriter = new PrintWriter(fW);
			// respectivamente: O+, O-, A+, A-, B+, B-, AB+, AB-
			float[] standardBloodValues = new float[] { 36f, 9f, 34f, 8f, 8f, 2f, 2.5f, 0.5f };

			for (int i = 0; i < standardBloodValues.length; i++) {
				if (i == 0) {
					printWriter.print(standardBloodValues[i]);
				} else {
					printWriter.print("\n" + standardBloodValues[i]);
				}
			}

			fW.close();
			System.out.println("Banco de dados foi criado com sucesso.");
		} catch (Exception e) {
			System.out.println("Banco de dados n�o p�de ser criado.");
			e.printStackTrace();
		}
	}

	/**
	 * Lista dados de acordo com a opera��o
	 * 
	 * Author: Madson
	 * 
	 * @param operation
	 *            0: listar tudo, 1: listar tipos, 2: listar estoque, 3: listar
	 *            compatibilidade
	 * @return
	 */
	private static String List(int operation) {
		String data = "";
		if (operation == 0) {// listar tudo
			try {
				// copia todos os dados do banco de dados num ArrayList
				File databaseFile = new File(path + databaseName);
				Path filePath = databaseFile.toPath();
				ArrayList<String> linhas = (ArrayList<String>) Files.readAllLines(filePath);
				// total de litros de sangue no estoque
				float total = 0;
				for (String valor : linhas) {
					total += Float.valueOf(valor);
				}

				data += String.format("%-24s%-21s%-20s%-24s%-25s\n", "Tipo Sangu�neo", "Estoque (l)", "% do total",
						"Pode doar para", "Pode receber de");
				data += String.format("%-24s%11.2f%20.2f%25s%15s\n", "O+", Float.valueOf(linhas.get(0)),
						(Float.valueOf(linhas.get(0)) / total) * 100, "O+, A+, B+, AB+", "O+, O-");
				data += String.format("%-24s%11.2f%20.2f%15s%21s\n", "O-", Float.valueOf(linhas.get(1)),
						(Float.valueOf(linhas.get(1)) / total) * 100, "Todos", "O-");
				data += String.format("%-24s%11.2f%20.2f%17s%31s\n", "A+", Float.valueOf(linhas.get(2)),
						(Float.valueOf(linhas.get(2)) / total) * 100, "A+, AB+", "O+, O-, A+, A-");
				data += String.format("%-24s%11.2f%20.2f%26s%14s\n", "A-", Float.valueOf(linhas.get(3)),
						(Float.valueOf(linhas.get(3)) / total) * 100, "A+, A-, AB+, AB-", "O-, A-");
				data += String.format("%-24s%11.2f%20.2f%17s%31s\n", "B+", Float.valueOf(linhas.get(4)),
						(Float.valueOf(linhas.get(4)) / total) * 100, "B+, AB+", "O+, O-, B+, B-");
				data += String.format("%-24s%11.2f%20.2f%26s%14s\n", "B-", Float.valueOf(linhas.get(5)),
						(Float.valueOf(linhas.get(5)) / total) * 100, "B+, B-, AB+, AB-", "O-, B-");
				data += String.format("%-24s%11.2f%20.2f%13s%26s\n", "AB+", Float.valueOf(linhas.get(6)),
						(Float.valueOf(linhas.get(6)) / total) * 100, "AB+", "Todos");
				data += String.format("%-24s%11.2f%20.2f%18s%31s\n", "AB-", Float.valueOf(linhas.get(7)),
						(Float.valueOf(linhas.get(7)) / total) * 100, "AB+, AB-", "O-, A-, B-, AB-");
				data += "Total de sangue em estoque: " + total + " litros.\n";
			} catch (Exception e) {
				data = "N�o foi poss�vel executar esta consulta.";
			}
		} else if (operation == 1) {// listar tipos
			data += String.format("%-25s%-18s%-25s\n", "Grupo Sangu�neo", "Positivo", "Negativo");
			data += String.format("%-25s%-18s%-25s\n", "O", "36%", "9%");
			data += String.format("%-25s%-18s%-25s\n", "A", "34%", "8%");
			data += String.format("%-25s%-18s%-25s\n", "B", "8%", "2%");
			data += String.format("%-25s%-18s%-25s\n", "AB", "2.5%", "0.5%");
			data += String.format("%-25s%-18s%-25s\n", "Total", "80.5%", "19.5%");
			data += "Percentual de ocorr�ncia dos tipos sangu�neos, considerando a popula��o total do Brasil.\n";
		} else if (operation == 2) {// listar estoque
			try {
				// copia todos os dados do banco de dados num ArrayList
				File databaseFile = new File(path + databaseName);
				Path filePath = databaseFile.toPath();
				ArrayList<String> linhas = (ArrayList<String>) Files.readAllLines(filePath);
				// total de litros de sangue no estoque
				float total = 0;
				for (String valor : linhas) {
					total += Float.valueOf(valor);
				}

				data += String.format("%-24s%-21s%-10s\n", "Tipo Sangu�neo", "Estoque (l)", "% do total");
				data += String.format("%-24s%11.2f%20.2f\n", "O+", Float.valueOf(linhas.get(0)),
						(Float.valueOf(linhas.get(0)) / total) * 100);
				data += String.format("%-24s%11.2f%20.2f\n", "O-", Float.valueOf(linhas.get(1)),
						(Float.valueOf(linhas.get(1)) / total) * 100);
				data += String.format("%-24s%11.2f%20.2f\n", "A+", Float.valueOf(linhas.get(2)),
						(Float.valueOf(linhas.get(2)) / total) * 100);
				data += String.format("%-24s%11.2f%20.2f\n", "A-", Float.valueOf(linhas.get(3)),
						(Float.valueOf(linhas.get(3)) / total) * 100);
				data += String.format("%-24s%11.2f%20.2f\n", "B+", Float.valueOf(linhas.get(4)),
						(Float.valueOf(linhas.get(4)) / total) * 100);
				data += String.format("%-24s%11.2f%20.2f\n", "B-", Float.valueOf(linhas.get(5)),
						(Float.valueOf(linhas.get(5)) / total) * 100);
				data += String.format("%-24s%11.2f%20.2f\n", "AB+", Float.valueOf(linhas.get(6)),
						(Float.valueOf(linhas.get(6)) / total) * 100);
				data += String.format("%-24s%11.2f%20.2f\n", "AB-", Float.valueOf(linhas.get(7)),
						(Float.valueOf(linhas.get(7)) / total) * 100);
				data += "Total de sangue em estoque: " + total + " litros.\n";

			} catch (Exception e) {
				data = "N�o foi poss�vel executar esta consulta.\n";
			}
		} else {// listar compatibilidade
			data += String.format("%-24s%-24s%-25s\n", "Tipo Sangu�neo", "Pode doar para", "Pode receber de");
			data += String.format("%-24s%-24s%-25s\n", "O+", "O+, A+, B+, AB+", "O+, O-");
			data += String.format("%-24s%-24s%-25s\n", "O-", "Todos", "O-");
			data += String.format("%-24s%-24s%-25s\n", "A+", "A+, AB+", "O+, O-, A+, A-");
			data += String.format("%-24s%-24s%-25s\n", "A-", "A+, A-, AB+, AB-", "O-, A-");
			data += String.format("%-24s%-24s%-25s\n", "B+", "B+, AB+", "O+, O-, B+, B-");
			data += String.format("%-24s%-24s%-25s\n", "B-", "B+, B-, AB+, AB-", "O-, B-");
			data += String.format("%-24s%-24s%-25s\n", "AB+", "AB+", "Todos");
			data += String.format("%-24s%-24s%-25s\n", "AB-", "AB+, AB-", "O-, A-, B-, AB-");
		}
		return data;
	}

	/**
	 * Author: Madson
	 * 
	 * @param operation
	 *            0 para soma, 1 para subtra��o
	 * @param type
	 *            tipos aceitos: "O+", "O-", "A+", "A-", "B+", "B-", "AB+",
	 *            "AB-"
	 * @param value
	 *            valor maior que 0 a ser somado ou subtra�do
	 * @return
	 * @throws IOException
	 */
	private static boolean changeValue(int operation, String type, float value) throws IOException {
		// 0 == soma, 1 == subtra��o
		if (operation < 0 || operation > 1) {
			return false;
		}
		// checa se type � v�lido
		if (type == null || type.trim().isEmpty()) {
			return false;
		}
		// checa se type � um dos valores aceitos
		if (validTypes.contains(type) == false) {
			return false;
		}
		// checa se o valor � v�lido
		if (value <= 0) {
			return false;
		}

		// copia todos os dados do banco de dados num ArrayList
		File databaseFile = new File(path + databaseName);
		Path filePath = databaseFile.toPath();
		ArrayList<String> linhas = (ArrayList<String>) Files.readAllLines(filePath);

		// identifica qual linha corresponde ao tipo informado
		int linha = 0;
		switch (type) {
		case "O+":
			linha = 0;
			break;
		case "O-":
			linha = 1;
			break;
		case "A+":
			linha = 2;
			break;
		case "A-":
			linha = 3;
			break;
		case "B+":
			linha = 4;
			break;
		case "B-":
			linha = 5;
			break;
		case "AB+":
			linha = 6;
			break;
		case "AB-":
			linha = 7;
			break;
		}

		// identifica o valor atual e realiza a opera��o
		float valorAtual = Float.valueOf(linhas.get(linha));
		if (operation == 0) {// soma
			linhas.set(linha, "" + (valorAtual + value));
		} else {// subtra��o
			// tenta subtrair mais que o valor existente
			if (valorAtual < value) {
				return false;
			} else {
				linhas.set(linha, "" + (valorAtual - value));
			}
		}
		Files.write(filePath, linhas);
		return true;
	}
}
