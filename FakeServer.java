// part of Toast
// author: Ulrike Hager

import java.io.*;
import java.net.*;


class ClientHandler extends Thread {

  protected Socket incoming;
	FileReader inFile;

  public ClientHandler(Socket incoming) {
    this.incoming = incoming;
  }

  public void run() {
    try {
		inFile = new FileReader("tracks.txt");
      BufferedReader in = new BufferedReader(inFile);
	  in.mark(4000);
	  PrintWriter out = new PrintWriter(new OutputStreamWriter(incoming.getOutputStream()));
	  String l;
	  boolean connect = true;
	  while(connect){
		  while ((l = in.readLine()) != null) {
			  if (l.startsWith("#")) sleep(50);
			  out.println(l);
			  out.flush();
		  }
		  in.reset();
	  }
      in.close();
	  out.close();
    } catch (Exception e) {
      System.out.println("Error: " + e); 
    }
  }

}

public class FakeServer {

  public static void main(String[] args) {
	  //   System.out.println("MultiEchoServer started."); 
    
    try {
      ServerSocket s = new ServerSocket(4711); 
      for (;;) {
	Socket incoming = s.accept(); 
	new ClientHandler(incoming).start(); 
      }
    } catch (Exception e) {
      System.out.println("Error: " + e); 
    }

//    System.out.println("MultiEchoServer stopped."); 
  }
}
