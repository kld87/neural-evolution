import java.awt.*;
import java.text.DecimalFormat;

public class Network {

    private static final DecimalFormat rounder = new DecimalFormat("#.##");
    private static final int additionalLayerMax = 3; //how many layers besides default hidden + output can we have?
    private static final int neuronMax = 16; //how many neurons per layer can we have?
    private static final int networkInputs = 3;
    private static final int networkOutputs = 5;
    private int layerNum;
    private Layer[] layers;

    //TODO randomize the # of memory inputs for each neuron in this network? ie. remember last n actions each as an input.. (short-term memory)
    //TODO aggregate memory input? ie. if it fired n/n times in a row input 1, (n/2)/n 0.5, 0/n 0, etc... (long-term memory)
    //TODO re: memory, not needed, ie. do additional neurons + the existing 1-bit memory give capacity to evolve longer-term memory?
    public Network() {
        //determine layer structure, 2 + for at least 1 hidden layer, and the output layer
        layerNum = Controller.random.nextInt(additionalLayerMax+1) + 2;
        layers = new Layer[layerNum];
        //first layer
        layers[0] = new Layer(Controller.random.nextInt(neuronMax) + 1, networkInputs);
        //middle layers
        for (int i = 1; i < layerNum - 1; i++) {
            layers[i] = new Layer(Controller.random.nextInt(neuronMax) + 1, layers[i-1].neuronNum);
        }
        //output layer
        layers[layerNum-1] = new Layer(networkOutputs, layers[layerNum-2].neuronNum);
    }

    //create a new network modelled off an existing one, mutate if desired
    public Network(Network oldNetwork, boolean mutate) {
        layerNum = oldNetwork.layerNum;
        layers = new Layer[layerNum];
        for (int i = 0; i < layerNum; i++) {
            layers[i] = new Layer(oldNetwork.layers[i]);
        }
        //mutate? randomize a random weight in a random neuron in a random layer
        Layer mutationLayer;
        Neuron mutationNeuron;
        if (mutate) {
            mutationLayer = layers[Controller.random.nextInt(layers.length)];
            mutationNeuron = mutationLayer.neurons[Controller.random.nextInt(mutationLayer.neurons.length)];
            mutationNeuron.weights[Controller.random.nextInt(mutationNeuron.weights.length)] = Neuron.getRandomWeight();
        }
    }

    public double[] tick(double[] inputs) {
        //run through layers, output of one is input for the next
        for (int i = 0; i < layerNum; i++) {
            inputs = layers[i].tick(inputs);
        }
        return inputs;
    }

    public String getReadable() {
        String weights = "";
        String summary = layerNum + " layers: (";
        for (int i = 0; i < layerNum; i++) {
            if (i > 0) {
                weights += "\n";
                summary += ", ";
            }
            summary += layers[i].neuronNum;
            for (int j = 0; j < layers[i].neuronNum; j++) {
                weights += "[";
                for (int k = 0; k < layers[i].neurons[j].weights.length; k++) { //using weight length v. inputNum b/c of bias/memory/etc
                    if (k != 0) {
                        weights += ", ";
                    }
                    weights += rounder.format(layers[i].neurons[j].weights[k]);
                }
                weights += "] ";
            }
        }
        //DISABLED for brevity
        //return weights + "\n" + summary;
        summary += ")";
        return summary;
    }
}
