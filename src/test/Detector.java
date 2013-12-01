package test;
import java.awt.AWTException;
import java.awt.Canvas;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.sql.Ref;
import java.util.ArrayList;
import java.util.List;

import com.googlecode.javacpp.Pointer;
import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_core.CvArr;
import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_core.CvPoint;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_imgproc;
import com.googlecode.javacv.cpp.opencv_legacy.CvObjectDetector;
import com.googlecode.javacv.cpp.opencv_legacy.CvRandState;
import com.googlecode.javacv.cpp.opencv_objdetect;
import com.googlecode.javacv.cpp.opencv_objdetect.CvHaarClassifierCascade;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;
import static com.googlecode.javacv.cpp.opencv_objdetect.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;


import com.googlecode.javacv.cpp.opencv_highgui.*;
import com.googlecode.javacv.cpp.opencv_imgproc.*;
import java.awt.Rectangle;
import java.io.File;

 
public class Detector{
	private static Accumulator leftProfile;
	private static Accumulator rightProfile;
	private static Accumulator frontal;
	private static Accumulator leftEye;
	private static Accumulator rightEye;
	public static final String PROFILE_FILE = 
			"/usr/share/opencv/haarcascades/haarcascade_profileface.xml";

	public static final String FRONTAL_FILE = 
			"/usr/share/opencv/haarcascades/haarcascade_frontalface_default.xml";
	
	//EYES

	static IplImage mRgba;
	static IplImage mGray;
	static int learnFrames;
	static IplImage templateRightEye;
	static IplImage templateLeftEye;
	static IplImage mZoomWindow;
	static IplImage mZoomWindow2;
	private static double match_value;

	public static final int LEFT_EYE_BLINK = 1;
	public static final int RIGHT_EYE_BLINK = 2;

	public static final String EYE_FILE = 
			"/usr/share/opencv/haarcascades/haarcascade_eye_tree_eyeglasses.xml";
	static CvHaarClassifierCascade EyeCascade = new 
			CvHaarClassifierCascade(cvLoad(EYE_FILE));
	public static final String LEFT_EYE_FILE = 
			"/usr/share/opencv/haarcascades/haarcascade_lefteye_2splits.xml";

	public static final String RIGHT_EYE_FILE = 
			"/usr/share/opencv/haarcascades/haarcascade_righteye_2splits.xml";

	static CvHaarClassifierCascade leftEyeCascade = new 
			CvHaarClassifierCascade(cvLoad(LEFT_EYE_FILE));
	static CvHaarClassifierCascade rightEyeCascade = new 
			CvHaarClassifierCascade(cvLoad(LEFT_EYE_FILE));
//	static opencv_objdetect.CascadeClassifier leftEyeCascade
//	= new opencv_objdetect.CascadeClassifier();
//	static opencv_objdetect.CascadeClassifier rightEyeCascade
//	= new opencv_objdetect.CascadeClassifier();
//= new opencv_objdetect.CascadeClassifier(cvLoad(RIGHT_EYE_FILE));

	static CvHaarClassifierCascade profileCascade = new 
			CvHaarClassifierCascade(cvLoad(PROFILE_FILE));
	static CvHaarClassifierCascade frontalCascade = new 
			CvHaarClassifierCascade(cvLoad(FRONTAL_FILE));

    static CanvasFrame canvas = new CanvasFrame("eye");
	public Detector(IplImage src) {
        canvas.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
/*		leftEyeCascade.load(LEFT_EYE_FILE);
		rightEyeCascade.load(RIGHT_EYE_FILE);*/
		mRgba=src;
		mGray = cvCreateImage(cvGetSize(mRgba), IPL_DEPTH_8U, 1); 
		cvCvtColor(mRgba, mGray, opencv_imgproc.CV_BGR2GRAY);
/*
        mGray = IplImage.create(src.width(), src.height(), IPL_DEPTH_8U, 1);
        cvCvtColor(src, mGray, opencv_imgproc.CV_BGR2GRAY);*/
//        cvSaveImage("/home/mis/test2.jpg", mGray);
		int accumulatorLimit = 5;
		leftProfile = new Accumulator(accumulatorLimit);
		rightProfile = new Accumulator(accumulatorLimit);
		frontal = new Accumulator(accumulatorLimit);
		leftEye = new Accumulator(2*accumulatorLimit);
		rightEye = new Accumulator(2*accumulatorLimit);
		/*for (int i=0; i<accumulatorLimit;i++) {
			leftEye.add(0);
			rightEye.add(0);
		}*/
	}
	public void clear(IplImage src) {
		mRgba=src;
		mGray = cvCreateImage(cvGetSize(mRgba), IPL_DEPTH_8U, 1); 
		cvCvtColor(mRgba, mGray, opencv_imgproc.CV_BGR2GRAY);
	}
	/*
	public Detector(IplImage src, int accumulatorLimit) {
		mRgba=src;
        cvCvtColor(src, mGray, opencv_imgproc.CV_BGR2GRAY);
        cvSaveImage("/home/mis/test2.jpg", mGray);
		leftProfile = new Accumulator(accumulatorLimit);
		rightProfile = new Accumulator(accumulatorLimit);
		frontal = new Accumulator(accumulatorLimit);
	}*/
	
	public boolean detectLeftProfile() {
        cvFlip(mGray,mGray,1);
		CvSeq sign = detectProfile();
		int total_Faces = sign.total();		
		leftProfile.add(total_Faces);
		if(leftProfile.mean()>0.5) {
			for(int i = 0; i < total_Faces; i++){
				CvRect r = new CvRect(cvGetSeqElem(sign, i));
				cvRectangle (
						mRgba,
						cvPoint(r.x(), r.y()),
						cvPoint(r.width() + r.x(), r.height() + r.y()),
						CvScalar.BLUE,
						2,
						CV_AA,
						0);
			}
	        cvFlip(mGray,mGray,1);
			return true;
		}
		else {
	        cvFlip(mGray,mGray,1);
			return false;
		}
	}
	
	public boolean detectRightProfile() {
		CvSeq sign = detectProfile();
		int total_Faces = sign.total();		
		rightProfile.add(total_Faces);
		if(rightProfile.mean()>0.5) {
			for(int i = 0; i < total_Faces; i++){
				CvRect r = new CvRect(cvGetSeqElem(sign, i));
				cvRectangle (
						mRgba,
						cvPoint(r.x(), r.y()),
						cvPoint(r.width() + r.x(), r.height() + r.y()),
						CvScalar.BLUE,
						2,
						CV_AA,
						0);
			}
			return true;
		}
		else
			return false;
	}

	public static CvSeq detectProfile(){
		CvMemStorage storage = CvMemStorage.create();
		CvSeq sign = cvHaarDetectObjects(
				mGray,
				profileCascade,
				storage,
				1.5,
				4,
				CV_HAAR_SCALE_IMAGE | CV_HAAR_FIND_BIGGEST_OBJECT);
 
		cvClearMemStorage(storage);
		return sign;
	}
	
	public static CvSeq detectFrontal(){
		CvMemStorage storage = CvMemStorage.create();
		CvSeq sign = cvHaarDetectObjects(
				mGray,
				frontalCascade,
				storage,
				1.5,
				5,
				CV_HAAR_DO_CANNY_PRUNING | CV_HAAR_FIND_BIGGEST_OBJECT);
 
		cvClearMemStorage(storage);
		int total_Faces = sign.total();		
 
		for(int i = 0; i < total_Faces; i++){
			CvRect r = new CvRect(cvGetSeqElem(sign, i));
			cvRectangle (
					mRgba,
					cvPoint(r.x(), r.y()),
					cvPoint(r.width() + r.x(), r.height() + r.y()),
					CvScalar.BLUE,
					2,
					CV_AA,
					0);// compute the eye area
			//CvRect eyearea = new CvRect(r.x() +r.width()/8,(int)(r.y() + (r.height()/4.5)),r.width() - 2*r.width()/8,(int)( r.height()/3.0));
			// split it
			//CvRect eyearea_right = new CvRect(r.x() +r.height()/16,(int)(r.y() + (r.height()/4.5)),(r.width() - 2*r.width()/16)/2,(int)( r.height()/3.0));
			//CvRect eyearea_left = new CvRect(r.x() +r.width()/16 +(r.width() - 2*r.width()/16)/2,(int)(r.y() + (r.height()/4.5)),(r.width() - 2*r.width()/16)/2,(int)( r.height()/3.0));
			// draw the area - mGray is working grayscale mat, if you want to see area in rgb preview, change mGray to mRgba
			//cvRectangle(mRgba,cvPoint(eyearea_left.x(),eyearea_left.y()),cvPoint(eyearea_left.x()+eyearea_left.width(),eyearea_left.y()+eyearea_left.height()) , CvScalar.GREEN, 2, CV_AA, 0);
			//cvRectangle(mRgba,cvPoint(eyearea_right.x(),eyearea_right.y()),cvPoint(eyearea_right.x()+eyearea_right.width(),eyearea_right.y()+eyearea_right.height()) , CvScalar.GREEN, 2, CV_AA, 0);
			//detectEye(leftEyeCascade,eyearea_left,24);
			/*
			if(learnFrames<5){
				//templateRightEye = get_template(rightEyeCascade,eyearea_right,24);
				//templateLeftEye = get_template(leftEyeCascade,eyearea_left,24);
				//learnFrames++;
				}
			else{
				// Learning finished, use the new templates for template matching
				//match_value = match_eye(eyearea_right,templateRightEye,opencv_imgproc.CV_TM_SQDIFF_NORMED); //Or hardcode method you needs eg TM_SQDIFF_NORMED
				//match_value = match_eye(eyearea_left,templateLeftEye,opencv_imgproc.CV_TM_SQDIFF_NORMED); //Or hardcode method you needs eg TM_SQDIFF_NORMED
				}
				// cut eye areas and put them to zoom windows
			opencv_imgproc.cvResize(mRgba.roi(toIplROI(eyearea_left)), mZoomWindow2);
			opencv_imgproc.cvResize(mRgba.roi(toIplROI(eyearea_right)), mZoomWindow);
				*/
			}
		return  sign;
	}
	
	public int detectEyes(CvRect facearea) {
		CvRect leftEyeArea = new CvRect(facearea.x() +facearea.width()/16,(int)(facearea.y() + (facearea.height()/5.5)),(facearea.width() - 2*facearea.width()/16)/2,(int)( facearea.height()/3.0));
		CvRect rightEyeArea = new CvRect(facearea.x() +facearea.width()/16 +(facearea.width() - 2*facearea.width()/16)/2,(int)(facearea.y() + (facearea.height()/5.5)),(facearea.width() - 2*facearea.width()/16)/2,(int)( facearea.height()/3.0));
		cvRectangle(mRgba,cvPoint(leftEyeArea.x(),leftEyeArea.y()),cvPoint(leftEyeArea.x()+leftEyeArea.width(),leftEyeArea.y()+leftEyeArea.height()) , CvScalar.CYAN, 2, CV_AA, 0);
		cvRectangle(mRgba,cvPoint(rightEyeArea.x(),rightEyeArea.y()),cvPoint(rightEyeArea.x()+rightEyeArea.width(),rightEyeArea.y()+rightEyeArea.height()) , CvScalar.GREEN, 2, CV_AA, 0);
		boolean leftEyeOpen = detectEye(EyeCascade,leftEyeArea,24);
		boolean rightEyeOpen = detectEye(EyeCascade,rightEyeArea,24);
		if(leftEyeOpen != rightEyeOpen) {
			if(leftEyeOpen) {
				rightEye.add(1);
				leftEye.add(0);
			}
			else {
				rightEye.add(0);
				leftEye.add(1);
			}
		}
		else if(leftEyeOpen) {
			rightEye.add(0);
			leftEye.add(0);
		}
		if((rightEye.accumulated() && rightEye.mean()>=0.99) != (leftEye.accumulated() && leftEye.mean()>=0.99)){
			int result;
			if (rightEye.accumulated() && rightEye.mean()>=0.99) {
				result =  RIGHT_EYE_BLINK;
			}
			else {
				result =  LEFT_EYE_BLINK;
			}
			rightEye.clear();
			leftEye.clear();
			return result;
		}
		return 0;
	}

    private static boolean detectEye(CvHaarClassifierCascade cascade, CvRect eyearea, int size) {
		CvMemStorage storage = CvMemStorage.create();
		IplImage mROI = mGray.roi(toIplROI(eyearea));
		cvFlip(mROI, mROI, 1);
		//cvSaveImage("/home/mis/"+eyearea.x()+".jpg", mROI);
		IplImage binary = mGray.roi(toIplROI(eyearea));
		CvArr test = mROI;
		CvScalar test2 = new CvScalar(0);
		CvScalar test3 = new CvScalar(50);
		CvArr test4 = null;
		cvInRangeS(mROI, test2, test3, binary);
		canvas.showImage(binary);
		CvSeq sign = cvHaarDetectObjects(
				mROI,
				cascade,
				storage,
				1.5,
				1,
				CV_HAAR_SCALE_IMAGE | CV_HAAR_FIND_BIGGEST_OBJECT);
 
		cvClearMemStorage(storage);
		int totalEyes = sign.total();
		for(int i = 0; i < totalEyes; i++){
			CvRect r = new CvRect(cvGetSeqElem(sign, i));
//            r.x(eyearea.x() + r.x());
            r.x(eyearea.x() + eyearea.width() - r.x() - r.width());
            r.y(eyearea.y() + r.y());
			cvRectangle (
					mRgba,
					cvPoint(r.x(), r.y()),
					cvPoint(r.width() + r.x(), r.height() + r.y()),
					CvScalar.BLUE,
					2,
					CV_AA,
					0);
			
		}
		if (totalEyes>0)
			return true;
		else
			return false;
    }
	 
    private static IplImage get_template(CascadeClassifier clasificator, CvRect eyearea, int size) {
    	IplImage mROI = mGray.roi(toIplROI(eyearea));
        CvRect eyes = new CvRect();
        CvPoint iris = new CvPoint();
        clasificator.detectMultiScale(mROI, eyes, 1.15, 15,
        		opencv_objdetect.CV_HAAR_FIND_BIGGEST_OBJECT | opencv_objdetect.CV_HAAR_SCALE_IMAGE, new CvSize(30, 30),
                new CvSize());
        //CvRect[] eyesArray = new CvRect[eyes.capacity()];
        for (int i=0; i<eyes.capacity();i++) {
        	CvRect e = eyes.position(i);
        	if (e.x() == 0 && e.y()==0)
        		return null;
            e.x(eyearea.x() + e.x());
            e.y(eyearea.y() + e.y());
            CvRect eye_only_rectangle = new CvRect((int) e.x(),
                    (int) (e.y() + e.height()*0.4), (int) e.width(),
                    (int)(e.height()*0.6));
            IplImage tmp = mGray.roi(toIplROI(eye_only_rectangle));
            
            double[] min_val = new double[2];
    		double[] max_val = new double[2];
    		CvPoint minLoc = new CvPoint();
    		CvPoint maxLoc = new CvPoint();

            //cvSaveImage("/home/mis/test2.jpg", mGray);
			cvMinMaxLoc(tmp,min_val,max_val,minLoc,maxLoc,null);
           /* cvCircle(mRgba, minLoc, 2, new CvScalar(255, 255, 255, 255),2,
					CV_AA,
					0);*/
            iris.x(minLoc.x() + eye_only_rectangle.x());
            iris.y(minLoc.y() + eye_only_rectangle.y());
            CvRect eye_template = new CvRect((int) iris.x() - size / 2, (int) iris.y()
                    - size / 2, size, size);
            cvRectangle(mRgba, 
					cvPoint(eye_template.x(), eye_template.y()), 
					cvPoint(eye_template.x()+eye_template.width(),eye_template.y()+eye_template.height()),
                    CvScalar.CYAN, 
					2,
					CV_AA,
					0);
            IplImage template = (mGray.roi(toIplROI(eye_template))).clone();
            return template;
        }
        return null;
    }
    
    private static IplROI toIplROI(Rectangle area) {
    	return toIplROI(area.x, area.y, area.width, area.height);
    }
    
    private static IplROI toIplROI(CvRect area) {
    	return toIplROI(area.x(), area.y(), area.width(), area.height());
    }

    private static IplROI toIplROI(int x, int y, int width, int height) {
        IplROI roi =new IplROI();
        roi.xOffset(x);
        roi.yOffset(y);
        roi.width(width);
        roi.height(height);
        return roi;
    }
    
    private static double  match_eye(CvRect eyearea, IplImage template,int type){
    	CvPoint matchLoc;
    	IplImage mROI = mGray.roi(toIplROI(eyearea));
    	int result_cols =  Math.abs(mROI.width() - template.width()) + 1;
    	int result_rows = Math.abs(mROI.height() - template.height()) + 1;
    	//Check for bad template size
    	  if(template.width()==0 ||template.height()==0){
    	              return 0.0;
    	          }
    	  CvSize size = new CvSize(Math.abs(mROI.asCvMat().cols() - template.asCvMat().cols()) + 1, Math.abs(mROI.asCvMat().rows() - template.asCvMat().rows()) + 1);
    	  IplImage result = cvCreateImage(size,IPL_DEPTH_32F,1); 
          cvMatchTemplate(mROI, template, result, CV_TM_CCOEFF_NORMED); 
    	  //IplImage mResult = IplImage.create(result_cols,result_rows, IPL_DEPTH_32F, 1); 
  			//IplImage mResult = IplImage.create(result_cols,result_rows,CV_8U,3);
    			//new IplImageCv(result_cols, result_rows, CvType.CV_8U);
    	 
    	//opencv_imgproc.cvMatchTemplate(mROI, template, mResult, opencv_imgproc.CV_TM_SQDIFF_NORMED);

        double[] min_val = new double[1];
		double[] max_val = new double[1];
		CvPoint minLoc = new CvPoint();
		CvPoint maxLoc = new CvPoint();
		cvMinMaxLoc(result.asCvMat(),min_val,max_val,minLoc,maxLoc,null);
    	matchLoc = maxLoc;
    	 
    	CvPoint  matchLoc_tx = new CvPoint(matchLoc.x()+eyearea.x(),matchLoc.y()+eyearea.y());
    	CvPoint  matchLoc_ty = new CvPoint(matchLoc.x() + template.asCvMat().cols() + eyearea.x(), matchLoc.y() + template.asCvMat().rows()+eyearea.y());
    	 
    	cvRectangle(mRgba, matchLoc_tx,matchLoc_ty, CvScalar.WHITE,2, CV_AA, 0);
    	 
    	return max_val[0];
    }
}