package app;

import java.util.TreeMap;

import javafx.application.Application;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import sun.nio.ch.sctp.SctpStdSocketOption;

public class ganttdemo extends Application {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
        Application.launch(args);

	}

	@Override
	public void start(Stage primaryStage) throws Exception {
        Group root = new Group();
        Canvas canvas = new Canvas(300, 250);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        drawShapes(gc);
        Canvas chartCanvas = new Canvas(600, 600);
        paintChart(chartCanvas);
        root.getChildren().add(chartCanvas);
        //root.getChildren().add(canvas);
        primaryStage.setScene(new Scene(root));

        primaryStage.setTitle("Gantt demo");
		primaryStage.show();
		
	}

	private void paintChart(Canvas chartCanvas) {
		// TODO Auto-generated method stub
		// vertical proportions: headHeight, chartHeight, footHeight
		// as a proportion of canvas height
		Double canvasHeight = chartCanvas.getHeight();
		Double headHeight = 0.1;
		Double chartHeight = 0.8;
		Double footHeight = 0.1;
		// proportion of bar space filled with bar. Remainder is padding split 
		// above and below the bar
		Double barHeightRatio = 0.25;
		
		// horizontal proportions: leftMargin, labelCol, chartWidth, rightMargin
		Double canvasWidth = chartCanvas.getWidth();
		Double leftMargin = 0.02;
		Double labelCol = 0.18;
		Double chartWidth = 0.7;
		Double rightMargin = 0.1;
		Double minorTickLength = .01;
		Double majorTickLength = .02;
		Double blockLabelHeight = .03;
		Double taskBarCornerArcWidth = 5.0;
		Double taskBarCornerArcHeight = 5.0;
		int preferredInterval = 7;
		Color labelColor = Color.DARKCYAN;
		Color axisColor = Color.DARKCYAN;
		Color chartColor = Color.LIGHTCYAN;
		
		TreeMap<Integer, Task> tasks;
		tasks = getTaskDemoData();
		String title = "Chart Title";
		//Integer qtyTicks = 30;
		//Integer majorTickInterval = 5;
		
		Double projectDuration = getDuration(tasks);
		TimeScale timeScale = new TimeScale(projectDuration, preferredInterval);
		//qtyTicks = projectDuration.intValue();
        GraphicsContext gc = chartCanvas.getGraphicsContext2D();
        gc.setLineWidth(1);
        gc.setTextBaseline(VPos.CENTER);
       //gc.strokeRoundRect(160, 60, 30, 30, 10, 10);
        
        //title
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.setFill(labelColor);
        gc.fillText(title, canvasWidth / 2, canvasHeight * headHeight / 2);

        // chart area
        Double chartX1 = leftMargin + labelCol;
        Double chartY1 = headHeight;
        Double chartX2 = chartWidth;
        Double chartY2 = chartHeight;
        gc.setFill(chartColor);
        gc.fillRect(canvasWidth * chartX1, canvasHeight * chartY1, 
        		canvasWidth * chartX2, canvasHeight * chartY2);
        
		// horizontal axis
		Double axisV = (1-footHeight);
		Double axisHStart = leftMargin + labelCol;
		Double axisHEnd = axisHStart + chartWidth;
 
        // axis
        gc.setStroke(axisColor);
        gc.strokeLine(axisHStart * canvasWidth, axisV * canvasHeight
        		, axisHEnd * canvasWidth, 
        		axisV * canvasHeight);
        gc.strokeLine(axisHStart * canvasWidth, headHeight * canvasHeight,
        		axisHStart * canvasWidth, axisV * canvasHeight);
        // ticks
    	Double tickY1 = canvasHeight * axisV;
        for (int i = 0; i <= timeScale.getMinorDivisions(); ++i) {
        	Double tickX1 = canvasWidth * (axisHStart + ((axisHEnd - axisHStart) 
        			* i / timeScale.getMinorDivisions()));
        	Double tickX2 = tickX1;
        	Double tickLength;
        	if(i % timeScale.getInterval() == 0) {
        		tickLength = majorTickLength;
        	} else {
        		tickLength = minorTickLength;
        	}
        	Double tickY2 = canvasHeight * (axisV + tickLength);
        	gc.strokeLine(tickX1, tickY1, tickX2, tickY2);
        	
        }
        // blocks
        long blockInterval = timeScale.getInterval();
    	Double blockY1 = canvasHeight * headHeight;
    	Double blockYcl = canvasHeight * (headHeight - (blockLabelHeight / 2));
        gc.setFill(labelColor);
        for (int i = 0; i <= timeScale.getMinorDivisions(); ++i) {
        	Double blockX1 = canvasWidth * (axisHStart + ((axisHEnd - axisHStart) 
        			* (i) / timeScale.getMinorDivisions()));
        	String blockText = Integer.toString(i);
        	if (i % blockInterval == 0) {
                gc.fillText(blockText, blockX1, 
                		blockYcl);
        	}
        }
        // grid lines
        gc.setFill(Color.DARKCYAN);
        gc.setLineDashes(5, 10);
        Double vGridY1 = canvasHeight * headHeight;
        Double vGridY2 = canvasHeight * axisV;
        for (int i = 1; i < timeScale.getMajorDivisions(); ++i) {
        	Double vGridX = canvasWidth * (axisHStart + (
        			i * chartWidth /timeScale.getMajorDivisions()));
        	gc.strokeLine(vGridX, vGridY1, vGridX, vGridY2);
        	
        }
        
        // task bars
        gc.setTextAlign(TextAlignment.LEFT);
        Double barHeight = chartHeight / tasks.size() * barHeightRatio ;
        gc.setFill(Color.DARKCYAN);
        gc.setLineDashes(null);
        for(int i = 0; i < tasks.values().size(); ++i) {
        	Task task = (Task) tasks.values().toArray()[i] ;
        	Double x = axisHStart + 
        			chartWidth * ((task.getPredecessor() == null) ? 0 : 
        		durationWithPredecessor(tasks.get(task.getPredecessor()), tasks) / 
        		timeScale.getDuration());
        	Double chartTop = headHeight;
        	Double ycl = chartTop + chartHeight * (1.0 / (1 + tasks.size()) * (i + 1));
        	Double y = (ycl - (barHeight / 2)) ;
        	Double w = (chartWidth *(task.getDuration() / timeScale.getDuration()));
        	Double h = ((barHeight));
        	//System.out.printf("top %s; bar %s; cl %s; l%s\n", chartTop, barHeight, ycl, task.getLabel());
        	//System.out.printf("x1 %s; y1 %s; x2 %s; y2 %s, H %s; W %s\n",
        	//		x, y, w, h, canvasHeight, canvasWidth);
            gc.fillRoundRect(x * canvasWidth, y * canvasHeight, 
            		w * canvasWidth, h * canvasHeight, 
            		taskBarCornerArcWidth, taskBarCornerArcHeight);
            // task labels
            gc.fillText(task.getLabel(), leftMargin * canvasWidth, 
            		ycl * canvasHeight);
            

        }
		
	}

	private Double getDuration(TreeMap<Integer, Task> tasks) {
		Double totalDuration = 0.0;
		for (Task task : tasks.values()) {
			totalDuration = Math.max(totalDuration, durationWithPredecessor(task, tasks));
		}
		return totalDuration;
	}
	
	private Double durationWithPredecessor(Task task, TreeMap<Integer, Task> tasks) {
		if (task.getPredecessor() == null) {
			return task.getDuration();
		}
		Task predecessor = tasks.get(task.getPredecessor());
		return task.duration + durationWithPredecessor(predecessor, tasks);
	}

	private TreeMap<Integer, Task> getTaskDemoData() {
		TreeMap<Integer, Task> tasks = new TreeMap<>();
		
		Task firstTask = new Task(10, "the first task");
		firstTask.setDuration(30.0);
		
		Task secondTask = new Task(20, "the second task");
		secondTask.setDuration(20.0);
		secondTask.setPredecessor(10);
		
		Task thirdTask = new Task(30, "3rd task");
		thirdTask.setDuration(30.0);
		thirdTask.setPredecessor(10);

		tasks.put(firstTask.getIndex(),firstTask);
		tasks.put(secondTask.getIndex(), secondTask);
		tasks.put(thirdTask.getIndex(), thirdTask);
		return tasks;
	}

	private void drawShapes(GraphicsContext gc) {
        gc.setFill(Color.GREEN);
        gc.setStroke(Color.BLUE);
        gc.setLineWidth(5);
        gc.strokeLine(40, 10, 10, 40);
        gc.fillOval(10, 60, 30, 30);
        gc.strokeOval(60, 60, 30, 30);
        gc.fillRoundRect(110, 60, 30, 30, 10, 10);
        gc.strokeRoundRect(160, 60, 30, 30, 10, 10);
        gc.fillArc(10, 110, 30, 30, 45, 240, ArcType.OPEN);
        gc.fillArc(60, 110, 30, 30, 45, 240, ArcType.CHORD);
        gc.fillArc(110, 110, 30, 30, 45, 240, ArcType.ROUND);
        gc.strokeArc(10, 160, 30, 30, 45, 240, ArcType.OPEN);
        gc.strokeArc(60, 160, 30, 30, 45, 240, ArcType.CHORD);
        gc.strokeArc(110, 160, 30, 30, 45, 240, ArcType.ROUND);
        gc.fillPolygon(new double[]{10, 40, 10, 40},
                       new double[]{210, 210, 240, 240}, 4);
        gc.strokePolygon(new double[]{60, 90, 60, 90},
                         new double[]{210, 210, 240, 240}, 4);
        gc.strokePolyline(new double[]{110, 140, 110, 140},
                          new double[]{210, 210, 240, 240}, 4);
		gc.fillText("Hello", 150, 150);
	}
	class Task {
		private Integer index;
		private String label;
		private Double duration; // say this is calendar days
		private Integer predecessor = null;
		
		public Task (Integer index, String label) {
			this.index = index;
			this.label = label;
		}
		public Integer getPredecessor() {
			return predecessor;
		}
		public void setPredecessor(Integer predecessor) {
			this.predecessor = predecessor;
		}
		public Integer getIndex() {
			return index;
		}
		public void setIndex(Integer index) {
			this.index = index;
		}
		public String getLabel() {
			return label;
		}
		public void setLabel(String label) {
			this.label = label;
		}
		public Double getDuration() {
			return duration;
		}
		public void setDuration(Double duration) {
			this.duration = duration;
		}
		
	}
	
	class TimeScale {
		private long axisDuration;
		private long interval;
		private long span;
		
		
		public TimeScale(Double projectDuration, long preferredInterval) {
			setAxisDuration(projectDuration, preferredInterval);
		}
		
		public long getMajorDivisions() {
			return axisDuration / interval;
		}

		public long getDuration() {
			return axisDuration;
		}

		public long getInterval() {
			return interval;
		}

		public long getMinorDivisions() {
			// for longer durations this may not be appropriate...
			return axisDuration;
		}

		private void setAxisDuration(Double projectDuration, long preferredInterval) {
			interval = preferredInterval;
			Double rawSpan = projectDuration / interval;
			span = Math.round(rawSpan);
			if (span < rawSpan) {
				span++;
			}
			axisDuration = interval * span;
			
		}
		
	}
}
