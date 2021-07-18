package com.lglab.diego.simple_cms.create.utility.model;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.lglab.diego.simple_cms.connection.LGCommand;
import com.lglab.diego.simple_cms.connection.LGConnectionManager;
import com.lglab.diego.simple_cms.connection.LGConnectionSendFile;
import com.lglab.diego.simple_cms.create.utility.model.balloon.Balloon;
import com.lglab.diego.simple_cms.create.utility.model.poi.POI;
import com.lglab.diego.simple_cms.create.utility.model.shape.Shape;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * This class is in charge of sending the commands to liquid galaxy
 */
public class ActionController {

    private static final String TAG_DEBUG = "ActionController";

    private static ActionController instance = null;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Handler handler2 = new Handler(Looper.getMainLooper());

    public synchronized static ActionController getInstance() {
        if (instance == null)
            instance = new ActionController();
        return instance;
    }

    /**
     * Enforce private constructor
     */
    private ActionController() {}

    /**
     * Move the screen to the poi
     *
     * @param poi      The POI that is going to move
     * @param listener The listener of lgcommand
     */
    public void moveToPOI(POI poi, LGCommand.Listener listener) {
        cleanFileKMLs(0);
        sendPoiToLG(poi, listener);
    }

    /**
     * Create the lGCommand to send to the liquid galaxy
     *
     * @param listener The LGCommand listener
     */
    private void sendPoiToLG(POI poi, LGCommand.Listener listener) {
        LGCommand lgCommand = new LGCommand(ActionBuildCommandUtility.buildCommandPOITest(poi), LGCommand.CRITICAL_MESSAGE, (String result) -> {
            if (listener != null) {
                listener.onResponse(result);
            }
        });
        LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
        lgConnectionManager.startConnection();
        lgConnectionManager.addCommandToLG(lgCommand);
    }


    /**
     * First Clean the KML and then do the orbit
     *
     * @param poi      POI
     * @param listener Listener
     */
    public synchronized void cleanOrbit(POI poi, LGCommand.Listener listener) {
        cleanFileKMLs(0);
        orbit(poi, listener);
    }

    /**
     * Do the orbit
     *
     * @param poi      POI
     * @param listener Listener
     */
    public void orbit(POI poi, LGCommand.Listener listener) {
        LGCommand lgCommandOrbit = new LGCommand(ActionBuildCommandUtility.buildCommandOrbit(poi), LGCommand.CRITICAL_MESSAGE, (String result) -> {
            if (listener != null) {
                listener.onResponse(result);
            }
        });
        LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
        lgConnectionManager.startConnection();
        lgConnectionManager.addCommandToLG(lgCommandOrbit);

        LGCommand lgCommandWriteOrbit = new LGCommand(ActionBuildCommandUtility.buildCommandWriteOrbit(), LGCommand.CRITICAL_MESSAGE, (String result) -> {
            if (listener != null) {
                listener.onResponse(result);
            }
        });
        lgConnectionManager.addCommandToLG(lgCommandWriteOrbit);

        LGCommand lgCommandStartOrbit = new LGCommand(ActionBuildCommandUtility.buildCommandStartOrbit(), LGCommand.CRITICAL_MESSAGE, (String result) -> {
            if (listener != null) {
                listener.onResponse(result);
            }
        });
        handler.postDelayed(() -> lgConnectionManager.addCommandToLG(lgCommandStartOrbit), 500);
        cleanFileKMLs(46000);
    }

    /**
     * @param balloon  Balloon with the information to build command
     * @param listener listener
     */
    public void sendBalloon(Balloon balloon, LGCommand.Listener listener) {
        cleanFileKMLs(0);

        Uri imageUri = balloon.getImageUri();
        if (imageUri != null) {
            createResourcesFolder();
            String imagePath = balloon.getImagePath();
            LGConnectionSendFile lgConnectionSendFile = LGConnectionSendFile.getInstance();
            lgConnectionSendFile.addPath(imagePath);
            lgConnectionSendFile.startConnection();
        }

        handler.postDelayed(() -> {
            LGCommand lgCommand = new LGCommand(ActionBuildCommandUtility.buildCommandBalloonTest(balloon), LGCommand.CRITICAL_MESSAGE, (String result) -> {
                if (listener != null) {
                    listener.onResponse(result);
                }
            });
            LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
            lgConnectionManager.startConnection();
            lgConnectionManager.addCommandToLG(lgCommand);

            handler.postDelayed(this::writeFileBalloonFile, 500);
        }, 500);
    }

    /**
     * @param balloon  Balloon with the information to build command
     * @param listener listener
     */
    public void sendBalloonTestStoryBoard(Balloon balloon, LGCommand.Listener listener) {
        cleanFileKMLs(0);

        LGCommand lgCommand = new LGCommand(ActionBuildCommandUtility.buildCommandBalloonTest(balloon), LGCommand.CRITICAL_MESSAGE, (String result) -> {
            if (listener != null) {
                listener.onResponse(result);
            }
        });
        LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
        lgConnectionManager.startConnection();
        lgConnectionManager.addCommandToLG(lgCommand);

        handler.postDelayed(this::writeFileBalloonFile, 500);
    }

    /**
     * Send the image of the balloon
     *
     * @param balloon Balloon
     */
    public void sendImageTestStoryboard(Balloon balloon) {
        Uri imageUri = balloon.getImageUri();
        if (imageUri != null) {
            String imagePath = balloon.getImagePath();
            Log.w(TAG_DEBUG, "Image Path: " + imagePath);
            LGConnectionSendFile lgConnectionSendFile = LGConnectionSendFile.getInstance();
            lgConnectionSendFile.addPath(imagePath);
            lgConnectionSendFile.startConnection();
        }
    }

    /**
     * Paint a balloon with the logos
     */
    public void sendBalloonWithLogos(AppCompatActivity activity) {
        createResourcesFolder();

        String imagePath = getLogosFile(activity);
        LGConnectionSendFile lgConnectionSendFile = LGConnectionSendFile.getInstance();
        lgConnectionSendFile.addPath(imagePath);
        lgConnectionSendFile.startConnection();

        cleanFileKMLs(0);

        handler.postDelayed(() -> {
            LGCommand lgCommand = new LGCommand(ActionBuildCommandUtility.buildCommandBalloonWithLogos(),
                    LGCommand.CRITICAL_MESSAGE, (String result) -> {
            });
            LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
            lgConnectionManager.startConnection();
            lgConnectionManager.addCommandToLG(lgCommand);
            }, 2000);
    }
    public void sendNoaa18file(AppCompatActivity activity) {
        createResourcesFolder();

        String imagePath = getNoaa18File(activity);
        Log.w(TAG_DEBUG, "ISS KML FILEPATH: " + imagePath);
        LGConnectionSendFile lgConnectionSendFile = LGConnectionSendFile.getInstance();
        lgConnectionSendFile.addPath(imagePath);
        lgConnectionSendFile.startConnection();

        cleanFileKMLs(0);

        handler.postDelayed(() -> {
            LGCommand lgCommand = new LGCommand(ActionBuildCommandUtility.buildWriteStarlinkFile(),
                    LGCommand.CRITICAL_MESSAGE, (String result) -> {
            });
            LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
            lgConnectionManager.startConnection();
            lgConnectionManager.addCommandToLG(lgCommand);
        }, 2000);
    }
    public void sendStarlinkfile(AppCompatActivity activity) {
        createResourcesFolder();

        String imagePath = getStarlinkFile(activity);
        Log.w(TAG_DEBUG, "ISS KML FILEPATH: " + imagePath);
        LGConnectionSendFile lgConnectionSendFile = LGConnectionSendFile.getInstance();
        lgConnectionSendFile.addPath(imagePath);
        lgConnectionSendFile.startConnection();

        cleanFileKMLs(0);

        handler.postDelayed(() -> {
            LGCommand lgCommand = new LGCommand(ActionBuildCommandUtility.buildWriteStarlinkFile(),
                    LGCommand.CRITICAL_MESSAGE, (String result) -> {
            });
            LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
            lgConnectionManager.startConnection();
            lgConnectionManager.addCommandToLG(lgCommand);
        }, 2000);
    }

    public void sendISSfile(AppCompatActivity activity) {
        createResourcesFolder();

        String imagePath = getISSFile(activity);
        Log.w(TAG_DEBUG, "ISS KML FILEPATH: " + imagePath);
        LGConnectionSendFile lgConnectionSendFile = LGConnectionSendFile.getInstance();
        lgConnectionSendFile.addPath(imagePath);
        lgConnectionSendFile.startConnection();

        cleanFileKMLs(0);

        handler.postDelayed(() -> {
            LGCommand lgCommand = new LGCommand(ActionBuildCommandUtility.buildWriteISSFile(),
                    LGCommand.CRITICAL_MESSAGE, (String result) -> {
            });
            LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
            lgConnectionManager.startConnection();
            lgConnectionManager.addCommandToLG(lgCommand);
        }, 2000);
    }

    public void sendEnxanetaFile(AppCompatActivity activity) {
        createResourcesFolder();

        String imagePath = getEnxanetaFile(activity);
        Log.w(TAG_DEBUG, "Enxaneta KML FILEPATH: " + imagePath);
        LGConnectionSendFile lgConnectionSendFile = LGConnectionSendFile.getInstance();
        lgConnectionSendFile.addPath(imagePath);
        lgConnectionSendFile.startConnection();

        cleanFileKMLs(0);

        handler.postDelayed(() -> {
            LGCommand lgCommand = new LGCommand(ActionBuildCommandUtility.buildWriteEnxanetaFile(),
                    LGCommand.CRITICAL_MESSAGE, (String result) -> {
            });
            LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
            lgConnectionManager.startConnection();
            lgConnectionManager.addCommandToLG(lgCommand);
        }, 2000);
    }

    public void sendNOAA18ConstFile(AppCompatActivity activity) {
        createResourcesFolder();

        String imagePath = getNoaa18File(activity);
        Log.w(TAG_DEBUG, "NOAA18 KML FILEPATH: " + imagePath);
        LGConnectionSendFile lgConnectionSendFile = LGConnectionSendFile.getInstance();
        lgConnectionSendFile.addPath(imagePath);
        lgConnectionSendFile.startConnection();

        cleanFileKMLs(0);

        handler.postDelayed(() -> {
            LGCommand lgCommand = new LGCommand(ActionBuildCommandUtility.buildWriteNoaa18File(),
                    LGCommand.CRITICAL_MESSAGE, (String result) -> {
            });
            LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
            lgConnectionManager.startConnection();
            lgConnectionManager.addCommandToLG(lgCommand);
        }, 2000);
    }
    public void sendIridiumConstFile(AppCompatActivity activity) {
        createResourcesFolder();

        String imagePath = getIridiumConstFile(activity);
        Log.w(TAG_DEBUG, "IridiumConst KML FILEPATH: " + imagePath);
        LGConnectionSendFile lgConnectionSendFile = LGConnectionSendFile.getInstance();
        lgConnectionSendFile.addPath(imagePath);
        lgConnectionSendFile.startConnection();

        cleanFileKMLs(0);

        handler.postDelayed(() -> {
            LGCommand lgCommand = new LGCommand(ActionBuildCommandUtility.buildWriteIridiumConstFile(),
                    LGCommand.CRITICAL_MESSAGE, (String result) -> {
            });
            LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
            lgConnectionManager.startConnection();
            lgConnectionManager.addCommandToLG(lgCommand);
        }, 2000);
    }

    private String getLogosFile(AppCompatActivity activity) {
        File file = new File(activity.getCacheDir() + "/logos.png");
        if (!file.exists()) {
            try {
                InputStream is = activity.getAssets().open("logos.png");
                int size = is.available();
                Log.w(TAG_DEBUG, "SIZE: " + size);
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();

                FileOutputStream fos = new FileOutputStream(file);
                fos.write(buffer);
                fos.close();

                return file.getPath();
            } catch (Exception e) {
                Log.w(TAG_DEBUG, "ERROR: " + e.getMessage());
            }
        }
        return file.getPath();
    }
    private String getKMLFile(AppCompatActivity activity) {
        File file = new File(activity.getFilesDir() + "/ISS.kml");
        if (!file.exists()) {
            try {
                InputStream is = activity.getAssets().open("ISS.kml");
                int size = is.available();
                Log.w(TAG_DEBUG, "GET ISS KML SIZE: " + size);
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();

                FileOutputStream fos = new FileOutputStream(file);
                fos.write(buffer);
                fos.close();

                return file.getPath();
            } catch (Exception e) {
                Log.w(TAG_DEBUG, "ERROR: " + e.getMessage());
            }
        }
        return file.getPath();
    }
    private String getNoaa18File(AppCompatActivity activity) {
        File file = new File(activity.getFilesDir() + "/Noaa18.kml");
        if (!file.exists()) {
            try {
                InputStream is = activity.getAssets().open("Noaa18.kml");
                int size = is.available();
                Log.w(TAG_DEBUG, "GET NOAA18 KML SIZE: " + size);
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();

                FileOutputStream fos = new FileOutputStream(file);
                fos.write(buffer);
                fos.close();

                return file.getPath();
            } catch (Exception e) {
                Log.w(TAG_DEBUG, "ERROR: " + e.getMessage());
            }
        }
        return file.getPath();
    }
    private String getStarlinkFile(AppCompatActivity activity) {
        File file = new File(activity.getFilesDir() + "/Starlink.kml");
        if (!file.exists()) {
            try {
                InputStream is = activity.getAssets().open("Starlink.kml");
                int size = is.available();
                Log.w(TAG_DEBUG, "GET Starlink KML SIZE: " + size);
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();

                FileOutputStream fos = new FileOutputStream(file);
                fos.write(buffer);
                fos.close();

                return file.getPath();
            } catch (Exception e) {
                Log.w(TAG_DEBUG, "ERROR: " + e.getMessage());
            }
        }
        return file.getPath();
    }
    private String getISSFile(AppCompatActivity activity) {
        File file = new File(activity.getFilesDir() + "/ISS.kml");
        if (!file.exists()) {
            try {
                InputStream is = activity.getAssets().open("ISS.kml");
                int size = is.available();
                Log.w(TAG_DEBUG, "GET ISS KML SIZE: " + size);
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();

                FileOutputStream fos = new FileOutputStream(file);
                fos.write(buffer);
                fos.close();

                return file.getPath();
            } catch (Exception e) {
                Log.w(TAG_DEBUG, "ERROR: " + e.getMessage());
            }
        }
        return file.getPath();
    }
    private String getEnxanetaFile(AppCompatActivity activity) {
        File file = new File(activity.getFilesDir() + "/Enxaneta.kml");
        if (!file.exists()) {
            try {
                InputStream is = activity.getAssets().open("Enxaneta.kml");
                int size = is.available();
                Log.w(TAG_DEBUG, "GET Enxaneta KML SIZE: " + size);
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();

                FileOutputStream fos = new FileOutputStream(file);
                fos.write(buffer);
                fos.close();

                return file.getPath();
            } catch (Exception e) {
                Log.w(TAG_DEBUG, "ERROR: " + e.getMessage());
            }
        }
        return file.getPath();
    }



    private String getStarlinkConstFile(AppCompatActivity activity) {
        File file = new File(activity.getFilesDir() + "/StarlinkConst.kml");
        if (!file.exists()) {
            try {
                InputStream is = activity.getAssets().open("StarlinkConst.kml");
                int size = is.available();
                Log.w(TAG_DEBUG, "GET ISS KML SIZE: " + size);
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();

                FileOutputStream fos = new FileOutputStream(file);
                fos.write(buffer);
                fos.close();

                return file.getPath();
            } catch (Exception e) {
                Log.w(TAG_DEBUG, "ERROR: " + e.getMessage());

            }
        }
        return file.getPath();
    }


    private String getIridiumConstFile(AppCompatActivity activity) {
        File file = new File(activity.getFilesDir() + "/IridiumConst.kml");
        if (!file.exists()) {
            try {
                InputStream is = activity.getAssets().open("IridiumConst.kml");
                int size = is.available();
                Log.w(TAG_DEBUG, "GET ISS KML SIZE: " + size);
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();

                FileOutputStream fos = new FileOutputStream(file);
                fos.write(buffer);
                fos.close();

                return file.getPath();
            } catch (Exception e) {
                Log.w(TAG_DEBUG, "ERROR: " + e.getMessage());
            }
        }
        return file.getPath();
    }

    private String readDemoFile(AppCompatActivity activity) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(activity.getAssets().open("demo.txt"), StandardCharsets.UTF_8));

            StringBuilder string = new StringBuilder();
            String mLine;
            while ((mLine = reader.readLine()) != null) {
                string.append(mLine);
            }
            return string.toString();
        } catch (IOException e) {
            Log.w(TAG_DEBUG, "ERROR READING FILE: " + e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.w(TAG_DEBUG, "ERROR CLOSING: " + e.getMessage());
                }
            }
        }
        return "";
    }


    /**
     * Create the Resource folder
     */
    public void createResourcesFolder() {
        LGCommand lgCommand = new LGCommand(ActionBuildCommandUtility.buildCommandCreateResourcesFolder(), LGCommand.CRITICAL_MESSAGE, (String result) -> {
        });
        LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
        lgConnectionManager.startConnection();
        lgConnectionManager.addCommandToLG(lgCommand);
    }


    /**
     * Write the shape.kml in the Liquid Galaxy
     */
    private void writeFileShapeFile() {
        LGCommand lgCommand = new LGCommand(ActionBuildCommandUtility.buildWriteShapeFile(),
                LGCommand.CRITICAL_MESSAGE, (String result) -> {
        });
        LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
        lgConnectionManager.startConnection();
        lgConnectionManager.addCommandToLG(lgCommand);
    }

    private void writeFileISSFile() {
        LGCommand lgCommand = new LGCommand(ActionBuildCommandUtility.buildWriteISSFile(),
                LGCommand.CRITICAL_MESSAGE, (String result) -> {
        });
        LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
        lgConnectionManager.startConnection();
        lgConnectionManager.addCommandToLG(lgCommand);
    }

    /**
     * Send the command to liquid galaxy
     *
     * @param shape    Shape with the information to build the command
     * @param listener listener
     */
    public void sendShape(Shape shape, LGCommand.Listener listener) {
        cleanFileKMLs(0);

        LGCommand lgCommand = new LGCommand(ActionBuildCommandUtility.buildCommandSendShape(), LGCommand.CRITICAL_MESSAGE, (String result) -> { //Should be buildCommandSendShape(shape)
            if (listener != null) {
                listener.onResponse(result);
            }
        });
        LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
        lgConnectionManager.startConnection();
        lgConnectionManager.addCommandToLG(lgCommand);

        handler.postDelayed(this::writeFileShapeFile, 500);
    }

    public void sendFixedShape(LGCommand.Listener listener) {
        cleanFileKMLs(0);

        LGCommand lgCommand = new LGCommand(ActionBuildCommandUtility.buildCommandSendShape(), LGCommand.CRITICAL_MESSAGE, (String result) -> {
            if (listener != null) {
                listener.onResponse(result);
            }
        });
        LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
        lgConnectionManager.startConnection();
        lgConnectionManager.addCommandToLG(lgCommand);

        handler.postDelayed(this::writeFileShapeFile, 500);
    }

    public void sendNoaa18(LGCommand.Listener listener) {
        cleanFileKMLs(0);

        LGCommand lgCommand = new LGCommand(ActionBuildCommandUtility.buildCommandSendNoaa18(), LGCommand.CRITICAL_MESSAGE, (String result) -> {
            if (listener != null) {
                listener.onResponse(result);
            }
        });
        LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
        lgConnectionManager.startConnection();
        lgConnectionManager.addCommandToLG(lgCommand);

        handler.postDelayed(this::writeFileShapeFile, 500);
    }

    public void sendStarlink(LGCommand.Listener listener) {
        cleanFileKMLs(0);

        LGCommand lgCommand = new LGCommand(ActionBuildCommandUtility.buildCommandSendStarlink(), LGCommand.CRITICAL_MESSAGE, (String result) -> {
            if (listener != null) {
                listener.onResponse(result);
            }
        });
        LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
        lgConnectionManager.startConnection();
        lgConnectionManager.addCommandToLG(lgCommand);

        handler.postDelayed(this::writeFileShapeFile, 500);
    }

    public void sendISS(LGCommand.Listener listener) {
        cleanFileKMLs(0);
        /*
        LGCommand lgCommand = new LGCommand(ActionBuildCommandUtility.buildCommandSendISS(), LGCommand.CRITICAL_MESSAGE, (String result) -> {
            if (listener != null) {
                listener.onResponse(result);
            }
        });
        LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
        lgConnectionManager.startConnection();
        lgConnectionManager.addCommandToLG(lgCommand);
        */
        handler.postDelayed(this::writeFileShapeFile, 500);
    }
    /*
    public void sendEnxaneta(LGCommand.Listener listener) {
        cleanFileKMLs(0);

        LGCommand lgCommand = new LGCommand(ActionBuildCommandUtility.buildCommandSendEnxaneta(), LGCommand.CRITICAL_MESSAGE, (String result) -> {
            if (listener != null) {
                listener.onResponse(result);
            }
        });
        LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
        lgConnectionManager.startConnection();
        lgConnectionManager.addCommandToLG(lgCommand);

        handler.postDelayed(this::writeFileShapeFile, 500);
    }

    public void sendStarlinkConst(LGCommand.Listener listener) {
        cleanFileKMLs(0);

        LGCommand lgCommand = new LGCommand(ActionBuildCommandUtility.buildCommandSendStarlinkConst(), LGCommand.CRITICAL_MESSAGE, (String result) -> {
            if (listener != null) {
                listener.onResponse(result);
            }
        });
        LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
        lgConnectionManager.startConnection();
        lgConnectionManager.addCommandToLG(lgCommand);

        handler.postDelayed(this::writeFileShapeFile, 500);
    }

    public void sendIridiumConst(LGCommand.Listener listener) {
        cleanFileKMLs(0);

        LGCommand lgCommand = new LGCommand(ActionBuildCommandUtility.buildCommandSendIridiumConst(), LGCommand.CRITICAL_MESSAGE, (String result) -> {
            if (listener != null) {
                listener.onResponse(result);
            }
        });
        LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
        lgConnectionManager.startConnection();
        lgConnectionManager.addCommandToLG(lgCommand);

        handler.postDelayed(this::writeFileShapeFile, 500);
    }*/

    /**
     * It cleans the kmls.txt file
     */
    public void cleanFileKMLs(int duration) {
        handler.postDelayed(() -> {
            LGCommand lgCommand = new LGCommand(ActionBuildCommandUtility.buildCleanKMLs(),
                    LGCommand.CRITICAL_MESSAGE, (String result) -> {
            });
            LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
            lgConnectionManager.startConnection();
            lgConnectionManager.addCommandToLG(lgCommand);
        }, duration);
    }

    /**
     * It cleans the kmls.txt file
     */
    public void cleanQuery(int duration) {
        handler.postDelayed(() -> {
            LGCommand lgCommand = new LGCommand(ActionBuildCommandUtility.buildCleanQuery(),
                    LGCommand.CRITICAL_MESSAGE, (String result) -> {
            });
            LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
            lgConnectionManager.startConnection();
            lgConnectionManager.addCommandToLG(lgCommand);
        }, duration);
    }


    /**
     * Send both command to the Liquid Galaxy
     *
     * @param poi     Poi with the location information
     * @param balloon Balloon with the information to paint the balloon
     */
    public void TourGDG(POI poi, Balloon balloon) {
        cleanFileKMLs(0);
        sendBalloonTourGDG(balloon, null);
        sendPoiToLG(poi, null);
    }

    /**
     * Send a balloon in the case of the tour
     *
     * @param balloon  Balloon with the information to build command
     * @param listener listener
     */
    private void sendBalloonTourGDG(Balloon balloon, LGCommand.Listener listener) {
        LGCommand lgCommand = new LGCommand(ActionBuildCommandUtility.buildCommandBalloonTest(balloon), LGCommand.CRITICAL_MESSAGE, (String result) -> {
            if (listener != null) {
                listener.onResponse(result);
            }
        });
        LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
        lgConnectionManager.startConnection();
        lgConnectionManager.addCommandToLG(lgCommand);

        handler.postDelayed(this::writeFileBalloonFile, 1000);
    }

    /**
     * Write the file of the balloon
     */
    private void writeFileBalloonFile() {
        LGCommand lgCommand = new LGCommand(ActionBuildCommandUtility.buildWriteBalloonFile(),
                LGCommand.CRITICAL_MESSAGE, (String result) -> {
        });
        LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
        lgConnectionManager.startConnection();
        lgConnectionManager.addCommandToLG(lgCommand);
    }

    /**
     * Send the tour kml
     * @param actions Storyboard's actions
     * @param listener Listener
     */
    public void sendTour(List<Action> actions, LGCommand.Listener listener){
        cleanFileKMLs(0);
        handler.postDelayed(() -> {
            LGCommand lgCommand = new LGCommand(ActionBuildCommandUtility.buildCommandTour(actions), LGCommand.CRITICAL_MESSAGE, (String result) -> {
                if (listener != null) {
                    listener.onResponse(result);
                }
            });
            LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
            lgConnectionManager.startConnection();
            lgConnectionManager.addCommandToLG(lgCommand);

            LGCommand lgCommandWriteTour = new LGCommand(ActionBuildCommandUtility.buildCommandwriteStartTourFile(), LGCommand.CRITICAL_MESSAGE, (String result) -> {
                if (listener != null) {
                    listener.onResponse(result);
                }
            });
            lgConnectionManager.addCommandToLG(lgCommandWriteTour);

            LGCommand lgCommandStartTour = new LGCommand(ActionBuildCommandUtility.buildCommandStartTour(),
                    LGCommand.CRITICAL_MESSAGE, (String result) -> {
            });
            handler2.postDelayed(() -> lgConnectionManager.addCommandToLG(lgCommandStartTour), 1500);
        }, 1000);
    }


    /**
     * Exit Tour
     */
    public void exitTour(){
        cleanFileKMLs(0);
        LGCommand lgCommand = new LGCommand(ActionBuildCommandUtility.buildCommandExitTour(),
                LGCommand.CRITICAL_MESSAGE, (String result) -> {
        });
        LGConnectionManager lgConnectionManager = LGConnectionManager.getInstance();
        lgConnectionManager.startConnection();
        lgConnectionManager.addCommandToLG(lgCommand);

        LGCommand lgCommandCleanSlaves = new LGCommand(ActionBuildCommandUtility.buildCommandCleanSlaves(),
                LGCommand.CRITICAL_MESSAGE, (String result) -> {
        });
        lgConnectionManager.addCommandToLG(lgCommandCleanSlaves);
    };

}
