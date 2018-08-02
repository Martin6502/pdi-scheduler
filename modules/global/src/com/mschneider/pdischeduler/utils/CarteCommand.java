/*
 * PDI Scheduler - Scheduler Tool for Pentaho Carte Server
 *
 * Copyright (C) 2018 Martin Schneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.mschneider.pdischeduler.utils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

import static java.nio.charset.StandardCharsets.UTF_8;

@SuppressWarnings("unused")
public class CarteCommand {

    private static final Logger logger = LoggerFactory.getLogger(CarteCommand.class);

    @SuppressWarnings("Convert2Lambda")
    private static void disableHostnameVerifier() {
        // disable hostname verify, because it fails with given IP address
        javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(
                new javax.net.ssl.HostnameVerifier() {
                    public boolean verify(String hostname, javax.net.ssl.SSLSession sslSession) {
                        return true;
                    }
                }
                );
    }

    private static HashMap<String, String> getMapFromDOM(Document doc) {
        HashMap<String, String> map = new HashMap<>();
        if (doc != null) {
            Element element = doc.getDocumentElement();
            // System.out.println("root element: " + element.getNodeName());
            NodeList nodes1 = element.getChildNodes();
            for (int i = 0; i < nodes1.getLength(); i++) {
                Node currNode1 = nodes1.item(i);
                //System.out.println(currNode1.getNodeType() + " " + currNode1.getNodeName() + " = " + currNode1.getTextContent());
                if (currNode1.getNodeType() == Node.ELEMENT_NODE) {
                    map.put(element.getNodeName() + "_" + currNode1.getNodeName(), currNode1.getTextContent());
                    NodeList nodes2 = currNode1.getChildNodes();
                    for (int j = 0; j < nodes2.getLength(); j++) {
                        Node currNode2 = nodes2.item(j);
                        //System.out.println(currNode2.getNodeType() + " " + currNode2.getNodeName() + " = " + currNode2.getTextContent());
                        if (currNode2.getNodeType() == Node.ELEMENT_NODE) {
                            map.put(element.getNodeName() + "_" + currNode1.getNodeName() + "_" + currNode2.getNodeName(), currNode2.getTextContent());
                        }
                    }
                }
            }
        }
        return map;
    }

    private static String decode_logging_string(String logStrCData) {
        String logStr = null;
        try {
            String logStrBase64 = logStrCData.substring(0, logStrCData.length() - 3).substring(9);
            // byte[] logStrZip = Base64.getDecoder().decode(logStrBase64);
            byte[] logStrZip = Base64.getMimeDecoder().decode(logStrBase64);
            final GZIPInputStream gzipInput = new GZIPInputStream(new ByteArrayInputStream(logStrZip));
            final StringWriter stringWriter = new StringWriter();
            IOUtils.copy(gzipInput, stringWriter, UTF_8);
            logStr = stringWriter.toString();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return logStr;
    }

    private static HashMap<String, String> runCarteCommand(String user, String password, String urlString, String urlParameter) {
        logger.debug("runCarteCommand: " + urlString + " param: " + urlParameter);
        HashMap<String, String> map;
        try {
            disableHostnameVerifier();
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            String userPassword = user + ":" + password;
            conn.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString(userPassword.getBytes()));
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
            writer.write(urlParameter);
            writer.flush();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(conn.getInputStream());
            conn.disconnect();

            map = getMapFromDOM(doc);
            map.put("result", "OK");

            if (map.containsKey("jobstatus_logging_string")) {
                String logStr = decode_logging_string(map.get("jobstatus_logging_string"));
                map.put("jobstatus_logging_string", logStr);
            }

        } catch (Exception e) {
            map = new HashMap<>();
            map.put("result", "FATAL: " + e.getMessage());
            // e.printStackTrace();
        }
        // logger.debug("runCarteCommand: map = " + map);
        return map;
    }

    public static void printMap(HashMap<String, String> map) {
        for (HashMap.Entry<String, String> entry : map.entrySet()) {
            System.out.println(entry.getKey() + " = " + entry.getValue());
        }
    }

    public static String getJobName(String jobFullPath) {
        return FilenameUtils.getBaseName(jobFullPath);
    }

    @SuppressWarnings("unused")
    public static HashMap<String, String> carteStatus(String carteUser, String cartePassword, String carteBaseUrl) {
        String urlString = carteBaseUrl + "/kettle/status/";
        String urlParameter = "&xml=Y";
        return runCarteCommand(carteUser, cartePassword, urlString, urlParameter);
    }

    public static HashMap<String, String> jobExec(String carteUser, String cartePassword, String carteBaseUrl, String jobFullPath, String logLevel, String jobDir, String dataDir, String param) {
        String urlString = carteBaseUrl + "/kettle/executeJob/";
        String urlParameter = "job=" + jobFullPath + "&level=" + logLevel + "&pdiDataDir=" + dataDir + "&pdiJobDir=" + jobDir + (param != null ? "&" + param : "");
        return runCarteCommand(carteUser, cartePassword, urlString, urlParameter);
    }

    public static HashMap<String, String> jobExecRepos(String carteUser, String cartePassword, String carteBaseUrl, String reposId, String reposUser, String reposPassword, String jobFullPath, String logLevel, String dataDir, String param) {
        String urlString = carteBaseUrl + "/kettle/executeJob/";
        String urlParameter = "rep=" +  reposId + "&user=" +  reposUser + "&pass=" +  reposPassword + "&job=" + jobFullPath + "&level=" + logLevel + "&pdiDataDir=" + dataDir + (param != null ? "&" + param : "");
        return runCarteCommand(carteUser, cartePassword, urlString, urlParameter);
    }

    public static HashMap<String, String> jobStatus(String carteUser, String cartePassword, String carteBaseUrl, String jobName, String jobId) {
        String urlString = carteBaseUrl + "/kettle/jobStatus/";
        String urlParameter = "name=" + jobName + "&id=" + jobId + "&xml=Y";
        return runCarteCommand(carteUser, cartePassword, urlString, urlParameter);
    }

    public static HashMap<String, String> jobStop(String carteUser, String cartePassword, String carteBaseUrl, String jobName, String jobId) {
        String urlString = carteBaseUrl + "/kettle/stopJob/";
        String urlParameter = "name=" + jobName + "&id=" + jobId + "&xml=Y";
        return runCarteCommand(carteUser, cartePassword, urlString, urlParameter);
    }

    public static HashMap<String, String> jobRemove(String carteUser, String cartePassword, String carteBaseUrl, String jobName, String jobId) {
        String urlString = carteBaseUrl + "/kettle/removeJob/";
        String urlParameter = "name=" + jobName + "&id=" + jobId + "&xml=Y";
        return runCarteCommand(carteUser, cartePassword, urlString, urlParameter);
    }

    public static String createFullPath(String rootDir, String subDir, String fileName) {
        String fullPath = rootDir;
        if (subDir != null && subDir.length() > 0) {
            fullPath = fullPath + "/" + subDir;
        }
        if (fileName != null && fileName.length() > 0) {
            fullPath = fullPath + "/" + fileName;
        }
        return fullPath;
    }

    public static String createFullPath(String rootDir, String subDir) {
        String fullPath = rootDir;
        if (subDir != null && subDir.length() > 0) {
            fullPath = fullPath + "/" + subDir;
        }
        return fullPath;
    }

}
