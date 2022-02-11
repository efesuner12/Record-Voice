import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import java.io.PrintStream;
import java.util.Scanner;

public class Test
{
    private static String filePath;
    private static double duration;

    private static void setFilePath(String filePath)
    {
        Test.filePath = filePath;
    }

    private static void setDuration(double duration)
    {
        Test.duration = duration;
    }

    public static boolean test() throws Exception
    {
        Scanner scanner = new Scanner(System.in);

        String recordChoice = "";
        String confChoice = "";
        MicrophoneRecorder mr = new MicrophoneRecorder(null);

        while(!confChoice.equals("Y"))
        {
            recordChoice = "";

            while(!recordChoice.equals("Q"))
            {
                System.out.println("S - Start Q - Quit");
                System.out.print("--> ");

                recordChoice = (scanner.nextLine()).toUpperCase();

                switch (recordChoice)
                {
                    case ("S"):
                        mr.setFormat(new AudioFormat(8000f, 16, 1, true, false));
                        mr.start();
                        System.out.println("\nRecord Started...\n");
                        Thread.sleep(1);
                        break;
                    case ("Q"):
                        mr.stop();
                        break;
                }
            }

            Thread.sleep(2000);

            PrintStream stream = new PrintStream(System.out);
            double duration = mr.getDuration();
            setDuration(duration);
            stream.println("\nDURATION = " + duration);

            System.out.print("\nDo you confirm (Y/N): ");
            confChoice = scanner.nextLine().toUpperCase();
        }

        Thread.sleep(3000);
        String toSendFile = mr.saveToFile("tmp", AudioFileFormat.Type.WAVE, mr.getAudioInputStream());
        setFilePath(toSendFile);
        System.out.println("\nFILEPATH = " + toSendFile);

        if (toSendFile != null)
        {
            System.out.println("\nSuccessfully saved!");
            return true;
        }
        else
        {
            System.out.println("\nThere has been an error while saving the file!");
            return false;
        }
    }

    public static void main(String args[])
    {
        try
        {
            test();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
