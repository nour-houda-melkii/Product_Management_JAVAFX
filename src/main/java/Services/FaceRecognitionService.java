package Services;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

import com.github.sarxos.webcam.Webcam;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

public class FaceRecognitionService {

    // Static initializer to load OpenCV native library
    static {
        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            System.out.println("OpenCV loaded successfully");
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Failed to load OpenCV library: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Webcam webcam;
    private CascadeClassifier faceDetector;
    private ScheduledExecutorService timer;
    private boolean isRunning = false;
    private final BooleanProperty faceRecognized = new SimpleBooleanProperty(false);
    private Path knownFacesDir = Paths.get("src/main/resources/known_faces");

    public FaceRecognitionService() {
        initialize();
    }

    private void initialize() {
        // Initialize webcam
        try {
            webcam = Webcam.getDefault();
            if (webcam != null) {
                webcam.setViewSize(webcam.getViewSizes()[0]); // Use smallest resolution for speed
                System.out.println("Webcam initialized");
            } else {
                System.err.println("No webcam detected");
            }
        } catch (Exception e) {
            System.err.println("Webcam initialization error: " + e.getMessage());
        }

        // Initialize face detector
        try {
            // Load the face cascade classifier
            String cascadePath = "src/main/resources/haarcascade_frontalface_default.xml";
            if (!Files.exists(Paths.get(cascadePath))) {
                System.err.println("Cascade classifier file not found at: " + cascadePath);
                return;
            }

            faceDetector = new CascadeClassifier(cascadePath);
            if (faceDetector.empty()) {
                System.err.println("Failed to load cascade classifier");
                return;
            }
            System.out.println("Face detector initialized");
        } catch (Exception e) {
            System.err.println("Face detector initialization error: " + e.getMessage());
        }

        // Ensure known faces directory exists
        try {
            if (!Files.exists(knownFacesDir)) {
                Files.createDirectories(knownFacesDir);
                System.out.println("Created known faces directory");
            }
        } catch (IOException e) {
            System.err.println("Failed to create known faces directory: " + e.getMessage());
        }
    }

    public void startFaceDetection(FaceDetectionCallback callback) {
        if (webcam == null || faceDetector == null || faceDetector.empty()) {
            callback.onError("Face detection components not initialized properly");
            return;
        }

        if (isRunning) {
            return;
        }

        try {
            webcam.open();
            isRunning = true;

            timer = Executors.newSingleThreadScheduledExecutor();
            timer.scheduleAtFixedRate(() -> {
                if (!isRunning) {
                    return;
                }

                try {
                    BufferedImage img = webcam.getImage();
                    if (img == null) {
                        return;
                    }

                    Mat frame = bufferedImageToMat(img);

                    // Convert to grayscale for face detection
                    Mat grayFrame = new Mat();
                    Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
                    Imgproc.equalizeHist(grayFrame, grayFrame);

                    // Detect faces
                    MatOfRect faces = new MatOfRect();
                    faceDetector.detectMultiScale(
                            grayFrame,
                            faces,
                            1.1,
                            3,
                            0,
                            new Size(30, 30)
                    );

                    Rect[] facesArray = faces.toArray();

                    // Draw rectangles around faces
                    for (Rect rect : facesArray) {
                        Imgproc.rectangle(
                                frame,
                                rect.tl(),
                                rect.br(),
                                new Scalar(0, 255, 0),
                                3
                        );
                    }

                    // Convert processed image back to JavaFX Image
                    final Image fxImage = convertMatToImage(frame);

                    // Update UI and check for face match
                    Platform.runLater(() -> {
                        callback.onFrame(fxImage);

                        // If face detected, check if it matches known faces
                        if (facesArray.length > 0) {
                            try {
                                // Extract the largest face for recognition
                                Rect faceRect = getLargestFace(facesArray);
                                Mat face = new Mat(grayFrame, faceRect);

                                // Resize to standard size for comparison
                                Mat resizedFace = new Mat();
                                Imgproc.resize(face, resizedFace, new Size(150, 150));

                                boolean isRecognized = compareFaceWithKnown(resizedFace);
                                faceRecognized.set(isRecognized);

                                if (isRecognized) {
                                    callback.onFaceRecognized();
                                    stopFaceDetection();
                                }
                            } catch (Exception e) {
                                System.err.println("Error in face recognition: " + e.getMessage());
                            }
                        }
                    });
                } catch (Exception e) {
                    System.err.println("Error processing webcam frame: " + e.getMessage());
                }
            }, 0, 100, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            isRunning = false;
            callback.onError("Failed to start face detection: " + e.getMessage());
        }
    }

    public void stopFaceDetection() {
        isRunning = false;
        if (timer != null) {
            timer.shutdown();
            try {
                timer.awaitTermination(33, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                System.err.println("Timer termination interrupted");
            }
        }

        if (webcam != null && webcam.isOpen()) {
            webcam.close();
        }
    }

    public void enrollFace() {
        if (webcam == null || !webcam.isOpen()) {
            try {
                webcam.open();
            } catch (Exception e) {
                System.err.println("Failed to open webcam for enrollment: " + e.getMessage());
                return;
            }
        }

        try {
            // Capture image
            BufferedImage img = webcam.getImage();
            Mat frame = bufferedImageToMat(img);

            // Convert to grayscale
            Mat grayFrame = new Mat();
            Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
            Imgproc.equalizeHist(grayFrame, grayFrame);

            // Detect faces
            MatOfRect faces = new MatOfRect();
            faceDetector.detectMultiScale(grayFrame, faces);

            Rect[] facesArray = faces.toArray();
            if (facesArray.length > 0) {
                // Get largest face
                Rect faceRect = getLargestFace(facesArray);
                Mat face = new Mat(grayFrame, faceRect);

                // Resize for consistency
                Mat resizedFace = new Mat();
                Imgproc.resize(face, resizedFace, new Size(150, 150));

                // Save face
                String fileName = "admin_face_" + System.currentTimeMillis() + ".jpg";
                Path filePath = knownFacesDir.resolve(fileName);

                BufferedImage faceImage = matToBufferedImage(resizedFace);
                ImageIO.write(faceImage, "jpg", filePath.toFile());

                System.out.println("Face enrolled: " + filePath.toString());
            } else {
                System.err.println("No face detected for enrollment");
            }
        } catch (Exception e) {
            System.err.println("Error enrolling face: " + e.getMessage());
        } finally {
            if (webcam != null && webcam.isOpen()) {
                webcam.close();
            }
        }
    }

    private boolean compareFaceWithKnown(Mat detectedFace) {
        try {
            // Simple implementation: use template matching with known faces
            File directory = knownFacesDir.toFile();
            File[] knownFaces = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".jpg"));

            if (knownFaces == null || knownFaces.length == 0) {
                System.out.println("No known faces found for comparison");
                return false;
            }

            double bestMatchConfidence = 0;
            double matchThreshold = 0.60; // Threshold for considering a match

            for (File knownFace : knownFaces) {
                try {
                    // Load known face
                    BufferedImage knownImg = ImageIO.read(knownFace);
                    Mat knownMat = bufferedImageToMat(knownImg);

                    // If color, convert to grayscale
                    if (knownMat.channels() > 1) {
                        Imgproc.cvtColor(knownMat, knownMat, Imgproc.COLOR_BGR2GRAY);
                    }

                    // Ensure same size
                    if (knownMat.size() != detectedFace.size()) {
                        Imgproc.resize(knownMat, knownMat, detectedFace.size());
                    }

                    // Template matching
                    Mat result = new Mat();
                    Imgproc.matchTemplate(knownMat, detectedFace, result, Imgproc.TM_CCOEFF_NORMED);

                    Core.MinMaxLocResult mmr = Core.minMaxLoc(result);
                    double matchVal = mmr.maxVal; // Higher value means better match

                    if (matchVal > bestMatchConfidence) {
                        bestMatchConfidence = matchVal;
                    }
                } catch (Exception e) {
                    System.err.println("Error comparing with face: " + knownFace.getName() + " - " + e.getMessage());
                }
            }

            System.out.println("Best match confidence: " + bestMatchConfidence);
            return bestMatchConfidence >= matchThreshold;
        } catch (Exception e) {
            System.err.println("Error in face comparison: " + e.getMessage());
            return false;
        }
    }

    private Rect getLargestFace(Rect[] faces) {
        Rect largestFace = faces[0];
        for (Rect face : faces) {
            if (face.area() > largestFace.area()) {
                largestFace = face;
            }
        }
        return largestFace;
    }

    // Convert BufferedImage to OpenCV Mat
    private Mat bufferedImageToMat(BufferedImage img) {
        Mat mat = new Mat(img.getHeight(), img.getWidth(), 16); // CvType.CV_8UC3
        byte[] data = new byte[img.getWidth() * img.getHeight() * 3];

        int[] rgb = img.getRGB(0, 0, img.getWidth(), img.getHeight(), null, 0, img.getWidth());
        for (int i = 0; i < rgb.length; i++) {
            data[i * 3] = (byte) ((rgb[i] >> 0) & 0xFF); // B
            data[i * 3 + 1] = (byte) ((rgb[i] >> 8) & 0xFF); // G
            data[i * 3 + 2] = (byte) ((rgb[i] >> 16) & 0xFF); // R
        }

        mat.put(0, 0, data);
        return mat;
    }

    // Convert OpenCV Mat to BufferedImage
    private BufferedImage matToBufferedImage(Mat mat) {
        int width = mat.width();
        int height = mat.height();
        int channels = mat.channels();
        byte[] sourcePixels = new byte[width * height * channels];
        mat.get(0, 0, sourcePixels);

        BufferedImage image;
        if (channels == 1) {
            image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        } else {
            image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        }

        final byte[] targetPixels = ((java.awt.image.DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(sourcePixels, 0, targetPixels, 0, sourcePixels.length);

        return image;
    }

    // Convert OpenCV Mat to JavaFX Image
    private Image convertMatToImage(Mat mat) {
        BufferedImage bufferedImage = matToBufferedImage(mat);
        return SwingFXUtils.toFXImage(bufferedImage, null);
    }

    public BooleanProperty faceRecognizedProperty() {
        return faceRecognized;
    }

    // Callback interface for face detection events
    public interface FaceDetectionCallback {
        void onFrame(Image frame);
        void onFaceRecognized();
        void onError(String message);
    }
}