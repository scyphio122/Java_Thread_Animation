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
import java.nio.Buffer;
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
        this.setBounds(0, 0, 1280, 1024);

        gif_reader = (ImageReader)ImageIO.getImageReadersByFormatName("gif").next();
        this.shared_params = params_to_update;
        shared_params.histogram_data_red = new int[256];
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

       // g2.clearRect(0, 0, (int)this.getWidth(), (int)this.getHeight());
        synchronized(shared_params)
        {
            try
            {
                BufferedImage bgImage = gif_reader.read(shared_params.current_frame_number++);
                shared_params.frame = bgImage;
                RoundRectangle2D rectangle = new RoundRectangle2D.Float(0, 0, bgImage.getWidth(), (int) bgImage.getHeight(), 10, 10);
                Rectangle2D place_to_fill = new Rectangle2D.Double(0, 0,
                        bgImage.getWidth(), bgImage.getHeight());
                TexturePaint texture_paint = new TexturePaint(bgImage, place_to_fill);

                g2.setPaint((Paint) texture_paint);

                g2.fill(rectangle);

                if (shared_params.current_frame_number == number_of_frames)
                {
                    shared_params.current_frame_number = 0;
                }
            } catch (Exception e)
            {
                System.out.println("Blad watku gifa");
            }

            int width = 400;
            int height = 400;

            g2.setColor(new Color(238,238,238));
            g2.clearRect(width, height, width, -height);

            g2.setColor(Color.RED);
            /// Y axis
            g2.drawLine(width + 5, 0, width + 5, height-6);
            g2.drawLine(width + 5, 0, width, 10);
            g2.drawLine(width + 5, 0, width + 10, 10);

            /// X axis
            g2.drawLine(width + 5, height-6, 2*width, height-6);
            g2.drawLine(2*width, height-6, 2*width - 10, height - 11);
            g2.drawLine(2*width, height - 6, 2*width - 10, height - 1);

            /// Draw histogram
            for(int i=0; i<256; i++)
            {

                g2.drawRect(width + 5 + i, height-6, 1, -(shared_params.histogram_data_red[i]/120));
            }
            shared_params.notifyAll();
        }
    }
}

class Histogram implements Runnable
{
    Shared_Params shared_params;
    Histogram(int x, int y, int width, int height, Shared_Params updated_params)
    {
        this.shared_params = updated_params;
    }

    public void CalculateHistogram(BufferedImage frame)
    {
        for(int i=0; i< 256;i++)
        {
            shared_params.histogram_data_red[i] = 0;
        }
        for(int i=0; i<frame.getWidth(); i++)
        {
            for(int j=0; j<frame.getHeight(); j++)
            {
                int data = ((frame.getRGB(i,j)>>16) & 0xFF);
                shared_params.histogram_data_red[data]++;
            }
        }
    }


/*
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
            g2.drawRect(i*2, height-6, 2, height - 6 - (shared_params.histogram_data_red[i]/200));
        }

    }*/

    public void run()
    {
        do
        {
            synchronized(shared_params)
            {
                if(shared_params.current_frame_number != 0)
               {
                    CalculateHistogram(shared_params.frame);
               }
                try
                {
                   shared_params.wait();
                }catch (Exception e)
                {
                    System.out.println(e.getClass().getName());


                }
            }
        }while(true);


    }


}

class Shared_Params
{
   BufferedImage frame;
   int current_frame_number;
    int histogram_data_red[];
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
        histogram = new Histogram(500, 0, 512, 400, shared_params);
        gif_panel =  new Panel(shared_params);

        mainwindow.getContentPane().add(gif_panel);
        Thread gif_thread = new Thread(gif_panel);
        Thread histogram_thread = new Thread(histogram);
        gif_thread.start();
        histogram_thread.start();

        mainwindow.setVisible(true);
    }

}
