public class Food extends Entity {

    public static final double energyMax = 100;
    public double energy;

    public Food(int[] point, double energy) {
        super(point);
        this.energy = energy;
    }
}
