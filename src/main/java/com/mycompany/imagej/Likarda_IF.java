import fiji.util.gui.GenericDialogPlus;
import ij.*;
import ij.gui.*;
import ij.io.FileSaver;
import ij.measure.*;
import ij.plugin.*;
import ij.plugin.filter.ParticleAnalyzer;
import ij.process.*;
import ij.text.*;
import inra.ijpb.morphology.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.imageio.*;
import javax.swing.* ;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.table.AbstractTableModel;

   
/**
 
 * @version          1.0 12-19-2020
 * @author           Jon Whitlow
 *
*/


public class Likarda_IF
    implements ActionListener, MouseListener, PlugIn, TableModelListener {
    public String[] headings = new String[] { "Sample", "%expression", "Hoechst", "Stain", "Circ1", "Circ2"};


    ImagePlus img;        
    ImageCanvas canvas;  
    ImageStack stack;    
   

    JFrame frame;        
    static JFrame instance;// Only one copy of the plugin can run at
                         



    JTable dataTable ;     // This is the table where all the data will

    JComboBox forkBox;     // Box(list) that contains the fork points
                           // that have occurred in the file.
    int row;               // Row of the table that will contain the data
                           // of the next working point.

    /* Work variables. */

    int Xold;              // Coordinates of the point where the mouse has
    int Yold;              // been pressed.
    boolean doMeasurement; // Flag to say the the data point is valid.
    File morphoFile;       // File that contains the morphology data.
    java.awt.List header;  // Header of the file that contains the data.


public String path = "/Users/jonwhitlow/Dropbox (Likarda, LLC)/Jon Whitlow/imagej/201002NHP14-2   DE  Day 3/C3_1_ ( FOXA2  488  )  Hst  only    x4.png";//System.getProperty("user.dir");
public String userInputScale = "1.0";
public String circ = "0";
public String minSize = "0";
public boolean saveImgs = false;
public boolean running = false;
public String imgDir = "";
public ImagePlus mask;
public String hstDir = "";
public String saveName = "test.png";
public File dir2;
public int count = 0;
public ArrayList<ImagePlus> imgs = new ArrayList<ImagePlus>();
public float hArea;
public float hCirc;
public float sArea;
public float sCirc;
final ExecutorService exec = Executors.newFixedThreadPool(1);

private Thread segmentationThread = null;
    public void run(String arg) {


if (instance!=null) {
   instance.toFront();
   return;
}

doMeasurement = false;

frame = new JFrame();
instance = frame;
frame.setSize(600,240) ;
frame.setTitle("Quantitative IF - Likarda");
frame.getContentPane().setLayout(new BorderLayout());


JPanel northPanel = new JPanel();
northPanel.setLayout(new GridLayout(1,5));
addButton("Open",northPanel);
addButton("Settings",northPanel);
addButton("Open Overlay",northPanel);
addButton("Help",northPanel);
addButton("Quit",northPanel);
frame.getContentPane().add(northPanel, BorderLayout.NORTH);

/*JPanel southPanel = new JPanel();
southPanel.setLayout(new GridLayout(1,2));

start = new JButton("Start");
start.addActionListener(this);
southPanel.add(start);
stop = new JButton("Stop");
stop.addActionListener(this);
southPanel.add(stop);

stop.setEnabled(false);
frame.getContentPane().add(southPanel, BorderLayout.SOUTH);*/


LikardaTableModel dataModel = new LikardaTableModel();
dataModel.addTableModelListener(this);
dataTable = new JTable(dataModel);
dataTable.setFont(new Font("SansSerif", Font.PLAIN, 12));
    TableColumn previousColumn = dataTable.getColumn("Circ2");

for (int i = 0; i <headings.length;++i){
TableColumn col1 = dataTable.getColumn(headings[i]);
col1.setPreferredWidth(250);
}


forkBox = new JComboBox();
forkBox.setEditable(false);
previousColumn.setPreferredWidth(150);



frame.getContentPane().add(
  new JScrollPane(dataTable), BorderLayout.CENTER);

frame.pack();
frame.setVisible(true);

row = 0;          
String defaultMorphoFile = Prefs.getString(Prefs.DIR_IMAGE);
if (defaultMorphoFile == null) {
   defaultMorphoFile = System.getProperty("user.dir") ;
}
morphoFile = new File(defaultMorphoFile);
// Create an empty file header.
header = new java.awt.List();

    }

    /******************************************************************
     *        Actions performed when the buttons are pressed          *
     ******************************************************************/

    public void actionPerformed(ActionEvent e) {
String label = e.getActionCommand();
if (label==null)
   return;

if (label=="Open") {
if (running==false){
if(openHoechstDialog()){
if (openStainDialog()){

runThis();
++count;

}
}
}
}
if (label=="Open Overlay") {
int selRow = dataTable.getSelectedRow();
if (count<1){
	//do nuthin
}else{
	ImagePlus showPrev = imgs.get(selRow);
	ImagePlus showPrev2 = showPrev.duplicate();
	showPrev2.setTitle(showPrev.getTitle());
	showPrev2.show();
}
}

if (label=="Settings") {

IJ.showMessage("not quite that fancy yet");
}

if (label=="Help") {
   // Find the current working directory: it is assumed that
   // ImageJ is in this directory.
  // String urlString = "file:" + System.getProperty("user.dir") + "/plugins/morpho_doc/index.html";
   //new NeuronMorphoHelpViewer(urlString);
   IJ.showMessage("ask Jon");
   return;
};

if (label=="Quit") {
         instance = null;
   frame.dispose();
   return;
}

/*****************************************
* South panel - Measurements operations.
*****************************************/
 
/* We need an image. */

img = WindowManager.getCurrentImage();
if (img==null) {
  // IJ.beep();
   IJ.showStatus("No image");
   return;
}



if (label=="Start") {


   return;
}


if (label=="Stop") {



   return;
}

    }


    public void mousePressed(MouseEvent e) {
if (e.isShiftDown()) {

}
    }



public void mouseReleased(MouseEvent e) {
if (e.isShiftDown()) {
IJ.showMessage("h");



   }
   dataTable.repaint();

}

    public void mouseExited(MouseEvent e) {}

    public void mouseClicked(MouseEvent e) {}

    public void mouseEntered(MouseEvent e) {}


public void tableChanged(TableModelEvent e) {
if (e.getColumn() == 1) {
   forkBox.removeItem(dataTable.getValueAt(e.getFirstRow(),0));

}
}



void addButton(String label, JPanel panel) {
JButton b = new JButton(label);
b.addActionListener(this);
panel.add(b);
}

public boolean openHoechstDialog(){
GenericDialogPlus gd=new GenericDialogPlus("Choose Hoechst stain image");
gd.addFileField("hoechst", path);
gd.showDialog();
if (gd.wasCanceled()){
return false;
}else{
hstDir = gd.getNextString();
File dir = new File(imgDir);


dir2 = new File(dir.getParent() + "/mask/");

boolean isMade = dir2.mkdir();
return true;
}
}
public boolean openStainDialog(){
GenericDialogPlus gd=new GenericDialogPlus("Choose secondary stain image");
gd.addFileField("stain", path);
gd.showDialog();
if (gd.wasCanceled()){
return false;
}else{
imgDir = gd.getNextString();
String[] selFilePath = imgDir.split("/");
String nam = selFilePath[selFilePath.length-1];
File dir = new File(imgDir);
saveName = dir.getName();
dir2 = new File(dir.getParent() + "/mask/");
boolean isMade = dir2.mkdir();
return true;

}
}


public void runThis(){

final Thread oldThread = segmentationThread;
Thread newThread = new Thread() {
public void run()
{

if (null != oldThread)
{
try {
IJ.log("Waiting for old task to finish...");
oldThread.join();
}
catch (InterruptedException ie) { /*IJ.log("interrupted");*/ }
}


try{
running = true;
float[][] data = new float[2][2];
float[][] newData;
float[][] newData2;

newData= loopA("hst",saveName,hstDir,true,0,0,0);
newData2=loopA("img",saveName,imgDir,true,0,0,1);    
//ImagePlus diff = maskDiff(newData2,newData);
//diff.show();
float c = 100*sArea/hArea;
String p = Float.toString(c);
dataTable.setValueAt(saveName,count-1,0);
dataTable.setValueAt(p,count-1,1);
dataTable.setValueAt(Float.toString(hArea),count-1,2);
dataTable.setValueAt(Float.toString(sArea),count-1,3);
dataTable.setValueAt(Float.toString(hCirc),count-1,4);

ImagePlus hst = IJ.openImage(hstDir);
olay(mask, hst);
hst.setTitle(saveName);

saveName = saveName.replaceAll("[\\\\/:*?\"<>|]", "");

new FileSaver(hst).saveAsPng(dir2 + "/" + saveName);
running = false;
}
catch( Exception ex )
{
ex.printStackTrace();
//IJ.log( "Error while runing watershed: " + ex.getMessage() );
}
catch( OutOfMemoryError err )
{
err.printStackTrace();
IJ.log( "Error: the plugin run out of memory. Please use a smaller input image." );
}



segmentationThread = null;
}

};
segmentationThread = newThread;
newThread.start();

}


public void showResult(float[][] data, String sampleName){

// dataTable.setValueAt(Float.toString(data[0][1]),count-1,5);

}
public ImagePlus maskDiff(float[][] imp1, float[][] imp2){
	ImageProcessor ip1 = new FloatProcessor(imp1);
	ImageProcessor ip2 = new FloatProcessor(imp2);
	ImagePlus diff1 = new ImagePlus("1",ip1);
	ImagePlus diff2 = new ImagePlus("2",ip2);
	diff1 = convertTo8bit(diff1);
	diff2 = convertTo8bit(diff2);
	//nmmndiff1.show();
	//diff2.show();
	ImageCalculator ic = new ImageCalculator();
	ImagePlus imp3 = ic.run("Subtract create", diff1, diff2);
	return imp3;
}
public void olay(ImagePlus imp1, ImagePlus imp2){
	
	//imp1.show();
	
	imp2.setTitle("hst");
	IJ.run(imp1,"Label Boundaries","");
	ImagePlus imp3 = WindowManager.getCurrentImage();
	imp3.setTitle("mask");
	//ImageProcessor iz = imp1.getProcessor();
	//iz.findEdges();
	
	//IJ.run(imp1,"8-bit Color","256");
	IJ.run(imp1,"Red","");
	imp2.show();
IJ.run(imp2, "Binary Overlay", "reference=hst binary=mask overlay=Red");
imp2.close();
imp1.close();

int width = imp2.getWidth();
int height = imp2.getHeight();
int width2 = imp1.getWidth();
int height2 = imp1.getHeight();

if ((width == width2) && (height == height2)){
}else{
IJ.showMessage("Warning: dimensions of Hoechst stain image ("+ Integer.toString(width2) + "x"+Integer.toString(height2)+ ")" +" are different from the dimensions of the selected stain image ("+ Integer.toString(width) + "x"+Integer.toString(height)+ "). Please adjust images to the same size and discard this measurement.");
}

ImagePlus ip2 = imp2.duplicate();
imgs.add(ip2);

//imgs.show();
return;
}

public float[][] loopA(String change, String nam, String dirI, boolean saveImgs,float minSize, float circ,int order){
float[][] imp_ = processImg1(dirI,saveImgs);
//int colz = imp_.length;
//int rowz = imp_[0].length;
//float[][][] arrg = new float[colz][rowz][count];
//arrg[count] = imp_;
ImageProcessor ip2 = new FloatProcessor(imp_);
ImagePlus imps2 =new ImagePlus(nam,ip2);
imps2.setTitle(change);
conv8(imps2);
mask = imps2;
//float[] data = analyzeArea(imps2, Float.parseFloat(minSize), Float.parseFloat(circ));
ResultsTable rt = new ResultsTable();
ParticleAnalyzer pa = new ParticleAnalyzer(ParticleAnalyzer.SHOW_NONE, ParticleAnalyzer.AREA +ParticleAnalyzer.CIRCULARITY, rt, minSize, Double.POSITIVE_INFINITY);
//Analyzer.setRedirectImage(img1);
pa.analyze(imps2);

float[] data = rt.getColumn(0);
float[] datacirc = rt.getColumn(ResultsTable.CIRCULARITY);
float[] res = new float[2];
if (order == 0){
	hArea = sumArray(data);
	hCirc = sumArray(datacirc)/datacirc.length;
}else{
	sArea = sumArray(data);
	sCirc = sumArray(datacirc)/datacirc.length;

}
rt.reset();

return imp_;
}


public float[] analyzeArea(ImagePlus imp, float minSize, float circ){
ResultsTable rt = new ResultsTable();
ParticleAnalyzer pa = new ParticleAnalyzer(ParticleAnalyzer.SHOW_NONE, ParticleAnalyzer.AREA +ParticleAnalyzer.CIRCULARITY, rt, minSize, Double.POSITIVE_INFINITY);
//Analyzer.setRedirectImage(img1);
pa.analyze(imp);

float[] data = rt.getColumn(0);
float[] datacirc = rt.getColumn(ResultsTable.CIRCULARITY);
float[] res = new float[2];
res[0] = sumArray(data);

res[1] = sumArray(datacirc)/datacirc.length;
rt.reset();
return res;
////String n = Float.toString(sum);
//IJ.showMessage(n);
}

public void saveOverlay(ImagePlus imp, File dir){

if (dir.isDirectory()){

new FileSaver(imp).saveAsPng(dir + "/" + saveName);
}
}
public float sumArray(float[] arr){
float sum = 0;
for (int i=0;i<arr.length;++i){
sum = sum + arr[i];
}
return sum;
}
void conv8(ImagePlus img){
ImageConverter ic = new ImageConverter(img);
ic.convertToGray8();
img.updateAndDraw();
return;
}



public float[][] processImg1(String imgUrl, boolean saveFiles){

if (imgUrl==null){
return null;
}else {
ImagePlus imp = IJ.openImage(imgUrl);
String title = imp.getTitle();
imp.setTitle("ref");
ImagePlus imp2 = dupe("new",imp);
imp2.show();
conv8(imp2);
IJ.run(imp2,"Despeckle","");
IJ.run(imp2, "Subtract Background...", "rolling=15 sliding"); //"disable

IJ.run(imp2,"Unsharp Mask...","radius=3 mask=0.7");
IJ.run(imp2,"Smooth","");

IJ.run(imp2,"Invert","");


ImagePlus thresh=autoThresh(imp2);

IJ.run(imp2, "Invert", "");
imp2 = morph(imp2,0,1);
imp2=morph(imp2,2,1);
IJ.run(imp2,"Fill Holes","");
IJ.run(imp2, "Distance Transform Watershed", "distances=[Quasi-Euclidean (1,1.41)] output=[32 bits] normalize dynamic=1 connectivity=4");

ImagePlus impnew =WindowManager.getCurrentImage();
imp2.close();
IJ.run(impnew, "3-3-2 RGB", "");
IJ.run(impnew,"Kill Borders","");
ImagePlus impnew3 =WindowManager.getCurrentImage();
conv8(impnew3);
impnew3=setthresh(impnew3,1);
impnew3.setTitle(title);
IJ.run(impnew3,"Set Scale...", "distance=1 known="+userInputScale+" pixel=1 unit=pixel");


ImageProcessor img = impnew3.getProcessor();
closeAllWindowsBehind();
return img.getFloatArray();

}
}
public ImagePlus morph(ImagePlus imp_old,int proc, int rad){
//proc= 0  dilation; proc = 1 erosion
ImageProcessor img = imp_old.getProcessor();
ImagePlus morphImg;
if (proc==0){
ImageProcessor dil  = Morphology.dilation(img,Strel.Shape.OCTAGON.fromDiameter(rad));
morphImg = new ImagePlus("dilation",dil);
morphImg.updateAndDraw();
return morphImg;
} else if (proc==1){
ImageProcessor dil  = Morphology.erosion(img,Strel.Shape.OCTAGON.fromDiameter(rad));
morphImg = new ImagePlus("erosion",dil);
morphImg.updateAndDraw();
return morphImg;
} else{
ImageProcessor dil  = Morphology.closing(img,Strel.Shape.OCTAGON.fromDiameter(rad));
morphImg = new ImagePlus("closing",dil);
morphImg.updateAndDraw();
return morphImg;
}
}

void closeAllWindowsBehind(){
int[] k= WindowManager.getIDList();
for (int z=0;z<k.length;++z){
ImagePlus impt = WindowManager.getImage(k[z]);
impt.changes=false;
closeImage(impt);
}
return;
}

 public void closeImage(ImagePlus imp) {
        if (imp==null) {
            IJ.noImage();
            return;
        }
        imp.close();
     
    }

public ImagePlus setthresh(ImagePlus imp_1, int lower){
    ImageProcessor ip = imp_1.getProcessor();
    ImagePlus newImp = new ImagePlus(imp_1.getTitle()+" th1-255",ip);
    ip.setThreshold(lower,255, ImageProcessor.NO_LUT_UPDATE);
    newImp.setProcessor(ip);
    newImp.show();
    newImp.updateAndDraw();
    IJ.run(newImp,"Convert to Mask","");
    newImp.updateAndDraw();
    return newImp;
}

public ImagePlus autoThresh(ImagePlus imp_1){
ImageProcessor ip_th = imp_1.getProcessor();
ImagePlus imp = new ImagePlus(imp_1.getTitle()+ " thresh",ip_th);
imp.show();
imp.updateAndDraw();
imp = convertTo8bit(imp);
imp.updateAndDraw();
ip_th.setAutoThreshold(AutoThresholder.Method.Otsu, true);
imp.setProcessor(ip_th);
IJ.run(imp,"Convert to Mask","");
imp.updateAndDraw();
return imp;
}

public ImagePlus convertTo8bit(ImagePlus imp_1){
ImagePlus imp = new ImagePlus(imp_1.getTitle()+"8bit",imp_1.getProcessor());
ImageConverter ic = new ImageConverter(imp);
ic.convertToGray8();
imp.updateAndDraw();
return imp;
}


public ImagePlus dupe(String title,ImagePlus imp_2){
    ImagePlus temp =WindowManager.getTempCurrentImage();
    WindowManager.setTempCurrentImage(imp_2);
    ImagePlus newWin= imp_2.duplicate();
    newWin.setTitle(title);
    newWin.getProcessor().setColorModel(imp_2.getProcessor().getColorModel());
    WindowManager.setTempCurrentImage(temp);
    return newWin;
}





    public String suggestedPrevious() {

return null;
    }

    /* The plugin dies if the window is closed. */

    public void processWindowEvent(WindowEvent e) {
// super.processWindowEvent(e);
if (e.getID()==WindowEvent.WINDOW_CLOSING) {
   instance = null;
}
    }
}


class LikardaTableModel extends AbstractTableModel {
 
     String[] headings = new String[] { "Sample", "%expression", "Hoechst", "Stain", "Circ1", "Circ2"};

   
    int tableLength = 10000;
    int tableWidth =  6; // headings.length();
   
    // Body of the table.

    String[][] data = new String[tableLength][tableWidth];
   
    // These methods always need to be implemented.

    public int getColumnCount() { return tableWidth;}

    public int getRowCount() { return tableLength;}

    public Object getValueAt(int row, int col) {
if (data[row][col] != null ) {
   return data[row][col];
} else {
   return "";
}
    }
   
    public void setValueAt(Object value, int row, int col) {
data[row][col] = new String( (String) value);
fireTableCellUpdated(row,col);
    }

    public String getColumnName(int column) {
return headings[column];
    }

    public boolean isCellEditable(int row, int col) {
if ( (col == 1) || (col == 6) ) {
   return true;
} else {
   return false;
}
    }

}