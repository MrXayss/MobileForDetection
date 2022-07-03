package org.tensorflow.lite.examples.classification.tflite;

import android.app.Activity;
import java.io.IOException;
import org.tensorflow.lite.support.common.TensorOperator;
import org.tensorflow.lite.support.common.ops.NormalizeOp;

public class ClassifierQuantizedEfficientNet extends Classifier {

  private static final float IMAGE_MEAN = 0.0f;

  private static final float IMAGE_STD = 1.0f;

  private static final float PROBABILITY_MEAN = 0.0f;

  private static final float PROBABILITY_STD = 255.0f;

  public ClassifierQuantizedEfficientNet(Activity activity, Device device, int numThreads)
      throws IOException {
    super(activity, device, numThreads);
  }

  @Override
  protected String getModelPath() {
//    return "model1.tflite";
    return "model_test4.tflite";
  }

  @Override
  protected String getLabelPath() {
    return "labels.txt";
  }

  @Override
  protected TensorOperator getPreprocessNormalizeOp() {
    return new NormalizeOp(IMAGE_MEAN, IMAGE_STD);
  }

  @Override
  protected TensorOperator getPostprocessNormalizeOp() {
    return new NormalizeOp(PROBABILITY_MEAN, PROBABILITY_STD);
  }
}
