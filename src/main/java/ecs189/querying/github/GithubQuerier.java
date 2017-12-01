package ecs189.querying.github;

import ecs189.querying.Util;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Vincent on 10/1/2017.
 */
public class GithubQuerier {

    private static final String BASE_URL = "https://api.github.com/users/";

    public static String eventsAsHTML(String user) throws IOException, ParseException {
        List<JSONObject> response = getEvents(user);
        StringBuilder sb = new StringBuilder();
        sb.append("<div>");
        for (int i = 0; i < response.size(); i++) {
            JSONObject event = response.get(i);
            // Get event type
            String type = event.getString("type");
            // Get created_at date, and format it in a more pleasant style
            String creationDate = event.getString("created_at");
            SimpleDateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'");
            SimpleDateFormat outFormat = new SimpleDateFormat("dd MMM, yyyy");
            Date date = inFormat.parse(creationDate);
            String formatted = outFormat.format(date);

            // Get commit SHA and message
            JSONObject root = event.getJSONObject("payload");
            JSONArray commits = root.getJSONArray("commits");

            // Add type of event as header
            sb.append("<h3 class=\"type\">");
            sb.append(type);
            sb.append("</h3>");
            // Add formatted date
            sb.append(" on ");
            sb.append(formatted);
            sb.append("<br />");

            for (int j = 0; j < commits.length(); j++)
            {
                sb.append(commits.getJSONObject(j).getString("sha")); // prints SHA
                sb.append("<br />");
                sb.append(commits.getJSONObject(j).getString("message")); // prints message
                sb.append("<br />");
            }

            // Add collapsible JSON textbox (don't worry about this for the homework; it's just a nice CSS thing I like)
            sb.append("<a data-toggle=\"collapse\" href=\"#event-" + i + "\">JSON</a>");
            sb.append("<div id=event-" + i + " class=\"collapse\" style=\"height: auto;\"> <pre>");
            sb.append(event.toString());
            sb.append("</pre> </div>");
        }
        sb.append("</div>");
        return sb.toString();
    }

    private static List<JSONObject> getEvents(String user) throws IOException {
        List<JSONObject> eventList = new ArrayList<JSONObject>();
        int counter = 0; // Show maximum of 10 PushEvents

        for (int pageNum = 1; pageNum > 0; pageNum++) {
            String url = BASE_URL + user + "/events?page=" + pageNum + "&per_page=100";
            System.out.println(url);
            JSONObject json = Util.queryAPI(new URL(url));
            System.out.println(json);
            JSONArray events = json.getJSONArray("root");

            // If events is empty (out of data)
            if (events.length() == 0)
                break;

            for (int i = 0; i < events.length(); i++) {
                if (events.getJSONObject(i).get("type").equals("PushEvent"))    // Only save object if PushEvent
                {
                    eventList.add(events.getJSONObject(i));
                    counter++;
                } // if

                // If 10 PushEvents have been saved, stop looking for more --> break out of all loops
                if (counter >= 10) {
                    i = events.length();
                    pageNum = -1;
                } // if
            } // for
        } // for

        return eventList;
    } // getEvents()
}
