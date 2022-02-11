import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import javax.sound.sampled.*;

public class MicrophoneRecorder implements Runnable
{
    private AudioInputStream audioInputStream;
    private AudioFormat format;
    private double duration;

    public TargetDataLine line;
    public Thread thread;

    public MicrophoneRecorder(AudioFormat format)
    {
        super();
        this.format = format;
        duration = 0;
    }

    public AudioInputStream getAudioInputStream()
    {
        return audioInputStream;
    }

    public double getDuration()
    {
        return duration;
    }

    public void setDuration(Double duration)
    {
        this.duration = duration;
    }

    public void setFormat(AudioFormat format)
    {
        this.format = format;
    }

    public void start()
    {
        thread = new Thread(this);
        thread.start();
    }

    public void stop()
    {
        thread = null;
    }

    @Override
    public void run()
    {
        line = getTargetDataLineForRecord();

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final int frameSizeInBytes = format.getFrameSize();
        final int bufferLengthInFrames = line.getBufferSize() / 8;
        final int bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;
        final byte[] data = new byte[bufferLengthInBytes];

        int numBytesRead;
        line.start();

        while (thread != null)
        {
            if ((numBytesRead = line.read(data, 0, bufferLengthInBytes)) == -1)
                break;

            out.write(data, 0, numBytesRead);
        }

        // we reached the end of the stream. stop and close the line.
        line.stop();
        line.close();
        line = null;

        // stop and close the output stream
        try
        {
            out.flush();
            out.close();
        }
        catch (final IOException ex)
        {
            ex.printStackTrace();
        }

        // load bytes into the audio input stream for playback
        final byte[] audioBytes = out.toByteArray();
        final ByteArrayInputStream bais = new ByteArrayInputStream(audioBytes);
        audioInputStream = new AudioInputStream(bais, format,
                audioBytes.length / frameSizeInBytes);
        final long milliseconds = (long) ((audioInputStream.getFrameLength()
                * 1000) / format.getFrameRate());

        double duration = milliseconds / 1000.0;
        setDuration(duration);

        try
        {
            audioInputStream.reset();
        }
        catch (final Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private TargetDataLine getTargetDataLineForRecord()
    {
        TargetDataLine line;
        final DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        if (!AudioSystem.isLineSupported(info))
            return null;

        // get and open the target data line for capture.
        try
        {
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format, line.getBufferSize());
        }
        catch (final Exception ex)
        {
            return null;
        }

        return line;
    }

    public String saveToFile(String name, AudioFileFormat.Type fileType, AudioInputStream audioInputStream) throws IOException
    {
        if (null == name || null == fileType || audioInputStream == null)
            return null;

        String mainPath = new java.io.File(".").getCanonicalPath();
        String filePath = mainPath + "\\resources\\recordings\\";

        File myFile = new File(filePath + name + "." + fileType.getExtension());

        // reset to the beginning of the captured data
        try
        {
            audioInputStream.reset();
        }
        catch (Exception e)
        {
            return null;
        }

        int i = 0;
        while (myFile.exists())
        {
            String temp = filePath + "" + i + myFile.getName();
            myFile = new File(temp);
        }

        try
        {
            AudioSystem.write(audioInputStream, fileType, myFile);
        }
        catch (Exception ex)
        {
            return null;
        }

        return myFile.getAbsolutePath();
    }
}
