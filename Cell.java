import java.text.DecimalFormat;

public class Cell extends Entity {

    //misc/helper vars
    private static final DecimalFormat rounder = new DecimalFormat("#.#");

    //id vars
    private static int speciesTop = 1;
    public int species;
    public int generation;
    public int mutations;

    //status vars
    public static final int tickMax = 25000; //kill cell after n ticks (old age)
    public static final double energyMax = 2000;
    public double energy;
    public boolean facingFood = false;
    public boolean blocked = false;
    public double splittable = -1;

    //action vars
    public boolean move;
    public boolean eat;
    public boolean split;

    //neural vars and stats
    public Network network;
    private boolean hasMoved = false;
    private boolean hasEaten = false;
    private boolean hasTurned = false;
    public int offspring = 0;
    public int ticks = 0;

    //direction vars
    public int[] direction;
    public int directionIndex;
    public static final int[][] directions = new int[][] {
        {-1, -1}, //NW
        {0, -1}, //N
        {1, -1}, //NE
        {1, 0}, //E
        {1, 1}, //SE
        {0, 1}, //S
        {-1, 1}, //SW
        {-1, 0} //W
    };

    //default constructor for new cell
    public Cell(int[] point) {
        super(point);
        //randomize energy a bit so all the dud cells don't die at the exact same time, and over rebirth cycles offsets grow
        //energy = energyMax/2 + Controller.random.nextInt(200);
        energy = energyMax/2;
        //start pointing in a random direction
        directionIndex = Controller.random.nextInt(8);
        direction = directions[directionIndex];
        //initialize network
        network = new Network();
        //give this cell a unique species ID
        species = speciesTop;
        speciesTop++;
        generation = 1;
        mutations = 0;
    }

    //overloaded, this cell split from an existing cell
    public Cell(int[] point, Cell parent) {
        super(point);
        this.energy = parent.energy; //parent's energy was halved before the split
        //random direction
        directionIndex = Controller.random.nextInt(8);
        direction = directions[directionIndex];
        //speciation - TODO better tracking? will have to number offspring and etc..
        species = parent.species;
        mutations = parent.mutations;
        generation = parent.generation + 1;
        //inherit network, possibly mutate
        boolean mutate = Controller.random.nextBoolean();
        network = new Network(parent.network, mutate);
        if (mutate) {
            mutations++;
        }
    }

    public void tick() {
        //reset vars
        move = false;
        eat = false;
        split = false;

        //tick the network
        double[] inputs = {energy / energyMax, (facingFood) ? 1 : (blocked) ? -1 : 0, splittable};
        double[] outputs = network.tick(inputs);

        //perform action iff 1 output neuron fired
        double sum = 0;
        for (int i = 0; i < outputs.length; i++) {
            sum += outputs[i];
        }
        if (sum == 1) {
            if (outputs[0] == 1) { //eat
                eat = true;
                if (facingFood) {
                    hasEaten = true;
                }
            } else if (outputs[1] == 1) { //move
                move = true;
                hasMoved = true;
            } else if (outputs[2] == 1) { //clockwise
                if (--directionIndex < 0) {
                    directionIndex = 7;
                }
                hasTurned = true;
            } else if (outputs[3] == 1) { //counterclockwise
                if (++directionIndex > 7) {
                    directionIndex = 0;
                }
                hasTurned = true;
            } else if (outputs[4] == 1) { //split
                if (splittable > 0) {
                    split = true;
                }
            }
        }

        //in case we've turned
        direction = directions[directionIndex];

        //track
        ticks++;
    }

    //cell considered viable if it moves, turns, and eats
    public boolean isViable() {
        return hasEaten && hasTurned && hasMoved;
    }
}
