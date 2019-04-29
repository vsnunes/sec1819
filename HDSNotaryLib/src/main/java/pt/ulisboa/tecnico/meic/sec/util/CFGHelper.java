package pt.ulisboa.tecnico.meic.sec.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CFGHelper {

    /**
     * Given a path to the Servers.cfg returns a list of servers URLs
     * @param pathToServersCfg path to Servers.cfg
     * @return list of urls containing the servers URLs.
     */
    public static List<String> fetchURLsFromCfg(String pathToServersCfg, int replicas_N) throws IOException {
        FileReader fileReader = new FileReader(pathToServersCfg);
        BufferedReader bufferedReader = new BufferedReader(fileReader);

        List<String> urls = new ArrayList<>();
        String url;

        for (int i = 1; (url = bufferedReader.readLine()) != null; i++) {
            if ((url != "") && ((replicas_N == 0) || (i <= replicas_N))) urls.add(url);
        }
        System.out.println(String.format("** NotaryMiddleware: Found %d url(s) of servers!", urls.size()));
        return urls;
    }
}
