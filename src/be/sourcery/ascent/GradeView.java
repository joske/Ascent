package be.sourcery.ascent;

import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;


public class GradeView extends View {

    private Paint paint;
    private Paint osPaint;
    private Paint flPaint;
    private Paint rpPaint;
    private Paint tpPaint;
    private List<GradeInfo> data = null;

    public GradeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(0x80000000);
        osPaint = new Paint();
        osPaint.setStyle(Paint.Style.FILL);
        osPaint.setColor(0x80000000);
        flPaint = new Paint();
        flPaint.setColor(0x80FFFF00);
        rpPaint = new Paint();
        rpPaint.setColor(0x80FF0000);
        tpPaint = new Paint();
        tpPaint.setColor(0x80D0D0D0);
    }

    public void setGradeInfo(List<GradeInfo> grades) {
        this.data = grades;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (data != null) {
            int boxStart = 160;
            int availableWidth = getWidth() - boxStart;
            int maxSize = getMax();
            int center = boxStart + availableWidth / 2;
            int scale = availableWidth / maxSize;
            int rectHeight = 10;
            int count = 0;
            for (Iterator iterator = data.iterator(); iterator.hasNext();) {
                count++;
                GradeInfo grades = (GradeInfo)iterator.next();
                String grade = grades.getGrade();
                int totalSize = grades.getTotal();
                int totalWidth = totalSize * scale;
                int osSize = grades.getOsCount();
                int osWidth = osSize * scale;
                int flSize = grades.getFlashCount();
                int flWidth = flSize * scale;
                int rpSize = grades.getRpCount();
                int rpWidth = rpSize * scale;
                int tpSize = grades.getTpCount();
                int tpWidth = tpSize * scale;
                canvas.drawText(grade, 5, paint.getTextSize() + count * rectHeight, paint);
                canvas.drawText("" + osSize, 30, paint.getTextSize() + count * rectHeight, paint);
                canvas.drawText("" + flSize, 60, paint.getTextSize() + count * rectHeight, paint);
                canvas.drawText("" + rpSize, 90, paint.getTextSize() + count * rectHeight, paint);
                canvas.drawText("" + tpSize, 120, paint.getTextSize() + count * rectHeight, paint);
                canvas.drawText("" + grades.getTotal(), 150, paint.getTextSize() + count * rectHeight, paint);
                canvas.drawRect(new RectF(center - (totalWidth / 2), count * rectHeight, center + (totalWidth / 2), (count + 1) * rectHeight), paint);
                //OS
                canvas.drawRect(new RectF(center - (osWidth / 2), count * rectHeight, center + (osWidth / 2), (count + 1) * rectHeight), osPaint);
                //FL
                canvas.drawRect(new RectF(center - (osWidth / 2) - (flWidth / 2), count * rectHeight, center - (osWidth / 2), (count + 1) * rectHeight), flPaint);
                canvas.drawRect(new RectF(center + (osWidth / 2), count * rectHeight, center + (osWidth / 2) + (flWidth / 2), (count + 1) * rectHeight), flPaint);
                //RP
                canvas.drawRect(new RectF(center - (osWidth / 2) - (flWidth / 2) - (rpWidth / 2), count * rectHeight, center - (osWidth / 2) - (flWidth / 2), (count + 1) * rectHeight), rpPaint);
                canvas.drawRect(new RectF(center + (osWidth / 2) + (flWidth / 2), count * rectHeight, center + (osWidth / 2) + (flWidth / 2) + (rpWidth / 2), (count + 1) * rectHeight), rpPaint);
                //TP
                canvas.drawRect(new RectF(center - (osWidth / 2) - (flWidth / 2) - (rpWidth / 2) - (tpWidth / 2), count * rectHeight, center - (osWidth / 2) - (flWidth / 2) - (rpWidth / 2), (count + 1) * rectHeight), tpPaint);
                canvas.drawRect(new RectF(center + (osWidth / 2) + (flWidth / 2) + (rpWidth / 2), count * rectHeight, center + (osWidth / 2) + (flWidth / 2) + (rpWidth / 2) + (tpWidth / 2), (count + 1) * rectHeight), tpPaint);
            }
        }
        super.onDraw(canvas);
    }

    private int getMax() {
        int max = 1;
        if (data != null) {
            for (Iterator iterator = data.iterator(); iterator.hasNext();) {
                GradeInfo grades = (GradeInfo)iterator.next();
                if (grades.getTotal() > max) {
                    max = grades.getTotal();
                }
            }
        }
        return max;
    }

}
