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
 * Um programa servidor o qual aceita conexões com clientes, os quais podem pedir informações e modificar dados do servidor. 
 * Cada cliente utiliza uma thread própria, permitindo multiplas conexões simultâneas ao servidor.
 *
 * O servidor é executado num loop infinito, sendo então necessário o encerramento manual do servidor. Caso seja executado no console
 * Java, normalmente o atalho Ctrl+C encerra o processo.
 */

/**
 * PROTOCOLO
 * 
 * FLUXO DE COMUNICAÇÃO
 * 
 * Primeiro o servidor deverá ser iniciado. Após isso, incontáveis clientes
 * podem ser conectados ao servidor ao mesmo tempo, cada um com uma instância de
 * Cliente.
 * 
 * Ao instanciar um cliente, ele irá pedir o endereço Ipv4 do servidor para
 * poder conectar-se. Se o usuário informar um endereço inválido, receberá uma
 * notificação de erro, e poderá digitar o endereço Ipv4 do servidor incontáveis
 * vezes até desistir ou informar o endereço correto. O cliente também pode
 * simplesmente apertar OK ou Cancelar, no pop-up que pede o endereço IP. Isso
 * irá conectar o cliente em localhost.
 * 
 * Após informar o endereço correto, será estabelecida a conexão entre o cliente
 * e o servidor num socket único, na porta 9898, permitindo a troca de
 * mensagens.
 * 
 * AÇÕES
 * 
 * Segue a lista de comandos que o cliente pode enviar ao servidor, e o retorno
 * que o servidor irá entregar ao cliente:
 * 
 * Comandos válidos:
 * 
 * 'comandos' : Lista todos os comandos válidos do sistema;
 * 
 * 'listar tudo' : Lista todos os dados do sistema;
 * 
 * 'listar tipos' : Lista os tipos sanguíneos e a prevalência de cada tipo de
 * sangue na população;
 * 
 * 'listar estoque' : lista a quantidade de sangue armazenada nesta unidade de
 * coleta de sangue;
 * 
 * 'listar compatibilidade' : Lista quais tipos sanguíneos podem doar e receber
 * sangue de outros tipos sanguíneos;
 * 
 * 'adicionar TIPO, VALOR' : Adiciona a quantidade VALOR de litros de sangue do
 * tipo sanguíneo TIPO no banco de dados;
 * 
 * 'remover TIPO, VALOR' : remove a quantidade VALOR de litros de sangue do tipo
 * sanguíneo TIPO no banco de dados; e
 * 
 * 'desconectar' : encerra a conexão com o servidor.
 * 
 * Os valores válidos para TIPO são: O+, O-, A+, A-, B+, B-, AB+, AB-
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
		System.out.println("O servidor entrou em execução.");
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
	 * Conexão servidor-cliente.
	 */
	private static class Instancia extends Thread {
		private Socket socket;
		private int numCliente;

		public Instancia(Socket socket, int clientNumber) {
			this.socket = socket;
			this.numCliente = clientNumber;
			System.out.println("Nova conexão com o cliente #" + clientNumber + " em " + socket);
		}

		/**
		 * Envia uma mensagem de boas vindas para os cliente recém-criados. Após
		 * isso, escuta mensagens do cliente e envia respostas enquanto
		 * estiverem conectados.
		 */
		public void run() {
			String instrucoes = "Comandos válidos:" + "\n'comandos' : Lista todos os comandos válidos do sistema;"
					+ "\n'listar tudo' : Lista todos os dados do sistema;"
					+ "\n'listar tipos' : Lista os tipos sanguíneos e a prevalência de cada tipo de sangue na população;"
					+ "\n'listar estoque' : lista a quantidade de sangue armazenada nesta unidade de coleta de sangue;"
					+ "\n'listar compatibilidade' : Lista quais tipos sanguíneos podem doar e receber sangue de outros tipos sanguíneos;"
					+ "\n'adicionar TIPO, VALOR' : Adiciona a quantidade VALOR de litros de sangue do tipo sanguíneo TIPO no banco de dados;"
					+ "\n'remover TIPO, VALOR' : remove a quantidade VALOR de litros de sangue do tipo sanguíneo TIPO no banco de dados; e"
					+ "\n'desconectar' : encerra a conexão com o servidor."
					+ "\nOs valores válidos para TIPO são: O+, O-, A+, A-, B+, B-, AB+, AB-\n";

			try {
				// lê as entradas de String do cliente
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				// retorna saídas ao cliente
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

				// envia mensagem de boas vindas para o cliente
				out.println("Olá, você é o cliente #" + numCliente + ".");
				// envia as instruções do sistema para o cliente
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
						// separa a entrada do cliente a cada espaço encontrado
						String[] comando = entrada.split(" ");
						// só é válido se tiver 3 palavras no comando
						if (comando.length == 3) {
							// checa se a primeira palavra é adicionar ou
							// remover
							if (comando[0].equalsIgnoreCase("adicionar") || comando[0].equalsIgnoreCase("remover")) {
								// checa se a segunda palavra contém vírgula no
								// final
								if (comando[1].substring(comando[1].length() - 1).equals(",")) {
									// checa se o tipo é algum dos 8 válidos
									String tipoSangue = comando[1].substring(0, comando[1].length() - 1);
									if (validTypes.contains(tipoSangue)) {
										try {
											// recebe o valor
											float valor = Float.valueOf(comando[2]);
											if (valor > 0) {
												int operacao = 0;
												// determina a operação
												if (comando[0].equalsIgnoreCase("remover")) {
													operacao = 1;
												}
												// executa a operação
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
																"Não foi possível completar a operação. Provavelmente o valor a ser "
																		+ "removido é maior do que o estoque para este tipo sanguíneo"
																		+ " no banco de dados.\n");
													} else {
														out.println("Não foi possível completar a operação.\n");
													}
												}
											} else
												out.println("O valor precisa ser maior que 0.\n");
										} catch (Exception e) {
											out.println("O valor informado é inválido.\n");
										}
									} else
										out.println(
												"Tipo de sangue não reconhecido. Digite 'comandos' para ver os tipos válidos.\n");
								} else
									out.println(
											"Aparentemente a vírgula está faltando. Padrão: 'adicionar/remover TIPO, VALOR'.\n");
							} else
								out.println(
										"Comando não reconhecido. Digite 'comandos' para ver os comandos válidos.\n");
						} else
							out.println("Comando não reconhecido. Digite 'comandos' para ver os comandos válidos.\n");
					}
				}
			} catch (IOException e) {
				System.out.println("Erro ao lidar com o cliente #" + numCliente + ": " + e.getMessage());
			} finally {
				try {
					// fecha o socket
					socket.close();
				} catch (IOException e) {
					System.out.println("Não foi possível fechar o socket.");
				}
				System.out.println("Conexão com o cliente #" + numCliente + " encerrada.");
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
					// checa se existe o número mínimo de linhas
					if (linhas.size() < 8) {
						System.out.println(
								"O banco de dados existente é inválido e será excluído. Será criado um banco de dados com os"
										+ " valores padrão.");
						databaseFile.delete();
						createDatabase();
					} else {
						// percorre todas as linhas
						for (int i = 0; i < linhas.size(); i++) {
							try {
								Float.valueOf(linhas.get(i));// checa se é um
																// número
							} catch (Exception e) {
								linhas.set(i, "0.0");
								System.out.println("O valor na linha " + i + " era inválido e foi substituído por 0.");
							}
						}
					}
					// reescreve o arquivo após checar os dados
					Files.write(path, linhas);
				} else {// banco de dados não existe
					createDatabase();
				}
			} else {// pasta não existe
				databaseDirectory.mkdirs();
				System.out.println("Pasta do banco de dados não existia, então foi criada.");
				createDatabase();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Cria o banco de dados com valores padrão(fictícios, para preencher o
	 * banco de dados) para cada tipo sanguíneo
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
			System.out.println("Banco de dados não pôde ser criado.");
			e.printStackTrace();
		}
	}

	/**
	 * Lista dados de acordo com a operação
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

				data += String.format("%-24s%-21s%-20s%-24s%-25s\n", "Tipo Sanguíneo", "Estoque (l)", "% do total",
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
				data = "Não foi possível executar esta consulta.";
			}
		} else if (operation == 1) {// listar tipos
			data += String.format("%-25s%-18s%-25s\n", "Grupo Sanguíneo", "Positivo", "Negativo");
			data += String.format("%-25s%-18s%-25s\n", "O", "36%", "9%");
			data += String.format("%-25s%-18s%-25s\n", "A", "34%", "8%");
			data += String.format("%-25s%-18s%-25s\n", "B", "8%", "2%");
			data += String.format("%-25s%-18s%-25s\n", "AB", "2.5%", "0.5%");
			data += String.format("%-25s%-18s%-25s\n", "Total", "80.5%", "19.5%");
			data += "Percentual de ocorrência dos tipos sanguíneos, considerando a população total do Brasil.\n";
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

				data += String.format("%-24s%-21s%-10s\n", "Tipo Sanguíneo", "Estoque (l)", "% do total");
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
				data = "Não foi possível executar esta consulta.\n";
			}
		} else {// listar compatibilidade
			data += String.format("%-24s%-24s%-25s\n", "Tipo Sanguíneo", "Pode doar para", "Pode receber de");
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
	 *            0 para soma, 1 para subtração
	 * @param type
	 *            tipos aceitos: "O+", "O-", "A+", "A-", "B+", "B-", "AB+",
	 *            "AB-"
	 * @param value
	 *            valor maior que 0 a ser somado ou subtraído
	 * @return
	 * @throws IOException
	 */
	private static boolean changeValue(int operation, String type, float value) throws IOException {
		// 0 == soma, 1 == subtração
		if (operation < 0 || operation > 1) {
			return false;
		}
		// checa se type é válido
		if (type == null || type.trim().isEmpty()) {
			return false;
		}
		// checa se type é um dos valores aceitos
		if (validTypes.contains(type) == false) {
			return false;
		}
		// checa se o valor é válido
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

		// identifica o valor atual e realiza a operação
		float valorAtual = Float.valueOf(linhas.get(linha));
		if (operation == 0) {// soma
			linhas.set(linha, "" + (valorAtual + value));
		} else {// subtração
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
