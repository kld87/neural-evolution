import java.util.ArrayList;
import javax.swing.*;
import java.awt.*;

public class UI {

    private final int blockSize = 9; //must be divisible by 3
    private final int xMax;
    private final int yMax;
    private Container container;
    private Grid grid;

    public UI(int xMax, int yMax, ArrayList<Cell> cellList, ArrayList<Food> foodList) {
        this.xMax = xMax;
        this.yMax = yMax;
        grid = new Grid(blockSize, cellList, foodList);
    }

    public void show() {
        JFrame frame = new JFrame();

        container = frame.getContentPane();
        container.setBackground(Color.black);
        container.setPreferredSize(new Dimension(xMax * blockSize, yMax * blockSize));
        container.add(grid);

        frame.setLocation(50, 50);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public void render()  {
        grid.repaint();
    }
}
