package zz_java_playground;

import java.awt.GraphicsEnvironment;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class SwingGuiExample extends JFrame {

  public SwingGuiExample() {
    
    // first ...
    init_the_stupid_GUI();
  }

  
  private void init_the_stupid_GUI() {

    // make a window ...
    setTitle("Welcome to the SWING GUI example ...");
    setSize(300,200);
    setLocation(300, 400);
    setDefaultCloseOperation(EXIT_ON_CLOSE); // closing a window DOES NOT NECESSARILY mean to exit !!
    
    // define 2 buttons ...
    JButton quitButton = new JButton("Goodbye cruel world ... !");
    quitButton.setSize(100, 100);
    JButton calcButton = new JButton("Do the Math!");
    calcButton.setSize(quitButton.getMaximumSize());
//    quitButton.setBounds(10,50,50,100);  <- wtf is that for ... ?????
    
    // ... and place it on the (current) content pane (which is basically the whole JFrame) 
    getContentPane().add(quitButton);
    getContentPane().add(calcButton);
   
  }
  
  
  public static void main(String[] args) {
    // TODO Auto-generated method stub
    
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        SwingGuiExample sge = new SwingGuiExample();
        sge.setVisible(true);
      }
    });
  }

}
