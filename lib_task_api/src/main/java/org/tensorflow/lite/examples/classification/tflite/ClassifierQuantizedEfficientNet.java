package org.tensorflow.lite.examples.classification.tflite;

import android.app.Activity;
import java.io.IOException;

public class ClassifierQuantizedEfficientNet extends Classifier {

  public ClassifierQuantizedEfficientNet(Activity activity, Device device, int numThreads)
      throws IOException {
    super(activity, device, numThreads);
  }

  @Override
  protected String getModelPath() {
//    return "model1.tflite";
    return "model_test4.tflite";
  }
}
