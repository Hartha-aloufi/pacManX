/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pacmanx;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author harth
 */
public class test {
    public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException {
        ArrayList<Line> arr;
        ObjectInputStream reader = new ObjectInputStream(new FileInputStream("src/pacmanx/maps lines/testWalles.abc"));
        arr = (ArrayList<Line>) reader.readObject();
        for(Line x  :arr){
            System.out.println(x.a.x + " " + x.b.y);
        }
    }
}
