/**
 * Chess.java
 * Reedit Syed Shahriar
 *
 * 25 April 2017 - ongoing
 * 
 * (C) Copyright Reedit Syed Shahriar 2017.
 * All rights reserved.
 *
 * This program sets up a 2-player chess game on the local
 * machine. It is used as a framework, through inheritance,
 * for the ChessServer and ChessClient programs. As of 28
 * May 2017, it is a work in progress.
 *
 * TODO:
 * 	Implement pawn promotion
 *  Implement checks (partly done)
 *  Implement en passant
 *  Fix issues with capturing pieces
 *  Fix issues with castling
 * 
 */

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.awt.event.*;

import java.awt.image.ImageObserver;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Chess extends JFrame{

	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
		
		new Chess();
		
	}
	
	public Chess(){
		
		super("Chess");
		setSize(800,835);
		setLocation(400,50);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		ChessPanel panel = new ChessPanel();
		setContentPane(panel);
		setResizable(false);
		setVisible(true);
		
	}

}

class ChessPanel extends JPanel implements MouseListener,KeyListener{
	
	private static final long serialVersionUID = 1L;
	
	protected Tile[][] board;
	private Piece[] white,black;
	private Image whiteImg,blkImg;
	protected boolean whtMove,selecting,justMoved;
	private Piece selPc;
	private String move;
	
	public ChessPanel(){
		
		setBackground(Color.WHITE);
		board = new Tile[8][8];
		boolean white = true;
		
		selecting = false;
		selPc = null;
		
		for (int x=0;x<8;x++){
			for (int y=0;y<8;y++){
				board[x][y] = new Tile(x,y,white);
				white = !white;
			}
			white = !white;
		}
		
		try {
			whiteImg = ImageIO.read(new File("chessImg.png"));
			blkImg = ImageIO.read(new File("blkChessImg.png"));
		} catch (IOException e) {
			System.err.println("Can't find image. Quitting...");
			System.exit(1);
		}
		
		whtMove = true;
		justMoved = false;
		
		move = "";
		
		this.white = new Piece[16];
		this.black = new Piece[16];
		
		for (int i=0;i<8;i++){	// instantiate pawns
			this.white[i] = new Pawn(i,6,true);
			this.black[i] = new Pawn(i,1,false);
		}
		
		this.white[8] = new Rook(0,7,true);
		this.white[15] = new Rook(7,7,true);
		this.black[8] = new Rook(0,0,false);
		this.black[15] = new Rook(7,0,false);
		
		this.white[9] = new Knight(1,7,true);
		this.white[14] = new Knight(6,7,true);
		this.black[9] = new Knight(1,0,false);
		this.black[14] = new Knight(6,0,false);
		
		this.white[10] = new Bishop(2,7,true);
		this.white[13] = new Bishop(5,7,true);
		this.black[10] = new Bishop(2,0,false);
		this.black[13] = new Bishop(5,0,false);
		
		this.white[11] = new Queen(3,7,true);
		this.white[12] = new King(4,7,true);
		this.black[11] = new Queen(3,0,false);
		this.black[12] = new King(4,0,false);
		
		for (int i=0;i<16;i++){
			board[this.white[i].getX()][this.white[i].getY()].setPiece(this.white[i]);
			board[this.black[i].getX()][this.black[i].getY()].setPiece(this.black[i]);
		}
		
		addMouseListener(this);
		addKeyListener(this);
		
	}
	
	public boolean getWhtMove(){return whtMove;}
	
	public void paintComponent(Graphics g){
		
		for (int x=0;x<8;x++){
			for (int y=0;y<8;y++)
				board[x][y].draw(g);
		}
		
		for (int i=0;i<16;i++){
			this.white[i].draw(g);
			this.black[i].draw(g);
		}
		
	}
	
	class Pawn extends Piece{
		
		public Pawn(int x,int y,boolean white) {
			
			super(x,y,white);
			type = 1;
			
		}
		
		public void draw(Graphics g){
			
			int x = this.x+1,y = this.y+1;
			
			if (!taken){
				if ((this.x+this.y)%2==0){
					if (white) g.drawImage(whiteImg,(x-1)*100,(y-1)*100,x*100,y*100,1667,0,2000,333,this);
					else g.drawImage(whiteImg,(x-1)*100,(y-1)*100,x*100,y*100,1667,333,2000,667,this);
				} else {
					if (white) g.drawImage(blkImg,(x-1)*100,(y-1)*100,x*100,y*100,1667,0,2000,333,this);
					else g.drawImage(blkImg,(x-1)*100,(y-1)*100,x*100,y*100,1667,333,2000,667,this);
				}
			}
			
		}
		
		public boolean allowedMove(int x,int y,boolean taking){
			
			int deltaX = Math.abs(this.x-x),deltaY = y-this.y;
			
			if (white){
				
				switch (deltaY){
				case -1:
					if (deltaX == 0 && !taking) return true;
					else if (deltaX == 1) if (taking) return true;
					break;
				case -2:
					if (this.y == 6 && deltaX == 0){
						if (isJumping(Arrays.asList(new Tile[]{board[this.x][5]})))
							return false;
						return true;
					}
					break;
				default: break;
				}
				
			} else {
				switch (deltaY){
				case 1:
					if (deltaX == 0 && !taking) return true;
					else if (deltaX == 1) if (taking) return true;
					break;
				case 2:
					if (this.y == 1 && deltaX == 0){
						if (isJumping(Arrays.asList(new Tile[]{board[this.x][2]})))
							return false;
						return true;
					}
					break;
				default: break;
				}
				
			}
			
			return false;
				
		}
		
	}
	
	class Queen extends Piece{

		public Queen(int x, int y, boolean white) {
			
			super(x, y, white);
			type = 2;
			
		}
		
		public void draw(Graphics g){
			
			int x = this.x+1,y = this.y+1;
			
			if (!taken){
				if ((this.x+this.y)%2==0){
					if (white) g.drawImage(whiteImg,(x-1)*100,(y-1)*100,x*100,y*100,333,0,667,333,this); // fixed
					else g.drawImage(whiteImg,(x-1)*100,(y-1)*100,x*100,y*100,333,333,667,667,this);
				} else {
					if (white) g.drawImage(blkImg,(x-1)*100,(y-1)*100,x*100,y*100,333,0,667,333,this); // fixed
					else g.drawImage(blkImg,(x-1)*100,(y-1)*100,x*100,y*100,333,333,667,667,this);
				}
			}
			
		}
		
		public boolean allowedMove(int x,int y,boolean taking){
			
			int deltaX = x-this.x,deltaY = y-this.y;
			////System.out.println("deltaX: "+deltaX+", deltaY: "+deltaY);
			
			List<Tile> tiles = new ArrayList<Tile>();
			
			if (deltaX == 0 || deltaY == 0){
				if (deltaX != 0){
					if (deltaX > 0){
						for (int i=1;i<=deltaX;i++)
							tiles.add(board[this.x+i][this.y]);
					} else {
						for (int i=-1;i>=deltaX;i--)
							tiles.add(board[this.x+i][this.y]);
					}
				} else if (deltaY != 0){
					if (deltaY > 0){
						for (int i=1;i<=deltaY;i++)
							tiles.add(board[this.x][this.y+i]);
					} else {
						for (int i=-1;i>=deltaY;i--)
							tiles.add(board[this.x][this.y+i]);
					}
				}
				if (isJumping(tiles)) return false;
				return true;
			}
			else if (Math.abs(deltaX) == Math.abs(deltaY)){
				boolean plusX = (this.x < x),plusY = (this.y < y);
				int xInd = this.x,yInd = this.y;
				
				for (int i=2;i<=deltaX;i++){
					
					if (plusX){
						if (plusY){
							xInd++;
							yInd++;
							tiles.add(board[xInd][yInd]);
						} else {
							xInd++;
							yInd--;
							tiles.add(board[xInd][yInd]);
						}
					} else {
						if (plusY){
							xInd--;
							yInd++;
							tiles.add(board[xInd][yInd]);
						} else {
							xInd--;
							yInd--;
							tiles.add(board[xInd][yInd]);
						}
					}
					
				}
				if (isJumping(tiles)) return false;
				return true;
			}
			else return false;
			
		}
		
	}
	
	public Piece[] getWhtPcs(){return white;}
	
	public Piece[] getBlkPcs(){return black;}
	
	class King extends Piece{
		
		private boolean canCastle;
		
		public King(int x, int y, boolean white) {
			
			super(x, y, white);
			canCastle = true;
			type = 3;
			
		}
		
		public void draw(Graphics g){
			
			int x = this.x+1,y = this.y+1;
			
			if (!taken){
				if ((this.x+this.y)%2==0){
					if (white) g.drawImage(whiteImg,(x-1)*100,(y-1)*100,x*100,y*100,0,0,333,333,this); // fixed
					else g.drawImage(whiteImg,(x-1)*100,(y-1)*100,x*100,y*100,0,333,333,667,this);
				} else {
					if (white) g.drawImage(blkImg,(x-1)*100,(y-1)*100,x*100,y*100,0,0,333,333,this); // fixed
					else g.drawImage(blkImg,(x-1)*100,(y-1)*100,x*100,y*100,0,333,333,667,this);
				}
			}
			
		}
		
		public boolean isInCheck(){
			
			if (white){
				for (Piece pc:getBlkPcs()){
					if (pc.allowedMove(x,y,true)){
						////System.out.println("true");
						return true;
					}
				}
			} else {
				for (Piece pc:getWhtPcs()){
					if (pc.allowedMove(x,y,true)){
						////System.out.println("true");
						return true;
					}
				}
			}
			////System.out.println("false");
			return false;
			
		}
		
		public boolean allowedMove(int x,int y,boolean taking){
			
			int deltaX = Math.abs(this.x-x),deltaY = Math.abs(this.y-y);
			
			if (!(deltaX == 2 && deltaY == 0)){
				
				canCastle = false;
				return (deltaX < 2 && deltaY < 2);
				
			} else if (canCastle){
				
				boolean allowed = (!isJumping(Arrays.asList(new Tile[]{board[this.x+1][this.y]})));
				
				if (allowed){
					if (this.x < x){
						if (white)
							getWhtPcs()[15].castle();
						else
							getBlkPcs()[15].castle();
					} else {
						if (white)
							getWhtPcs()[8].castle();
						else
							getBlkPcs()[8].castle();
					}
				}
				
				return allowed;
				
			} else return false;
			
		}
		
	}
	
	class Rook extends Piece{
		
		private boolean canCastle;
		
		public Rook(int x,int y,boolean white) {
			
			super(x,y,white);
			type = 4;
			canCastle = true;
			
		}
		
		public void quickMoveTo(int x,int y){
			
			board[this.x][this.y].setPiece(null);
			this.x = x;
			this.y = y;
			board[x][y].setPiece(this);
			
		}
		
		public void castle(){
			
			if (canCastle){
				if (x==7){
					if (white) quickMoveTo(5,7);
					else quickMoveTo(5,0);
				} else {
					if (white) quickMoveTo(3,7);
					else quickMoveTo(3,0);
				}
			}
			
		}
		
		public void draw(Graphics g){
			
			int x = this.x+1,y = this.y+1;
			
			if (!taken){
				if ((this.x+this.y)%2==0){
					if (white) g.drawImage(whiteImg,(x-1)*100,(y-1)*100,x*100,y*100,1333,0,1667,333,this); // fixed
					else g.drawImage(whiteImg,(x-1)*100,(y-1)*100,x*100,y*100,1333,333,1667,667,this);
				} else {
					if (white) g.drawImage(blkImg,(x-1)*100,(y-1)*100,x*100,y*100,1333,0,1667,333,this); // fixed
					else g.drawImage(blkImg,(x-1)*100,(y-1)*100,x*100,y*100,1333,333,1667,667,this);
				}
			}
			
		}
		
		public boolean allowedMove(int x,int y,boolean taking){
			
			int deltaX = x-this.x,deltaY = y-this.y;
			
			List<Tile> tiles = new ArrayList<Tile>();
			
			if (deltaX != 0){
				if (deltaX > 0){
					for (int i=1;i<=deltaX;i++)
						tiles.add(board[this.x+i][this.y]);
				} else {
					for (int i=-1;i>=deltaX;i--)
						tiles.add(board[this.x+i][this.y]);
				}
			} else if (deltaY != 0){
				if (deltaY > 0){
					for (int i=1;i<=deltaY;i++)
						tiles.add(board[this.x][this.y+i]);
				} else {
					for (int i=-1;i>=deltaY;i--)
						tiles.add(board[this.x][this.y+i]);
				}
			}
			
			if (isJumping(tiles)) return false;
			
			if (deltaX == 0 || deltaY == 0){
				canCastle = false;
				return true;
			}
			else return false;
				
		}
		
	} 
	
	class Knight extends Piece{
		
		public Knight(int x,int y,boolean white) {
			
			super(x,y,white);
			type = 5;
			
		}
		
		public void draw(Graphics g){
			
			int x = this.x+1,y = this.y+1;
			
			if (!taken){
				if ((this.x+this.y)%2==0){
					if (white) g.drawImage(whiteImg,(x-1)*100,(y-1)*100,x*100,y*100,1000,0,1333,333,this); // fixed
					else g.drawImage(whiteImg,(x-1)*100,(y-1)*100,x*100,y*100,1000,333,1333,667,this);
				} else {
					if (white) g.drawImage(blkImg,(x-1)*100,(y-1)*100,x*100,y*100,1000,0,1333,333,this); // fixed
					else g.drawImage(blkImg,(x-1)*100,(y-1)*100,x*100,y*100,1000,333,1333,667,this);
				}
			}
			
		}
		
		public boolean allowedMove(int x,int y,boolean taking){
			
			int deltaX = Math.abs(this.y-y),deltaY = Math.abs(this.x-x);
			
			if ((deltaX==2 && deltaY==1)||(deltaX==1 && deltaY==2)) return true;
			else return false;
			
		}
		
	}
	
	class Bishop extends Piece{
		
		public Bishop(int x,int y,boolean white) {
			
			super(x,y,white);
			type = 6;
			
		}
		
		public void draw(Graphics g){
			
			int x = this.x+1,y = this.y+1;
			
			if (!taken){
				if ((this.x+this.y)%2==0){
					if (white) g.drawImage(whiteImg,(x-1)*100,(y-1)*100,x*100,y*100,667,0,1000,333,this); // fixed
					else g.drawImage(whiteImg,(x-1)*100,(y-1)*100,x*100,y*100,667,333,1000,667,this);
				} else {
					if (white) g.drawImage(blkImg,(x-1)*100,(y-1)*100,x*100,y*100,667,0,1000,333,this); // fixed
					else g.drawImage(blkImg,(x-1)*100,(y-1)*100,x*100,y*100,667,333,1000,667,this);
				}
			}
			
		}
		
		public boolean allowedMove(int x,int y,boolean taking){
			
			int deltaX = Math.abs(this.y-y),deltaY = Math.abs(this.x-x);
			boolean plusX = (this.x < x),plusY = (this.y < y);
			int xInd = this.x,yInd = this.y;
			
			if (deltaX != deltaY) return false;
			
			List<Tile> tiles = new ArrayList<Tile>();
			
			for (int i=1;i<deltaX;i++){
				
				if (plusX){
					if (plusY){
						xInd++;
						yInd++;
						tiles.add(board[xInd][yInd]);
					} else {
						xInd++;
						yInd--;
						tiles.add(board[xInd][yInd]);
					}
				} else {
					if (plusY){
						xInd--;
						yInd++;
						tiles.add(board[xInd][yInd]);
					} else {
						xInd--;
						yInd--;
						tiles.add(board[xInd][yInd]);
					}
				}
				
			}
			
			if (isJumping(tiles)) return false;
			
			if (deltaX == deltaY) return true;
			else return false;
			
		}
		
	}
	
	class Piece implements ImageObserver{

		protected int x,y,type;
		protected boolean white,taken;
		
		public Piece(int x,int y,boolean white){
			
			this.x = x;
			this.y = y;
			this.type = 0;
			this.white = white;
			this.taken = false;
			
		}
		
		public void quickMoveTo(int x,int y){
			
			this.x = x;
			this.y = y;
			
		}
		
		public void castle(){}
		
		public String toString(){
			
			String out = null;
			if (white) out = "White ";
			else out = "Black ";
			
			switch (type){
			case 0:
				out += "piece";
				break;
			case 1:
				out += "pawn";
				break;
			case 2:
				out += "queen";
				break;
			case 3:
				out += "king";
				break;
			case 4:
				out += "rook";
				break;
			case 5:
				out += "knight";
				break;
			case 6:
				out += "bishop";
				break;
			}
			
			out += " at ("+x+","+y+")";
			return out;
			
		}
		
		public boolean isWhite(){return white;}
		
		public int getX(){return x;}
		
		public int getY(){return y;}
		
		public boolean isInCheck(){return false;}
		
		public boolean movedIntoCheck(int x,int y){
			
			boolean out = false;
			int i;
			int prevX = this.x;
			int prevY = this.y;
			
			if (white){
				for (i=0;i<16;i++)
					if (getWhtPcs()[i].equals(this)) break;
				
				getWhtPcs()[i].quickMoveTo(x,y);
				out = getBlkPcs()[12].isInCheck();
				getWhtPcs()[i].quickMoveTo(prevX,prevY);
			} else {
				for (i=0;i<16;i++)
					if (getBlkPcs()[i].equals(this)) break;
				
				getBlkPcs()[i].quickMoveTo(x,y);
				out = getWhtPcs()[12].isInCheck();
				getBlkPcs()[i].quickMoveTo(prevX,prevY);
			}
			
			//System.out.println(this+" to ("+x+","+y+"): "+out);
			return out;
			
		}
		
		public void draw(Graphics g){}
		
		public boolean moveTo(int x,int y,boolean taking){
			
			////System.out.println(allowedMove(x,y));
			
			if (allowedMove(x,y,taking)){
				
				boolean doNotMove = (white && !movedIntoCheck(x,y) && getWhtPcs()[12].isInCheck()) ||
					(!white && !movedIntoCheck(x,y) && getBlkPcs()[12].isInCheck());
				
				if (doNotMove) return false;
				
				if (taking){
					////System.out.println(board[x][y].hasPiece());
					board[x][y].setPiece(null);
				}
				
				board[x][y].setPiece(this);
				board[this.x][this.y].setPiece(null);
				int prevX = this.x;
				int prevY = this.y;
				this.x = x;
				this.y = y;
				
				move = selPc+" from ("+prevX+","+prevY+")";
				
				//System.out.println("Moved piece!");
				return true;
				
			} else return false;
				
		}
		
		public boolean isJumping(List<Tile> tiles){
			
			for (Tile tile:tiles){
				
				if (tile.hasPiece()) return true;
				
			}
			
			return false;
			
		}
		
		public boolean exists(){
			
			return taken;
			
		}
		
		public boolean allowedMove(int x,int y,boolean taking){return true;}
		
		public void capture(){
			
			this.taken = true;
			this.x = this.y = 0;
			
		}
		
		public boolean imageUpdate(Image arg0, int arg1,
				int arg2, int arg3, int arg4, int arg5) {
			return false;
		}
		
	}
	
	class Tile{
		
		private boolean white;
		private int x,y;
		private Piece piece;
		
		public Tile(int x,int y,boolean white){
			
			this.x = x;
			this.y = y;
			this.white = white;
			this.piece = null;
			
		}
		
		public Tile(int x,int y,boolean white,Piece piece){
			
			this.x = x;
			this.y = y;
			this.white = white;
			this.piece = piece;
			
		}
		
		public String toString(){return ("("+x+","+y+")");}
		
		public void draw(Graphics g){
			
			Color oldColor = g.getColor();
			if (white) g.setColor(Color.WHITE);
			else g.setColor(Color.GRAY);
			g.fillRect(x*100,y*100,100,100);
			g.setColor(oldColor);
			
		}
		
		public boolean hasPiece(){return (piece != null);}
		
		public void setPiece(Piece piece){this.piece = piece;}
		
		public Piece getPiece(){return piece;}
	
	}
	
	public String getMove(){return move;}

	public void mouseClicked(MouseEvent e) {
		
		requestFocusInWindow();
		
		int x = (e.getX()/100),y = (e.getY()/100);
		boolean taking = false;
		
		if (!selecting && board[x][y].hasPiece()){
			
			try{
				if (whtMove == board[x][y].getPiece().isWhite()){
					selPc = board[x][y].getPiece();
					selecting = true;
					//System.out.println(selPc);
				}
			} catch (NullPointerException exc){}
			
		}
		else if (selecting){
			
			try{
				if (whtMove == board[x][y].getPiece().isWhite()){
					selPc = board[x][y].getPiece();
					//System.out.println(selPc);
				} else { // taking piece
					taking = true;
					throw new NullPointerException();
				}
			} catch (NullPointerException exc){
				if (selPc.moveTo(x,y,taking)){
					selPc = null;
					selecting = false;
					whtMove = !(whtMove);
					justMoved = true;
				}
			}
			
		}
		
		repaint();
		
	}

	public void mouseEntered(MouseEvent e) {}

	public void mouseExited(MouseEvent e) {}

	public void mousePressed(MouseEvent e) {}

	public void mouseReleased(MouseEvent e) {}

	public void keyPressed(KeyEvent e) {
		
		int key = e.getKeyCode();
		if (key == KeyEvent.VK_W && e.isControlDown()) System.exit(0);
		
	}

	public void keyReleased(KeyEvent e) {}

	public void keyTyped(KeyEvent e) {}
	
}

class ChatPanel extends JPanel implements ActionListener{
	
	protected JTextField jtf;
	protected List<Message> messages;
	protected MessagePanel mp;
	protected PrintWriter pw;
	
	public ChatPanel(){
		
		setLayout(null);
		
		mp = new MessagePanel();
		mp.setBounds(0,0,200,700);
		
		add(mp);
		
		messages = new ArrayList<Message>();
		
		JButton button = new JButton("SEND");
		jtf = new JTextField("Message");
		
		button.addActionListener(this);
		jtf.addActionListener(this);
		
		jtf.setBounds(10,720,110,40);
		button.setBounds(120,720,70,40);
		
		add(button);
		add(jtf);
		
		setBackground(Color.WHITE);
		
	}
	
	public void addMessage(String msg){
		
		Message tempMsg = new Message(msg);
		messages.add(tempMsg);
		mp.repaint();
		
	}
	
	public void send(Message msg){
		
		pw.println(msg.getContent());
		pw.flush();
		
	}

	public void actionPerformed(ActionEvent e) {
		
		int maxLen = 0;
		for (Message msg:messages)
			maxLen = Math.max(maxLen,msg.getContent().length());
		
		mp.setMaxLen(maxLen);
		
	}
	
	public MessagePanel getMsgPanel(){return mp;}
	
	class MessagePanel extends JPanel implements AdjustmentListener{
		
		private int x,y,maxLen;
		private JScrollBar scroll,horizScroll;
		
		public MessagePanel(){
			
			setBackground(Color.WHITE);
			
			x = 5;
			y = 25;
			maxLen = 0;
			
			setLayout(null);
			
			scroll = new JScrollBar(JScrollBar.VERTICAL,0,700,0,700);
			scroll.setBounds(175,0,25,675);
			scroll.addAdjustmentListener(this);
			add(scroll);
			
			horizScroll = new JScrollBar(JScrollBar.HORIZONTAL,0,200,0,200);
			horizScroll.setBounds(0,675,200,25);
			horizScroll.addAdjustmentListener(this);
			add(horizScroll);
			
		}
		
		public void setMaxLen(int maxLen){
			
			this.maxLen = maxLen;
			updateSliders();
		
		}
		
		public void updateSliders(){
			
			if (messages.size() > 14)
				scroll.setVisibleAmount((int)(700*14.0/messages.size()));
			y = 25-scroll.getValue();
			
			if (maxLen > 17)
				horizScroll.setVisibleAmount((int)(200*17.0/maxLen));
			x = 5-(2*horizScroll.getValue());
			
			repaint();
			
		}

		public void adjustmentValueChanged(AdjustmentEvent e){updateSliders();}
		
		public void paintComponent(Graphics g){
			
			super.paintComponent(g);
			
			int yVal = y;
			
			g.setFont(new Font("Monospaced",0,12));
			
			for (Message msg:messages){
				g.drawString(msg.getContent(),x,yVal);
				yVal += 25;
				g.drawString(msg.getTimestamp()+"",x,yVal);
				yVal += 25;
			}
			
			if (messages.size() > 6) 
				scroll.setVisibleAmount((int)(700*(14.0/messages.size())));
			
		}
		
	}
	
	class Message{
		
		private String content;
		private Date time;
		
		public Message(String content){
			
			time = new Date();
			this.content = content;
		
		}
		
		public String getContent(){
			return content;
		}
		
		public String getTimestamp(){
			
			String out = time.toString();
			out = out.substring(11,19);
			return out;
			
		}
		
		public String toString(){return content;}
		
	}
	
}
