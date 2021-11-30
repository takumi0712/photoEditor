import java.io.IOException;
import javafx.fxml.FXML;
import javafx.stage.*;
import java.io.File;
import javafx.scene.image.*;
import java.awt.image.BufferedImage;
import javax.imageio.*;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.ResourceBundle;
import java.net.URL;
import javafx.fxml.Initializable;

public class MainController implements Initializable{
    
    private BufferedImage bufferedImg;
    private BufferedImage tempbufferedImg;
    private String filePath;
    private String extension;
    private WritableImage writableImage;
    
    
    @FXML
    Pane pane;
    @FXML
    Button saveBtn,saveAsBtn,resetBtn,inversionHrizontalBtn,inversionVerticalBtn,grayScaleBtn,warmFilterBtn,coolFilterBtn,luminousFilterBtn,sweetFilterBtn,executeAdjustmentBtn;
    @FXML
    Slider brightnessSlider,contrastSlider,redSlider,greenSlider,blueSlider;
    @FXML
    ImageView imageView;
    
    @Override
    public void initialize(URL location, ResourceBundle resource){
        //　ウィンドウの大きさに合わせて画像の大きさを大きくする
        imageView.fitHeightProperty().bind(pane.heightProperty());
        imageView.fitWidthProperty().bind(pane.widthProperty());
    }
 
//    ダイアログを表示させファイルを開く
    @FXML
    public void fileOpenAction() {
        FileChooser fc = new FileChooser();
        fc.setTitle("ファイル選択");
        fc.getExtensionFilters().addAll(
          new FileChooser.ExtensionFilter("イメージファイル", "*.jpg", "*.png")
        );
        // 初期フォルダをホームに設定
        fc.setInitialDirectory(new File(System.getProperty("user.home")));
        // ファイルダイアログ表示
        File file = fc.showOpenDialog(null);
        
        if(file != null) {
          System.out.println(file.getPath());
        }
        filePath = file.getPath();
        String fileName = file.getName();
        extension = fileName.substring(fileName.lastIndexOf(".")+1);
        try {
            tempbufferedImg = ImageIO.read(new File(filePath));
            bufferedImg = new BufferedImage(tempbufferedImg.getWidth(), tempbufferedImg.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
            bufferedImg.getGraphics().drawImage(tempbufferedImg, 0, 0, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        writableImage = SwingFXUtils.toFXImage(bufferedImg, null);
        imageView.setImage(writableImage);
        enableEditBtn();
        enableAdjustmentComponent();
        saveAsBtn.setDisable(false);
        saveBtn.setDisable(true);
        resetBtn.setDisable(true);
    }
    
//    編集を全てリセットする（未編集の画像を再度読み込む）
    @FXML
    public void resetEdit(){
        try {
            bufferedImg = ImageIO.read(new File(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        writableImage = SwingFXUtils.toFXImage(bufferedImg, null);
        imageView.setImage(writableImage);
        enableEditBtn();
        enableAdjustmentComponent();
        saveBtn.setDisable(true);
        resetBtn.setDisable(true);
    }
//    横方向に画像を反転する
    @FXML
    public void inversionHrizontal(){
        int x_s, x_e, y;
        int width,height;
        int color_s, color_e;
        
        width = bufferedImg.getWidth();
        height = bufferedImg.getHeight();
        
        for(y = 0;y<height;++y){
            x_s = 0;
            x_e = width -1;
            while( x_s < x_e){
                color_s = bufferedImg.getRGB(x_s, y);
                color_e = bufferedImg.getRGB(x_e, y);
                bufferedImg.setRGB(x_s, y, color_e);
                bufferedImg.setRGB(x_e, y, color_s);
                ++x_s;
                --x_e;
            }
        }
        
        afterEditAction(bufferedImg);
    }
//    縦方向に画像を反転する
    @FXML
    public void inversionVertical(){
        
        int x,y_s,y_e;
        int width,height;
        int color_s,color_e;
        
        width = bufferedImg.getWidth();
        height = bufferedImg.getHeight();
        
        for(x = 0;x<width;++x){
            y_s = 0;
            y_e = height -1;
            while( y_s < y_e){
                color_s = bufferedImg.getRGB(x, y_s);
                color_e = bufferedImg.getRGB(x, y_e);
                bufferedImg.setRGB(x, y_s, color_e);
                bufferedImg.setRGB(x, y_e, color_s);
                ++y_s;
                --y_e;
            }
        }
        
        afterEditAction(bufferedImg);
    }
//    画像を白黒にする
    @FXML
    public void grayScale(){
        int x,y;
        int width,height;
        int color,r,g,b;
        int p;
        int newcolor;
        
        width = bufferedImg.getWidth();
        height = bufferedImg.getHeight();
        for(y = 0;y < height;++y){
            for(x = 0;x < width;++x){
                color = bufferedImg.getRGB(x, y);
                 
                r = (color >> 16) & 0xff;
                g = (color >> 8) & 0xff;
                b = color & 0xff;
                
                p = (r + g + b)/3;
                
                r = p;
                g = p;
                b = p;
                
                newcolor = (r << 16)+(g<<8)+b; 
                
                bufferedImg.setRGB(x, y, newcolor);
            }
        }
        afterEditAction(bufferedImg);
        grayScaleBtn.setDisable(true);
    }
    
//    フィルター
    @FXML
    public void filter_warm(){
        colorBalanceAdjustment(25,-5,-15);
        warmFilterBtn.setDisable(true); 
    } 
    @FXML
    public void filter_cool(){
        colorBalanceAdjustment(-10,3,30);
        coolFilterBtn.setDisable(true);
    }
    @FXML
    public void filter_luminous(){
        colorBalanceAdjustment(25,25,25);
        contrastAdjustment(15);
        luminousFilterBtn.setDisable(true);
    }
    @FXML
    public void filter_sweet(){
        colorBalanceAdjustment(100,50,50);
        contrastAdjustment(-25);
        sweetFilterBtn.setDisable(true);
    }
    
    
//    画像の色を調整する
    public void colorBalanceAdjustment(double red,double green,double blue){
        int x,y;
        int width,height;
        int color,r,g,b;
        int newcolor;
        
        width = bufferedImg.getWidth();
        height = bufferedImg.getHeight();
        for(y = 0;y < height;++y){
            for(x = 0;x < width;++x){
                color = bufferedImg.getRGB(x, y);
                 
                r = (color >> 16) & 0xff;
                g = (color >> 8) & 0xff;
                b = color & 0xff;
                
                
                if(red>0){
                    if(r<256-red){
                        r+=red;
                    }
                    else{
                        r = 255;
                    }
                }
                else{
                    if(r>red*-1){
                        r+=red;
                    }
                    else{
                        r = 0;
                    }
                }
                if(green>0){
                    if(g<256-green){
                        g+=green;
                    }
                    else{
                        g = 255;
                    }
                }
                else{
                    if(g>green*-1){
                        g+=green;
                    }
                    else{
                        g = 0;
                    }
                }
                if(blue>0){
                    if(b<256-blue){
                        b+=blue;
                    }
                    else{
                        b = 255;
                    }
                }
                else{
                    if(b>blue*-1){
                        b+=blue;
                    }
                    else{
                        b = 0;
                    }
                }
                
                newcolor = (r << 16)+(g<<8)+b; 
                
                bufferedImg.setRGB(x, y, newcolor);
            }
        }
        afterEditAction(bufferedImg);
        
    }
    
//    画像の明暗差を調整する
    public void contrastAdjustment(double strength){
        int x,y;
        int width,height;
        int color,r,g,b;
        int newcolor;
        
        width = bufferedImg.getWidth();
        height = bufferedImg.getHeight();
        for(y = 0;y < height;++y){
            for(x = 0;x < width;++x){
                color = bufferedImg.getRGB(x, y);
                 
                r = (color >> 16) & 0xff;
                g = (color >> 8) & 0xff;
                b = color & 0xff;
                
                if(strength>0){
                    if(r<128){
                        r-=strength;
                        if(r<0){
                            r=0;
                        }
                    }
                    else{
                        r+=strength;
                        if(r>255){
                            r=255;
                        }
                    }

                    if(g<128){
                        g-=strength;
                        if(g<0){
                            g=0;
                        }
                    }
                    else{
                        g+=strength;
                        if(g>255){
                            g=255;
                        }
                    }

                    if(b<128){
                        b-=strength;
                        if(b<0){
                            b=0;
                        }
                    }
                    else{
                        b+=strength;
                        if(b>255){
                            b=255;
                        }
                    }
                }
                else{
                   if(r<128){
                        r-=strength;
                        if(r>128){
                            r=128;
                        }
                    }
                    else{
                        r+=strength;
                        if(r<128){
                            r=128;
                        }
                    }

                   if(g<128){
                        g-=strength;
                        if(g>128){
                            g=128;
                        }
                    }
                    else{
                        g+=strength;
                        if(g<128){
                            g=128;
                        }
                    }
                   
                   if(b<128){
                        b-=strength;
                        if(b>128){
                            b=128;
                        }
                    }
                    else{
                        b+=strength;
                        if(b<128){
                            b=128;
                        }
                    }
                }
                
                newcolor = (r << 16)+(g<<8)+b; 
                
                bufferedImg.setRGB(x, y, newcolor);
            }
        }
        afterEditAction(bufferedImg);
        
    }
    
//    スライダーの数値を元に画像を編集する
    @FXML
    public void executeAdjustment(){
        colorBalanceAdjustment(brightnessSlider.getValue(),brightnessSlider.getValue(),brightnessSlider.getValue());
        contrastAdjustment(contrastSlider.getValue());
        colorBalanceAdjustment(redSlider.getValue(),0,0);
        colorBalanceAdjustment(0,greenSlider.getValue(),0);
        colorBalanceAdjustment(0,0,blueSlider.getValue());
        disableAdjustmentComponent();
    }
    
//    画像を名前を付けて保存する
    @FXML
    public void saveAsImage(){
        FileChooser fc = new FileChooser();
        fc.setTitle("ファイル選択");
        fc.getExtensionFilters().addAll(
          new FileChooser.ExtensionFilter("イメージファイル","*.png")
        );
        fc.setInitialDirectory(new File(System.getProperty("user.home")));
        File file = fc.showSaveDialog(null);
        filePath = file.getPath();
        try {
            ImageIO.write(bufferedImg, "png", new File(filePath));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        saveBtn.setDisable(true);
        resetBtn.setDisable(true); 
        
    }
    
//    画像を上書き保存する
    @FXML
    public void saveImage(){
        try {
            ImageIO.write(bufferedImg, extension , new File(filePath));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        saveBtn.setDisable(true);
    }
    
//    編集済みの画像を表示させ保存、リセットを有効にする
    public void afterEditAction(BufferedImage bufferedImg){
        writableImage = SwingFXUtils.toFXImage(bufferedImg, null);
        imageView.setImage(writableImage);
        saveBtn.setDisable(false);
        resetBtn.setDisable(false);
    }
    
//    スライダーを初期値に設定し有効にする
    public void enableAdjustmentComponent(){
        brightnessSlider.setDisable(false);
        brightnessSlider.setValue(0);
        contrastSlider.setDisable(false);
        contrastSlider.setValue(0);
        redSlider.setDisable(false);
        redSlider.setValue(0);
        greenSlider.setDisable(false);
        greenSlider.setValue(0);
        blueSlider.setDisable(false);
        blueSlider.setValue(0);
        executeAdjustmentBtn.setDisable(false);
    }
    
//    スライダーを無効にする
    public void disableAdjustmentComponent(){
        brightnessSlider.setDisable(true);
        contrastSlider.setDisable(true);
        redSlider.setDisable(true);
        greenSlider.setDisable(true);
        blueSlider.setDisable(true);
        executeAdjustmentBtn.setDisable(true);
    }
    
//    編集を有効にする
    public void enableEditBtn(){
        inversionHrizontalBtn.setDisable(false);
        inversionVerticalBtn.setDisable(false);
        grayScaleBtn.setDisable(false);
        warmFilterBtn.setDisable(false);
        coolFilterBtn.setDisable(false);
        luminousFilterBtn.setDisable(false);
        sweetFilterBtn.setDisable(false);
    }
    
}
