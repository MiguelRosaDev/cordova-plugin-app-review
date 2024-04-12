package by.chemerisuk.cordova;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.LOG;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageManager;
import android.content.pm.ApplicationInfo;

import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.gms.tasks.Task;

public class AppReviewPlugin extends CordovaPlugin {
  @Override
  public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
    try {
      if (action.equals("requestReview")) {
        Activity activity = this.cordova.getActivity();
        ReviewManager manager = ReviewManagerFactory.create(activity);
        Task<ReviewInfo> request = manager.requestReviewFlow();
        request.addOnCompleteListener(task -> {
          if (task.isSuccessful()) {
            LOG.d("AppRate", "request review success");
            ReviewInfo reviewInfo = task.getResult();
            Task<Void> flow = manager.launchReviewFlow(activity, reviewInfo);
            flow.addOnCompleteListener(launchTask -> {
              if (task.isSuccessful()) {
                LOG.d("AppRate", "launch review success");
                callbackContext.success();
              } else {
                Exception error = task.getException();
                LOG.d("AppRate", "Failed to launch review", error);
                callbackContext.error("Failed to launch review - " + error.getMessage());
              }
            });
          } else {
            Exception error = task.getException();
            LOG.d("AppRate", "Failed to launch review", error);
            callbackContext.error("Failed to launch review flow - " + error.getMessage());
          }
        });
        return true;
      }
      return false;
    } catch (Exception e) {
        callbackContext.error("Failed to launch review - " + e.getMessage());
      return true;
    }
  }
}
