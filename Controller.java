import java.util.ArrayList;
import java.util.Random;

public class Controller {

    //utility
    public static Random random = new Random();

    //simulation variables
    private final int cellSeeds = 1000;
    private final int foodSeeds = 3000;
    private final int xMax = 200;
    private final int yMax = 85;

    //ui vars, note: glitchiness when tickTime = 0 && display = true
    private final int tickTime = 50;
    private final boolean display = true;
    private boolean fastForward = true; //if there are no viable cells, ignore tickTime

    //data structure
    private ArrayList<Cell> cellList = new ArrayList();
    private ArrayList<Food> foodList = new ArrayList();
    private Entity[][] grid = new Entity[xMax][yMax];

    public Controller() {
        //randomly seed cells food
        addRandomCells(cellSeeds);
        addRandomFood(foodSeeds);

        //UI stuff
        UI ui;
        if (display) {
            ui = new UI(xMax, yMax, cellList, foodList);
            ui.show();
        }

        //execution loop
        while (true) {
            try {
                if (tickTime > 0 && !fastForward) {
                    Thread.sleep(tickTime);
                }
                if (display) {
                    ui.render();
                }
                fastForward = true;

                //cell behaviour
                ArrayList<Cell> removals = new ArrayList();
                ArrayList<Cell> additions = new ArrayList();
                int[] point;
                int[] newPoint;
                Cell newCell;
                Entity entity; //entity (if any) that the cell is directly facing
                Food food;
                for (Cell cell:cellList) {
                    //give cell its inputs
                    point = new int[] {cell.x, cell.y};
                    cell.facingFood = false;
                    if ((entity = getEntityAt(point, cell.direction)) != null) {
                        if (entity instanceof Food) {
                            cell.facingFood = true;
                        } else { //facing another cell
                            cell.blocked = true;
                        }
                    } else { //we're either facing an empty space or edge of the map
                        cell.blocked = !isValidFreeSpace(point, cell.direction); //TODO a little lazy here, we know the space is null, this is just a convenient call re: edges
                    }
                    if (cell.isViable() ) {
                        if (cell.generation > 1) {
                            fastForward = false; //we have a viable cell on board, stop fast-forwarding
                        }
                        //"split" input ie can the cell procreate? if viable, over time we increase the "splittable input", after it is > 0 the cell can "split"
                        //also don't let cell split if it is below 10% energy just re: weirdness
                        if (cell.energy / cell.energyMax > 0.1) {
                            if (cell.splittable < 1) {
                                cell.splittable += 0.01;
                            }
                        } else {
                            cell.splittable = -1;
                        }
                    } else {
                        cell.splittable = -1;
                    }

                    //tick the cell
                    cell.tick();

                    //cell behaviour
                    if (cell.move) { //move?
                        if ((point[0] != 0 || point[1] != 0) && isValidFreeSpace(point, cell.direction)) {
                            grid[cell.x][cell.y] = null;
                            cell.x += cell.direction[0];
                            cell.y += cell.direction[1];
                            grid[cell.x][cell.y] = cell;
                        }
                    } else if (cell.eat) { //eat?
                        if (entity instanceof Food) { //cell may be stupid - trying to eat nothing
                            food = (Food)entity;
                            if (food.energy < 10) { //food almost gone, eat what's left and remove it
                                cell.energy += food.energy;
                                //remove
                                grid[food.x][food.y] = null;
                                foodList.remove(food);
                                //re-add food elsewhere, at least for now: re long simulations...
                                //addRandomFood(1);
                            } else {
                                cell.energy += 10;
                                food.energy -= 10;
                            }
                        }
                    } else if (cell.split) { //split
                        //split into an adjacent free space (if available)
                        for (int i = 0; i < 8; i++) {
                            if (isValidFreeSpace(point, cell.directions[i])) {
                                cell.offspring++;
                                cell.energy = (int)cell.energy / 2;
                                cell.splittable = -1; //cooldown on splitting
                                System.out.println("Cell " + cell.species + "." + cell.generation + "." + cell.mutations + " split!");
                                //TODO review here and below when applying the additions, may have to be careful around quirks of marking the grid off and etc, especially if we add carnivores
                                newPoint = point;
                                newPoint[0] += cell.directions[i][0];
                                newPoint[1] += cell.directions[i][1];
                                newCell = new Cell(newPoint, cell);
                                grid[newCell.x][newCell.y] = newCell;
                                additions.add(newCell);
                                break;
                            }
                        }
                    }

                    //reduce energy
                    cell.energy--;

                    //die? starve or gorge
                    if (cell.energy <= 0 || cell.energy > cell.energyMax || cell.ticks > cell.tickMax) {
                        //when a viable cell dies, output:
                        if (cell.isViable()) {
                            System.out.print("Cell " + cell.species + "." + cell.generation + "." + cell.mutations + " ");
                            if (cell.energy <= 0) { //starved
                                System.out.print("starved ");
                            } else if (cell.energy > cell.energyMax){ //gorged
                                System.out.print("gorged  ");
                            } else {
                                System.out.print("aged out  ");
                            }
                            System.out.print("after " + cell.ticks + " ticks, " + cell.offspring + " offspring");
                            System.out.print(" at [" + cell.x + "," + cell.y + "], ");
                            System.out.print(cell.network.getReadable());
                            System.out.println("");
                        }
                        grid[cell.x][cell.y] = null;
                        removals.add(cell);
                    }
                }

                //cleanup, rebirths, splits
                cellList.addAll(additions);
                cellList.removeAll(removals);
                if (cellList.size() < cellSeeds) { //rebirths, keep us equal to the seed #, re: splitting and etc we do it this way..
                    addRandomCells(cellSeeds - cellList.size());
                }
                if (foodList.size() < foodSeeds) { //throttle food regen re: successful species...
                    addRandomFood(1);
                }
            } catch (InterruptedException e) {
                System.out.println("Interrupted!");
            }
        }
    }

    private void addRandomCells(int num) {
        for (int i = 0; i < num; i++) {
            cellList.add((Cell)addEntityToGrid(new Cell(getRandomFreeSpace())));
        }
    }

    private void addRandomFood(int num) {
        for (int i = 0; i < num; i++) {
            foodList.add((Food)addEntityToGrid(new Food(getRandomFreeSpace(), Food.energyMax)));
        }
    }

    private boolean isValidFreeSpace(int[] point, int[] offset) {
        return (point[0] + offset[0] >= 0 && point[1] + offset[1] >= 0
            && point[0] + offset[0] < xMax && point[1] + offset[1] < yMax
            && grid[point[0] + offset[0]][point[1] + offset[1]] == null);
    }

    private Entity getEntityAt(int[] point, int[] offset) {
        int x = point[0] + offset[0];
        int y = point[1] + offset[1];
        if (x < 0 || x >= xMax || y < 0 || y >= yMax) {
            return null;
        }
        return grid[x][y];
    }

    private Entity addEntityToGrid(Entity entity) {
        grid[entity.x][entity.y] = entity;
        return entity;
    }

    //TODO: what if the grid is full?
    private int[] getRandomFreeSpace() {
        int[] point = new int[2];
        do {
            point[0] = random.nextInt(xMax);
            point[1] = random.nextInt(yMax);
        } while (grid[point[0]][point[1]] != null);
        return point;
    }
}
