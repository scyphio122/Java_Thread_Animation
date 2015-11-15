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
        shared_params.histogram_data_green = new int[256];
        shared_params.histogram_data_blue = new int[256];
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

            int width = 800;
            int gif_width = 350;
            int height_r = 200;
            int height_g = 410;
            int height_b = 620;

            g2.setColor(new Color(238,238,238));
            g2.fillRect(gif_width, 0, width, 800);

            g2.setColor(Color.RED);
            /// Y axis
            g2.drawLine(gif_width + 5, 0, gif_width + 5, height_r-6);
            g2.drawLine(gif_width + 5, 0, gif_width, 10);
            g2.drawLine(gif_width + 5, 0, gif_width + 10, 10);

            /// X axis
            g2.drawLine(gif_width + 5, height_r-6, gif_width + width, height_r-6);
            g2.drawLine(gif_width + width, height_r-6, gif_width + width - 10, height_r - 11);
            g2.drawLine(gif_width + width, height_r - 6, gif_width + width - 10, height_r - 1);

            g2.setColor(Color.GREEN);
            /// Y axis
            g2.drawLine(gif_width + 5, height_r + 10, gif_width + 5, height_g-6);
            g2.drawLine(gif_width + 5, height_r + 10, gif_width, height_r + 20);
            g2.drawLine(gif_width + 5, height_r + 10, gif_width + 10, height_r + 20);

            /// X axis
            g2.drawLine(gif_width + 5, height_g-6, gif_width + width, height_g-6);
            g2.drawLine(gif_width + width, height_g-6, gif_width + width - 10, height_g - 11);
            g2.drawLine(gif_width + width, height_g - 6, gif_width + width - 10, height_g - 1);

            g2.setColor(Color.BLUE);
            /// Y axis
            g2.drawLine(gif_width + 5, height_g + 10, gif_width + 5, height_b-6);
            g2.drawLine(gif_width + 5, height_g + 10, gif_width, height_g + 20);
            g2.drawLine(gif_width + 5, height_g + 10, gif_width + 10, height_g + 20);

            /// X axis
            g2.drawLine(gif_width + 5, height_b-6, gif_width + width, height_b-6);
            g2.drawLine(gif_width + width, height_b-6, gif_width + width - 10, height_b - 11);
            g2.drawLine(gif_width + width, height_b - 6, gif_width + width - 10, height_b - 1);


            /// Draw histogram
            g2.setColor(Color.RED);
            for(int i=0; i<256; i++)
            {
                int size = -(shared_params.histogram_data_red[i]/200);
                g2.drawRect(gif_width + 5 + i*3, height_r-6, 3, size);
            }

            g2.setColor(Color.GREEN);
            for(int i=0; i<256; i++)
            {
                int size = -(shared_params.histogram_data_green[i]/200);
                g2.drawRect(gif_width + 5 + i*3, height_g-6, 3, size);
            }

            g2.setColor(Color.BLUE);
            for(int i=0; i<256; i++)
            {
                int size = -(shared_params.histogram_data_blue[i]/200);
                g2.drawRect(gif_width + 5 + i*3, height_b-6, 3, size);
            }
            shared_params.notifyAll();
        }
    }
}

class Histogram implements Runnable
{
    Shared_Params shared_params;
    Histogram(Shared_Params updated_params)
    {
        this.shared_params = updated_params;
    }

    public void CalculateHistogram(BufferedImage frame)
    {
        for(int i=0; i< 256;i++)
        {
            shared_params.histogram_data_red[i] = 0;
            shared_params.histogram_data_blue[i] = 0;
            shared_params.histogram_data_green[i] = 0;
        }
        for(int i=0; i<frame.getWidth(); i++)
        {
            for(int j=0; j<frame.getHeight(); j++)
            {
                shared_params.histogram_data_red[(frame.getRGB(i,j)>>16) & 0xFF]++;
                shared_params.histogram_data_green[(frame.getRGB(i,j)>>8) & 0xFF]++;
                shared_params.histogram_data_blue[frame.getRGB(i,j) & 0xFF]++;
            }
        }
    }

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
    int histogram_data_green[];
    int histogram_data_blue[];
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
        histogram = new Histogram(shared_params);
        gif_panel =  new Panel(shared_params);

        mainwindow.getContentPane().add(gif_panel);
        Thread gif_thread = new Thread(gif_panel);
        Thread histogram_thread = new Thread(histogram);
        gif_thread.start();
        histogram_thread.start();

        mainwindow.setVisible(true);
    }

}
