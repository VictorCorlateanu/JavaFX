package student.examples.services;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import student.examples.domain.Currency;

public class BNMService implements ICurrencyService{
    private final String endpoint = "https://www.bnm.md/ro/official_exchange_rates";
    private final List<String> selectedCurrencies = new ArrayList<>(Arrays.asList(new String[]{"MDL", "USD", "EUR"}));
    private String activeCurrency = "MDL";
    private Map<String, Currency> currencies = new HashMap<>();
    private String lastAccessed = null;

    public Map<String, Currency> getData() throws IOException, ParserConfigurationException, SAXException {
        String currentDay = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        if(!currentDay.equals(lastAccessed)) {
            String endpointWithParams = endpoint + "?" + "get_xml=1" + "&" + "date=" + currentDay;
            URL url = new URL(endpointWithParams);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(connection.getInputStream()));

            NodeList valuteObjects = doc.getDocumentElement().getElementsByTagName("Valute");
            for (int i = 0; i < valuteObjects.getLength(); i++) {
                Element valuteElement = (Element)valuteObjects.item(i);
                int valuteNumCode  = Integer.parseInt(valuteElement.getElementsByTagName("NumCode").item(0).getTextContent().trim());
                String valuteCharCode = valuteElement.getElementsByTagName("CharCode").item(0).getTextContent().trim();
                float valuteValue  = Float.parseFloat(valuteElement.getElementsByTagName("Value").item(0).getTextContent().trim());
                if (selectedCurrencies.contains(valuteCharCode)){
                    Currency currency = new Currency(valuteCharCode, valuteNumCode, valuteValue);
                    currencies.put(valuteCharCode, currency);
                }
            }
            currencies.put("MDL", new Currency("MDL", 498, 1));
            lastAccessed = currentDay;
        }
        return currencies;
    }

    public List<String> getSelectedCurrencies() {
        return selectedCurrencies;
    }

    public String getActiveCurrency() {
        return activeCurrency;
    }

    public void setActiveCurrency(String activeCurrency) {
        this.activeCurrency = activeCurrency;
    }

}
