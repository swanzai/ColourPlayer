/* Copyright (C) 2006 Michael Voong

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA. */

package controllers;

import helpers.StoppableThread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import models.Track;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import config.Constants;

public class ProfileWriter {
    private String compiledXml;
    private final int uniqueid;
    private int count;

    private URL url;
    protected QueuedProfileWriter profileWriter;

    // public static void main(String[] args) {
    // ProfileWriter writer = new ProfileWriter();
    // ArrayList<Track> tracks = new ArrayList<Track>();
    // Track track = new Track();
    // track.setArtist("Test");
    // track.setTitle("Test2");
    // track.setAlbum("TestAlbum");
    // tracks.add(track);
    // track.setColour(2, 3, 4);
    // writer.process(tracks);
    //
    // // writer.submit();
    // }

    public ProfileWriter(int uniqueid) {
        this.uniqueid = uniqueid;

        try {
            this.url = new URL(
                    "http://colourmusic.mublog.co.uk/profilewriter.php");
        } catch (MalformedURLException e) {
            System.out.println(e.getMessage());
        }

    }

    public String process(Track[] tracks) {
        count = 0;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();

            Element root = document.createElement("colourprofile");

            document.appendChild(root);

            Element userEl = document.createElement("user");
            root.appendChild(userEl);

            Element idEl = document.createElement("id");
            idEl.appendChild(document
                    .createTextNode(Integer.toString(uniqueid)));
            userEl.appendChild(idEl);

            Element versionEl = document.createElement("version");
            versionEl.appendChild(document.createTextNode(Constants.VERSION));
            userEl.appendChild(versionEl);

            Element tracksEl = document.createElement("tracks");
            root.appendChild(tracksEl);

            for (Track track : tracks) {
                if (track.getColour() != null && track.getArtist() != null
                        && track.getTitle() != null) {
                    Element trackEl = document.createElement("track");
                    tracksEl.appendChild(trackEl);

                    // create detail nodes for track
                    Element artistEl = document.createElement("artist");
                    artistEl.appendChild(document.createTextNode(track
                            .getArtist()));
                    trackEl.appendChild(artistEl);

                    if (track.getAlbum() != null) {
                        Element albumEl = document.createElement("album");
                        trackEl.appendChild(albumEl);
                        albumEl.appendChild(document.createTextNode(track
                                .getAlbum()));
                    }

                    Element titleEl = document.createElement("title");
                    trackEl.appendChild(titleEl);
                    titleEl.appendChild(document.createTextNode(track
                            .getTitle()));

                    Element colourEl = document.createElement("colour");
                    trackEl.appendChild(colourEl);

                    Element rEl = document.createElement("r");
                    rEl.appendChild(document.createTextNode(Integer
                            .toString(track.getColour()[0])));
                    colourEl.appendChild(rEl);
                    Element gEl = document.createElement("g");
                    colourEl.appendChild(gEl);
                    gEl.appendChild(document.createTextNode(Integer
                            .toString(track.getColour()[1])));
                    Element bEl = document.createElement("b");
                    colourEl.appendChild(bEl);
                    bEl.appendChild(document.createTextNode(Integer
                            .toString(track.getColour()[2])));

                    count++;

                }
            }

            // document.getDocumentElement().normalize();
            //
            // Create a transformer
            Transformer xformer = TransformerFactory.newInstance()
                    .newTransformer();

            // Set the public and system id
            xformer.setOutputProperty(OutputKeys.INDENT, "yes");

            Source source = new DOMSource(document);
            StringWriter writer = new StringWriter();
            Result result = new StreamResult(writer);
            xformer.transform(source, result);

            compiledXml = writer.toString();
            return compiledXml;
        } catch (ParserConfigurationException pce) {
            // Parser with specified options can't be built
            pce.printStackTrace();
        }

        catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }

        return null;
    }

    public synchronized void submit() {
        try {
            // Construct data
            String data = URLEncoder.encode("data", "UTF-8") + "="
                    + URLEncoder.encode(compiledXml, "UTF-8");
            if (profileWriter == null) {
                profileWriter = new QueuedProfileWriter();
                profileWriter.start();
            }
            profileWriter.queueData(data);
        } catch (UnsupportedEncodingException uee) {

        }
    }

    public String getCompiledXml() {
        return compiledXml;
    }

    public int getCount() {
        return count;
    }
    
    public void cleanUp() {
        if (profileWriter!=null && !profileWriter.isStopped()) {
            profileWriter.setStop();
        }
    }

    class QueuedProfileWriter extends StoppableThread {
        Queue<String> queue;

        public QueuedProfileWriter() {
            this.queue = new LinkedBlockingQueue<String>();
        }

        public void queueData(String xml) {
            queue.add(xml);
        }

        @Override
        public void run() {
            while (!isStopped()) {
                if (queue.size() > 0) {
                    try {
                        // Send data
                        URLConnection conn = url.openConnection();
                        conn.setDoOutput(true);
                        OutputStreamWriter wr = new OutputStreamWriter(conn
                                .getOutputStream());
                        wr.write(queue.peek());
                        wr.flush();

                        // Get the response
                        BufferedReader rd = new BufferedReader(
                                new InputStreamReader(conn.getInputStream()));
                        // String line;
                        // while ((line = rd.readLine()) != null) {
                        // System.out.println(line);
                        // }
                        wr.close();
                        rd.close();
                    } catch (IOException ioe) {
                        // put at end of queue as it failed
                        queue.add(queue.remove());
                        System.out
                                .println("Submit profile failed.. will try again");
                    }

                    // no problem.. remove from queue
                    System.out.println("Submit profile OK");
                    queue.remove();
                }
                sleepSilent(5000);
            }
        }
    }
}
