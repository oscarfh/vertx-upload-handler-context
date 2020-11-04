import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.acme.MyModule;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava.core.Vertx;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@ExtendWith(VertxExtension.class)
class ATest {

    @Test
    void start_server(io.vertx.core.Vertx avertx, VertxTestContext context) throws MalformedURLException, InterruptedException {
        Vertx vertx = new Vertx(avertx);
        vertx.rxDeployVerticle(new MyModule())
                .subscribe(stringAsyncResult -> {
                    InputStream contentInputStream = new ByteArrayInputStream(new byte[100]);
                    try {
                        int responseCode = uploadFile(new URL("http://localhost:8080/"), "a.txt", contentInputStream, null);

                        assertEquals(200, responseCode);
                        context.completeNow();
                    } catch (Throwable e) {
                        context.failNow(e);
                    }

                });
    }

    public int uploadFile(URL uploadUrl, String filename, InputStream inputStream, String accessToken)
            throws IOException {
        String boundaryString = "*****";

        URL url = new URL(uploadUrl.toString() + filename);
        HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
        httpConnection.setDoOutput(true);
        httpConnection.setDoInput(true);
        httpConnection.setUseCaches(false);
        httpConnection.setChunkedStreamingMode(1024);
        httpConnection.setRequestProperty(
                "Content-Type", "multipart/form-data;boundary=" + boundaryString);
        if (accessToken != null) {
            httpConnection.setRequestProperty("Authorization", "bearer " + accessToken);
        }

        httpConnection.setRequestMethod("PUT");

        OutputStream outputStreamToRequestBody = httpConnection.getOutputStream();
        OutputStreamWriter httpRequestBodyWriter = new OutputStreamWriter(outputStreamToRequestBody);

        // Include value from the myFileDescription text area in the post data
        httpRequestBodyWriter.write("\n--" + boundaryString + "\n");
        httpRequestBodyWriter.write("Content-Disposition: form-data; name=\"myFileDescription\"");
        httpRequestBodyWriter.write("\n\n");

        // Include the section to describe the file
        httpRequestBodyWriter.write("\n--" + boundaryString + "\n");
        httpRequestBodyWriter.write("Content-Disposition: form-data;"
                + "name=\"" + filename + "\";"
                + "filename=\"" + filename + "\""
                + "\nContent-Type: text/plain\n\n");
        httpRequestBodyWriter.flush();

        //FileStreamCopier.INSTANCE.copy(inputStream, outputStreamToRequestBody);

        // Mark the end of the multi-part HTTP request
        httpRequestBodyWriter.write("\n--" + boundaryString + "--\n");
        httpRequestBodyWriter.flush();

        return httpConnection.getResponseCode();

    }
}
