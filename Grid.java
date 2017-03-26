import java.util.ArrayList;
import javax.swing.*;
import java.awt.*;

class Grid extends JComponent {

    private final int blockSize; //px wide a block is
    private final int stepSize; //1/3rd block size, for direction indicators
    private ArrayList<Cell> cellList;
    private ArrayList<Food> foodList;

    public Grid(int blockSize, ArrayList<Cell> cellList, ArrayList<Food> foodList) {
        this.blockSize = blockSize;
        this.stepSize = blockSize/3;
        this.cellList = cellList;
        this.foodList = foodList;
    }

    //TODO can we be smarter re: having to do the > 0 || < 255 checks?
    public void paint(Graphics g) {
        //energyMidpoint for cell drawing, ie. 50% energy, less the cell is hungry more it's bloated
        double energyMidpoint = Cell.energyMax / 2;

        //draw cells
        Cell cell;
        int channel;
        int colorMax;
        for (int i = 0; i <cellList.size(); i++) {
            cell = cellList.get(i);
            colorMax = (cell.isViable()) ? 255 : 127;
            //cell body
            if (cell.energy > cell.energyMax / 2) { //bloated, shift towards magenta
                channel = (int)((cell.energy - energyMidpoint) / energyMidpoint * colorMax);
                if (channel < 0 || channel > colorMax) { //b/c of async rendering and starved cells
                    channel = 0;
                }
                g.setColor(new Color(colorMax, 0, channel));
            } else { //hungry, shift towards yellow
                channel = (int)((1 - cell.energy / energyMidpoint) * colorMax);
                if (channel < 0 || channel > colorMax) { //b/c of async rendering and gorged cells
                    channel = colorMax;
                }
                g.setColor(new Color(colorMax, channel, 0));
            }
            g.fillRect(cell.x * blockSize, cell.y * blockSize, blockSize, blockSize);

            //direction indicator
            g.setColor(Color.black);
            g.fillRect(cell.x * blockSize + ((cell.direction[0] + 1) * stepSize), cell.y * blockSize + ((cell.direction[1] + 1) * stepSize), stepSize, stepSize);
        }

        //draw food
        Food food;
        for (int i = 0; i < foodList.size(); i++) {
            food = foodList.get(i);
            g.setColor(new Color(0, (int)(255 * (food.energy / food.energyMax)), 0));
            g.fillRect(food.x * blockSize, food.y * blockSize, blockSize, blockSize);
        }
    }
}