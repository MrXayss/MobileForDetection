package org.tensorflow.lite.examples.classification;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.ImageReader.OnImageAvailableListener;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.util.List;
import org.tensorflow.lite.examples.classification.env.BorderedText;
import org.tensorflow.lite.examples.classification.env.Logger;
import org.tensorflow.lite.examples.classification.tflite.Classifier;
import org.tensorflow.lite.examples.classification.tflite.Classifier.Device;
import org.tensorflow.lite.examples.classification.tflite.Classifier.Model;

public class ClassifierActivity extends CameraActivity implements OnImageAvailableListener {
  private static final Logger LOGGER = new Logger();
  private static final Size DESIRED_PREVIEW_SIZE = new Size(1920, 1080);
  private static final float TEXT_SIZE_DIP = 10;
  private Bitmap rgbFrameBitmap = null;
  private long lastProcessingTimeMs;
  private Integer sensorOrientation;
  private Classifier classifier;
  private BorderedText borderedText;
  private int imageSizeX;
  private int imageSizeY;
  MediaPlayer mSuccess,mRed;
  ImageView tv1;

  @Override
  protected int getLayoutId() {
    mSuccess = MediaPlayer.create(this, R.raw.success);
    mSuccess.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
      @Override
      public void onCompletion(MediaPlayer mp) {
        stopPlay();
      }
    });
    mSuccess.start();
    mRed = MediaPlayer.create(this, R.raw.red);
    mRed.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
      @Override
      public void onCompletion(MediaPlayer mp) {
        mRed.stop();
        try {
          mRed.prepare();
          mRed.seekTo(0);
        }
        catch (Throwable t) {

        }

      }
    });
    tv1= (ImageView) findViewById(R.id.imageView4);
    return R.layout.tfe_ic_camera_connection_fragment;

  }
  private void stopPlay(){
    mSuccess.stop();
    try {
      mSuccess.prepare();
      mSuccess.seekTo(0);
    }
    catch (Throwable t) {
      Toast.makeText(this, t.getMessage(), Toast.LENGTH_SHORT).show();
    }

  }
  @Override
  protected Size getDesiredPreviewFrameSize() {
    return DESIRED_PREVIEW_SIZE;
  }

  @Override
  public void onPreviewSizeChosen(final Size size, final int rotation) {
    final float textSizePx =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
    borderedText = new BorderedText(textSizePx);
    borderedText.setTypeface(Typeface.MONOSPACE);

    recreateClassifier(getModel(), getDevice(), getNumThreads());
    if (classifier == null) {
      LOGGER.e("No classifier on preview!");
      return;
    }

    previewWidth = size.getWidth();
    previewHeight = size.getHeight();

    sensorOrientation = rotation - getScreenOrientation();
    LOGGER.i("Camera orientation relative to screen canvas: %d", sensorOrientation);

    LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);
    rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);
  }

  @RequiresApi(api = Build.VERSION_CODES.M)
  @Override
  protected void processImage() {
    tv1= (ImageView) findViewById(R.id.imageView4);
    rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);
    final int cropSize = Math.min(previewWidth, previewHeight);
    int [] allpixels = new int [rgbFrameBitmap.getHeight() * rgbFrameBitmap.getWidth()];

    rgbFrameBitmap.getPixels(allpixels, 0, rgbFrameBitmap.getWidth(), 0, 0, rgbFrameBitmap.getWidth(), rgbFrameBitmap.getHeight());

    for(int i = 0; i < allpixels.length; i++)
    {
      int redValue = Color.red(allpixels[i]);
      int blueValue = Color.blue(allpixels[i]);
      int greenValue = Color.green(allpixels[i]);
//       Red:
      if(redValue > 160 && greenValue<110 && blueValue <110)
      {
        allpixels[i] = Color.rgb(255,0,0);
      }
//      Green:
      else if(redValue < 80 && greenValue > 150 && blueValue > 80){
        allpixels[i] = Color.rgb(0,255,0);
      }
      //      Black:
      else if(redValue < 100 && greenValue < 100 && blueValue < 120){
        allpixels[i] = Color.rgb(0,0,0);
      }
      else
      {
        allpixels[i] = Color.rgb(255,255,255);
      }
    }

    rgbFrameBitmap.setPixels(allpixels,0,rgbFrameBitmap.getWidth(),0, 0, rgbFrameBitmap.getWidth(),rgbFrameBitmap.getHeight());
    runOnUiThread(new Runnable() {

      @Override
      public void run() {

        tv1.setImageBitmap(rgbFrameBitmap);

      }
    });
    runInBackground(
        new Runnable() {
          @Override
          public void run() {
            if (classifier != null) {
              final long startTime = SystemClock.uptimeMillis();
              final List<Classifier.Recognition> results =
                  classifier.recognizeImage(rgbFrameBitmap, sensorOrientation);
              lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;
              LOGGER.v("Detect: %s", results);

              runOnUiThread(
                  new Runnable() {
                    @Override
                    public void run() {
//                      LOGGER.d("LAbel:"+recognition.getTitle().toString());
//                      mRed.start();
                      showResultsInBottomSheet(results);
                      showFrameInfo(previewWidth + "x" + previewHeight);
                      showCropInfo(imageSizeX + "x" + imageSizeY);
                      showCameraResolution(cropSize + "x" + cropSize);
                      showRotationInfo(String.valueOf(sensorOrientation));
                      showInference(lastProcessingTimeMs + "ms");
                    }
                  });
            }
            readyForNextImage();
          }
        });
  }
  @Override
  protected void onInferenceConfigurationChanged() {
    if (rgbFrameBitmap == null) {
      return;
    }
    final Device device = getDevice();
    final Model model = getModel();
    final int numThreads = getNumThreads();
    runInBackground(() -> recreateClassifier(model, device, numThreads));
  }

  private void recreateClassifier(Model model, Device device, int numThreads) {
    if (classifier != null) {
      LOGGER.d("Closing classifier.");
      classifier.close();
      classifier = null;
    }
    if (device == Device.GPU
        && (model == Model.QUANTIZED_MOBILENET || model == Model.QUANTIZED_EFFICIENTNET)) {
      LOGGER.d("Not creating classifier: GPU doesn't support quantized models.");
      runOnUiThread(
          () -> {
            Toast.makeText(this, R.string.tfe_ic_gpu_quant_error, Toast.LENGTH_LONG).show();
          });
      return;
    }
    try {
      LOGGER.d(
          "Creating classifier (model=%s, device=%s, numThreads=%d)", model, device, numThreads);
      classifier = Classifier.create(this, model, device, numThreads);
    } catch (IOException | IllegalArgumentException e) {
      LOGGER.e(e, "Failed to create classifier.");
      runOnUiThread(
          () -> {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
          });
      return;
    }

    imageSizeX = classifier.getImageSizeX();
    imageSizeY = classifier.getImageSizeY();
  }
}
