/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pacmanx;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 *
 * @author harth
 */
public class MapCreatorPanel extends JPanel {

    private static final int WIDTH = 600, HEIGHT = 500; // size of the game area 
    private static final int LINE_THIN = 6; // thikness of the walls
    private static final Color wallColor = new Color(27, 83, 186);
    private static final int THRESHOLD = 30; // to make the creation of the wall more easy
    private static final Color backGrColor = new Color(51, 51, 51);
    
    private ArrayList<Line> lineList;
    private MouseHandler mouseHandler;
    public BufferedImage image; // at first we paint on this image then draw the image on the panel
    private static int numberOfPoints; // pair of points will represent a single line "wall"
    private int wallPosX, wallPosY;
    private String name;
    public File imageNames;

    public MapCreatorPanel() {
        mouseHandler = new MouseHandler();
        image = new BufferedImage(WIDTH, WIDTH, BufferedImage.TYPE_INT_RGB);
        imageNames = new File("src\\pacmanx\\image names.txt");
        lineList = new ArrayList();
        
        addMouseListener(mouseHandler);
        
    }

    @Override
    protected void paintComponent(Graphics grphcs) {
        // color the background
        grphcs.setColor(backGrColor);
        grphcs.fillRect(0, 0, WIDTH, HEIGHT);

        // drawing the walles and the chips before starting the game
        Graphics2D g2d = (Graphics2D) grphcs;
        g2d.drawImage(image, 0, 0, null);

    }

    // paint the walles the BuffuredImage 
    public void paintOnImage() {
        Graphics2D g = image.createGraphics(); // create graphics object to draw on the image
        g.setColor(wallColor);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // make the shape more smoothly
        g.fillOval(wallPosX, wallPosY, LINE_THIN, LINE_THIN);
        g.dispose();
        repaint();
    }

    // name of the imge file 
    public void setImageName(String name) {
        this.name = name;
    }

    // save lines as painr of tow points with spacific name
    public void SaveLineList(String name) throws IOException{
        try {
            ObjectOutputStream writer = new ObjectOutputStream(new FileOutputStream("src/pacmanx/maps lines/" + name + ".abc"));
            writer.writeObject(lineList);
        } catch (FileNotFoundException ex) {
            System.out.println(ex);
        }
    }
    // mouse Event to draw the walls
    private class MouseHandler extends MouseAdapter {

        private Point firstPoint, secondPoint;

        @Override
        public void mouseClicked(MouseEvent me) {
            wallPosX = me.getX();
            wallPosY = me.getY();

            if (numberOfPoints % 2 == 0) {
                firstPoint = new Point(wallPosX, wallPosY);
                numberOfPoints++;
                paintOnImage();
            } else {
                Point secondPoint = new Point(wallPosX, wallPosY);

                if (Point.inStrateLine(firstPoint, secondPoint)) {
                    numberOfPoints++;
                    lineList.add(new Line(firstPoint, secondPoint));
                    
                    wallPosX = secondPoint.x;
                    wallPosY = secondPoint.y;
                    while (wallPosX != firstPoint.x) {
                        if (wallPosX > firstPoint.x) {
                            wallPosX--;
                        } else {
                            wallPosX++;
                        }

                        paintOnImage();
                    }

                    while (wallPosY != firstPoint.y) {
                        if (wallPosY > firstPoint.y) {
                            wallPosY--;
                        } else {
                            wallPosY++;
                        }

                        paintOnImage();
                    }
                }
            }
        }

    }
}

class Point implements Serializable {

    int x, y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // to check if the two cliked was approximately at the same line horizontally or vertically 
    public static boolean inStrateLine(Point a, Point b) {
        if (Math.abs(a.x - b.x) < 30) {
            b.x = a.x;
            return true;
        }
        if (Math.abs(a.y - b.y) < 30) {
            b.y = a.y;
            return true;
        }

        return false;
    }
}

class Line implements Serializable{
    Point a, b;

    public Line(Point a, Point b) {
        this.a = a;
        this.b = b;
    }
    
    
}