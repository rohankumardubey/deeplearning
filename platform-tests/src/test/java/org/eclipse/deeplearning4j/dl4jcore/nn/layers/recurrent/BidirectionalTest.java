/*
 *  ******************************************************************************
 *  *
 *  *
 *  * This program and the accompanying materials are made available under the
 *  * terms of the Apache License, Version 2.0 which is available at
 *  * https://www.apache.org/licenses/LICENSE-2.0.
 *  *
 *  *  See the NOTICE file distributed with this work for additional
 *  *  information regarding copyright ownership.
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  * License for the specific language governing permissions and limitations
 *  * under the License.
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *  *****************************************************************************
 */
package org.eclipse.deeplearning4j.dl4jcore.nn.layers.recurrent;

import lombok.extern.slf4j.Slf4j;
import org.deeplearning4j.BaseDL4JTest;
import org.deeplearning4j.datasets.iterator.impl.SingletonMultiDataSetIterator;
import org.deeplearning4j.earlystopping.EarlyStoppingConfiguration;
import org.deeplearning4j.earlystopping.saver.InMemoryModelSaver;
import org.deeplearning4j.earlystopping.scorecalc.DataSetLossCalculator;
import org.deeplearning4j.earlystopping.termination.MaxEpochsTerminationCondition;
import org.deeplearning4j.earlystopping.trainer.EarlyStoppingGraphTrainer;
import org.deeplearning4j.nn.conf.*;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.GravesBidirectionalLSTM;
import org.deeplearning4j.nn.conf.layers.GravesLSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.conf.layers.recurrent.Bidirectional;
import org.deeplearning4j.nn.conf.layers.recurrent.SimpleRnn;
import org.deeplearning4j.nn.conf.layers.variational.BernoulliReconstructionDistribution;
import org.deeplearning4j.nn.conf.layers.variational.VariationalAutoencoder;
import org.deeplearning4j.nn.gradient.Gradient;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.updater.MultiLayerUpdater;
import org.deeplearning4j.nn.updater.graph.ComputationGraphUpdater;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.util.ModelSerializer;
import org.deeplearning4j.util.TimeSeriesUtils;
import org.junit.jupiter.api.Tag;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.nd4j.common.tests.tags.NativeTag;
import org.nd4j.common.tests.tags.TagNames;
import org.nd4j.linalg.BaseNd4jTestWithBackends;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.buffer.DataType;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.MultiDataSet;
import org.nd4j.linalg.dataset.api.iterator.MultiDataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.factory.Nd4jBackend;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.nd4j.common.primitives.Pair;
import org.deeplearning4j.nn.workspace.ArrayType;
import org.deeplearning4j.nn.workspace.LayerWorkspaceMgr;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.deeplearning4j.nn.conf.RNNFormat.NCW;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;

@Slf4j
@DisplayName("Bidirectional Test")
@NativeTag
@Tag(TagNames.DL4J_OLD_API)
class BidirectionalTest extends BaseDL4JTest {



    public static Stream<Arguments> params() {
        List<Arguments> args = new ArrayList<>();
        for(Nd4jBackend nd4jBackend : BaseNd4jTestWithBackends.BACKENDS) {
            for(RNNFormat rnnFormat : RNNFormat.values()) {
                args.add(Arguments.of(rnnFormat,nd4jBackend));
            }
        }
        return args.stream();
    }


    @DisplayName("Compare Implementations")
    @ParameterizedTest
    @MethodSource("params")
    void compareImplementations(RNNFormat rnnDataFormat,Nd4jBackend backend) {
        for (WorkspaceMode wsm : WorkspaceMode.values()) {
            log.info("*** Starting workspace mode: " + wsm);
            // Bidirectional(GravesLSTM) and GravesBidirectionalLSTM should be equivalent, given equivalent params
            // Note that GravesBidirectionalLSTM implements ADD mode only
            MultiLayerConfiguration conf1 = new NeuralNetConfiguration.Builder().activation(Activation.TANH).weightInit(WeightInit.XAVIER).trainingWorkspaceMode(wsm).inferenceWorkspaceMode(wsm).updater(new Adam()).list().layer(new Bidirectional(Bidirectional.Mode.ADD, new GravesLSTM.Builder().nIn(10).nOut(10).dataFormat(rnnDataFormat).build())).layer(new Bidirectional(Bidirectional.Mode.ADD, new GravesLSTM.Builder().nIn(10).nOut(10).dataFormat(rnnDataFormat).build())).layer(new RnnOutputLayer.Builder().lossFunction(LossFunctions.LossFunction.MSE).dataFormat(rnnDataFormat).nIn(10).nOut(10).build()).build();
            MultiLayerConfiguration conf2 = new NeuralNetConfiguration.Builder().activation(Activation.TANH).weightInit(WeightInit.XAVIER).trainingWorkspaceMode(wsm).inferenceWorkspaceMode(wsm).updater(new Adam()).list().layer(new GravesBidirectionalLSTM.Builder().nIn(10).nOut(10).dataFormat(rnnDataFormat).build()).layer(new GravesBidirectionalLSTM.Builder().nIn(10).nOut(10).dataFormat(rnnDataFormat).build()).layer(new RnnOutputLayer.Builder().lossFunction(LossFunctions.LossFunction.MSE).dataFormat(rnnDataFormat).nIn(10).nOut(10).build()).build();
            MultiLayerNetwork net1 = new MultiLayerNetwork(conf1);
            net1.init();
            MultiLayerNetwork net2 = new MultiLayerNetwork(conf2);
            net2.init();
            assertEquals(net1.numParams(), net2.numParams());
            for (int i = 0; i < 3; i++) {
                int n1 = (int) net1.getLayer(i).numParams();
                int n2 = (int) net2.getLayer(i).numParams();
                assertEquals(n1, n2);
            }
            // Assuming exact same layout here...
            net2.setParams(net1.params());
            INDArray in;
            if (rnnDataFormat == NCW) {
                in = Nd4j.rand(new int[] { 3, 10, 5 });
            } else {
                in = Nd4j.rand(new int[] { 3, 5, 10 });
            }
            INDArray out1 = net1.output(in);
            INDArray out2 = net2.output(in);
            assertEquals(out1, out2);
            INDArray labels;
            if (rnnDataFormat == NCW) {
                labels = Nd4j.rand(new int[] { 3, 10, 5 });
            } else {
                labels = Nd4j.rand(new int[] { 3, 5, 10 });
            }
            net1.setInput(in);
            net1.setLabels(labels);
            net2.setInput(in);
            net2.setLabels(labels);
            net1.computeGradientAndScore();
            net2.computeGradientAndScore();
            // Ensure scores are equal:
            assertEquals(net1.score(), net2.score(), 1e-6);
            // Ensure gradients are equal:
            Gradient g1 = net1.gradient();
            Gradient g2 = net2.gradient();
            assertEquals(g1.gradient(), g2.gradient());
            // Ensure updates are equal:
            MultiLayerUpdater u1 = (MultiLayerUpdater) net1.getUpdater();
            MultiLayerUpdater u2 = (MultiLayerUpdater) net2.getUpdater();
            assertEquals(u1.getUpdaterStateViewArray(), u2.getUpdaterStateViewArray());
            u1.update(net1, g1, 0, 0, 3, LayerWorkspaceMgr.noWorkspaces());
            u2.update(net2, g2, 0, 0, 3, LayerWorkspaceMgr.noWorkspaces());
            assertEquals(g1.gradient(), g2.gradient());
            assertEquals(u1.getUpdaterStateViewArray(), u2.getUpdaterStateViewArray());
            // Ensure params are equal, after fitting
            net1.fit(in, labels);
            net2.fit(in, labels);
            INDArray p1 = net1.params();
            INDArray p2 = net2.params();
            assertEquals(p1, p2);
        }
    }

    @DisplayName("Compare Implementations Comp Graph")
    @ParameterizedTest
    @MethodSource("params")
    void compareImplementationsCompGraph(RNNFormat rnnFormat,Nd4jBackend backend) {
        // for(WorkspaceMode wsm : WorkspaceMode.values()) {
        for (WorkspaceMode wsm : new WorkspaceMode[] { WorkspaceMode.NONE, WorkspaceMode.ENABLED }) {
            log.info("*** Starting workspace mode: " + wsm);
            // Bidirectional(GravesLSTM) and GravesBidirectionalLSTM should be equivalent, given equivalent params
            // Note that GravesBidirectionalLSTM implements ADD mode only
            ComputationGraphConfiguration conf1 = new NeuralNetConfiguration.Builder().activation(Activation.TANH).weightInit(WeightInit.XAVIER).updater(new Adam()).trainingWorkspaceMode(wsm).inferenceWorkspaceMode(wsm).graphBuilder().addInputs("in").layer("0", new Bidirectional(Bidirectional.Mode.ADD, new GravesLSTM.Builder().nIn(10).nOut(10).build()), "in").layer("1", new Bidirectional(Bidirectional.Mode.ADD, new GravesLSTM.Builder().nIn(10).nOut(10).build()), "0").layer("2", new RnnOutputLayer.Builder().lossFunction(LossFunctions.LossFunction.MSE).nIn(10).nOut(10).build(), "1").setOutputs("2").build();
            ComputationGraphConfiguration conf2 = new NeuralNetConfiguration.Builder().activation(Activation.TANH).weightInit(WeightInit.XAVIER).updater(new Adam()).trainingWorkspaceMode(wsm).inferenceWorkspaceMode(wsm).graphBuilder().addInputs("in").layer("0", new GravesBidirectionalLSTM.Builder().nIn(10).nOut(10).build(), "in").layer("1", new GravesBidirectionalLSTM.Builder().nIn(10).nOut(10).build(), "0").layer("2", new RnnOutputLayer.Builder().lossFunction(LossFunctions.LossFunction.MSE).nIn(10).nOut(10).build(), "1").setOutputs("2").build();
            ComputationGraph net1 = new ComputationGraph(conf1);
            net1.init();
            ComputationGraph net2 = new ComputationGraph(conf2);
            net2.init();
            assertEquals(net1.numParams(), net2.numParams());
            for (int i = 0; i < 3; i++) {
                int n1 = (int) net1.getLayer(i).numParams();
                int n2 = (int) net2.getLayer(i).numParams();
                assertEquals(n1, n2);
            }
            // Assuming exact same layout here...
            net2.setParams(net1.params());
            INDArray in = Nd4j.rand(new int[] { 3, 10, 5 });
            INDArray out1 = net1.outputSingle(in);
            INDArray out2 = net2.outputSingle(in);
            assertEquals(out1, out2);
            INDArray labels = Nd4j.rand(new int[] { 3, 10, 5 });
            net1.setInput(0, in);
            net1.setLabels(labels);
            net2.setInput(0, in);
            net2.setLabels(labels);
            net1.computeGradientAndScore();
            net2.computeGradientAndScore();
            // Ensure scores are equal:
            assertEquals(net1.score(), net2.score(), 1e-6);
            // Ensure gradients are equal:
            Gradient g1 = net1.gradient();
            Gradient g2 = net2.gradient();
            assertEquals(g1.gradient(), g2.gradient());
            // Ensure updates are equal:
            ComputationGraphUpdater u1 = net1.getUpdater();
            ComputationGraphUpdater u2 = net2.getUpdater();
            assertEquals(u1.getUpdaterStateViewArray(), u2.getUpdaterStateViewArray());
            u1.update(g1, 0, 0, 3, LayerWorkspaceMgr.noWorkspaces());
            u2.update(g2, 0, 0, 3, LayerWorkspaceMgr.noWorkspaces());
            assertEquals(g1.gradient(), g2.gradient());
            assertEquals(u1.getUpdaterStateViewArray(), u2.getUpdaterStateViewArray());
            // Ensure params are equal, after fitting
            net1.fit(new DataSet(in, labels));
            net2.fit(new DataSet(in, labels));
            INDArray p1 = net1.params();
            INDArray p2 = net2.params();
            assertEquals(p1, p2);
        }
    }

    @DisplayName("Test Serialization")
    @ParameterizedTest
    @MethodSource("params")
    void testSerialization(RNNFormat rnnDataFormat,Nd4jBackend backend) throws Exception {
        for (WorkspaceMode wsm : WorkspaceMode.values()) {
            log.info("*** Starting workspace mode: " + wsm);
            Nd4j.getRandom().setSeed(12345);
            MultiLayerConfiguration conf1 = new NeuralNetConfiguration.Builder().activation(Activation.TANH).weightInit(WeightInit.XAVIER).trainingWorkspaceMode(wsm).inferenceWorkspaceMode(wsm).updater(new Adam()).list().layer(new Bidirectional(Bidirectional.Mode.ADD, new GravesLSTM.Builder().nIn(10).nOut(10).dataFormat(rnnDataFormat).build())).layer(new Bidirectional(Bidirectional.Mode.ADD, new GravesLSTM.Builder().nIn(10).nOut(10).dataFormat(rnnDataFormat).build())).layer(new RnnOutputLayer.Builder().lossFunction(LossFunctions.LossFunction.MSE).nIn(10).nOut(10).dataFormat(rnnDataFormat).build()).build();
            MultiLayerNetwork net1 = new MultiLayerNetwork(conf1);
            net1.init();
            INDArray in;
            INDArray labels;
            long[] inshape = rnnDataFormat == NCW ? new long[] { 3, 10, 5 } : new long[] { 3, 5, 10 };
            in = Nd4j.rand(inshape);
            labels = Nd4j.rand(inshape);
            net1.fit(in, labels);
            byte[] bytes;
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                ModelSerializer.writeModel(net1, baos, true);
                bytes = baos.toByteArray();
            }
            MultiLayerNetwork net2 = ModelSerializer.restoreMultiLayerNetwork(new ByteArrayInputStream(bytes), true);
            in = Nd4j.rand(inshape);
            labels = Nd4j.rand(inshape);
            INDArray out1 = net1.output(in);
            INDArray out2 = net2.output(in);
            assertEquals(out1, out2);
            net1.setInput(in);
            net2.setInput(in);
            net1.setLabels(labels);
            net2.setLabels(labels);
            net1.computeGradientAndScore();
            net2.computeGradientAndScore();
            assertEquals(net1.score(), net2.score(), 1e-6);
            assertEquals(net1.gradient().gradient(), net2.gradient().gradient());
        }
    }

    @DisplayName("Test Serialization Comp Graph")
    @ParameterizedTest
    @MethodSource("params")
    void testSerializationCompGraph(RNNFormat rnnDataFormat,Nd4jBackend backend) throws Exception {
        for (WorkspaceMode wsm : WorkspaceMode.values()) {
            log.info("*** Starting workspace mode: " + wsm);
            Nd4j.getRandom().setSeed(12345);
            ComputationGraphConfiguration conf1 = new NeuralNetConfiguration.Builder().activation(Activation.TANH).weightInit(WeightInit.XAVIER).trainingWorkspaceMode(wsm).inferenceWorkspaceMode(wsm).updater(new Adam()).graphBuilder().addInputs("in").layer("0", new Bidirectional(Bidirectional.Mode.ADD, new GravesLSTM.Builder().nIn(10).nOut(10).dataFormat(rnnDataFormat).build()), "in").layer("1", new Bidirectional(Bidirectional.Mode.ADD, new GravesLSTM.Builder().nIn(10).nOut(10).dataFormat(rnnDataFormat).build()), "0").layer("2", new RnnOutputLayer.Builder().lossFunction(LossFunctions.LossFunction.MSE).dataFormat(rnnDataFormat).nIn(10).nOut(10).build(), "1").setOutputs("2").build();
            ComputationGraph net1 = new ComputationGraph(conf1);
            net1.init();
            long[] inshape = (rnnDataFormat == NCW) ? new long[] { 3, 10, 5 } : new long[] { 3, 5, 10 };
            INDArray in = Nd4j.rand(inshape);
            INDArray labels = Nd4j.rand(inshape);
            net1.fit(new DataSet(in, labels));
            byte[] bytes;
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                ModelSerializer.writeModel(net1, baos, true);
                bytes = baos.toByteArray();
            }
            ComputationGraph net2 = ModelSerializer.restoreComputationGraph(new ByteArrayInputStream(bytes), true);
            in = Nd4j.rand(inshape);
            labels = Nd4j.rand(inshape);
            INDArray out1 = net1.outputSingle(in);
            INDArray out2 = net2.outputSingle(in);
            assertEquals(out1, out2);
            net1.setInput(0, in);
            net2.setInput(0, in);
            net1.setLabels(labels);
            net2.setLabels(labels);
            net1.computeGradientAndScore();
            net2.computeGradientAndScore();
            assertEquals(net1.score(), net2.score(), 1e-6);
            assertEquals(net1.gradient().gradient(), net2.gradient().gradient());
        }
    }

    @DisplayName("Test Simple Bidirectional")
    @ParameterizedTest
    @MethodSource("params")
    public void testSimpleBidirectional(RNNFormat rnnDataFormat,Nd4jBackend backend) {
        for (WorkspaceMode wsm : WorkspaceMode.values()) {
            log.info("*** Starting workspace mode: " + wsm);
            Nd4j.getRandom().setSeed(12345);
            Bidirectional.Mode[] modes = new Bidirectional.Mode[] { Bidirectional.Mode.CONCAT, Bidirectional.Mode.ADD, Bidirectional.Mode.AVERAGE, Bidirectional.Mode.MUL };
            long[] inshape = rnnDataFormat == NCW ? new long[] { 3, 10, 6 } : new long[] { 3, 6, 10 };
            INDArray in = Nd4j.rand(inshape);
            for (Bidirectional.Mode m : modes) {
                MultiLayerConfiguration conf1 = new NeuralNetConfiguration.Builder().dataType(DataType.DOUBLE).activation(Activation.TANH).weightInit(WeightInit.XAVIER).trainingWorkspaceMode(wsm).inferenceWorkspaceMode(wsm).updater(new Adam()).list().layer(new Bidirectional(m, new SimpleRnn.Builder().nIn(10).nOut(10).dataFormat(rnnDataFormat).build())).build();
                MultiLayerNetwork net1 = new MultiLayerNetwork(conf1);
                net1.init();
                MultiLayerConfiguration conf2 = new NeuralNetConfiguration.Builder().dataType(DataType.DOUBLE).activation(Activation.TANH).weightInit(WeightInit.XAVIER).updater(new Adam()).list().layer(new SimpleRnn.Builder().nIn(10).nOut(10).dataFormat(rnnDataFormat).build()).build();
                MultiLayerNetwork net2 = new MultiLayerNetwork(conf2.clone());
                net2.init();
                MultiLayerNetwork net3 = new MultiLayerNetwork(conf2.clone());
                net3.init();
                net2.setParam("0_W", net1.getParam("0_fW"));
                net2.setParam("0_RW", net1.getParam("0_fRW"));
                net2.setParam("0_b", net1.getParam("0_fb"));
                net3.setParam("0_W", net1.getParam("0_bW"));
                net3.setParam("0_RW", net1.getParam("0_bRW"));
                net3.setParam("0_b", net1.getParam("0_bb"));
                INDArray inReverse = TimeSeriesUtils.reverseTimeSeries(in, LayerWorkspaceMgr.noWorkspaces(), ArrayType.INPUT, rnnDataFormat);
                INDArray out1 = net1.output(in);
                INDArray out2 = net2.output(in);
                INDArray out3 = TimeSeriesUtils.reverseTimeSeries(net3.output(inReverse), LayerWorkspaceMgr.noWorkspaces(), ArrayType.INPUT, rnnDataFormat);
                INDArray outExp;
                switch(m) {
                    case ADD:
                        outExp = out2.add(out3);
                        break;
                    case MUL:
                        outExp = out2.mul(out3);
                        break;
                    case AVERAGE:
                        outExp = out2.add(out3).muli(0.5);
                        break;
                    case CONCAT:
                        outExp = Nd4j.concat((rnnDataFormat == NCW) ? 1 : 2, out2, out3);
                        break;
                    default:
                        throw new RuntimeException();
                }
                assertEquals(outExp, out1,m.toString());
                // Check gradients:
                if (m == Bidirectional.Mode.ADD || m == Bidirectional.Mode.CONCAT) {
                    INDArray eps = Nd4j.rand(inshape);
                    INDArray eps1;
                    if (m == Bidirectional.Mode.CONCAT) {
                        eps1 = Nd4j.concat((rnnDataFormat == NCW) ? 1 : 2, eps, eps);
                    } else {
                        eps1 = eps;
                    }
                    net1.setInput(in);
                    net2.setInput(in);
                    net3.setInput(TimeSeriesUtils.reverseTimeSeries(in, LayerWorkspaceMgr.noWorkspaces(), ArrayType.INPUT, rnnDataFormat));
                    net1.feedForward(true, false);
                    net2.feedForward(true, false);
                    net3.feedForward(true, false);
                    Pair<Gradient, INDArray> p1 = net1.backpropGradient(eps1, LayerWorkspaceMgr.noWorkspaces());
                    Pair<Gradient, INDArray> p2 = net2.backpropGradient(eps, LayerWorkspaceMgr.noWorkspaces());
                    Pair<Gradient, INDArray> p3 = net3.backpropGradient(TimeSeriesUtils.reverseTimeSeries(eps, LayerWorkspaceMgr.noWorkspaces(), ArrayType.INPUT, rnnDataFormat), LayerWorkspaceMgr.noWorkspaces());
                    Gradient g1 = p1.getFirst();
                    Gradient g2 = p2.getFirst();
                    Gradient g3 = p3.getFirst();
                    for (boolean updates : new boolean[] { false, true }) {
                        if (updates) {
                            net1.getUpdater().update(net1, g1, 0, 0, 3, LayerWorkspaceMgr.noWorkspaces());
                            net2.getUpdater().update(net2, g2, 0, 0, 3, LayerWorkspaceMgr.noWorkspaces());
                            net3.getUpdater().update(net3, g3, 0, 0, 3, LayerWorkspaceMgr.noWorkspaces());
                        }
                        assertEquals(g2.gradientForVariable().get("0_W"), g1.gradientForVariable().get("0_fW"));
                        assertEquals(g2.gradientForVariable().get("0_RW"), g1.gradientForVariable().get("0_fRW"));
                        assertEquals(g2.gradientForVariable().get("0_b"), g1.gradientForVariable().get("0_fb"));
                        assertEquals(g3.gradientForVariable().get("0_W"), g1.gradientForVariable().get("0_bW"));
                        assertEquals(g3.gradientForVariable().get("0_RW"), g1.gradientForVariable().get("0_bRW"));
                        assertEquals(g3.gradientForVariable().get("0_b"), g1.gradientForVariable().get("0_bb"));
                    }
                }
            }
        }
    }

    @DisplayName("Test Simple Bidirectional Comp Graph")
    @ParameterizedTest
    @MethodSource("params")
    void testSimpleBidirectionalCompGraph(RNNFormat rnnDataFormat,Nd4jBackend backend) {
        for (WorkspaceMode wsm : WorkspaceMode.values()) {
            log.info("*** Starting workspace mode: " + wsm);
            Nd4j.getRandom().setSeed(12345);
            Bidirectional.Mode[] modes = new Bidirectional.Mode[] { Bidirectional.Mode.CONCAT, Bidirectional.Mode.ADD, Bidirectional.Mode.AVERAGE, Bidirectional.Mode.MUL };
            long[] inshape = rnnDataFormat == NCW ? new long[] { 3, 10, 6 } : new long[] { 3, 6, 10 };
            INDArray in = Nd4j.rand(inshape);
            for (Bidirectional.Mode m : modes) {
                ComputationGraphConfiguration conf1 = new NeuralNetConfiguration.Builder().dataType(DataType.DOUBLE).activation(Activation.TANH).weightInit(WeightInit.XAVIER).trainingWorkspaceMode(wsm).inferenceWorkspaceMode(wsm).updater(new Adam()).graphBuilder().addInputs("in").layer("0", new Bidirectional(m, new SimpleRnn.Builder().nIn(10).nOut(10).dataFormat(rnnDataFormat).build()), "in").setOutputs("0").build();
                ComputationGraph net1 = new ComputationGraph(conf1);
                net1.init();
                ComputationGraphConfiguration conf2 = new NeuralNetConfiguration.Builder().dataType(DataType.DOUBLE).activation(Activation.TANH).weightInit(WeightInit.XAVIER).updater(new Adam()).graphBuilder().addInputs("in").layer("0", new SimpleRnn.Builder().nIn(10).nOut(10).dataFormat(rnnDataFormat).build(), "in").setOutputs("0").build();
                ComputationGraph net2 = new ComputationGraph(conf2.clone());
                net2.init();
                ComputationGraph net3 = new ComputationGraph(conf2.clone());
                net3.init();
                net2.setParam("0_W", net1.getParam("0_fW"));
                net2.setParam("0_RW", net1.getParam("0_fRW"));
                net2.setParam("0_b", net1.getParam("0_fb"));
                net3.setParam("0_W", net1.getParam("0_bW"));
                net3.setParam("0_RW", net1.getParam("0_bRW"));
                net3.setParam("0_b", net1.getParam("0_bb"));
                INDArray out1 = net1.outputSingle(in);
                INDArray out2 = net2.outputSingle(in);
                INDArray out3;
                INDArray inReverse;
                if (rnnDataFormat == RNNFormat.NWC) {
                    inReverse = TimeSeriesUtils.reverseTimeSeries(in.permute(0, 2, 1), LayerWorkspaceMgr.noWorkspaces(), ArrayType.INPUT).permute(0, 2, 1);
                    out3 = net3.outputSingle(inReverse);
                    out3 = TimeSeriesUtils.reverseTimeSeries(out3.permute(0, 2, 1), LayerWorkspaceMgr.noWorkspaces(), ArrayType.INPUT).permute(0, 2, 1);
                } else {
                    inReverse = TimeSeriesUtils.reverseTimeSeries(in, LayerWorkspaceMgr.noWorkspaces(), ArrayType.INPUT);
                    out3 = net3.outputSingle(inReverse);
                    out3 = TimeSeriesUtils.reverseTimeSeries(out3, LayerWorkspaceMgr.noWorkspaces(), ArrayType.INPUT);
                }
                INDArray outExp;
                switch(m) {
                    case ADD:
                        outExp = out2.add(out3);
                        break;
                    case MUL:
                        outExp = out2.mul(out3);
                        break;
                    case AVERAGE:
                        outExp = out2.add(out3).muli(0.5);
                        break;
                    case CONCAT:
                        System.out.println(out2.shapeInfoToString());
                        System.out.println(out3.shapeInfoToString());
                        outExp = Nd4j.concat((rnnDataFormat == NCW) ? 1 : 2, out2, out3);
                        break;
                    default:
                        throw new RuntimeException();
                }
                assertEquals(outExp, out1,m.toString());
                // Check gradients:
                if (m == Bidirectional.Mode.ADD || m == Bidirectional.Mode.CONCAT) {
                    INDArray eps = Nd4j.rand(inshape);
                    INDArray eps1;
                    if (m == Bidirectional.Mode.CONCAT) {
                        eps1 = Nd4j.concat((rnnDataFormat == NCW) ? 1 : 2, eps, eps);
                    } else {
                        eps1 = eps;
                    }
                    INDArray epsReversed = (rnnDataFormat == NCW) ? TimeSeriesUtils.reverseTimeSeries(eps, LayerWorkspaceMgr.noWorkspaces(), ArrayType.INPUT) : TimeSeriesUtils.reverseTimeSeries(eps.permute(0, 2, 1), LayerWorkspaceMgr.noWorkspaces(), ArrayType.INPUT).permute(0, 2, 1);
                    net1.outputSingle(true, false, in);
                    net2.outputSingle(true, false, in);
                    net3.outputSingle(true, false, inReverse);
                    Gradient g1 = net1.backpropGradient(eps1);
                    Gradient g2 = net2.backpropGradient(eps);
                    Gradient g3 = net3.backpropGradient(epsReversed);
                    for (boolean updates : new boolean[] { false, true }) {
                        if (updates) {
                            net1.getUpdater().update(g1, 0, 0, 3, LayerWorkspaceMgr.noWorkspaces());
                            net2.getUpdater().update(g2, 0, 0, 3, LayerWorkspaceMgr.noWorkspaces());
                            net3.getUpdater().update(g3, 0, 0, 3, LayerWorkspaceMgr.noWorkspaces());
                        }
                        assertEquals(g2.gradientForVariable().get("0_W"), g1.gradientForVariable().get("0_fW"));
                        assertEquals(g2.gradientForVariable().get("0_RW"), g1.gradientForVariable().get("0_fRW"));
                        assertEquals(g2.gradientForVariable().get("0_b"), g1.gradientForVariable().get("0_fb"));
                        assertEquals(g3.gradientForVariable().get("0_W"), g1.gradientForVariable().get("0_bW"));
                        assertEquals(g3.gradientForVariable().get("0_RW"), g1.gradientForVariable().get("0_bRW"));
                        assertEquals(g3.gradientForVariable().get("0_b"), g1.gradientForVariable().get("0_bb"));
                    }
                }
            }
        }
    }

    @DisplayName("Test Issue 5472")
    @MethodSource("params")
    @ParameterizedTest
    void testIssue5472(RNNFormat rnnDataFormat,Nd4jBackend backend) {
        // https://github.com/eclipse/deeplearning4j/issues/5472
        int in = 2;
        int out = 2;
        ComputationGraphConfiguration.GraphBuilder builder = new NeuralNetConfiguration.Builder().updater(new Adam(0.01)).activation(Activation.RELU).graphBuilder().addInputs("IN").setInputTypes(InputType.recurrent(in)).addLayer("AUTOENCODER", new VariationalAutoencoder.Builder().encoderLayerSizes(64).decoderLayerSizes(64).nOut(7).pzxActivationFunction(Activation.IDENTITY).reconstructionDistribution(new BernoulliReconstructionDistribution(Activation.SIGMOID.getActivationFunction())).build(), "IN").addLayer("RNN", new Bidirectional(Bidirectional.Mode.ADD, new GravesLSTM.Builder().nOut(128).build()), "AUTOENCODER").addLayer("OUT", new RnnOutputLayer.Builder().nOut(out).activation(Activation.SOFTMAX).lossFunction(LossFunctions.LossFunction.MCXENT).build(), "RNN").setOutputs("OUT");
        ComputationGraph net = new ComputationGraph(builder.build());
        net.init();
        MultiDataSetIterator iterator = new SingletonMultiDataSetIterator(new MultiDataSet(Nd4j.create(10, in, 5), Nd4j.create(10, out, 5)));
        EarlyStoppingConfiguration.Builder b = new EarlyStoppingConfiguration.Builder<>().epochTerminationConditions(new MaxEpochsTerminationCondition(10)).scoreCalculator(new DataSetLossCalculator(iterator, true)).evaluateEveryNEpochs(1).modelSaver(new InMemoryModelSaver<>());
        EarlyStoppingGraphTrainer earlyStoppingGraphTrainer = new EarlyStoppingGraphTrainer(b.build(), net, iterator, null);
        earlyStoppingGraphTrainer.fit();
    }
}
