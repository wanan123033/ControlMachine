package com.feipulai.exam.activity.ranger;


public class RangerUtil {

    public static double level(int du,int fen,int miao){
        return du + fen / 60.0 + miao / 3600.0;
    }

    /**
     * 已知距离,X轴夹角  求目标点的坐标
     * @param level   X轴的夹角
     * @param length  距离
     * @return
     */
    public static Point getPoint(double level,double length){
        Point point = new Point();
        point.y = Math.round(Math.sin(Math.toRadians(level)) * 1000.0) / 1000.0 * length;
        point.x = Math.round(Math.cos(Math.toRadians(level)) * 1000.0) / 1000.0 * length;
        return point;
    }

    /**
     * 已知直线上的两点  求直线外一点到直线的距离
     * @param point1  直线上的一点
     * @param point2  直线上一点
     * @param point3       直线外的一点
     * @return
     */
    public static double length(Point point1,Point point2,Point point3){
//        //1.求直线方程的一般式  ax+by+c=0
//        double a = point2.y - point1.y; //a=y2-y1
//        double b = point1.x - point2.x; //b=x1-x2
//        double c = point2.x * point1.y - point1.x * point2.y; //c=x2*y1-x1*y2
//        //2.距离公式  d = | ax+by+c | / 根号下 a*a + b*b
//        double length = Math.abs(a * p.x + b * p.y + c) / Math.sqrt(a * a + b * b);

        //S=(x1y2-x1y3+x2y3-x2y1+x3y1-x3y2)  面积公式   没有乘以1/2  也就是面积的2倍
        double S = point1.x * point2.y - point1.x * point3.y + point2.x * point3.y - point2.x * point1.y + point3.x * point1.y - point3.x * point2.y;
        double len = Math.abs(S / length(point1,point2));
        return len;
    }

    /**
     * 余弦定理
     * 已知三角形的两条边和这两条边的夹角求第三条边的长度
     * @param level  两边的夹角
     * @param alength 一边长
     * @param blength 二边长
     * @return
     */
    public static double cosine(double level,double alength,double blength){
        return Math.sqrt(alength * alength + blength * blength - 2 * alength * blength * Math.round(Math.cos(Math.toRadians(level)) * 1000.0) / 1000.0);
    }

    public static double length(Point point1,Point point2){
        return Math.sqrt((point1.x - point2.x) * (point1.x - point2.x) + (point1.y - point2.y) * (point1.y - point2.y));
    }

    /**
     * 已知两条边与X轴的夹角 求两边的夹角
     * @param level1
     * @param level2
     * @return
     */
    public static double inclination(double level1, double level2) {
        return Math.abs(level1 - level2);
    }
}
