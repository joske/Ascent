package be.sourcery.ascent;

import java.util.Date;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.LinearLayout;


public class ScoreGraphActivity extends MyActivity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.score);
        setTitle(R.string.score);
        setupActionBar();
        XYMultipleSeriesDataset dataset = getDataset();
        XYSeries series = dataset.getSeriesAt(0);
        XYMultipleSeriesRenderer renderer = getRenderer(series.getMinX() - 0.5, series.getMaxX() + 0.5, series.getMinY() - 200, series.getMaxY() + 200);
        GraphicalView chartView = ChartFactory.getLineChartView(this, dataset, renderer);
        LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
        layout.addView(chartView);
    }

    private XYMultipleSeriesDataset getDataset() {
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        XYSeries series = new XYSeries("Score");
        dataset.addSeries(series);
        InternalDB db = new InternalDB(this);
        int firstYear = db.getFirstYear();
        int thisYear = new Date().getYear() + 1900;
        for(int year = firstYear; year < thisYear; year++) {
            int score = db.getScoreForYear(year);
            series.add(year, score);
        }
        series.add(thisYear, db.getScoreLast12Months());
        db.close();
        return dataset;
    }

    private XYMultipleSeriesRenderer getRenderer(double minX, double maxX, double minY, double maxY) {
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
        XYSeriesRenderer r = new XYSeriesRenderer();
        r.setColor(Color.WHITE);
        r.setPointStyle(PointStyle.SQUARE);
        r.setFillBelowLine(false);
        r.setFillPoints(true);
        renderer.addSeriesRenderer(r);
        renderer.setAxesColor(Color.DKGRAY);
        renderer.setLabelsColor(Color.WHITE);
        renderer.setDisplayChartValues(true);
        renderer.setChartValuesTextSize(25);
        renderer.setXAxisMin(minX);
        renderer.setXAxisMax(maxX);
        renderer.setYAxisMin(minY);
        renderer.setYAxisMax(maxY);
        renderer.setYTitle("Score");
        renderer.setXTitle("Year");
        return renderer;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
