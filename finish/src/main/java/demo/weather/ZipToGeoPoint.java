package demo.weather;

import java.util.logging.Logger;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;

public class ZipToGeoPoint {
	private final static Logger log = Logger.getLogger("demo.weather");
	// e.g.
	// https://public.opendatasoft.com/api/records/1.0/search/?dataset=us-zip-code-latitude-and-longitude&facet=state&facet=timezone&facet=dst&q=78613
	private static final String ZIP_TO_GEO_SERVICE = "https://public.opendatasoft.com/api/records/1.0/search/?dataset=us-zip-code-latitude-and-longitude&facet=state&facet=timezone&facet=dst&q=";

	private String zipcode;

	public ZipToGeoPoint(String zipcode) {
		this.zipcode = zipcode;
	}

	/**
	 * Returns a geopoint associated with the zip code.
	 * 
	 * @return A String geopoint (latitude,longitude)
	 */
	public String resolve() {
		String targetUrl = ZIP_TO_GEO_SERVICE + zipcode;
		log.fine("Resolve zip to geo URL: " + targetUrl);

		JsonObject urlResponse = getJsonObjectFromURL(targetUrl);
		return findGeopoint(urlResponse);
	}

	private JsonObject getJsonObjectFromURL(String targetUrl) {
		Client client = RestClientFactory.getRestClient();

		Response response = client.target(targetUrl).request().get();
		JsonObject jObj = response.readEntity(JsonObject.class);
		log.fine("Response object from zip to geo: " + jObj);

		response.close();
		client.close();

		return jObj;
	}

	/**
	 * Find the geopoint for this zip within the json object.
	 * 
	 * @param jObj
	 * @return
	 */
	private String findGeopoint(JsonObject jObj) {
		JsonArray records = jObj.getJsonArray("records");
		for (int i = 0; i < records.size(); i++) {
			JsonObject zipObj = records.getJsonObject(i);
			JsonObject fields = zipObj.getJsonObject("fields");
			if (zipcode.equals(fields.getString("zip"))) {
				return asGeoPointString(fields);
			}
		}

		log.severe("Could not resolve zipcode " + zipcode + " to a geopoint, returning 0,0");
		return "0,0";
	}

	/**
	 * @param fields
	 * @return String of "latitude,longitude"
	 */
	private String asGeoPointString(JsonObject fields) {
		JsonNumber latitude = fields.getJsonNumber("latitude");
		JsonNumber longitude = fields.getJsonNumber("longitude");
		log.info("fields.latitude:" + latitude);
		log.info("fields.longitude:" + longitude);
		return latitude + "," + longitude;
	}

}
