package test;
import static com.googlecode.javacv.cpp.opencv_core.cvFlip;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSeqElem;

import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.InputEvent;

import static com.googlecode.javacv.cpp.opencv_highgui.cvSaveImage;
import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.FFmpegFrameGrabber;
import com.googlecode.javacv.FrameGrabber;
import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.VideoInputFrameGrabber;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class GrabberShow implements Runnable {
    //final int INTERVAL=1000;///you may use interval
    IplImage image;
    CanvasFrame canvas = new CanvasFrame("Web Cam");
    public GrabberShow() {
        canvas.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
    }
    @Override
    public void run() {
        FrameGrabber grabber = new OpenCVFrameGrabber(0); // 1 for next camera
        int i=0;
        try {
            grabber.start();
            IplImage img;
            img = grabber.grab();
        	Detector detector = new Detector(img);
        	int eyes = 0;
        	boolean leftProfile, rightProfile;
        	long blinkTime = 0;
        	boolean mode = true;
        	boolean blinked = false;
        	boolean blink = false;
            while (true) {
                img = grabber.grab();
                if (img != null) {
                    cvFlip(img, img, 1);// l-r = 90_degrees_steps_anti_clockwise
                	detector.clear(img);
                    Robot mouse;
                    
        			try {
        				mouse = new Robot();
        			    Point mousePointer = MouseInfo.getPointerInfo().getLocation();
    			    	if(!blink) {
	        			    CvSeq faces = detector.detectFrontal();
	        			    for (int j=0;j<faces.total();j++) {
	        			    	blinked=false;
	        			    	eyes=detector.detectEyes(new CvRect(cvGetSeqElem(faces, j)));
	        			    	if (eyes>0) {
		        			    	blinkTime = System.currentTimeMillis();
		        			    	blink = true;
	        			    	}
        			    	}
    			    	}
        			    leftProfile=detector.detectLeftProfile();
        			    rightProfile=detector.detectRightProfile();
        			    if(blink&&!blinked) {
	        			    if(System.currentTimeMillis()-blinkTime<2000 && rightProfile!=leftProfile) {
	        			    	if (rightProfile)
	        			    		mode=false;
	        			    	else if(leftProfile)
	        			    		mode=true;
	        			    	blink=false;
	        			    	blinked=true;
	        			    }
	        			    else if(System.currentTimeMillis()-blinkTime>2000 && blinkTime != 0) {
	        			    	if (eyes==Detector.LEFT_EYE_BLINK) {
	            			    	mouse.mouseMove(mousePointer.x,mousePointer.y);
	        			            mouse.mousePress(InputEvent.BUTTON1_MASK);
	        			            mouse.mouseRelease(InputEvent.BUTTON1_MASK);
	        			    	}
	        			    	else if(eyes==Detector.RIGHT_EYE_BLINK) {
	            			    	mouse.mouseMove(mousePointer.x,mousePointer.y);
	        			            mouse.mousePress(InputEvent.BUTTON3_MASK);
	        			            mouse.mouseRelease(InputEvent.BUTTON3_MASK);
	        			    	}
	        			    	blinked=true;
	        			    	blink=false;
	        			    }
        			    }
        			    else if(blinked);
        			    else if(mode && leftProfile)
        			    	mouse.mouseMove(mousePointer.x+10,mousePointer.y);
        			    else if(mode && rightProfile)
        			    	mouse.mouseMove(mousePointer.x-10,mousePointer.y);
        			    else if(!mode && leftProfile)
        			    	mouse.mouseMove(mousePointer.x,mousePointer.y+10);
        			    else if(!mode && rightProfile)
        			    	mouse.mouseMove(mousePointer.x,mousePointer.y-10);
        			    System.out.println("eyes "+ eyes +" mode " + mode + " leftProfile " + leftProfile + " rightProfile " + rightProfile);
        			} catch (AWTException e) {
        				// TODO Auto-generated catch block
        				e.printStackTrace();
        			}
                    //cvSaveImage((i++)+"-aa.jpg", img);
                    // show image on window                    
                    canvas.showImage(img);
                }
                 //Thread.sleep(INTERVAL);
            }
        } catch (Exception e) {
        	e.printStackTrace();
        }
    }



  public static void main(String[] args) {
        GrabberShow gs = new GrabberShow();
        Thread th = new Thread(gs);
        th.start();
    }
}