import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;

public class Jardinains extends JFrame {
    private JComboBox<String> difficultyCombo;
    private JCheckBox soundCheckBox; // Placeholder for sound option (no actual sound implemented)
    private JButton startButton;
    private GamePanel gamePanel;
    private JPanel menuPanel;

    public Jardinains() {
        setTitle("Jardinains");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        menuPanel = new JPanel();
        menuPanel.setLayout(new FlowLayout());

        difficultyCombo = new JComboBox<>(new String[]{"Easy", "Medium", "Hard"});
        soundCheckBox = new JCheckBox("Sound On", true); // Example additional option
        startButton = new JButton("Start");
        startButton.addActionListener(e -> startGame());

        menuPanel.add(new JLabel("Difficulty:"));
        menuPanel.add(difficultyCombo);
        menuPanel.add(soundCheckBox); // Additional option (though sound not implemented)
        menuPanel.add(startButton);

        add(menuPanel);
        setVisible(true);
    }

    private void startGame() {
        remove(menuPanel);
        String diff = (String) difficultyCombo.getSelectedItem();
        boolean soundOn = soundCheckBox.isSelected(); // Can be used if sound is added later
        gamePanel = new GamePanel(diff);
        add(gamePanel);
        revalidate();
        gamePanel.requestFocus();
    }

    public static void main(String[] args) {
        new Jardinains();
    }
}

class GamePanel extends JPanel implements ActionListener, KeyListener {
    private Timer timer;
    private Ball ball;
    private Paddle paddle;
    private ArrayList<Brick> bricks;
    private int lives = 3;
    private int score = 0;
    private boolean inGame = true;
    private boolean ballLaunched = false; // To launch ball with space
    private String difficulty;
    private double ballSpeed;
    private int paddleWidth;

    public GamePanel(String diff) {
        difficulty = diff;
        switch (diff) {
            case "Easy":
                ballSpeed = 2;
                paddleWidth = 100;
                lives = 5;
                break;
            case "Medium":
                ballSpeed = 3;
                paddleWidth = 80;
                lives = 3;
                break;
            case "Hard":
                ballSpeed = 4;
                paddleWidth = 60;
                lives = 2;
                break;
            default:
                ballSpeed = 3;
                paddleWidth = 80;
                lives = 3;
        }

        addKeyListener(this);
        setFocusable(true);
        setPreferredSize(new Dimension(800, 600));
        timer = new Timer(5, this);
        initGame();
    }

    private void initGame() {
        paddle = new Paddle(350, 550, paddleWidth);
        ball = new Ball(paddle.getX() + paddleWidth / 2 - 5, paddle.getY() - 10, ballSpeed);
        bricks = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 10; j++) {
                bricks.add(new Brick(j * 70 + 50, i * 30 + 50));
            }
        }
        timer.start();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (inGame) {
            paddle.draw(g);
            ball.draw(g);
            for (Brick b : bricks) {
                if (!b.isDestroyed()) {
                    b.draw(g);
                }
            }
            g.drawString("Lives: " + lives, 10, 20);
            g.drawString("Score: " + score, 700, 20);
            if (!ballLaunched) {
                g.drawString("Press SPACE to launch the ball", 280, 300);
            }
        } else {
            g.drawString("Game Over", 350, 300);
        }
        if (bricks.isEmpty()) {
            g.drawString("You Win!", 350, 300);
            timer.stop();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (inGame) {
            paddle.move();
            if (ballLaunched) {
                ball.move();
                checkCollisions();
            } else {
                // Keep ball attached to paddle before launch
                ball.setX(paddle.getX() + paddleWidth / 2 - 5);
                ball.setY(paddle.getY() - 10);
            }
            repaint();
        }
    }

    private void checkCollisions() {
        // Ball with paddle
        if (ball.getRect().intersects(paddle.getRect())) {
            ball.setDY(-ball.getDY());
        }

        // Ball with bricks
        for (Brick b : bricks) {
            if (!b.isDestroyed() && ball.getRect().intersects(b.getRect())) {
                ball.setDY(-ball.getDY());
                b.setDestroyed(true);
                score += 10;
                // Simple "gnome" effect: occasionally reverse dx for fun (simulating throwback)
                if (Math.random() < 0.2) { // 20% chance for "gnome throw"
                    ball.setDX(-ball.getDX());
                }
                break;
            }
        }

        // Ball with walls
        if (ball.getX() <= 0 || ball.getX() >= 780) {
            ball.setDX(-ball.getDX());
        }
        if (ball.getY() <= 0) {
            ball.setDY(-ball.getDY());
        }
        if (ball.getY() >= 600) {
            lives--;
            if (lives == 0) {
                inGame = false;
            } else {
                ballLaunched = false;
                ball = new Ball(paddle.getX() + paddleWidth / 2 - 5, paddle.getY() - 10, ballSpeed);
            }
        }

        // Remove destroyed bricks
        bricks.removeIf(Brick::isDestroyed);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_SPACE && !ballLaunched) {
            ballLaunched = true;
            ball.setDY(-ballSpeed); // Launch upward
        }
        paddle.keyPressed(e);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        paddle.keyReleased(e);
    }

    @Override
    public void keyTyped(KeyEvent e) {}
}

class Paddle {
    private int x, y, width, height = 10;
    private int dx = 0;

    public Paddle(int x, int y, int width) {
        this.x = x;
        this.y = y;
        this.width = width;
    }

    public void move() {
        x += dx;
        if (x < 0) x = 0;
        if (x > 800 - width) x = 800 - width;
    }

    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT) {
            dx = -5;
        }
        if (key == KeyEvent.VK_RIGHT) {
            dx = 5;
        }
    }

    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_RIGHT) {
            dx = 0;
        }
    }

    public void draw(Graphics g) {
        g.setColor(Color.BLUE);
        g.fillRect(x, y, width, height);
    }

    public Rectangle getRect() {
        return new Rectangle(x, y, width, height);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}

class Ball {
    private int x, y, size = 10;
    private double dx = 0, dy = 0; // dx starts at 0, set on launch

    public Ball(int x, int y, double speed) {
        this.x = x;
        this.y = y;
        this.dx = speed * (Math.random() > 0.5 ? 1 : -1); // Random initial direction
    }

    public void move() {
        x += dx;
        y += dy;
    }

    public void draw(Graphics g) {
        g.setColor(Color.RED);
        g.fillOval(x, y, size, size);
    }

    public Rectangle getRect() {
        return new Rectangle(x, y, size, size);
    }

    public double getDY() {
        return dy;
    }

    public void setDY(double dy) {
        this.dy = dy;
    }

    public double getDX() {
        return dx;
    }

    public void setDX(double dx) {
        this.dx = dx;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }
}

class Brick {
    private int x, y, width = 60, height = 20;
    private boolean destroyed = false;

    public Brick(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void draw(Graphics g) {
        g.setColor(Color.GREEN);
        g.fillRect(x, y, width, height);
        // Simple "gnome" drawing on brick for theme
        g.setColor(Color.BLACK);
        g.fillOval(x + 25, y - 5, 10, 10); // Head
    }

    public Rectangle getRect() {
        return new Rectangle(x, y, width, height);
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public void setDestroyed(boolean d) {
        destroyed = d;
    }
}
