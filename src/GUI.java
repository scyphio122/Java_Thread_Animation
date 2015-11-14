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

/**
 * Created by Konrad on 2015-11-14.
 */
class Panel extends JPanel
{
    Image               gif;
    ImageReader         gif_reader;
    ImageInputStream    gif_input_stream;
    IIOMetadata         gif_metadata;
    static int          number_of_frames;
    static int          current_frame_number;
    Timer timer;
    // BufferedImage       frames[];
    Panel(String file_name)
    {
        this.setBounds(0, 0, 480, 640);
       /* try
        {
            gif = ImageIO.read(new File(file_name));
        } catch (Exception e)
        {
            System.out.println("Blad otwarcia gif'a");
        }*/

        gif_reader = (ImageReader)ImageIO.getImageReadersByFormatName("gif").next();
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
        {}
       // frames = new BufferedImage[number_of_frames];
        timer = new Timer(100, new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                repaint();
            }
        });
        timer.start();
    }

    public void paintComponent(Graphics g)
    {

        Graphics2D g2 = (Graphics2D)g;
        BufferedImage bgImage;
       // g2.clearRect(0, 0, (int)this.getWidth(), (int)this.getHeight());
        try
        {
          bgImage  = gif_reader.read(current_frame_number++);
            RoundRectangle2D rectangle = new RoundRectangle2D.Float(0, 0, (int)bgImage
                    .getWidth(), (int)bgImage.getHeight(), 10, 10);
            Rectangle2D place_to_fill = new Rectangle2D.Double(0, 0,
                    bgImage.getWidth(), bgImage.getHeight());
            TexturePaint texture_paint = new TexturePaint(bgImage, place_to_fill);

            g2.setPaint((Paint)texture_paint);

            g2.fill(rectangle);

            if(current_frame_number == number_of_frames)
            {
                current_frame_number = 0;
            }

        }
        catch(Exception e)
        {

        }

    }
}

public class GUI
{
    JFrame mainwindow;
    Panel panel;
    private void SetSize(int width, int height)
    {
        int screen_width = (int)Toolkit.getDefaultToolkit().getScreenSize().getWidth();
        int screen_height = (int)Toolkit.getDefaultToolkit().getScreenSize().getWidth();

        this.mainwindow.setBounds((screen_width - width) / 2, 0, width, height);
    }

    public void CreateGUI()
    {
        mainwindow = new JFrame("Animacja");

        mainwindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        SetSize(1280, 1024);
        mainwindow.setLayout(null);

        panel =  new Panel("loading.gif");
        mainwindow.getContentPane().add(panel);
        mainwindow.setVisible(true);
    }

}
