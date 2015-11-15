import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;
import javax.tools.Tool;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.nio.BufferOverflowException;

/**
 * Created by Konrad on 2015-11-14.
 */
class Panel extends JPanel implements Runnable
{
    ImageReader         gif_reader;
    ImageInputStream    gif_input_stream;
    Shared_Params       shared_params;
    int                 number_of_frames;


    Panel(Shared_Params params_to_update)
    {
        this.setBounds(0, 0, 480, 640);

        gif_reader = (ImageReader)ImageIO.getImageReadersByFormatName("gif").next();
        this.shared_params = params_to_update;
        try
        {
            gif_input_stream = ImageIO.createImageInputStream(new File("color.gif"));
        }
        catch(Exception e)
        {
        }
        gif_reader.setInput(gif_input_stream, false);
        try
        {
            number_of_frames = gif_reader.getNumImages(true);
        }
        catch(Exception e)
        {

        }


    }

    public void run()
    {
        do
        {
            repaint();
            notify();
            try
            {
                Thread.sleep(100);
            } catch (Exception e)
            {
                System.out.println("Blad usypiania watku gifa");
            }
        }while(true);
    }

    public void paintComponent(Graphics g)
    {

        Graphics2D g2 = (Graphics2D)g;
        BufferedImage bgImage;
       // g2.clearRect(0, 0, (int)this.getWidth(), (int)this.getHeight());
        try
        {
            synchronized (shared_params)
            {
                bgImage = gif_reader.read(shared_params.current_frame_number++);
                shared_params.frame = bgImage;
                RoundRectangle2D rectangle = new RoundRectangle2D.Float(0, 0, (int) bgImage
                        .getWidth(), (int) bgImage.getHeight(), 10, 10);
                Rectangle2D place_to_fill = new Rectangle2D.Double(0, 0,
                        bgImage.getWidth(), bgImage.getHeight());
                TexturePaint texture_paint = new TexturePaint(bgImage, place_to_fill);

                g2.setPaint((Paint) texture_paint);

                g2.fill(rectangle);

                if (shared_params.current_frame_number == number_of_frames)
                {
                    shared_params.current_frame_number = 0;
                }
            }
        }
        catch(Exception e)
        {
        }
    }
}

class Histogram extends JPanel implements Runnable
{
    int histogram_data_red[];
    Shared_Params shared_params;
    Histogram(int x, int y, int width, int height, Shared_Params updated_params)
    {
        this.setBounds(x, y, width, height);
        histogram_data_red = new int[256];
        this.shared_params = updated_params;
    }

    public void CalculateHistogram(BufferedImage frame)
    {
        for(int i=0; i< 256;i++)
        {
            histogram_data_red[i] = 0;
        }
        for(int i=0; i<frame.getWidth(); i++)
        {
            for(int j=0; j<frame.getHeight(); j++)
            {
                histogram_data_red[frame.getRGB(i,j>>16)]++;
            }
        }
    }



    public void paintComponent(Graphics g)
    {
        Graphics2D g2 = (Graphics2D)g;
        int width = getWidth();
        int height = getHeight();

        /// X axis
        g2.drawLine(5, 0, 5, height-6);
        g2.drawLine(5, 0, 0, 10);
        g2.drawLine(5, 0, 10, 10);

        /// Y axis
        g2.drawLine(5, height-6, width, height-6);
        g2.drawLine(width, height-6, width - 10, height - 11);
        g2.drawLine(width, height - 6, width - 10, height - 1);


        for(int i=0; i<256; i++)
        {
            g2.drawRect(i*2, height-6, 2, height - 6 - (histogram_data_red[i]/200));
        }

    }

    public void run()
    {
        do
        {
            try
            {
                wait();
            }catch(Exception e)
            {}
            synchronized (shared_params)
            {
                CalculateHistogram(shared_params.frame);
                repaint();
            }
        }while(true);


    }


}

class Shared_Params
{
   BufferedImage frame;
   int current_frame_number;

    Shared_Params()
    {
        frame = new BufferedImage();
    }

}

public class GUI
{
    JFrame mainwindow;
    Panel gif_panel;
    Histogram histogram;

    Shared_Params shared_params;
    private void SetSize(int width, int height)
    {
        int screen_width = (int)Toolkit.getDefaultToolkit().getScreenSize().getWidth();
        int screen_height = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();

        this.mainwindow.setBounds((screen_width - width) / 2, 0, width, height);
    }

    public void CreateGUI()
    {
        mainwindow = new JFrame("Animacja");

        mainwindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        SetSize(1280, 1024);
        mainwindow.setLayout(null);

        shared_params = new Shared_Params();

        gif_panel =  new Panel(shared_params);
        mainwindow.getContentPane().add(gif_panel);
        Thread gif_thread = new Thread(gif_panel);
        gif_thread.start();
        histogram = new Histogram(500, 0, 512, 400, shared_params);
        mainwindow.getContentPane().add(histogram);
        histogram.repaint();

        Thread histogram_thread = new Thread(histogram);
        histogram_thread.start();

        mainwindow.setVisible(true);
    }

}
