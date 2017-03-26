public class Layer {

    public int neuronNum;
    public Neuron[] neurons;

    public Layer(int neuronNum, int inputNum) {
        this.neuronNum = neuronNum;
        neurons = new Neuron[neuronNum];
        for (int i = 0; i < neuronNum; i++) {
            neurons[i] = new Neuron(inputNum);
        }
    }

    //new layer modelled off old layer
    public Layer (Layer oldLayer) {
        neuronNum = oldLayer.neuronNum;
        neurons = new Neuron[neuronNum];
        for (int i = 0; i < neuronNum; i++) {
            neurons[i] = new Neuron(oldLayer.neurons[i]);
        }
    }

    public double[] tick(double[] inputs) {
        double[] outputs = new double[neuronNum];
        for (int i = 0; i < neuronNum; i++) {
            outputs[i] = neurons[i].tick(inputs);
        }
        return outputs;
    }
}
