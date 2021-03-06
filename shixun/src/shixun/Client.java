package shixun;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;


public class Client extends JFrame implements ActionListener{
	JPanel contenpane;
	
	JLabel label1=new JLabel("端口号");
	JLabel label2=new JLabel("服务器IP");
	JLabel label3=new JLabel("请输入信息");
	
	JTextField text1=new JTextField();
	JTextField text2=new JTextField();
	JTextField text3=new JTextField();
	
	JButton button1=new JButton("连接");
	JButton button2=new JButton("发送");
	JButton button3=new JButton("悔棋");
	//存放棋盘中棋子的信息
	int[][] allChess=new int[19][19];
	//显示信息
	String message=null;
	//棋盘面板
	JPanel panel2=null;
	//通信时用到的输入输出流
	Socket client=null;
	BufferedReader instr=null;
	PrintWriter os=null;
	//存放落子信息
	String[] ss=new String[10];
	//存放落子位置
	int x=0;
	int y=0;
	//判断是否能落子
	boolean canPlay=false;
	JTextArea textarea1=new JTextArea();
	JScrollPane jscrollpane=new JScrollPane();
	
	public Client(){
		//设置窗体
		contenpane=(JPanel)this.getContentPane();
		this.setTitle("客户端");
		this.setBounds(20, 20, 600, 700);
		//设置靠上的面板内容
		JPanel panel1=new JPanel();
		panel1.setLayout(null);
		label1.setBounds(new Rectangle(20,5,50,22));
		label1.setFont(new Font("宋体",0,14));
		text1.setBounds(new Rectangle(75,5,50,22));
		text1.setText("4321");
		
		label2.setBounds(new Rectangle(20,27,70,22));
		label2.setFont(new Font("宋体",0,14));
		
		text2.setBounds(new Rectangle(95,27,120,22));
		button1.setBounds(new Rectangle(220,27,100,22));
		button1.setFont(new Font("宋体",0,14));
		
		button3.setBounds(new Rectangle(325,27,70,22));
		button3.setFont(new Font("宋体",0,14));
		
		label3.setBounds(new Rectangle(20,50,70,22));
		label3.setFont(new Font("宋体",0,14));
		
		text3.setBounds(new Rectangle(95,50,100,22));
		text3.setText("请输入信息");
		
		button2.setBounds(new Rectangle(200,50,70,22));
		button2.setFont(new Font("宋体",0,14));
		
		
		jscrollpane.setBounds(new Rectangle(5,75,520,100));
		
		textarea1.setText("聊天内容");
		jscrollpane.getViewport().add(textarea1);
		
		panel1.add(label1);
		panel1.add(text1);
		panel1.add(label2);
		panel1.add(text2);
		panel1.add(button1);
		panel1.add(label3);
		panel1.add(text3);
		panel1.add(button2);
		panel1.add(button3);
		panel1.add(jscrollpane);
		contenpane.setLayout(null);
		panel1.setBounds(new Rectangle(0,0,600,170));
		contenpane.add(panel1);
		
		panel2=new QipanPane();
		panel2.setBounds(10, 180, 540, 460);
		contenpane.add(panel2);
		
		button1.addActionListener(this);
		button2.addActionListener(this);
		button3.addActionListener(this);
		this.addWindowListener(new WindowAdapter(){
		public void windowClosing(WindowEvent e){
			try {
				sendData("quit|");
				client.close();
				instr.close();
				os.close();
				System.exit(0);;
				
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		});
		//this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
		
		
	}
	//定义内部类面板来绘制棋盘
		class QipanPane extends JPanel{
			BufferedImage bi=null;//棋盘背景图片
			QipanPane(){
				this.addMouseListener(new MousLis());
				String imagepath="001.jpg";
				URL image=this.getClass().getResource(imagepath);
				try {
					bi=ImageIO.read(image);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			//重写paintComponent是重写背景
			protected void paintComponent(Graphics g){
				super.paintComponent(g);
				//双缓冲技术
				BufferedImage bImage=new BufferedImage(500,500,BufferedImage.TYPE_INT_RGB);
				Graphics g1=bImage.createGraphics();
				
				g1.setColor(Color.BLACK);
				//将背景图片绘制在面板上
				g1.drawImage(bi, 0, 0, this);
				//绘制标题信息
				g1.setFont(new Font("宋体",Font.BOLD,14));
				g1.drawString("游戏信息:"+message, 130, 60);
				//绘制棋盘
				for(int i=0;i<19;i++){
					g1.drawLine(10, 70+20*i, 10+18*20, 70+20*i);
					g1.drawLine(10+20*i, 70, 10+20*i, 70+18*20);
				}
				//绘制准星
				g1.fillOval(68, 128, 6, 6);
				g1.fillOval(188, 128, 6, 6);
				g1.fillOval(308, 128, 6, 6);
				g1.fillOval(188, 248, 6, 6);
				g1.fillOval(188, 368, 6, 6);
				g1.fillOval(68, 248, 6, 6);
				g1.fillOval(68, 368, 6, 6);
				g1.fillOval(308, 248, 6, 6);
				g1.fillOval(308, 368, 6, 6);
				//绘制全部棋子
				for(int i=0;i<19;i++)
					for(int j=0;j<19;j++){
						if(allChess[i][j]==1){
							int tempX=10+20*i;
							int tempY=70+20*j;
							g1.setColor(Color.BLACK);
							g1.fillOval(tempX-7, tempY-7, 14, 14);
						}else if(allChess[i][j]==2){
							int tempX=10+20*i;
							int tempY=70+20*j;
							g1.setColor(Color.WHITE);
							g1.fillOval(tempX-7, tempY-7, 14, 14);
							g1.setColor(Color.BLACK);
							g1.drawOval(tempX-7, tempY-7, 14, 14);
						}
							
					}
				
			g.drawImage(bImage, 0, 0, this);	
			}
}
		class MousLis extends MouseAdapter{
			
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
			if(canPlay==true){	
				int x1=e.getX();
				int y1=e.getY();
				if(x1>=10&&x1<=370&&y1>=70&&y1<=430){
					x=x1/20;
					y=(y1-60)/20;
					if(allChess[x][y]==0){
						allChess[x][y]=2;
						canPlay=false;
						message="对方落子！";
						sendData("move|"+String.valueOf(x)+"|"+String.valueOf(y));
						repaint();	
						if(checkWin()){
							message="游戏结束，白方获胜！";
							sendData("over|"+message);
							JOptionPane.showMessageDialog(null, message);
							canPlay=false;
							
						}
					}else{
						JOptionPane.showMessageDialog(null, "此处已经有棋子了！！");
					}
			}
			}else{
				JOptionPane.showMessageDialog(null, "轮到对方落子！");
			}
				
			}	
		}
		//侦听方法
		private void ConnectServer(String ip, int port){
			if(button1.getText().trim().equals("连接")){
				try {
					button1.setText("连接服务器。。");
					client=new Socket(ip,port);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				button1.setText("正在聊天");
				
				message="自己是白棋，黑方先行";
				panel2.repaint();
				MyThread t=new MyThread();
				t.start();
						
			}
		}
		//发送信息
		private void sendData(String s) {
			// TODO Auto-generated method stub
			try {
				os=new PrintWriter(client.getOutputStream());
				os.println(s);
				os.flush();
				if(!s.startsWith("chat")){
					textarea1.append("客户端："+s+"\n");}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//接收信息
		class MyThread extends Thread{
			public void run(){
				try {
					while(true){
						this.sleep(100);
					instr=new BufferedReader(new InputStreamReader(client.getInputStream()));
					if(instr.ready()){String cmd=instr.readLine();
					ss=cmd.split("\\|");
					//棋子移动的信息
					if(cmd.startsWith("move")){
						x=Integer.parseInt(ss[1]);
						y=Integer.parseInt(ss[2]);
						allChess[x][y]=1;//落服务器端的黑子
						message="轮到自己下棋";
						canPlay=true;
						textarea1.append("服务器端："+cmd+"\n");
						panel2.repaint();
						
					}
					//悔棋信息
					if(cmd.startsWith("undo")){
						allChess[x][y]=0;
						message="对方悔棋";
						canPlay=false;
						panel2.repaint();
					}
					//游戏结束信息
					if(cmd.startsWith("over")){
						JOptionPane.showMessageDialog(null, "游戏结束，对方获胜！");
						canPlay=false;
					}
					//对方离开信息
					if(cmd.startsWith("quit")){
						JOptionPane.showMessageDialog(null, "游戏结束，对方离开了！");	
						canPlay=false;
					}
					//聊天信息
					if(cmd.startsWith("chat")){
						textarea1.append("服务器端说："+ss[1]+"\n");
					}
					
				}
					}} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
			}
			}
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			//连接
			if(e.getSource()==button1){
				int port=Integer.parseInt(text1.getText().trim());
				String ip=text2.getText().trim();
				ConnectServer(ip,port);
			}
			//发送聊天信息
			if(e.getSource()==button2){
				String str=text3.getText();
				sendData("chat|"+str);
				textarea1.append("客户端说："+str+"\n");
			}
			//悔棋
			if(e.getSource()==button3){
				if(canPlay!=true){
				allChess[x][y]=0;
				message="轮到自己下棋";
				canPlay=true;
				sendData("undo|"+String.valueOf(x)+"|"+String.valueOf(y));
				repaint();
				}else {
					JOptionPane.showMessageDialog(this, "对方已走棋，不能悔棋！");
				}
			}
		}
		
		private boolean checkWin(){
			boolean Flag=false;
			int count=1;
			int color=allChess[x][y];
			//判断横向是否有5个棋子相连，特点是纵坐标相同
			int i=1;
			while(allChess[x+i][y+0]==color){
					i++;
					count++;
				}
			 i=1;
			while(allChess[x-i][y-0]==color){
				i++;
				count++;
			}
			if(count>=5){
				Flag=true;
			}
			//判断纵向是否有5个棋子相连，特点是横坐标相同
			int i2=1;
			int count2=1;
			while(allChess[x+0][y+i2]==color){
					i2++;
					count2++;
				}
			 i2=1;
			while(allChess[x-0][y-i2]==color){
				i2++;
				count2++;
			}
			if(count2>=5){
				Flag=true;
			}
			//判断斜方向（左上+右下）
			int i3=1;
			int count3=1;
			while(allChess[x-i3][y-i3]==color){
					i3++;
					count3++;
				}
			 i3=1;
			while(allChess[x+i3][y+i3]==color){
				i3++;
				count3++;
			}
			if(count3>=5){
				Flag=true;
			}
			//判断斜方向（左下+右上）
			int i4=1;
			int count4=1;
			while(allChess[x-i4][y+i4]==color){
					i4++;
					count4++;
				}
			 i4=1;
			while(allChess[x+i4][y-i4]==color){
				i4++;
				count4++;
			}
			if(count4>=5){
				Flag=true;
			}
			return Flag;
			
		}
}
