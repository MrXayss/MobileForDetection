/* Copyright 2019 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package org.tensorflow.lite.examples.classification.tflite;

import static java.lang.Math.min;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.SystemClock;
import android.os.Trace;
import android.util.Log;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.support.metadata.MetadataExtractor;
import org.tensorflow.lite.task.core.BaseOptions;
import org.tensorflow.lite.task.core.vision.ImageProcessingOptions;
import org.tensorflow.lite.task.core.vision.ImageProcessingOptions.Orientation;
import org.tensorflow.lite.task.vision.classifier.Classifications;
import org.tensorflow.lite.task.vision.classifier.ImageClassifier;
import org.tensorflow.lite.task.vision.classifier.ImageClassifier.ImageClassifierOptions;

public abstract class Classifier {
  public static final String TAG = "ClassifierWithTaskApi";

  public enum Model {
    FLOAT_MOBILENET,
    QUANTIZED_MOBILENET,
    QUANTIZED_EFFICIENTNET
  }

  public enum Device {
    CPU,
    NNAPI,
    GPU
  }

  private static final int MAX_RESULTS = 3;

  private final int imageSizeX;

  private final int imageSizeY;
  protected final ImageClassifier imageClassifier;


  public static Classifier create(Activity activity, Model model, Device device, int numThreads)
      throws IOException {
    if (model == Model.QUANTIZED_EFFICIENTNET) {
      return new ClassifierQuantizedEfficientNet(activity, device, numThreads);
    } else {
      throw new UnsupportedOperationException();
    }
  }


  public static class Recognition {

    private final String id;

    private final String title;


    private final Float confidence;

    private RectF location;

    public Recognition(
        final String id, final String title, final Float confidence, final RectF location) {
      this.id = id;
      this.title = title;
      this.confidence = confidence;
      this.location = location;
    }

    public String getId() {
      return id;
    }

    public String getTitle() {
      return title;
    }

    public Float getConfidence() {
      return confidence;
    }

    public RectF getLocation() {
      return new RectF(location);
    }

    public void setLocation(RectF location) {
      this.location = location;
    }

    @Override
    public String toString() {
      String resultString = "";
      if (id != null) {
        resultString += "[" + id + "] ";
      }

      if (title != null) {
        resultString += title + " ";
      }

      if (confidence != null) {
        resultString += String.format("(%.1f%%) ", confidence * 100.0f);
      }

      if (location != null) {
        resultString += location + " ";
      }

      return resultString.trim();
    }
  }

  protected Classifier(Activity activity, Device device, int numThreads) throws IOException {
    BaseOptions.Builder baseOptionsBuilder = BaseOptions.builder();
    switch (device) {
      case GPU:
        baseOptionsBuilder.useGpu();
        break;
      case NNAPI:
        baseOptionsBuilder.useNnapi();
        break;
      default:
        break;
    }

    ImageClassifierOptions options =
        ImageClassifierOptions.builder()
            .setBaseOptions(baseOptionsBuilder.setNumThreads(numThreads).build())
            .setMaxResults(MAX_RESULTS)
            .build();
    imageClassifier = ImageClassifier.createFromFileAndOptions(activity, getModelPath(), options);
    Log.d(TAG, "Created a Tensorflow Lite Image Classifier.");

    MappedByteBuffer tfliteModel = FileUtil.loadMappedFile(activity, getModelPath());
    MetadataExtractor metadataExtractor = new MetadataExtractor(tfliteModel);
    int[] imageShape = metadataExtractor.getInputTensorShape(/*inputIndex=*/ 0);
    imageSizeY = imageShape[1];
    imageSizeX = imageShape[2];
  }

  public List<Recognition> recognizeImage(final Bitmap bitmap, int sensorOrientation) {
    Trace.beginSection("recognizeImage");

    TensorImage inputImage = TensorImage.fromBitmap(bitmap);
    int width = bitmap.getWidth();
    int height = bitmap.getHeight();
    int cropSize = min(width, height);
    ImageProcessingOptions imageOptions =
        ImageProcessingOptions.builder()
            .setOrientation(getOrientation(sensorOrientation))
            // Set the ROI to the center of the image.
            .setRoi(
                new Rect(
                    /*left=*/ (width - cropSize) / 2,
                    /*top=*/ (height - cropSize) / 2,
                    /*right=*/ (width + cropSize) / 2,
                    /*bottom=*/ (height + cropSize) / 2))
            .build();

    Trace.beginSection("runInference");
    long startTimeForReference = SystemClock.uptimeMillis();
    List<Classifications> results = imageClassifier.classify(inputImage, imageOptions);
    long endTimeForReference = SystemClock.uptimeMillis();
    Trace.endSection();
    Log.v(TAG, "Timecost to run model inference: " + (endTimeForReference - startTimeForReference));

    Trace.endSection();

    return getRecognitions(results);
  }

  public void close() {
    if (imageClassifier != null) {
      imageClassifier.close();
    }
  }

  public int getImageSizeX() {
    return imageSizeX;
  }

  public int getImageSizeY() {
    return imageSizeY;
  }

  private static List<Recognition> getRecognitions(List<Classifications> classifications) {

    final ArrayList<Recognition> recognitions = new ArrayList<>();
    // All the demo models are single head models. Get the first Classifications in the results.
    for (Category category : classifications.get(0).getCategories()) {
      recognitions.add(
          new Recognition(
              "" + category.getLabel(), category.getLabel(), category.getScore(), null));
    }
    return recognitions;
  }

  private static Orientation getOrientation(int cameraOrientation) {
    switch (cameraOrientation / 90) {
      case 3:
        return Orientation.BOTTOM_LEFT;
      case 2:
        return Orientation.BOTTOM_RIGHT;
      case 1:
        return Orientation.TOP_RIGHT;
      default:
        return Orientation.TOP_LEFT;
    }
  }

  protected abstract String getModelPath();
}
