package org.firstinspires.ftc.teamcode;

import android.provider.Settings;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvWebcam;

@TeleOp(name="VisionCV", group="Vision")
public class Vision extends OpMode {
    OpenCvCamera camera;


    @Override
    public void init() {
        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        camera = new OpenCvWebcam(hardwareMap.get(WebcamName.class, "Webcam 1"), cameraMonitorViewId);
        camera.openCameraDevice();
        camera.setPipeline(new GlobalPipline());
        camera.startStreaming(640, 480, OpenCvCameraRotation.UPRIGHT);
    }

    @Override
    public void loop() {
        telemetry.addData("Frame Count", camera.getFrameCount());
        telemetry.addData("FPS", String.format("%.2f", camera.getFps()));
        telemetry.addData("Total frame time ms", camera.getTotalFrameTimeMs());
        telemetry.addData("Pipeline time ms", camera.getPipelineTimeMs());
        telemetry.addData("Overhead time ms", camera.getOverheadTimeMs());
        telemetry.addData("Theoretical max FPS", camera.getCurrentPipelineMaxFps());
        telemetry.update();
    }
}
