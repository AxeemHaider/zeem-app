package org.octabyte.zeem.Camera.camutil;

import android.content.Context;

import com.zomato.photofilters.geometry.Point;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.imageprocessors.subfilters.BrightnessSubfilter;
import com.zomato.photofilters.imageprocessors.subfilters.ColorOverlaySubfilter;
import com.zomato.photofilters.imageprocessors.subfilters.ContrastSubfilter;
import com.zomato.photofilters.imageprocessors.subfilters.SaturationSubfilter;
import com.zomato.photofilters.imageprocessors.subfilters.ToneCurveSubfilter;
import com.zomato.photofilters.imageprocessors.subfilters.VignetteSubfilter;


/**
 * Created by Azeem on 6/13/2017.
 */
public final class CameraFilters {

    private CameraFilters() {
    }

    public static Filter Welcome(){
        Point[] redKnots, greenKnots, blueKnots;

        redKnots = new Point[6];
        redKnots[0] = new Point(0, 19);
        redKnots[1] = new Point(30, 62);
        redKnots[2] = new Point(82, 48);
        redKnots[3] = new Point(128, 188);
        redKnots[4] = new Point(145, 200);
        redKnots[5] = new Point(255, 250);

        greenKnots = new Point[6];
        greenKnots[0] = new Point(0, 0);
        greenKnots[1] = new Point(48, 72);
        greenKnots[2] = new Point(115, 188);
        greenKnots[3] = new Point(160, 220);
        greenKnots[4] = new Point(233, 245);
        greenKnots[5] = new Point(255, 255);

        blueKnots = new Point[7];
        blueKnots[0] = new Point(0, 25);
        blueKnots[1] = new Point(35, 80);
        blueKnots[2] = new Point(106, 175);
        blueKnots[3] = new Point(151, 188);
        blueKnots[4] = new Point(215, 215);
        blueKnots[5] = new Point(240, 235);
        blueKnots[6] = new Point(255, 245);

        Filter filter = new Filter();
        filter.addSubFilter(new ToneCurveSubfilter(null, redKnots, greenKnots, blueKnots));

        return filter;
    }

    public static Filter Old(){
        Point[] redKnots, greenKnots, blueKnots;

        redKnots = new Point[5];
        redKnots[0] = new Point(0, 75);
        redKnots[1] = new Point(75, 125);
        redKnots[2] = new Point(145, 200);
        redKnots[3] = new Point(190, 220);
        redKnots[4] = new Point(255, 230);

        greenKnots = new Point[6];
        greenKnots[0] = new Point(0, 52);
        greenKnots[1] = new Point(42, 45);
        greenKnots[2] = new Point(110, 120);
        greenKnots[3] = new Point(154, 168);
        greenKnots[4] = new Point(232, 235);
        greenKnots[5] = new Point(255, 242);

        blueKnots = new Point[6];
        blueKnots[0] = new Point(0, 62);
        blueKnots[1] = new Point(65, 82);
        blueKnots[2] = new Point(108, 132);
        blueKnots[3] = new Point(175, 210);
        blueKnots[4] = new Point(210, 208);
        blueKnots[5] = new Point(255, 208);

        Filter filter = new Filter();
        filter.addSubFilter(new ToneCurveSubfilter(null, redKnots, greenKnots, blueKnots));

        return filter;
    }

    public static Filter YLight(){
        Point[] redKnots, greenKnots, blueKnots;

        redKnots = new Point[5];
        redKnots[0] = new Point(0, 35);
        redKnots[1] = new Point(40, 50);
        redKnots[2] = new Point(125, 165);
        redKnots[3] = new Point(175,230);
        redKnots[4] = new Point(255, 255);

        greenKnots = new Point[5];
        greenKnots[0] = new Point(0, 0);
        greenKnots[1] = new Point(65, 50);
        greenKnots[2] = new Point(92, 102);
        greenKnots[3] = new Point(180, 220);
        greenKnots[4] = new Point(255, 255);

        blueKnots = new Point[6];
        blueKnots[0] = new Point(0, 35);
        blueKnots[1] = new Point(62, 62);
        blueKnots[2] = new Point(88, 95);
        blueKnots[3] = new Point(132, 158);
        blueKnots[4] = new Point(225, 230);
        blueKnots[5] = new Point(255, 232);

        Filter filter = new Filter();
        filter.addSubFilter(new ToneCurveSubfilter(null, redKnots, greenKnots, blueKnots));

        return filter;
    }

    public static Filter Morning(){
        Point[] redKnots, greenKnots, blueKnots;

        redKnots = new Point[6];
        redKnots[0] = new Point(0, 25);
        redKnots[1] = new Point(45, 80);
        redKnots[2] = new Point(85, 135);
        redKnots[3] = new Point(120,180);
        redKnots[4] = new Point(230, 240);
        redKnots[5] = new Point(255, 255);

        greenKnots = new Point[7];
        greenKnots[0] = new Point(0, 0);
        greenKnots[1] = new Point(40, 55);
        greenKnots[2] = new Point(88, 112);
        greenKnots[3] = new Point(132, 172);
        greenKnots[4] = new Point(168, 198);
        greenKnots[5] = new Point(215, 218);
        greenKnots[6] = new Point(255, 240);

        blueKnots = new Point[7];
        blueKnots[0] = new Point(0, 18);
        blueKnots[1] = new Point(42, 58);
        blueKnots[2] = new Point(90, 102);
        blueKnots[3] = new Point(120, 130);
        blueKnots[4] = new Point(164, 170);
        blueKnots[5] = new Point(212, 195);
        blueKnots[6] = new Point(255, 210);

        Filter filter = new Filter();
        filter.addSubFilter(new ToneCurveSubfilter(null, redKnots, greenKnots, blueKnots));

        return filter;
    }

    public static Filter Inside(){
        Point[] redKnots, greenKnots, blueKnots;

        redKnots = new Point[4];
        redKnots[0] = new Point(0, 0);
        redKnots[1] = new Point(60, 55);
        redKnots[2] = new Point(130, 155);
        redKnots[3] = new Point(255,255);

        greenKnots = new Point[4];
        greenKnots[0] = new Point(0, 0);
        greenKnots[1] = new Point(64, 40);
        greenKnots[2] = new Point(125, 125);
        greenKnots[3] = new Point(255, 255);

        blueKnots = new Point[5];
        blueKnots[0] = new Point(0, 0);
        blueKnots[1] = new Point(65, 30);
        blueKnots[2] = new Point(125, 105);
        blueKnots[3] = new Point(170, 165);
        blueKnots[4] = new Point(255, 240);

        Filter filter = new Filter();
        filter.addSubFilter(new ToneCurveSubfilter(null, redKnots, greenKnots, blueKnots));

        return filter;
    }

    public static Filter Blue(){
        Point[] redKnots, greenKnots, blueKnots;

        redKnots = new Point[7];
        redKnots[0] = new Point(0, 35);
        redKnots[1] = new Point(42, 68);
        redKnots[2] = new Point(85, 115);
        redKnots[3] = new Point(124,165);
        redKnots[4] = new Point(170,200);
        redKnots[5] = new Point(215,228);
        redKnots[6] = new Point(255,255);

        greenKnots = new Point[6];
        greenKnots[0] = new Point(0, 0);
        greenKnots[1] = new Point(45, 60);
        greenKnots[2] = new Point(102, 135);
        greenKnots[3] = new Point(140, 182);
        greenKnots[4] = new Point(192, 215);
        greenKnots[5] = new Point(255, 255);

        blueKnots = new Point[7];
        blueKnots[0] = new Point(0, 0);
        blueKnots[1] = new Point(24, 42);
        blueKnots[2] = new Point(60, 100);
        blueKnots[3] = new Point(105, 170);
        blueKnots[4] = new Point(145, 208);
        blueKnots[5] = new Point(210, 235);
        blueKnots[6] = new Point(255, 245);

        Filter filter = new Filter();
        filter.addSubFilter(new ToneCurveSubfilter(null, redKnots, greenKnots, blueKnots));

        return filter;
    }

    public static Filter Favourite(){
        Point[] rgbKnots;

        rgbKnots = new Point[4];

        rgbKnots[0] = new Point(0,0);
        rgbKnots[1] = new Point(60,75);
        rgbKnots[2] = new Point(168,218);
        rgbKnots[3] = new Point(255,255);

        Filter filter = new Filter();
        filter.addSubFilter(new ToneCurveSubfilter(rgbKnots, null, null, null));

        return filter;
    }

    public static Filter Sun(){
        Point[] redKnots, greenKnots, blueKnots;

        redKnots = new Point[6];
        redKnots[0] = new Point(0, 0);
        redKnots[1] = new Point(60, 102);
        redKnots[2] = new Point(110, 185);
        redKnots[3] = new Point(150,220);
        redKnots[4] = new Point(235,245);
        redKnots[5] = new Point(255,245);

        greenKnots = new Point[5];
        greenKnots[0] = new Point(0, 0);
        greenKnots[1] = new Point(68, 68);
        greenKnots[2] = new Point(105, 120);
        greenKnots[3] = new Point(190, 220);
        greenKnots[4] = new Point(255, 255);

        blueKnots = new Point[5];
        blueKnots[0] = new Point(0, 0);
        blueKnots[1] = new Point(88, 12);
        blueKnots[2] = new Point(145, 140);
        blueKnots[3] = new Point(185, 212);
        blueKnots[4] = new Point(255, 255);

        Filter filter = new Filter();
        filter.addSubFilter(new ToneCurveSubfilter(null, redKnots, greenKnots, blueKnots));

        return filter;
    }

    public static Filter Low(){
        Point[] redKnots, greenKnots, blueKnots;

        redKnots = new Point[7];
        redKnots[0] = new Point(0, 0);
        redKnots[1] = new Point(40, 20);
        redKnots[2] = new Point(88, 80);
        redKnots[3] = new Point(128,150);
        redKnots[4] = new Point(170,200);
        redKnots[5] = new Point(230,245);
        redKnots[6] = new Point(255,255);

        greenKnots = new Point[7];
        greenKnots[0] = new Point(0, 0);
        greenKnots[1] = new Point(35, 15);
        greenKnots[2] = new Point(90, 70);
        greenKnots[3] = new Point(105, 105);
        greenKnots[4] = new Point(148, 180);
        greenKnots[5] = new Point(188, 218);
        greenKnots[6] = new Point(255, 255);

        blueKnots = new Point[7];
        blueKnots[0] = new Point(0, 0);
        blueKnots[1] = new Point(62, 50);
        blueKnots[2] = new Point(100, 95);
        blueKnots[3] = new Point(130, 155);
        blueKnots[4] = new Point(150, 182);
        blueKnots[5] = new Point(190, 220);
        blueKnots[6] = new Point(255, 255);

        Filter filter = new Filter();
        filter.addSubFilter(new ToneCurveSubfilter(null, redKnots, greenKnots, blueKnots));

        return filter;
    }

    public static Filter Fair(){
        Point[] redKnots, greenKnots, blueKnots;

        redKnots = new Point[5];
        redKnots[0] = new Point(0, 30);
        redKnots[1] = new Point(85, 110);
        redKnots[2] = new Point(125, 170);
        redKnots[3] = new Point(221,232);
        redKnots[4] = new Point(254,242);

        greenKnots = new Point[6];
        greenKnots[0] = new Point(0, 15);
        greenKnots[1] = new Point(40, 55);
        greenKnots[2] = new Point(80, 95);
        greenKnots[3] = new Point(142, 196);
        greenKnots[4] = new Point(188, 215);
        greenKnots[5] = new Point(255, 230);

        blueKnots = new Point[7];
        blueKnots[0] = new Point(0, 15);
        blueKnots[1] = new Point(45, 60);
        blueKnots[2] = new Point(85, 115);
        blueKnots[3] = new Point(135, 185);
        blueKnots[4] = new Point(182, 215);
        blueKnots[5] = new Point(235, 230);
        blueKnots[6] = new Point(255, 225);

        Filter filter = new Filter();
        filter.addSubFilter(new ToneCurveSubfilter(null, redKnots, greenKnots, blueKnots));

        return filter;
    }

    public static Filter GreenOne(){
        Point[] redKnots, greenKnots, blueKnots;

        redKnots = new Point[7];
        redKnots[0] = new Point(0, 0);
        redKnots[1] = new Point(30, 5);
        redKnots[2] = new Point(58, 25);
        redKnots[3] = new Point(83,85);
        redKnots[4] = new Point(112,140);
        redKnots[5] = new Point(190,120);
        redKnots[6] = new Point(255,255);

        greenKnots = new Point[6];
        greenKnots[0] = new Point(0, 0);
        greenKnots[1] = new Point(20, 5);
        greenKnots[2] = new Point(50, 62);
        greenKnots[3] = new Point(132, 150);
        greenKnots[4] = new Point(190, 205);
        greenKnots[5] = new Point(255, 225);

        blueKnots = new Point[5];
        blueKnots[0] = new Point(0, 65);
        blueKnots[1] = new Point(40, 90);
        blueKnots[2] = new Point(85, 115);
        blueKnots[3] = new Point(212, 185);
        blueKnots[4] = new Point(255, 205);

        Filter filter = new Filter();
        filter.addSubFilter(new ToneCurveSubfilter(null, redKnots, greenKnots, blueKnots));

        return filter;
    }

	public static Filter RiseUp(){
        Point[] redKnots, greenKnots, blueKnots;

        redKnots = new Point[6];
        redKnots[0] = new Point(0, 25);
        redKnots[1] = new Point(30, 70);
        redKnots[2] = new Point(130, 192);
        redKnots[3] = new Point(170,200);
        redKnots[4] = new Point(233,233);
        redKnots[5] = new Point(255,255);

        greenKnots = new Point[7];
        greenKnots[0] = new Point(0, 25);
        greenKnots[1] = new Point(30, 72);
        greenKnots[2] = new Point(65, 118);
        greenKnots[3] = new Point(100, 158);
        greenKnots[4] = new Point(152, 195);
        greenKnots[5] = new Point(210, 230);
        greenKnots[6] = new Point(255, 255);

        blueKnots = new Point[7];
        blueKnots[0] = new Point(0, 35);
        blueKnots[1] = new Point(40, 75);
        blueKnots[2] = new Point(82, 124);
        blueKnots[3] = new Point(120, 162);
        blueKnots[4] = new Point(175, 188);
        blueKnots[5] = new Point(220, 214);
        blueKnots[6] = new Point(255, 255);

        Filter filter = new Filter();
        filter.addSubFilter(new ToneCurveSubfilter(null, redKnots, greenKnots, blueKnots));

        return filter;
    }
	
	public static Filter Waldan(){
        Point[] redKnots, greenKnots, blueKnots;

        redKnots = new Point[7];
        redKnots[0] = new Point(0, 10);
        redKnots[1] = new Point(48, 88);
        redKnots[2] = new Point(105, 155);
        redKnots[3] = new Point(130,180);
        redKnots[4] = new Point(190,212);
        redKnots[5] = new Point(232,234);
        redKnots[6] = new Point(255,245);

        greenKnots = new Point[7];
        greenKnots[0] = new Point(0, 0);
        greenKnots[1] = new Point(38, 72);
        greenKnots[2] = new Point(85, 124);
        greenKnots[3] = new Point(124, 160);
        greenKnots[4] = new Point(172, 186);
        greenKnots[5] = new Point(218, 210);
        greenKnots[6] = new Point(255, 230);

        blueKnots = new Point[7];
        blueKnots[0] = new Point(0, 30);
        blueKnots[1] = new Point(45, 82);
        blueKnots[2] = new Point(95, 132);
        blueKnots[3] = new Point(138, 164);
        blueKnots[4] = new Point(176, 182);
        blueKnots[5] = new Point(210, 200);
        blueKnots[6] = new Point(255, 218);

        Filter filter = new Filter();
        filter.addSubFilter(new ToneCurveSubfilter(null, redKnots, greenKnots, blueKnots));

        return filter;
    }
	
	public static Filter Nature(){
        Point[] redKnots, greenKnots, blueKnots;

        redKnots = new Point[6];
        redKnots[0] = new Point(0, 0);
        redKnots[1] = new Point(40, 35);
        redKnots[2] = new Point(90, 92);
        redKnots[3] = new Point(145,155);
        redKnots[4] = new Point(235,230);
        redKnots[5] = new Point(255,235);

        greenKnots = new Point[5];
        greenKnots[0] = new Point(0, 0);
        greenKnots[1] = new Point(62, 50);
        greenKnots[2] = new Point(155, 140);
        greenKnots[3] = new Point(210, 188);
        greenKnots[4] = new Point(255, 255);

        blueKnots = new Point[5];
        blueKnots[0] = new Point(0, 0);
        blueKnots[1] = new Point(80, 80);
        blueKnots[2] = new Point(128, 112);
        blueKnots[3] = new Point(182, 145);
        blueKnots[4] = new Point(255, 220);

        Filter filter = new Filter();
        filter.addSubFilter(new ToneCurveSubfilter(null, redKnots, greenKnots, blueKnots));

        return filter;
    }
	
	public static Filter Red(){
        Point[] redKnots, greenKnots, blueKnots;

        redKnots = new Point[6];
        redKnots[0] = new Point(0, 120);
        redKnots[1] = new Point(50, 160);
        redKnots[2] = new Point(105, 198);
        redKnots[3] = new Point(145,215);
        redKnots[4] = new Point(190,230);
        redKnots[5] = new Point(255,255);

        greenKnots = new Point[4];
        greenKnots[0] = new Point(0, 0);
        greenKnots[1] = new Point(22, 60);
        greenKnots[2] = new Point(125, 180);
        greenKnots[3] = new Point(255, 255);

        blueKnots = new Point[6];
        blueKnots[0] = new Point(0, 50);
        blueKnots[1] = new Point(40, 60);
        blueKnots[2] = new Point(80, 102);
        blueKnots[3] = new Point(122, 148);
        blueKnots[4] = new Point(185, 185);
        blueKnots[5] = new Point(255, 210);

        Filter filter = new Filter();
        filter.addSubFilter(new ToneCurveSubfilter(null, redKnots, greenKnots, blueKnots));

        return filter;
    }
	
	public static Filter Wow(){
        Point[] redKnots, greenKnots, blueKnots;

        redKnots = new Point[6];
        redKnots[0] = new Point(0, 20);
        redKnots[1] = new Point(50, 80);
        redKnots[2] = new Point(85, 120);
        redKnots[3] = new Point(128,162);
        redKnots[4] = new Point(228,224);
        redKnots[5] = new Point(255,240);

        greenKnots = new Point[7];
        greenKnots[0] = new Point(0, 0);
        greenKnots[1] = new Point(18, 12);
        greenKnots[2] = new Point(60, 70);
        greenKnots[3] = new Point(104, 128);
        greenKnots[4] = new Point(148, 178);
        greenKnots[5] = new Point(212, 224);
        greenKnots[6] = new Point(255, 255);

        blueKnots = new Point[7];
        blueKnots[0] = new Point(0, 20);
        blueKnots[1] = new Point(42, 62);
        blueKnots[2] = new Point(80, 104);
        blueKnots[3] = new Point(124, 144);
        blueKnots[4] = new Point(170, 182);
        blueKnots[5] = new Point(220, 210);
        blueKnots[6] = new Point(255, 230);

        Filter filter = new Filter();
        filter.addSubFilter(new ToneCurveSubfilter(null, redKnots, greenKnots, blueKnots));

        return filter;
    }
	
	public static Filter Jaman(){
        Point[] redKnots, greenKnots, blueKnots;

        redKnots = new Point[6];
        redKnots[0] = new Point(0, 12);
        redKnots[1] = new Point(40, 44);
        redKnots[2] = new Point(85, 125);
        redKnots[3] = new Point(122,180);
        redKnots[4] = new Point(170,220);
        redKnots[5] = new Point(255,255);

        greenKnots = new Point[6];
        greenKnots[0] = new Point(0, 35);
        greenKnots[1] = new Point(40, 78);
        greenKnots[2] = new Point(90, 140);
        greenKnots[3] = new Point(130, 188);
        greenKnots[4] = new Point(175, 215);
        greenKnots[5] = new Point(255, 245);

        blueKnots = new Point[5];
        blueKnots[0] = new Point(0, 85);
        blueKnots[1] = new Point(85, 150);
        blueKnots[2] = new Point(130, 170);
        blueKnots[3] = new Point(165, 185);
        blueKnots[4] = new Point(255, 220);

        Filter filter = new Filter();
        filter.addSubFilter(new ToneCurveSubfilter(null, redKnots, greenKnots, blueKnots));

        return filter;
    }
	
	public static Filter Nice(){
        Point[] redKnots, greenKnots, blueKnots;

        redKnots = new Point[5];
        redKnots[0] = new Point(0, 30);
        redKnots[1] = new Point(68, 105);
        redKnots[2] = new Point(95, 145);
        redKnots[3] = new Point(175,215);
        redKnots[4] = new Point(255,240);

        greenKnots = new Point[5];
        greenKnots[0] = new Point(0, 30);
        greenKnots[1] = new Point(55, 85);
        greenKnots[2] = new Point(105, 160);
        greenKnots[3] = new Point(198, 210);
        greenKnots[4] = new Point(255, 230);

        blueKnots = new Point[5];
        blueKnots[0] = new Point(0, 30);
        blueKnots[1] = new Point(40, 70);
        blueKnots[2] = new Point(112, 165);
        blueKnots[3] = new Point(195, 215);
        blueKnots[4] = new Point(255, 218);

        Filter filter = new Filter();
        filter.addSubFilter(new ToneCurveSubfilter(null, redKnots, greenKnots, blueKnots));

        return filter;
    }
	
	public static Filter Pro(){
        Point[] redKnots, greenKnots, blueKnots;

        redKnots = new Point[6];
        redKnots[0] = new Point(0, 0);
        redKnots[1] = new Point(42, 28);
        redKnots[2] = new Point(105, 100);
        redKnots[3] = new Point(148,160);
        redKnots[4] = new Point(185,208);
        redKnots[5] = new Point(255,255);

        greenKnots = new Point[7];
        greenKnots[0] = new Point(0, 0);
        greenKnots[1] = new Point(40, 25);
        greenKnots[2] = new Point(85, 75);
        greenKnots[3] = new Point(125, 130);
        greenKnots[4] = new Point(165, 180);
        greenKnots[5] = new Point(212, 230);
        greenKnots[6] = new Point(255, 255);

        blueKnots = new Point[7];
        blueKnots[0] = new Point(0, 30);
        blueKnots[1] = new Point(40, 58);
        blueKnots[2] = new Point(82, 90);
        blueKnots[3] = new Point(125, 125);
        blueKnots[4] = new Point(170, 160);
        blueKnots[5] = new Point(235, 210);
        blueKnots[6] = new Point(255, 222);

        Filter filter = new Filter();
        filter.addSubFilter(new ToneCurveSubfilter(null, redKnots, greenKnots, blueKnots));

        return filter;
    }
	
    public static Filter New(Context context){
        Point[] redKnots, greenKnots, blueKnots;

        redKnots = new Point[6];
        redKnots[0] = new Point(0, 19);
        redKnots[1] = new Point(30, 62);
        redKnots[2] = new Point(82, 48);
        redKnots[3] = new Point(128, 188);
        redKnots[4] = new Point(145, 200);
        redKnots[5] = new Point(255, 250);

        greenKnots = new Point[6];
        greenKnots[0] = new Point(0, 0);
        greenKnots[1] = new Point(48, 72);
        greenKnots[2] = new Point(115, 188);
        greenKnots[3] = new Point(160, 220);
        greenKnots[4] = new Point(233, 245);
        greenKnots[5] = new Point(255, 255);

        blueKnots = new Point[7];
        blueKnots[0] = new Point(0, 25);
        blueKnots[1] = new Point(35, 80);
        blueKnots[2] = new Point(106, 175);
        blueKnots[3] = new Point(151, 188);
        blueKnots[4] = new Point(215, 215);
        blueKnots[5] = new Point(240, 235);
        blueKnots[6] = new Point(255, 245);

        Filter filter = new Filter();
        filter.addSubFilter(new ToneCurveSubfilter(null, redKnots, greenKnots, blueKnots));
        filter.addSubFilter(new VignetteSubfilter(context, 150));

        return filter;
    }

    public static Filter Bright(){
        Point[] rgbKnots, redKnots, greenKnots, blueKnots;

        rgbKnots = new Point[2];
        redKnots = new Point[2];
        greenKnots = new Point[2];
        blueKnots = new Point[2];

        rgbKnots[1] = new Point(255, 255);
        redKnots[1] = new Point(255,255);
        greenKnots[1] = new Point(255,255);
        blueKnots[1] = new Point(255,255);

        rgbKnots[0] = new Point(0, 58);

        Filter filter = new Filter();
        filter.addSubFilter(new ToneCurveSubfilter(rgbKnots, null, null, null));
        filter.addSubFilter(new ColorOverlaySubfilter(50, .03f, .03f, .0f));
        filter.addSubFilter(new BrightnessSubfilter(6));
        filter.addSubFilter(new ContrastSubfilter(1.1f));

        redKnots[0] = new Point(7, 0);
        greenKnots[0] = new Point(0, 9);
        blueKnots[0] = new Point(0, 21);

        filter.addSubFilter(new ToneCurveSubfilter(null, redKnots, greenKnots, blueKnots));

        redKnots[0] = new Point(0, 4);
        greenKnots[0] = new Point(0, 14);
        blueKnots[0] = new Point(0, 27);

        filter.addSubFilter(new ToneCurveSubfilter(null, redKnots, greenKnots, blueKnots));

        return filter;
    }

    public static Filter Focus(Context context){
        Point[] rgbKnots, redKnots;

        rgbKnots = new Point[2];
        redKnots = new Point[2];

        rgbKnots[1] = new Point(255, 255);
        redKnots[1] = new Point(255,255);

        rgbKnots[0] = new Point(0, 54);
        redKnots[0] = new Point(0, 21);

        Filter filter = new Filter();
        filter.addSubFilter(new ToneCurveSubfilter(rgbKnots, redKnots, null, null));
        filter.addSubFilter(new ContrastSubfilter(2f));
        filter.addSubFilter(new VignetteSubfilter(context, 150));

        return filter;
    }

    public static Filter ColorDown(){
        Point[] rgbKnots, redKnots, greenKnots, blueKnots;

        rgbKnots = new Point[2];
        redKnots = new Point[2];
        greenKnots = new Point[2];
        blueKnots = new Point[2];

        rgbKnots[1] = new Point(255, 255);
        redKnots[1] = new Point(255,255);
        greenKnots[1] = new Point(255,255);
        blueKnots[1] = new Point(255,255);

        redKnots[0] = new Point(0, 91);
        blueKnots[0] = new Point(0, 44);

        Filter filter = new Filter();
        filter.addSubFilter(new ToneCurveSubfilter(null, redKnots, null, blueKnots));
        filter.addSubFilter(new BrightnessSubfilter(2));
        filter.addSubFilter(new ContrastSubfilter(1.4f));

        redKnots[0] = new Point(0, 17);
        greenKnots[0] = new Point(0, 40);
        blueKnots[0] = new Point(0, 19);
        redKnots[0] = new Point(17, 0);

        filter.addSubFilter(new ToneCurveSubfilter(null, redKnots, greenKnots, blueKnots));

        return filter;
    }

    public static Filter B_W(){
        Filter filter = new Filter();
        filter.addSubFilter(new SaturationSubfilter(0));
        return filter;
    }

    public static Filter Round(Context context){
        Point[] greenKnots, blueKnots;

        greenKnots = new Point[2];
        blueKnots = new Point[2];

        greenKnots[1] = new Point(255, 255);
        blueKnots[1] = new Point(255, 255);

        blueKnots[0] = new Point(0, 57);

        Filter filter = new Filter();
        filter.addSubFilter(new ToneCurveSubfilter(null, null, null, blueKnots));
        filter.addSubFilter(new BrightnessSubfilter(5));
        filter.addSubFilter(new ContrastSubfilter(0.8f));

        greenKnots[0] = new Point(0, 15);
        blueKnots[0] = new Point(0, 6);

        filter.addSubFilter(new ToneCurveSubfilter(null, null, greenKnots, blueKnots));
        filter.addSubFilter(new VignetteSubfilter(context, 150));

        return filter;
    }

    public static Filter getStarLitFilter() {
        Point[] rgbKnots;
        rgbKnots = new Point[8];
        rgbKnots[0] = new Point(0, 0);
        rgbKnots[1] = new Point(34, 6);
        rgbKnots[2] = new Point(69, 23);
        rgbKnots[3] = new Point(100, 58);
        rgbKnots[4] = new Point(150, 154);
        rgbKnots[5] = new Point(176, 196);
        rgbKnots[6] = new Point(207, 233);
        rgbKnots[7] = new Point(255, 255);
        Filter filter = new Filter();
        filter.addSubFilter(new ToneCurveSubfilter(rgbKnots, null, null, null));
        return filter;
    }

    public static Filter getBlueMessFilter() {
        Point[] redKnots;
        redKnots = new Point[8];
        redKnots[0] = new Point(0, 0);
        redKnots[1] = new Point(86, 34);
        redKnots[2] = new Point(117, 41);
        redKnots[3] = new Point(146, 80);
        redKnots[4] = new Point(170, 151);
        redKnots[5] = new Point(200, 214);
        redKnots[6] = new Point(225, 242);
        redKnots[7] = new Point(255, 255);
        Filter filter = new Filter();
        filter.addSubFilter(new ToneCurveSubfilter(null, redKnots, null, null));
        filter.addSubFilter(new BrightnessSubfilter(30));
        filter.addSubFilter(new ContrastSubfilter(1f));
        return filter;
    }

    public static Filter getAweStruckVibeFilter() {
        Point[] rgbKnots;
        Point[] redKnots;
        Point[] greenKnots;
        Point[] blueKnots;

        rgbKnots = new Point[5];
        rgbKnots[0] = new Point(0, 0);
        rgbKnots[1] = new Point(80, 43);
        rgbKnots[2] = new Point(149, 102);
        rgbKnots[3] = new Point(201, 173);
        rgbKnots[4] = new Point(255, 255);

        redKnots = new Point[5];
        redKnots[0] = new Point(0, 0);
        redKnots[1] = new Point(125, 147);
        redKnots[2] = new Point(177, 199);
        redKnots[3] = new Point(213, 228);
        redKnots[4] = new Point(255, 255);


        greenKnots = new Point[6];
        greenKnots[0] = new Point(0, 0);
        greenKnots[1] = new Point(57, 76);
        greenKnots[2] = new Point(103, 130);
        greenKnots[3] = new Point(167, 192);
        greenKnots[4] = new Point(211, 229);
        greenKnots[5] = new Point(255, 255);


        blueKnots = new Point[7];
        blueKnots[0] = new Point(0, 0);
        blueKnots[1] = new Point(38, 62);
        blueKnots[2] = new Point(75, 112);
        blueKnots[3] = new Point(116, 158);
        blueKnots[4] = new Point(171, 204);
        blueKnots[5] = new Point(212, 233);
        blueKnots[6] = new Point(255, 255);

        Filter filter = new Filter();
        filter.addSubFilter(new ToneCurveSubfilter(rgbKnots, redKnots, greenKnots, blueKnots));
        return filter;
    }

    public static Filter getLimeStutterFilter() {
        Point[] blueKnots;
        blueKnots = new Point[3];
        blueKnots[0] = new Point(0, 0);
        blueKnots[1] = new Point(165, 114);
        blueKnots[2] = new Point(255, 255);
        // Check whether output is null or not.
        Filter filter = new Filter();
        filter.addSubFilter(new ToneCurveSubfilter(null, null, null, blueKnots));
        return filter;
    }

    public static Filter getNightWhisperFilter() {
        Point[] rgbKnots;
        Point[] redKnots;
        Point[] greenKnots;
        Point[] blueKnots;

        rgbKnots = new Point[3];
        rgbKnots[0] = new Point(0, 0);
        rgbKnots[1] = new Point(174, 109);
        rgbKnots[2] = new Point(255, 255);

        redKnots = new Point[4];
        redKnots[0] = new Point(0, 0);
        redKnots[1] = new Point(70, 114);
        redKnots[2] = new Point(157, 145);
        redKnots[3] = new Point(255, 255);

        greenKnots = new Point[3];
        greenKnots[0] = new Point(0, 0);
        greenKnots[1] = new Point(109, 138);
        greenKnots[2] = new Point(255, 255);

        blueKnots = new Point[3];
        blueKnots[0] = new Point(0, 0);
        blueKnots[1] = new Point(113, 152);
        blueKnots[2] = new Point(255, 255);

        Filter filter = new Filter();
        filter.addSubFilter(new ToneCurveSubfilter(rgbKnots, redKnots, greenKnots, blueKnots));
        return filter;
    }


}