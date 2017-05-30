/**
 * ChessClient.java
 * Reedit Syed Shahriar
 * 
 * 27 May 2017 - 29 May 2017
 * 
 * (C) Copyright Reedit Syed Shahriar 2017.
 * All rights reserved.
 * 
 * This program sets up a 2-player chess client at
 * the specified port and IP address. When a connection
 * is made with a ChessServer at the correct port, a
 * game of chess starts. The server plays as the white
 * side, while the client plays as the black side.
 * Chat is also permitted through a side panel.
 */

import java.net.*;
import javax.swing.*;

import java.util.*;
import java.io.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.*;

public class ChessClient extends JFrame{

	private ChessClientPanel panel;
	private ChatClientPanel chat;
	
	public static void main(String[] args) {
		
		new ChessClient();
		
	}
	
	public ChessClient(){
		
		super("Chess (Client)");
		setSize(1000,835);
		setLocation(400,50);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLayout(null);
		
		panel = new ChessClientPanel();
		panel.setBounds(0,0,800,835);
		getContentPane().add(panel);
		
		chat = new ChatClientPanel();
		chat.setBounds(800,0,200,835);
		getContentPane().add(chat);
		
		setResizable(false);
		setVisible(true);
		
	}
	
	class ChatClientPanel extends ChatPanel implements ActionListener{
		
		public ChatClientPanel(){
			
			super();
			
			try{
				pw = new PrintWriter(panel.getConnection().getOutputStream());
			} catch (IOException e){
				System.err.println("Cannot create server.");
				System.exit(1);
			}
			
		}

		public void actionPerformed(ActionEvent e) {
			
			if (e.getActionCommand().equals("SEND") && !jtf.getText().equals("")){
				Message msg = new Message("Black: "+jtf.getText());
				super.send(msg);
				messages.add(msg);
				jtf.setText("");
			}
			
			super.actionPerformed(e);
			mp.repaint();
			
		}
		
	}
	
	class ChessClientPanel extends ChessPanel{
		
		private Socket s;
		
		public ChessClientPanel(){
			
			super();
			
			Scanner sc = new Scanner(System.in);
			
			System.out.print("Enter IP address: ");
			String ip = sc.nextLine();
			System.out.print("Enter port number: ");
			int port = sc.nextInt();
			
			System.out.println("Waiting for connection...");
			s = null;
			try{
				s = new Socket(ip,port);
			} catch (IOException e){
				System.err.println("Connection not found. Quitting...");
				System.exit(1);
			}
			
			System.out.println("Connection found!");
			
			new ReceiveThread().start();
			
			sc.close();
			
		}
		
		public Socket getConnection(){return s;}
		
		public void send(String msg){
			
			PrintWriter pw = null;
			try{
				pw = new PrintWriter(s.getOutputStream());
			} catch (IOException e){
				System.err.println("Cannot transmit moves. Quitting...");
				System.exit(1);
			}
			
			pw.println(msg);
			pw.flush();
			
			justMoved = false;
		
		}
		
		public void mouseClicked(MouseEvent e){
			
			if (!whtMove) super.mouseClicked(e);
			if (!selecting && justMoved) send(getMove());
			
		}
		
		public void analyze(String msg){
			
			if (msg.charAt(5) != ':'){
				String oldCoords = msg.substring(msg.lastIndexOf('('));
				String newCoords = msg.substring(msg.indexOf('('),msg.indexOf(')')+1);
				
				int oldX = Integer.parseInt(oldCoords.charAt(1)+"");
				int oldY = Integer.parseInt(oldCoords.charAt(3)+"");
				int newX = Integer.parseInt(newCoords.charAt(1)+"");
				int newY = Integer.parseInt(newCoords.charAt(3)+"");
				
				Piece pc = board[oldX][oldY].getPiece();
				pc.moveTo(newX,newY,board[newX][newY].hasPiece());
				whtMove = false;
			} else chat.addMessage(msg);
			
			chat.getMsgPanel().updateSliders();
			repaint();
			chat.repaint();
			
		}
		
		class ReceiveThread extends Thread{
			
			public void run(){
				
				Scanner sc = null;
				try{
					sc = new Scanner(panel.getConnection().getInputStream());
				} catch (IOException e){
					System.err.println("Cannot send messages to client. Quitting...");
					System.exit(1);
				}
				
				while (true)
					panel.analyze(sc.nextLine());
				
			}
			
		}
		
	}

}