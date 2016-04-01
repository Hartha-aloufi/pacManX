package pacmanx;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/**
 *
 * @author harth 25/3/2016 10:34 pm
 */
public class AreaPanel extends JPanel {

    private static final int LINE_THIN = 6; // thikness of the walls
    private static final int WIDTH = 604, HEIGHT = 499; // size of the game area 
    private static final Color wallColor = new Color(27, 83, 186);
    private static final int THRESHOLD = 30; // to make the creation of the wall more easy
    private static final int packManSize = 30;
    private static final int maxChips = 300;
    private static final int chipsSize = 4;

    private Color backGrColor;
    private BufferedImage image; // at first we paint on this image then draw the image on the panel
    private static int numberOfPoints; // pair of points will represent a single line "wall"
    private int wallPosX, wallPosY;
    private int numberOfChips; // number of items which the packman should eat
    private int numberOfMovesInSameDir;
    private int startAngle, endAngle, prevStartAngle, prevEndAngle; // packMan factors
    private int packManPosX, packManPosY, prevPackManPosX, prevPackManPosY, direction;// packMan factors
    private boolean isStarted, isRunning; // starting of the game, run and puse
    private boolean isMouthOpen;

    private boolean isReached[][]; // each cell in this 2D array represent one pixle in the game area, we will mark all cells that the packMan 
    // can reache with "true" and the rest with "false", note that the packMan can't reach some region in game area
    // which inclosed with walls

    private Point chips[];
    private int chipsID[][], id;
    private ArrayList<Line> wallList;
    int b;

    private Thread moveThread, mouthThread;

    public AreaPanel() {
        init();
        addKeyListener(new KeyHandler());
        setFocusable(true);

    }

    // initialize all static fields
    public void init() {
        image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        isReached = new boolean[WIDTH][HEIGHT];
        chips = new Point[maxChips];
        chipsID = new int[WIDTH][HEIGHT];

        isStarted = false;
        isMouthOpen = true;
        startAngle = 35;
        endAngle = 280;
        prevStartAngle = 35;
        prevEndAngle = 280;
        packManPosX = 10;
        packManPosY = 10;
        direction = 1;
        b = 0;
        isRunning = false;

        Enemy.init();
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    public void setWalles(ArrayList<Line> wallList) {
        this.wallList = wallList;
    }

    private void setStatus() {
        if (numberOfChips == 0) {
            Area.setStatusColor(Color.GREEN);
            Area.setStatus("Congratulations you won !!");
        } else {
            Area.setStatus("Number of remaining Chips " + numberOfChips);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {

        Graphics2D grphcs = (Graphics2D) g;
        grphcs.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // make the shape more smoothly

        // color the background
        // drawing the walles and the chips before starting the game
        if (b < 10) {
            grphcs.drawImage(image, 0, 0, null);
            b++;
        }

        // create the packMan and move it after the area had been prepared "walls and  chips"
        if (isStarted) {

            // at first we remove the packman by at the previous position by drawing another one with the same color of the backgruond 
            grphcs.fillOval(prevPackManPosX - 1, prevPackManPosY - 1, packManSize + 2, packManSize + 2);
            // move the packman by drawing it in the new position with diffenet color
            grphcs.setColor(Color.yellow);
            grphcs.fillArc(packManPosX, packManPosY, packManSize, packManSize, startAngle, endAngle);

            // save the current position of the packman to remove it in the next move
            prevStartAngle = startAngle;
            prevEndAngle = endAngle;
            prevPackManPosX = packManPosX;
            prevPackManPosY = packManPosY;

            // remove any chips reached by the packMan
            Point p;
            if ((p = checkChips(packManPosX, packManPosY, packManSize, packManSize)) != null) {

                grphcs.setColor(Color.BLACK);
                grphcs.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // make the shape more smoothly

                Point cleaner = chips[chipsID[p.x][p.y]];
                grphcs.fillOval(cleaner.x - 1, cleaner.y - 1, chipsSize + 1, chipsSize + 1);

                chips[chipsID[p.x][p.y]] = null;
                numberOfChips--;
                setStatus();
            }

            for (int i = 0; i < Enemy.numberOfEnemys; i++) {
                int x = Enemy.position[i].x;
                int y = Enemy.position[i].y;

                grphcs.setColor(Color.BLACK);
                grphcs.fillRect(x - 1, y - 1, Enemy.size + 2, Enemy.size + 2);
                grphcs.setColor(Enemy.color[i]);
                grphcs.fillRect(x, y, Enemy.size, Enemy.size);

                if ((p = checkChips(x - 1, y - 1, Enemy.size + 2, Enemy.size + 2)) != null) {
                    Point cleaner = chips[chipsID[p.x][p.y]];

                    grphcs.setColor(Color.BLACK);
                    grphcs.fillOval(cleaner.x - 1, cleaner.y - 1, chipsSize + 1, chipsSize + 1);
                    grphcs.setColor(Color.RED);
                    grphcs.fillOval(cleaner.x, cleaner.y, chipsSize, chipsSize);

                }
            }

        }

    }

    private Point checkChips(int x, int y, int hSize, int vSize) {
        for (int i = 0; i <= hSize; i++) {
            for (int j = 0; j <= vSize; j++) {
                if (chipsID[x + i][y + j] != 0 && chips[chipsID[x + i][y + j]] != null) {
                    return new Point(x + i, y + j);
                }
            }
        }
        return null;
    }

    // paint the walles the BuffuredImage 
    public void paintOnImage(int wallPosX, int wallPosY) {
        Graphics2D g = image.createGraphics(); // create graphics object to draw on the image
        g.setColor(wallColor);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // make the shape more smoothly
        g.fillOval(wallPosX, wallPosY, LINE_THIN, LINE_THIN);
        g.dispose();
    }

    private void movePacMan() {

        // before make any move we check if this move is valid "boundries of the frame, walls"
        switch (direction) {
            case 1:
                if (isValidMove(packManPosX + 2, packManPosY)) {
                    packManPosX += 2;
                }
                break;

            case 2:
                if (isValidMove(packManPosX, packManPosY + 2)) {
                    packManPosY += 2;
                }
                break;
            case 3:
                if (isValidMove(packManPosX - 2, packManPosY)) {
                    packManPosX -= 2;
                }
                break;
            case 4:
                if (isValidMove(packManPosX, packManPosY - 2)) {
                    packManPosY -= 2;
                }
                break;
        }
    }

    // the validity of moveing
    private boolean isValidMove(int x, int y) {
        return !collision(x, y) && x > 5 && x < getWidth() - 35 && y > 5 && y < getHeight() - 35;
    }

    // move the mouth by increasing and decreasing the arc size
    private void moveMouth() {
        if (endAngle >= 0) {
            endAngle += 5;
        } else {
            endAngle += -5;
        }

        if (Math.abs(endAngle) >= 355) {
            if (endAngle >= 0) {
                endAngle = 240;
            } else {
                endAngle = -240;
            }
        }

    }

    // check if there is a collision between the packMan and the walls
    boolean collision(int packManPosX, int packManPosY) {
        for (int i = -2; i <= packManSize + 2; i++) {
            for (int j = -2; j <= packManSize + 2; j++) {
                if (image.getRGB(i + packManPosX, j + packManPosY) == wallColor.getRGB()) {
                    return true;
                }
            }
        }
        return false;
    }

    public void paintWalls() {
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);
        for (int i = 0; i < wallList.size(); i++) {
            Point a = wallList.get(i).a;
            Point b = wallList.get(i).b;

            int x = a.x;
            int y = a.y;

            while (x != b.x) {
                paintOnImage(x, y);
                x += x > b.x ? -1 : 1;
            }

            while (y != b.y) {
                paintOnImage(x, y);
                y += y > b.y ? -1 : 1;
            }

            numberOfPoints += 2;
        }
    }

    private void fillChips() {
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // make the shape more smoothly
        g.setColor(Color.RED);
        for (int i = 10; i < getWidth(); i += 35) {
            out:
            for (int j = 10; j < getHeight(); j += 35) {
                for (int k = 0; k < 7; k++) {
                    for (int l = 0; l < 7; l++) {
                        if (image.getRGB(i + k, j + l) == wallColor.getRGB()) {
                            continue out;
                        }
                    }
                }

                if (isReached[i][j]) {
                    id++;

                    g.fillOval(i, j, chipsSize, chipsSize);
                    numberOfChips++;
                    chips[id] = new Point(i, j);

                    for (int k = 0; k < chipsSize; k++) {
                        for (int l = 0; l < chipsSize; l++) {
                            chipsID[i + k][j + l] = id;
                        }
                    }
                }
            }
        }

        g.dispose();
        repaint();
        isStarted = true;
    }

    boolean v[][] = new boolean[WIDTH][HEIGHT];
    int dx[] = {1, 0, -1, 0};
    int dy[] = {0, 1, 0, -1};

    private void BFS() {
        Queue<Integer> q = new LinkedList();
        int i = 10, j = 10;
        q.add(i);
        q.add(j);
        v[i][j] = true;

        while (!q.isEmpty()) {
            i = q.poll();
            j = q.poll();

            if (!canReached(i, j, false, false)) {
                continue;
            }

            canReached(i, j, true, true);
            for (int k = 0; k < 4; k++) {
                int x = i + dx[k];
                int y = j + dy[k];

                if (x > 0 && x < getWidth() && y > 0 && y < getHeight() && !v[x][y]) {
                    v[x][y] = true;
                    q.add(x);
                    q.add(y);
                }
            }
        }
    }

    private boolean canReached(int i, int j, boolean toFill, boolean can) {
        for (int k = 0; k < 33; k++) {
            for (int l = 0; l < 33; l++) {
                if (!toFill) {
                    if (!(i + k > 0 && i + k < getWidth() && j + l > 0 && j + l < getHeight()) || image.getRGB(i + k, j + l) == wallColor.getRGB()) {
                        return false;
                    }
                } else {
                    isReached[i][j] |= can;
                }
            }
        }
        return true;
    }

    // start the game
    public void run() {

        isRunning = true;
        paintWalls();
        BFS(); // mark all pixles in the game area by reached or unreached
        fillChips(); // put chips in some reached pixles, we wouldn't put the chips in any unreached pixles
        Enemy.setPositions(isReached); // set Enemies initial positions
        
        moveThread = new Thread() {

            @Override
            public void run() {
                try {
                    while (isRunning) {
                        moveMouth();
                        movePacMan();
                        moveEnemies();
                        paint(getGraphics());
                        Thread.sleep(3);
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(AreaPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        };

        moveThread.start();
    }

    public void terminate() {
        isRunning = false;
    }

    /////////////////////////////////////////////////////
    public void moveEnemies() {
        for (int i = 0; i < Enemy.numberOfEnemys; i++) {
            switch (Enemy.dir[i]) {
                case 1:
                    if (numberOfMovesInSameDir < 100 + Math.random() * 300 && isValidMove(Enemy.position[i].x + 1, Enemy.position[i].y)) {
                        Enemy.position[i].x++;
                        numberOfMovesInSameDir++;
                    } else {
                        Enemy.dir[i] = 1 + (int) (Math.random() * 4);
                        numberOfMovesInSameDir = 0;
                    }
                    break;
                case 2:
                    if (numberOfMovesInSameDir < 100 + Math.random() * 300 && isValidMove(Enemy.position[i].x, Enemy.position[i].y + 1)) {
                        Enemy.position[i].y++;
                        numberOfMovesInSameDir++;
                    } else {
                        Enemy.dir[i] = 3;
                        numberOfMovesInSameDir = 0;
                    }
                    break;
                case 3:
                    if (numberOfMovesInSameDir < 100 + Math.random() * 300 && isValidMove(Enemy.position[i].x - 1, Enemy.position[i].y)) {
                        Enemy.position[i].x--;
                        numberOfMovesInSameDir++;
                    } else {
                        Enemy.dir[i] = 4;
                        numberOfMovesInSameDir = 0;
                    }
                    break;
                case 4:
                    if (numberOfMovesInSameDir < 100 + Math.random() * 300 && isValidMove(Enemy.position[i].x, Enemy.position[i].y - 1)) {
                        Enemy.position[i].y--;
                        numberOfMovesInSameDir++;
                    } else {
                        Enemy.dir[i] = 1;
                        numberOfMovesInSameDir = 0;
                    }
                    break;

            }

        }

    }

    private class KeyHandler extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent ke) {

            // arows events, each direction has it's own packMan factors to paint 
            if (ke.getKeyCode() == KeyEvent.VK_RIGHT) {
                direction = 1;
                prevStartAngle = startAngle;
                prevEndAngle = endAngle;
                startAngle = 35;
                endAngle = 280;
            } else if (ke.getKeyCode() == KeyEvent.VK_DOWN) {
                direction = 2;
                prevStartAngle = startAngle;
                prevEndAngle = endAngle;
                startAngle = 255;
                endAngle = -240;
            } else if (ke.getKeyCode() == KeyEvent.VK_LEFT) {
                direction = 3;
                prevStartAngle = startAngle;
                prevEndAngle = endAngle;
                startAngle = 135;
                endAngle = -240;
            } else if (ke.getKeyCode() == KeyEvent.VK_UP) {
                direction = 4;
                prevStartAngle = startAngle;
                prevEndAngle = endAngle;
                startAngle = 122;
                endAngle = 240;
            } else if (ke.getKeyCode() == KeyEvent.VK_ESCAPE) {
                isRunning = false;
                AreaPanel.this.setVisible(false);
            } else if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
                if (isRunning) {
                    isRunning = false;
                } else {
                    isRunning = true;
                    movePacMan();
                    moveMouth();
                }
            }
        }

    }

}

class Enemy {

    static final int numberOfEnemys = 3, size = 30;
    static Point position[];
    static Color color[];
    static int dir[];

    public static void init() {
        position = new Point[numberOfEnemys];
        color = new Color[numberOfEnemys];
        dir = new int[numberOfEnemys];

        color[0] = Color.WHITE;
        color[1] = Color.orange;
        color[2] = new Color(203, 79, 221);

        dir[0] = dir[1] = dir[2] = 1;
    }

    public static void setPositions(boolean isReach[][]) {

        for (int i = 0; i < numberOfEnemys; i++) {

            while (true) {
                int x = (int) (Math.random() * 600);
                int y = (int) (Math.random() * 600);

                if (isReach[x][y]) {
                    position[i] = new Point(x, y);
                    break;
                }
            }

        }
    }
}
