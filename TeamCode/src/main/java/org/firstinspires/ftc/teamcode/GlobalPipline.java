package org.firstinspires.ftc.teamcode;

import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvPipeline;

import org.opencv.core.*;

public class GlobalPipline extends OpenCvPipeline {

    public Result res;

    public void setRes(Result resInput) {
        res = resInput;
    }

    @Override
    public Mat processFrame(Mat input) {
        Mat process = new Mat();
        Mat maskedImg = new Mat();
        Mat canny = new Mat();
        Imgproc.blur(input, process,new Size(640, 480));
//        Imgproc.cvtColor(process, process, Imgproc.COLOR_RGB);
//        Imgproc.threshold(process, process, );
        Core.inRange(process, new Scalar(200,0,0,0), new Scalar(255,50,50,255), process);
        Core.bitwise_and(input, process, maskedImg);
        Imgproc.Canny(process, canny, 0,0, 3);

/*        Imgproc.rectangle(
                input,
                new Point(
                        input.cols()/4,
                        input.rows()/4),
                new Point(
                        input.cols()*(3f/4f),
                        input.rows()*(3f/4f)),
                new Scalar(0, 255, 0), 4);*/
        switch (res) {
            case Thresh: return process;
            case Mask: return maskedImg;
            case Edges: return canny;
        }
        return input;
    }

    enum Result {
        Thresh,
        Mask,
        Edges
    }
}
