package com.tle.webtests.framework;

import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;

public class ScreenshotTaker {

    public static String takeScreenshot(WebDriver driver, File screenshotFolder, String filename, boolean chrome)
    {
        File screenshot = null;
        if( chrome )
        {
            screenshot = chromeScreenie(driver);
        }
        else
        {
            screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        }

        if( !screenshotFolder.exists() )
        {
            screenshotFolder.mkdirs();
        }

        File destScreenshot = new File(screenshotFolder, filename + ".png");
        int count = 2;
        while( destScreenshot.exists() )
        {
            destScreenshot = new File(screenshotFolder, filename + '(' + (count++) + ").png");
        }
        if( screenshot != null && !screenshot.renameTo(destScreenshot) )
        {
            boolean failed = false;
            try
            {
                FileUtils.copyFile(screenshot, destScreenshot);
                screenshot.delete();
                failed = !destScreenshot.exists();

            }
            catch( IOException e )
            {
                failed = true;
            }

            if( failed )
            {
                System.err.println("Failed to rename " + screenshot + " screenshot to " + destScreenshot);
                return null;
            }
        }

        System.err.println("Failure screenshot:" + destScreenshot);
        return destScreenshot.getName();
    }

    // See https://code.google.com/p/chromedriver/issues/detail?id=294
    private static File chromeScreenie(WebDriver driver)
    {
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0)");

        int totalWidth = ((Long) ((JavascriptExecutor) driver).executeScript("return document.documentElement.scrollWidth"))
                .intValue();
        int totalHeight = ((Long) ((JavascriptExecutor) driver).executeScript("return document.documentElement.scrollHeight"))
                .intValue();

        // Get the Size of the Viewport
        int viewportWidth = ((Long) ((JavascriptExecutor) driver)
                .executeScript("return document.documentElement.clientWidth")).intValue();
        int viewportHeight = ((Long) ((JavascriptExecutor) driver)
                .executeScript("return document.documentElement.clientHeight")).intValue();

        // Split the Screen in multiple Rectangles
        List<Rectangle> rectangles = Lists.newArrayList();
        // Loop until the Total Height is reached
        for( int i = 0; i < totalHeight; i += viewportHeight )
        {
            int newHeight = viewportHeight;
            // Fix if the Height of the Element is too big
            if( i + viewportHeight > totalHeight )
            {
                newHeight = totalHeight - i;
            }
            // Loop until the Total Width is reached
            for( int ii = 0; ii < totalWidth; ii += viewportWidth )
            {
                int newWidth = viewportWidth;
                // Fix if the Width of the Element is too big
                if( ii + viewportWidth > totalWidth )
                {
                    newWidth = totalWidth - ii;
                }

                // Create and add the Rectangle
                Rectangle currRect = new Rectangle(ii, i, newWidth, newHeight);
                rectangles.add(currRect);
            }
        }

        // Build the Image
        BufferedImage stitchedImage = new BufferedImage(totalWidth, totalHeight, BufferedImage.TYPE_3BYTE_BGR);
        // Get all Screenshots and stitch them together
        Rectangle pr = null;
        for( Rectangle cr : rectangles )
        {
            // Calculate the Scrolling (if needed)
            if( pr != null )
            {
                int xDiff = (cr.x + cr.width) - (pr.x + pr.width);
                int yDiff = (cr.y + cr.height) - (pr.y + pr.height);
                // Scroll
                ((JavascriptExecutor) driver).executeScript(MessageFormat.format("window.scrollBy({0}, {1})", xDiff,
                        yDiff));
                try
                {
                    Thread.sleep(200);
                }
                catch( InterruptedException e )
                {
                    // Don't care
                }
            }

            // Take Screenshot
            // byte[] screenshotBytes = ((TakesScreenshot)
            // driver).getScreenshotAs(OutputType.BYTES);
            File blah = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

            // Build an Image out of the Screenshot
            Image screenshotImage;
            try
            {
                screenshotImage = ImageIO.read(blah);
                // Calculate the Source Rectangle
                Rectangle sr = new Rectangle(viewportWidth - cr.width, viewportHeight - cr.height, cr.width, cr.height);

                // Copy the Image
                // stitchedImage = new BufferedImage(totalWidth, totalHeight,
                // BufferedImage.TYPE_3BYTE_BGR);
                Graphics g = stitchedImage.createGraphics();
                // g.drawImage(screenshotImage, cr.x, cr.y, sr.x, sr.y, null);
                // System.out.println("Rectangle " + cr);
                // System.out.println("Source: " + sr);
                // System.out.println(MessageFormat.format("cr  x:{0} y:{1} x2:{2} y2:{3}, sr x:{4} y:{5} x2:{6} y2:{7}",
                // cr.x, cr.y, cr.x + cr.width, cr.y + cr.height, sr.x, sr.y,
                // sr.x + sr.width, sr.y + sr.height));
                g.drawImage(screenshotImage, cr.x, cr.y, cr.x + cr.width, cr.y + cr.height, sr.x, sr.y,
                        sr.x + sr.width, sr.y + sr.height, null);

                // ImageIO.write(stitchedImage, "png",
                // File.createTempFile("screenshot", ".png"));

                // Set the Previous Rectangle
                pr = cr;
            }
            catch( IOException e )
            {
                // blah
            }
        }
        try
        {
            File tmpFile = File.createTempFile("screenshot", ".png");
            tmpFile.deleteOnExit();

            ImageIO.write(stitchedImage, "png", tmpFile);
            return tmpFile;
        }
        catch( IOException e )
        {
            // Boring
        }

        // Fail
        return null;
    }

}
