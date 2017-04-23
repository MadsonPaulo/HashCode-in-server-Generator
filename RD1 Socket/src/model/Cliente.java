package model;

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
 * Uma interface feita em Swing para o cliente do servidor de HashCode. Trata-se
 * de um JFrame com um campo de texto para que as mensagems possam ser escritas.
 * Os resultados s�o visualizados logo abaixo num JTextArea.
 */
public class Cliente {

	private BufferedReader in;
	private PrintWriter out;
	private JFrame frame = new JFrame("Cliente do Servidor de HashCode");
	private JTextField campoDeTexto = new JTextField(40);
	private JTextArea areaDeMensagens = new JTextArea(8, 60);

	/**
	 * Constr�i o layout do cliente e regista o evento de enviar a mensagem
	 * escrita no campo de texto para o servidor ao pressionar a tecla ENTER.
	 */
	public Cliente() {

		// layout
		areaDeMensagens.setEditable(false);
		frame.getContentPane().add(campoDeTexto, "North");
		frame.getContentPane().add(new JScrollPane(areaDeMensagens), "Center");

		// listeners
		campoDeTexto.addActionListener(new ActionListener() {
			/**
			 * Envia o conte�do do campo de texto para o servidor ao pressionar
			 * a tecla ENTER. A resposta do servidor aparece na �rea de
			 * mensagens. Caso um ponto ('.') seja enviado, o socket e a
			 * aplica��o s�o fechados
			 */
			public void actionPerformed(ActionEvent e) {
				out.println(campoDeTexto.getText());
				String resposta;
				try {
					resposta = in.readLine();
					if (resposta == null || resposta.equals("")) {
						System.exit(0);
					}
				} catch (IOException ex) {
					resposta = "Ocorreu o seguinte erro: " + ex.getLocalizedMessage();
				}
				areaDeMensagens.append(resposta + "\n");
				// seleciona o texto da mensagem enviada sem apag�-la,
				// permitindo ao cliente ver o que foi enviado ao mesmo tempo em
				// que pode ver a resposta na ultima linha da �rea de mensagens.
				// Pelo fato da mensagem enviada ficar automaticamente
				// selecionada, pode ser facilmente apagada ou substituida por
				// outra mensagem.
				campoDeTexto.selectAll();
			}
		});
	}

	/**
	 * Implementa a conex�o l�gica do servidor com o cliente.
	 */

	public void conectarAoServidor() throws IOException {

		// pergunta o endere�o IP do servidor
		String enderecoServidor = JOptionPane.showInputDialog(frame, "Informe o endere�o IP do servidor HashCode:",
				"Bem vindo ao gerador de HashCode", JOptionPane.QUESTION_MESSAGE);
		Socket socket = null;
		try {
			// inicializa o socket, conectandoo ao endere�o IP do servidor na
			// porta 9898
			socket = new Socket(enderecoServidor, 9898);
		} catch (UnknownHostException | SocketException u) {
			// caso o IP inserido n�o seja o do servidor ou seja inv�lido,
			// continua pedindo o endere�o IP at� que um v�lido seja informado
			do {
				try {
					enderecoServidor = JOptionPane.showInputDialog(frame,
							"O endere�o IP informado � inv�lido. Informe o endere�o IP do servidor HashCode:",
							"Bem vindo ao gerador de HashCode", JOptionPane.QUESTION_MESSAGE);
					socket = new Socket(enderecoServidor, 9898);
				} catch (UnknownHostException | SocketException e) {
				}
			} while (socket == null);
		}

		// inicializa os objetos de entrada e sa�da
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new PrintWriter(socket.getOutputStream(), true);

		// Consome as mensagens de boas vindas do servidor
		for (int i = 0; i < 3; i++) {
			areaDeMensagens.append(in.readLine() + "\n");
		}

	}

	/**
	 * Executa a aplica��o cliente.
	 */
	public static void main(String[] args) throws Exception {
		Cliente cliente = new Cliente();
		cliente.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		cliente.frame.pack();
		cliente.frame.setVisible(true);
		cliente.conectarAoServidor();
	}
}