package cordova.plugin.file.content.reader;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileContentReader extends CordovaPlugin {

    private static final String ACTION_READ_CONTENT = "readContent";

    private CallbackContext callback;
    private String contentUri;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        this.callback = callbackContext;
        this.contentUri = args.getString(0);

        if (action.equals(ACTION_READ_CONTENT)) {
            readContent();
            return true;
        }
        return false;
    }

    private void readContent() {
        Context appContext = this.cordova.getActivity().getApplicationContext();
        Uri uri = Uri.parse(this.contentUri);

        InputStream is = null;
        ByteArrayOutputStream os = null;
        try {
            is = appContext.getContentResolver().openInputStream(uri);
            os = new ByteArrayOutputStream();

            final int BUFFER_SIZE = 8192;
            byte[] buffer = new byte[BUFFER_SIZE];

            for (;;) {
                int bytesRead = is.read(buffer, 0, BUFFER_SIZE);
                if (bytesRead <= 0) {
                    break;
                }
                os.write(buffer, 0, bytesRead);
            }

            PluginResult result = new PluginResult(PluginResult.Status.OK, os.toByteArray());
            this.callback.sendPluginResult(result);
        } catch (FileNotFoundException e) {
            JSONObject resultObj = new JSONObject();
            resultObj.put("code", "FileNotFoundException");
            resultObj.put("message", e.getMessage());
            this.callback.error(resultObj);
        } catch (IOException e) {
            JSONObject resultObj = new JSONObject();
            resultObj.put("code", "IOException");
            resultObj.put("message", e.getMessage());
            this.callback.error(resultObj);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
