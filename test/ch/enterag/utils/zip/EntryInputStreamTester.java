/*== EntryInputStreamTester.java =======================================
Tests EntryInputStream.
Version     : $Id: EntryInputStreamTester.java 51 2016-09-07 17:11:10Z hartwigthomas $
Application : Zip64File
Description : Tests EntryInputStream.
------------------------------------------------------------------------
Copyright  : Enter AG, Zurich, Switzerland, 2011
Created    : 08.02.2011, Hartwig Thomas
------------------------------------------------------------------------
The class ch.enterag.utils.zip.EntryInputStreamTester is free software;
you can redistribute it and/or modify it under the terms of the GNU General
Public License version 2 or later as published by the Free Software Foundation.

ch.enterag.utils.zip.EntryInputStreamTester is distributed in the hope 
that it will be useful, but WITHOUT ANY WARRANTY; without even the 
implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.

If you have a need for licensing ch.enterag.utils.zip.EntryInputStreamTester
without some of the restrictions specified in the GNU General Public License,
it is possible to negotiate a different license with the copyright holder.
======================================================================*/
package ch.enterag.utils.zip;

import java.io.*;
import static org.junit.Assert.*;
import org.junit.*;
import ch.enterag.utils.io.*;
import ch.enterag.utils.lang.Execute;

/*====================================================================*/
/** Tests EntryInputStream.
 @author Hartwig Thomas
 */
public class EntryInputStreamTester
{
  /** buffer size for I/O */
  private final static int iBUFFER_SIZE = 8192;
  /** number of buffers for more than 65 KB */
  private final static int iMODERATE_BUFFERS = 200;
  /** global file comment */
  private final static String sZIP_COMMENT = "a global ZIP file comment";
  /** zip file produced by pkzipc */
  private String m_sPkZipFile = null;
  /** temp directory */
  private final static String sTEMP_DIRECTORY = SpecialFolder.getUserTemp()+File.separator+"temp";

  /*------------------------------------------------------------------*/
  /** create file of moderate size.
   * @param fileModerate file to be created.
   * @throws FileNotFoundException if the folder could not be found.
   * @throws IOException if an I/O error occurred. 
   */
  private void createModerate(File fileModerate)
    throws FileNotFoundException, IOException
  {
    System.out.println("writing moderate file");
    FileOutputStream fos = new FileOutputStream(fileModerate);
    byte[] buffer = new byte[iBUFFER_SIZE];
    for (int iBuffer = 0; iBuffer < iMODERATE_BUFFERS; iBuffer++)
    {
      /* fill the buffer with random characters */
      for (int i = 0; i < buffer.length; i++)
      {
        if (i % 76 == 75)
          buffer[i] = 0x0A;
        else
          buffer[i] = (byte)(32+(int)Math.floor(96*Math.random()));
      }
      fos.write(buffer);
    }
    fos.close();
  } /* createModerate */
  
  /*------------------------------------------------------------------*/
  /* (non-Javadoc)
   @see junit.framework.TestCase#setUp()
   */
  @Before
  public void setUp() throws Exception
  {
    /* create a file of moderate length in Temp and zip it */
    /* temp directory */
    File fileTemp = new File(sTEMP_DIRECTORY);
    if (!fileTemp.exists())
      fileTemp.mkdirs();
    /* 2.a) write more than 65 KB random file */
    String sModerateFile = fileTemp.getAbsolutePath()+"\\moderate.txt";
    File fileModerate = new File(sModerateFile);
    if (!fileModerate.exists())
      createModerate(fileModerate);
    /* 5. zip everything using pkzipc */
    File fileZip = new File(fileTemp.getAbsolutePath()+"\\pktest.zip");
    if (!fileZip.exists())
    {
      String[] asCommand = new String[] 
      {
        "pkzipc.exe", /* must be ZIP64-capable and in path */
        "-add=all",
        "-attr=all",
        "-dir=specify",
        "-silent=normal",
        "-header="+sZIP_COMMENT, /* the whole thing is quoted by Runtime */
        fileZip.getAbsolutePath(),
        fileTemp.getAbsolutePath()+"\\*"
      };
      Execute exec = Execute.execute(asCommand);
      int iExitCode = exec.getResult();
      if (iExitCode != 0)
        fail("pkzipc exit code: "+String.valueOf(iExitCode));
    }
    m_sPkZipFile = fileZip.getAbsolutePath();
    fileModerate.delete();
  } /* setUp */

  /*------------------------------------------------------------------*/
  /* (non-Javadoc)
   @see junit.framework.TestCase#tearDown()
   */
  @After
  public void tearDown() throws Exception
  {
    if (m_sPkZipFile != null)
    {
      File fileZip = new File(m_sPkZipFile);
      fileZip.delete();
    }
  } /* tearDown */
  
  /**
   * Test method for {@link ch.enterag.utils.zip.Zip64File#openEntryInputStream(java.lang.String)}.
   */
  @Test
  public void testEntryInputStream()
  {
    try
    {
      Zip64File zf = new Zip64File(m_sPkZipFile,true);
      EntryInputStream eis = zf.openEntryInputStream("moderate.txt");
      eis.close();
      zf.close();
    }
    catch(FileNotFoundException fnfe) { fail(fnfe.getClass().getName()+": "+fnfe.getMessage()); }
    catch(IOException ie) { fail(ie.getClass().getName()+": "+ie.getMessage()); }
  } /* testEntryInputStream */

  /**
   * Test method for {@link ch.enterag.utils.zip.EntryInputStream#available()}.
   */
  @Test
  public void testAvailable()
  {
    try
    {
      Zip64File zf = new Zip64File(m_sPkZipFile,true);
      EntryInputStream eis = zf.openEntryInputStream("moderate.txt");
      int iAvailable = eis.available();
      if (iAvailable != iMODERATE_BUFFERS*iBUFFER_SIZE)
        fail("Wrong available count!");
      eis.close();
      zf.close();
    }
    catch(FileNotFoundException fnfe) { fail(fnfe.getClass().getName()+": "+fnfe.getMessage()); }
    catch(IOException ie) { fail(ie.getClass().getName()+": "+ie.getMessage()); }
  } /* testAvailable */

  /**
   * Test method for {@link ch.enterag.utils.zip.EntryInputStream#read(byte[],int,int)}.
   */
  @Test
  public void testReadByteArrayIntInt()
  {
    try
    {
      Zip64File zf = new Zip64File(m_sPkZipFile,true);
      EntryInputStream eis = zf.openEntryInputStream("moderate.txt");
      byte[] buf = new byte[iBUFFER_SIZE];
      int iRead = eis.read(buf,0,234);
      if (iRead != 234)
        fail("Wrong read count!");
      int iAvailable = eis.available();
      if (iAvailable != iMODERATE_BUFFERS*iBUFFER_SIZE - 234)
        fail("Wrong available count!");
      eis.close();
      zf.close();
    }
    catch(FileNotFoundException fnfe) { fail(fnfe.getClass().getName()+": "+fnfe.getMessage()); }
    catch(IOException ie) { fail(ie.getClass().getName()+": "+ie.getMessage()); }
  } /* testReadByteArrayIntInt */

  /**
   * Test method for {@link ch.enterag.utils.zip.EntryInputStream#read(byte[])}.
   */
  @Test
  public void testReadByteArray()
  {
    try
    {
      Zip64File zf = new Zip64File(m_sPkZipFile,true);
      EntryInputStream eis = zf.openEntryInputStream("moderate.txt");
      byte[] buf = new byte[iBUFFER_SIZE];
      int iRead = eis.read(buf);
      if (iRead > iBUFFER_SIZE)
        fail("Wrong read count!");
      int iAvailable = eis.available();
      if (iAvailable != iMODERATE_BUFFERS*iBUFFER_SIZE - iRead)
        fail("Wrong available count!");
      long lRead = 0;
      while(iRead != -1)
      {
        lRead = lRead + iRead;
        if (iRead != iBUFFER_SIZE)
          System.out.println("Unexpected read size "+String.valueOf(iRead)+" at "+String.valueOf(lRead)+"!");
        iRead = eis.read(buf);
      }
      eis.close();
      zf.close();
    }
    catch(FileNotFoundException fnfe) { fail(fnfe.getClass().getName()+": "+fnfe.getMessage()); }
    catch(IOException ie) { fail(ie.getClass().getName()+": "+ie.getMessage()); }
  } /* testReadByteArray */

  /**
   * Test method for {@link ch.enterag.utils.zip.EntryInputStream#read()}.
   */
  @Test
  public void testRead()
  {
    try
    {
      Zip64File zf = new Zip64File(m_sPkZipFile,true);
      EntryInputStream eis = zf.openEntryInputStream("moderate.txt");
      int iRead = eis.read();
      if (iRead < 0)
        fail("Unexpected EOF!");
      if (iRead == 0)
        fail("Unexpected 0 read!");
      int iAvailable = eis.available();
      if (iAvailable != iMODERATE_BUFFERS*iBUFFER_SIZE - 1)
        fail("Wrong available count!");
      eis.close();
      zf.close();
    }
    catch(FileNotFoundException fnfe) { fail(fnfe.getClass().getName()+": "+fnfe.getMessage()); }
    catch(IOException ie) { fail(ie.getClass().getName()+": "+ie.getMessage()); }
  } /* testRead */

  /**
   * Test method for {@link ch.enterag.utils.zip.EntryInputStream#skip(long)}.
   */
  @Test
  public void testSkip()
  {
    try
    {
      Zip64File zf = new Zip64File(m_sPkZipFile,true);
      EntryInputStream eis = zf.openEntryInputStream("moderate.txt");
      int iRead = eis.read();
      if (iRead < 0)
        fail("Unexpected EOF!");
      if (iRead == 0)
        fail("Unexpected 0 read!");
      int iAvailable = eis.available();
      if (iAvailable != iMODERATE_BUFFERS*iBUFFER_SIZE - 1)
        fail("Wrong available count!");
      long lSkipped = eis.skip(234);
      if (lSkipped != 234)
        fail("Wrong skipped count!");
      iAvailable = eis.available();
      if (iAvailable != iMODERATE_BUFFERS*iBUFFER_SIZE - 1 - lSkipped)
        fail("Wrong available count!");
      eis.close();
      zf.close();
    }
    catch(FileNotFoundException fnfe) { fail(fnfe.getClass().getName()+": "+fnfe.getMessage()); }
    catch(IOException ie) { fail(ie.getClass().getName()+": "+ie.getMessage()); }
  } /* testSkip */

} /* class EntryInputStreamTester */
