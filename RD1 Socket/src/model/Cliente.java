package model;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * Uma interface feita em Swing para o cliente do servidor de sangue. Trata-se
 * de um JFrame com um campo de texto para que as mensagems possam ser escritas.
 * Os resultados são visualizados logo abaixo num JTextArea.
 */
public class Cliente {

	private BufferedReader in;
	private PrintWriter out;
	private JFrame frame = new JFrame("Terminal do Servidor de Sangue");

	private JTextField campoDeTexto = new JTextField(40);
	private JTextArea areaDeMensagens = new JTextArea(8, 60);

	/**
	 * Constrói o layout do cliente e regista o evento de enviar a mensagem
	 * escrita no campo de texto para o servidor ao pressionar a tecla ENTER.
	 */
	public Cliente() {

		// layout
		campoDeTexto.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
		campoDeTexto.setBackground(Color.GRAY);
		campoDeTexto.setForeground(Color.GREEN);
		areaDeMensagens.setEditable(false);
		areaDeMensagens.setMargin(new Insets(10, 10, 10, 10));
		areaDeMensagens.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		areaDeMensagens.setBackground(Color.BLACK);
		areaDeMensagens.setForeground(Color.GREEN);
		frame.setPreferredSize(new Dimension(1000, 500));
		frame.getContentPane().add(campoDeTexto, "North");
		frame.getContentPane().add(new JScrollPane(areaDeMensagens), "Center");

		// listeners
		campoDeTexto.addActionListener(new ActionListener() {
			/**
			 * Envia o conteúdo do campo de texto para o servidor ao pressionar
			 * a tecla ENTER. A resposta do servidor aparece na área de
			 * mensagens. Caso o comando 'desconectar' seja enviado, serão
			 * encerrados a conexão com o servidor e a instância do cliente.
			 */
			public void actionPerformed(ActionEvent e) {
				// envia o comando escrito na caixa de texto
				out.println(campoDeTexto.getText());
				if (campoDeTexto.getText().equalsIgnoreCase("desconectar")) {
					// O servidor finaliza a conexão ao receber o comando. Aqui,
					// apenas a instância de Cliente é encerrada
					System.exit(0);
				}
				// seleciona a mensagem enviada
				campoDeTexto.selectAll();
				// limpa os textos anteriores
				areaDeMensagens.setText("");

				try {
					// lê todas as respostas do servidor
					String line = in.readLine();
					while (line != null && line.isEmpty() == false) {
						areaDeMensagens.append(line + "\n");
						line = in.readLine();
					}
				} catch (IOException ex) {
					areaDeMensagens.append("Ocorreu o seguinte erro: " + ex.getLocalizedMessage() + "\n");
				}
			}
		});
	}

	/**
	 * Parte lógica da conexão cliente-servidor
	 * 
	 * Author: Madson
	 * 
	 * @throws IOException
	 */
	public void conectarAoServidor() throws IOException {

		// pergunta o endereço IP do servidor
		String enderecoServidor = JOptionPane.showInputDialog(frame, "Informe o endereço IPv4 do Servidor de Sangue:",
				"Bem vindo ao sistema do Servidor de Sangue", JOptionPane.QUESTION_MESSAGE);
		Socket socket = null;
		try {
			// inicializa o socket, conectandoo ao endereço IP do servidor na
			// porta 9898
			socket = new Socket(enderecoServidor, 9898);
		} catch (UnknownHostException | SocketException u) {
			// caso o IP inserido não seja o do servidor ou seja inválido,
			// continua pedindo o endereço IP até que um válido seja informado
			do {
				try {
					enderecoServidor = JOptionPane.showInputDialog(frame,
							"O endereço IP informado é inválido. Informe o endereço IPv4 do Servidor de Sangue:",
							"Bem vindo ao sistema do Servidor de Sangue", JOptionPane.QUESTION_MESSAGE);
					socket = new Socket(enderecoServidor, 9898);
				} catch (UnknownHostException | SocketException e) {
				}
			} while (socket == null);
		}

		// inicializa os objetos de entrada e saída
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new PrintWriter(socket.getOutputStream(), true);

		// recebe as mensagens de boas vindas e instruções do servidor
		String line = in.readLine();
		while (line != null && line.isEmpty() == false) {
			areaDeMensagens.append(line + "\n");
			line = in.readLine();
		}
	}

	/**
	 * Executa a aplicação cliente.
	 */
	public static void main(String[] args) throws Exception {
		Cliente cliente = new Cliente();
		cliente.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		cliente.frame.pack();
		cliente.frame.setVisible(true);
		cliente.conectarAoServidor();
	}
}