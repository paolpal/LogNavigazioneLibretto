
import com.thoughtworks.xstream.*;
import java.io.*;
import java.net.*;
import java.time.*;
import javax.xml.*;
import javax.xml.parsers.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import javax.xml.validation.*;
import org.w3c.dom.*;
import org.xml.sax.*;


public class LogNavigazioneLibretto {
    
    private static boolean convalidaEvento(String e){
        try {
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document d = db.parse(new ByteArrayInputStream(e.getBytes()));
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema s = sf.newSchema(new StreamSource(new File("./schema/eventolog.xsd")));
            s.newValidator().validate(new DOMSource(d));
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            return false;
        }
        return true;
    }
    
    private static String riceviEvento(ServerSocket ser){
        String evento="";
        try(
            Socket sock = ser.accept();
            DataInputStream din = new DataInputStream(sock.getInputStream());
            ){
            evento = din.readUTF();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return evento;
    }
    
    public static void main(String[] args) {
        System.out.println(Instant.now().toString().replaceAll("[TZ]", " ")+" - Server di Log avviato");
        try(
                ServerSocket ser = new ServerSocket(8080);
            ){
            while (true) {
                String evento = riceviEvento(ser);
                if(convalidaEvento(evento)){
                    aggiungiEvento(evento);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static void aggiungiEvento(String evento) {
        XStream xs = new XStream();
        xs.registerConverter(new InstantConverter());
        EventoNavigazioneLibretto e = (EventoNavigazioneLibretto)xs.fromXML(evento);
        System.out.println(e.istante.toString().replaceAll("[TZ]", " ")+" - ricevuto evento "+e.evento+" da: "+e.ipClient);
        try (
                FileWriter writer = new FileWriter(new File("./log/log.xml"), true);
            ){
            writer.write(evento+"\n\n");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
}
