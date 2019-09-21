package org.firstinspires.ftc.teamcode;

import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvPipeline;

import org.opencv.core.*;

public class GlobalPipline extends OpenCvPipeline {

//    public Result res;

//    public void setRes(Result resInput) {
//        res = resInput;
//    }

    private Result stageToRenderToViewport = Result.Thresh;
    private Result[] stages = GlobalPipline.Result.values();

    public GlobalPipline(Result resInput) {stageToRenderToViewport = resInput;}

    @Override
    public void onViewportTapped()
    {
        /*
         * Note that this method is invoked from the UI thread
         * so whatever we do here, we must do quickly.
         */

        int currentStageNum = stageToRenderToViewport.ordinal();

        int nextStageNum = currentStageNum + 1;

        if(nextStageNum >= stages.length)
        {
            nextStageNum = 0;
        }

        stageToRenderToViewport = stages[nextStageNum];
    }

    @Override
    public Mat processFrame(Mat input) {
        Mat blur = new Mat();
        Mat thresh = new Mat();
        Mat maskedImg = new Mat();
        Mat canny = new Mat();
//        Imgproc.blur(input, process,new Size(640, 480));
//        Imgproc.medianBlur(input, process,31);
//        Imgproc.threshold()
//        Imgproc.cvtColor(process, process, Imgproc.COLOR_RGB);
//        Imgproc.threshold(process, process, );
//        Core.inRange(process, new Scalar(200,0,0,0), new Scalar(255,50,50,255), process);
//        Core.bitwise_and(input, process, maskedImg);
        Imgproc.Canny(thresh, canny, 0,0, 3);

/*        Imgproc.rectangle(
                input,
                new Point(
                        input.cols()/4,
                        input.rows()/4),
                new Point(
                        input.cols()*(3f/4f),
                        input.rows()*(3f/4f)),
                new Scalar(0, 255, 0), 4);*/
        switch (stageToRenderToViewport) {
            case Input: return input;
            case Blur: return blur;
            case Thresh: return maskedImg;
            case Edges: return canny;
        }
        return input;
    }

    enum Result {
        Input,
        Blur,
        Thresh,
        Edges
    }
}
