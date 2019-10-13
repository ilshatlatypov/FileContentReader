package cordova.plugin.file.content.reader;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class FileContentReader extends CordovaPlugin {

    private static final String ACTION_READ_CONTENT = "readContent";
    private static final String ACTION_GET_FILE_DETAILS = "getFileDetails";

    private static final String FIELD_DISPLAY_NAME = "_display_name";
    private static final String FIELD_SIZE = "_size";
    private static final String FIELD_MIME_TYPE = "mime_type";

    private CallbackContext callback;
    private String contentUri;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.callback = callbackContext;
        this.contentUri = args.getString(0);

        if (action.equals(ACTION_READ_CONTENT)) {
            readContent();
            return true;
        } else if (action.equals(ACTION_GET_FILE_DETAILS)) {
            getFileDetails();
            return true;
        }
        return false;
    }

    private void readContent() throws JSONException {
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

    private void getFileDetails() {
        Context appContext = this.cordova.getActivity().getApplicationContext();
        Uri uri = Uri.parse(this.contentUri);

        String[] columns = new String[]{FIELD_DISPLAY_NAME, FIELD_SIZE, FIELD_MIME_TYPE};
        Cursor cursor = null;

        try {
            cursor = appContext.getContentResolver()
                    .query(uri, columns, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int titleColumnIndex = cursor.getColumnIndexOrThrow(FIELD_DISPLAY_NAME);
                int sizeColumnIndex = cursor.getColumnIndexOrThrow(FIELD_SIZE);
                int typeColumnIndex = cursor.getColumnIndexOrThrow(FIELD_MIME_TYPE);

                Map<String, String> fileDetails = new HashMap<>();
                fileDetails.put("title", cursor.getString(titleColumnIndex));
                fileDetails.put("type", cursor.getString(typeColumnIndex));
                fileDetails.put("size", String.valueOf(cursor.getLong(sizeColumnIndex)));
                fileDetails.put("uri", this.contentUri);


                this.callback.success(new JSONObject(fileDetails));
            } else {
                this.callback.error("Информация о файле недоступна");
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
