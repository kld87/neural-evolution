public class Neuron {

    private final static int bias = 1; //bias input
    private double memory = 0; //output from last iteration
    public int inputNum;
    public double[] weights;

    public Neuron(int inputNum) {
        this.inputNum = inputNum;
        weights = new double[inputNum + 2]; //+2 for bias and memory inputs
        //initialize weights
        for (int i = 0; i < weights.length; i++) {
            weights[i] = getRandomWeight();
        }
    }

    //modelled off old neuron
    public Neuron(Neuron oldNeuron) {
        inputNum = oldNeuron.inputNum;
        weights = new double[oldNeuron.weights.length];
        for (int i = 0; i < weights.length; i++) {
            weights[i] = oldNeuron.weights[i];
        }
    }

    public double tick(double[] inputs) {
        //total/weight normal inputs
        double sum = 0;
        for (int i = 0; i < inputNum; i++) {
            sum += inputs[i] * weights[i];
        }
        //add bias and memory
        sum += bias * weights[inputNum];
        sum += memory * weights[inputNum+1];
        //do we fire or not? record to memory for next tick then return
        memory = (sum >= 1) ? 1 : 0;
        return memory;
    }

    public static double getRandomWeight() {
        return -1 + Controller.random.nextDouble()*2;
    }
}
