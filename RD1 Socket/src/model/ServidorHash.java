package model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Um programa servidor o qual aceita pedidos de clientes para retornar o
 * hashCode da String enviada. Quando um cliente conecta, uma nova thread �
 * iniciada para lidar com um di�logo interativo no qual o cliente envia Strings
 * e o thread do servidor retorna o hashCode correspondente.
 *
 * O programa roda num loop infinito, sendo ent�o necess�rio o desligamento
 * manual do servidor. Caso seja rodado no console Java, normalmente o atalho
 * Ctrl+C ir� encerrar o processo.
 */
public class ServidorHash {

	/**
	 * Inicia o servidor num loop infinito na porta 9898. Quando h� um pedido de
	 * conex�o, ela � aberta num novo thread e retorna imediatamente a resposta
	 * ao cliente.
	 */
	public static void main(String[] args) throws Exception {
		System.out.println("O servidor de HashCode entrou em execu��o.");
		int numCliente = 0;
		ServerSocket socketServidor = new ServerSocket(9898);
		try {
			while (true) {
				new TransformadorHash(socketServidor.accept(), numCliente++).start();
			}
		} finally {
			socketServidor.close();
		}
	}

	/**
	 * Um thread privado para lidar com os pedidos de hashCode num socket
	 * espec�fico. O cliente finaliza o di�logo enviando uma String com somente
	 * um ponto (".").
	 */
	private static class TransformadorHash extends Thread {
		private Socket socket;
		private int numCliente;

		public TransformadorHash(Socket socket, int clientNumber) {
			this.socket = socket;
			this.numCliente = clientNumber;
			log("Nova conex�o com o cliente #" + clientNumber + " em " + socket);
		}

		/**
		 * Envia uma mensagem de boas vindas ao cliente e come�a a ler Strings
		 * para retornar o valor correspondente em hashCode at� que o cliente
		 * encerre a conex�o.
		 */
		public void run() {
			try {
				// l� as entradas de String do cliente
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				// retorna a sa�da (hashCode correspondente) ao cliente
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

				// envia mensagem de boas vindas e instru��o de uso para o
				// cliente
				out.println("Ol�, voc� � o cliente #" + numCliente + ".");
				out.println("Insira uma mensagem para receber o HashCode correspondente."
						+ " Insira somente um ponto ('.') para finalizar a conex�o.\n");

				// Recebe as mensagens do cliente, linha por linha. Para cada
				// uma, retorna o hashCode correspondente.
				while (true) {
					String entrada = in.readLine();
					if (entrada == null || entrada.equals(".")) {
						break;
					}
					// retorna o hashCode correspondente
					out.println(entrada.hashCode());
				}
			} catch (IOException e) {
				log("Erro ao lidar com o cliente #" + numCliente + ": " + e.getMessage());
			} finally {
				try {
					// fecha o socket
					socket.close();
				} catch (IOException e) {
					log("N�o foi poss�vel fechar o socket.");
				}
				log("Conex�o com o cliente #" + numCliente + " encerrada.");
			}
		}

		/**
		 * Registra uma mensagem. Neste caso particular trata-se somente de
		 * escrever uma mensagem na aplica��o.
		 */
		private void log(String message) {
			System.out.println(message);
		}
	}
}
