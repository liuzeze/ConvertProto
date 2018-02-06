package download;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * @author Administrator
 */
public class DownloadManager {

    private final CountDownLatch mLatch;
    private String mNetURL;
    private String mSourceDirectoryPath;
    private String mBusinessName;
    private String mKey;
    private OnErrorListener mErrorListener;

    public DownloadManager(CountDownLatch latch) {
        mLatch = latch;
    }


    public void download(final String netURL, String sourceDirectoryPath, String businessName, String key) {
        mNetURL = netURL;
        mSourceDirectoryPath = sourceDirectoryPath;
        mBusinessName = businessName;
        mKey = key;
        DownloadTask downloadTask = new DownloadTask();
        ThreadPoolManager.getInstance().execute(downloadTask);


    }

    class DownloadTask implements Runnable {
        public DownloadTask() {
            super();
        }

        @Override
        public void run() {
            try {
                URL url = new URL(mNetURL);

                Map<String, Object> params = new LinkedHashMap<>();

                StringBuilder postData = new StringBuilder();
                for (Map.Entry<String, Object> param : params.entrySet()) {
                    if (postData.length() != 0) {
                        postData.append('&');
                    }
                    postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                    postData.append('=');
                    postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
                }
                byte[] postDataBytes = postData.toString().getBytes("UTF-8");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
                conn.setDoOutput(true);
                conn.getOutputStream().write(postDataBytes);

                Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

                StringBuilder sb = new StringBuilder();
                for (int c; (c = in.read()) >= 0; ) {
                    sb.append((char) c);
                }
                String response = sb.toString();


                File requestFile = new File(mSourceDirectoryPath + "/" + mBusinessName, mKey + ".proto");
                FileOutputStream requestOutput = new FileOutputStream(requestFile);
                requestOutput.write(response.getBytes("UTF-8"));
                requestOutput.close();

                mLatch.countDown();
            } catch (IOException e) {
                e.printStackTrace();
                mErrorListener.onError(e.getMessage());
            }
        }
    }

    public interface OnErrorListener {
        void onError(String error);
    }

    public void setOnErrorListener(OnErrorListener listener) {
        mErrorListener = listener;

    }

}
